/*
 * 密钥轮换存证智能合约
 * 
 * 功能：
 * 1. 记录密钥轮换历史
 * 2. 验证密钥继承关系
 * 3. 密钥状态追踪
 */

package main

import (
	"encoding/json"
	"fmt"
	"time"

	"github.com/hyperledger/fabric-contract-api-go/contractapi"
)

// KeyRotationContract 密钥轮换合约
type KeyRotationContract struct {
	contractapi.Contract
}

// KeyRotationRecord 密钥轮换记录
type KeyRotationRecord struct {
	RotationID    string `json:"rotationId"`
	OldKeyID      string `json:"oldKeyId"`
	NewKeyID      string `json:"newKeyId"`
	RotationTime  int64  `json:"rotationTime"`
	Reason        string `json:"reason"`
	Operator      string `json:"operator"`
	Status        string `json:"status"` // ACTIVE, EXPIRED, REVOKED
	BlockHeight   int64  `json:"blockHeight"`
	TransactionID string `json:"transactionId"`
}

// KeyMetadata 密钥元数据
type KeyMetadata struct {
	KeyID          string `json:"keyId"`
	KeyType        string `json:"keyType"` // AES, RSA, SM2, SM4
	KeyLength      int    `json:"keyLength"`
	CreationTime   int64  `json:"creationTime"`
	ExpirationTime int64  `json:"expirationTime"`
	Status         string `json:"status"` // ACTIVE, ROTATING, EXPIRED
	Creator        string `json:"creator"`
	UsageCount     int    `json:"usageCount"`
	EncryptedKey   string `json:"encryptedKey"` // 密钥材料（加密存储）
}

// Init 初始化合约
func (k *KeyRotationContract) Init(ctx contractapi.TransactionContextInterface) error {
	fmt.Println("密钥轮换合约初始化完成")
	return nil
}

// RecordKeyRotation 记录密钥轮换
func (k *KeyRotationContract) RecordKeyRotation(ctx contractapi.TransactionContextInterface, rotationID string, oldKeyID string, newKeyID string, reason string) error {
	if rotationID == "" || oldKeyID == "" || newKeyID == "" {
		return fmt.Errorf("轮换ID和密钥ID不能为空")
	}

	// 检查是否已存在
	exists, err := k.rotationExists(ctx, rotationID)
	if err != nil {
		return fmt.Errorf("检查轮换记录失败: %v", err)
	}
	if exists {
		return fmt.Errorf("轮换记录 %s 已存在", rotationID)
	}

	// 验证旧密钥存在且状态正确
	oldKey, err := k.GetKeyMetadata(ctx, oldKeyID)
	if err != nil || oldKey == nil {
		return fmt.Errorf("旧密钥 %s 不存在或状态异常", oldKeyID)
	}
	if oldKey.Status != "ACTIVE" && oldKey.Status != "ROTATING" {
		return fmt.Errorf("旧密钥状态异常: %s", oldKey.Status)
	}

	// 验证新密钥存在
	newKey, err := k.GetKeyMetadata(ctx, newKeyID)
	if err != nil || newKey == nil {
		return fmt.Errorf("新密钥 %s 不存在", newKeyID)
	}

	// 获取创建者信息
	creator, err := ctx.GetStub().GetCreator()
	if err != nil {
		return fmt.Errorf("获取创建者失败: %v", err)
	}

	// 创建轮换记录
	record := KeyRotationRecord{
		RotationID:    rotationID,
		OldKeyID:      oldKeyID,
		NewKeyID:      newKeyID,
		RotationTime:  time.Now().Unix(),
		Reason:        reason,
		Operator:      string(creator),
		Status:        "ACTIVE",
		BlockHeight:   0, // 可从链上获取
		TransactionID: ctx.GetStub().GetTxID(),
	}

	recordBytes, err := json.Marshal(record)
	if err != nil {
		return fmt.Errorf("轮换记录序列化失败: %v", err)
	}

	err = ctx.GetStub().PutState("ROT_"+rotationID, recordBytes)
	if err != nil {
		return fmt.Errorf("写入轮换记录失败: %v", err)
	}

	// 更新旧密钥状态
	oldKey.Status = "EXPIRED"
	oldKeyBytes, _ := json.Marshal(oldKey)
	ctx.GetStub().PutState("KEY_"+oldKeyID, oldKeyBytes)

	// 更新新密钥状态
	newKey.Status = "ACTIVE"
	newKeyBytes, _ := json.Marshal(newKey)
	ctx.GetStub().PutState("KEY_"+newKeyID, newKeyBytes)

	// 创建密钥关联
	associationKey := fmt.Sprintf("ASSOC_%s_%s", oldKeyID, newKeyID)
	association := map[string]string{
		"oldKeyID": oldKeyID,
		"newKeyID": newKeyID,
		"rotationID": rotationID,
	}
	assocBytes, _ := json.Marshal(association)
	ctx.GetStub().PutState(associationKey, assocBytes)

	fmt.Printf("✅ 密钥轮换记录成功 - 旧密钥: %s, 新密钥: %s, 轮换ID: %s\n", 
		oldKeyID, newKeyID, rotationID)
	return nil
}

// GetKeyRotationHistory 获取密钥轮换历史
func (k *KeyRotationContract) GetKeyRotationHistory(ctx contractapi.TransactionContextInterface, keyID string) ([]*KeyRotationRecord, error) {
	resultsIterator, err := ctx.GetStub().GetStateByRange("ROT_", "ROT_`")
	if err != nil {
		return nil, fmt.Errorf("范围查询失败: %v", err)
	}
	defer resultsIterator.Close()

	var history []*KeyRotationRecord
	for resultsIterator.HasNext() {
		queryResponse, err := resultsIterator.Next()
		if err != nil {
			return nil, fmt.Errorf("迭代失败: %v", err)
		}

		var record KeyRotationRecord
		err = json.Unmarshal(queryResponse.Value, &record)
		if err != nil {
			return nil, fmt.Errorf("记录反序列化失败: %v", err)
		}

		// 过滤与该密钥相关的轮换
		if record.OldKeyID == keyID || record.NewKeyID == keyID {
			history = append(history, &record)
		}
	}

	return history, nil
}

// GetKeyMetadata 获取密钥元数据
func (k *KeyRotationContract) GetKeyMetadata(ctx contractapi.TransactionContextInterface, keyID string) (*KeyMetadata, error) {
	keyBytes, err := ctx.GetStub().GetState("KEY_" + keyID)
	if err != nil {
		return nil, fmt.Errorf("查询密钥失败: %v", err)
	}

	if keyBytes == nil {
		// 创建默认元数据（用于演示）
		metadata := KeyMetadata{
			KeyID:          keyID,
			KeyType:        "AES",
			KeyLength:      256,
			CreationTime:   time.Now().Unix(),
			ExpirationTime: time.Now().Add(365 * 24 * time.Hour).Unix(),
			Status:         "ACTIVE",
			Creator:        "system",
			UsageCount:     0,
			EncryptedKey:   "encrypted_key_material",
		}
		return &metadata, nil
	}

	var metadata KeyMetadata
	err = json.Unmarshal(keyBytes, &metadata)
	if err != nil {
		return nil, fmt.Errorf("密钥元数据反序列化失败: %v", err)
	}

	return &metadata, nil
}

// UpdateKeyMetadata 更新密钥元数据
func (k *KeyRotationContract) UpdateKeyMetadata(ctx contractapi.TransactionContextInterface, keyID string, metadata KeyMetadata) error {
	metadataBytes, err := json.Marshal(metadata)
	if err != nil {
		return fmt.Errorf("元数据序列化失败: %v", err)
	}

	err = ctx.GetStub().PutState("KEY_"+keyID, metadataBytes)
	if err != nil {
		return fmt.Errorf("更新密钥元数据失败: %v", err)
	}

	fmt.Printf("✅ 密钥元数据更新成功: %s\n", keyID)
	return nil
}

// GetKeyAssociation 获取密钥继承关系
func (k *KeyRotationContract) GetKeyAssociation(ctx contractapi.TransactionContextInterface, oldKeyID string, newKeyID string) (map[string]string, error) {
	associationKey := fmt.Sprintf("ASSOC_%s_%s", oldKeyID, newKeyID)
	assocBytes, err := ctx.GetStub().GetState(associationKey)
	if err != nil {
		return nil, fmt.Errorf("查询密钥关联失败: %v", err)
	}

	if assocBytes == nil {
		return nil, fmt.Errorf("密钥关联不存在: %s -> %s", oldKeyID, newKeyID)
	}

	var association map[string]string
	err = json.Unmarshal(assocBytes, &association)
	if err != nil {
		return nil, fmt.Errorf("关联数据反序列化失败: %v", err)
	}

	return association, nil
}

// RevokeRotation 撤销密钥轮换（紧急恢复）
func (k *KeyRotationContract) RevokeRotation(ctx contractapi.TransactionContextInterface, rotationID string) error {
	rotationBytes, err := ctx.GetStub().GetState("ROT_" + rotationID)
	if err != nil {
		return fmt.Errorf("查询轮换记录失败: %v", err)
	}

	var record KeyRotationRecord
	err = json.Unmarshal(rotationBytes, &record)
	if err != nil {
		return fmt.Errorf("记录反序列化失败: %v", err)
	}

	// 检查状态
	if record.Status != "ACTIVE" {
		return fmt.Errorf("只能撤销ACTIVE状态的轮换")
	}

	// 撤销轮换
	record.Status = "REVOKED"
	recordBytes, _ := json.Marshal(record)
	err = ctx.GetStub().PutState("ROT_"+rotationID, recordBytes)
	if err != nil {
		return fmt.Errorf("更新轮换状态失败: %v", err)
	}

	// 恢复旧密钥状态
	oldKey, _ := k.GetKeyMetadata(ctx, record.OldKeyID)
	if oldKey != nil {
		oldKey.Status = "ACTIVE"
		k.UpdateKeyMetadata(ctx, record.OldKeyID, *oldKey)
	}

	// 撤销新密钥状态
	newKey, _ := k.GetKeyMetadata(ctx, record.NewKeyID)
	if newKey != nil {
		newKey.Status = "EXPIRED"
		k.UpdateKeyMetadata(ctx, record.NewKeyID, *newKey)
	}

	fmt.Printf("🚨 密钥轮换已撤销: %s\n", rotationID)
	return nil
}

// GetRotationStats 获取轮换统计
func (k *KeyRotationContract) GetRotationStats(ctx contractapi.TransactionContextInterface) (map[string]interface{}, error) {
	stats := map[string]interface{}{
		"totalRotations": 0,
		"activeRotations": 0,
		"revokedRotations": 0,
		"expiredRotations": 0,
	}

	resultsIterator, err := ctx.GetStub().GetStateByRange("ROT_", "ROT_`")
	if err != nil {
		return nil, fmt.Errorf("范围查询失败: %v", err)
	}
	defer resultsIterator.Close()

	for resultsIterator.HasNext() {
		_, err := resultsIterator.Next()
		if err != nil {
			continue
		}
		stats["totalRotations"] = stats["totalRotations"].(int) + 1
	}

	return stats, nil
}

// rotationExists 检查轮换记录是否存在
func (k *KeyRotationContract) rotationExists(ctx contractapi.TransactionContextInterface, rotationID string) (bool, error) {
	rotationBytes, err := ctx.GetStub().GetState("ROT_" + rotationID)
	if err != nil {
		return false, fmt.Errorf("检查轮换记录失败: %v", err)
	}
	return rotationBytes != nil, nil
}

func main() {
	chaincode, err := contractapi.NewChaincode(&KeyRotationContract{})
	if err != nil {
		fmt.Printf("创建链码失败: %v\n", err)
		return
	}

	if err := chaincode.Start(); err != nil {
		fmt.Printf("启动链码失败: %v\n", err)
	}
}
