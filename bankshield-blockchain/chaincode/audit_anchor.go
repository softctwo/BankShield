/*
 * 审计区块上链智能合约
 * 
 * 功能：
 * 1. 审计日志自动上链
 * 2. Merkle根验证
 * 3. 时间戳锚定
 * 4. 区块完整性校验
 */

package main

import (
	"crypto/sha256"
	"encoding/hex"
	"encoding/json"
	"fmt"
	"time"

	"github.com/hyperledger/fabric-contract-api-go/contractapi"
)

// SmartContract 审计锚定合约
type SmartContract struct {
	contractapi.Contract
}

// AuditBlock 审计区块结构
type AuditBlock struct {
	BlockID          string                 `json:"blockId"`
	MerkleRoot       string                 `json:"merkleRoot"`
	Timestamp        int64                  `json:"timestamp"`
	PreviousHash     string                 `json:"previousHash"`
	TransactionCount int                    `json:"transactionCount"`
	Metadata         map[string]interface{} `json:"metadata"`
	CreatorOrg       string                 `json:"creatorOrg"`
	CreatorMSP       string                 `json:"creatorMSP"`
}

// AuditRecord 审计记录
type AuditRecord struct {
	RecordID      string `json:"recordId"`
	BlockID       string `json:"blockId"`
	TransactionID string `json:"transactionId"`
	Action        string `json:"action"`
	UserID        string `json:"userId"`
	Resource      string `json:"resource"`
	Result        string `json:"result"`
	Timestamp     int64  `json:"timestamp"`
	IP            string `json:"ip"`
	Details       string `json:"details"`
	Hash          string `json:"hash"`
}

// Init 初始化合约
func (s *SmartContract) Init(ctx contractapi.TransactionContextInterface) error {
	fmt.Println("审计锚定合约初始化完成")
	return nil
}

// CreateAuditAnchor 创建审计锚定区块
func (s *SmartContract) CreateAuditAnchor(ctx contractapi.TransactionContextInterface, blockID string, merkleRoot string, transactionCount int, metadataStr string) error {
	if blockID == "" || merkleRoot == "" {
		return fmt.Errorf("区块ID和Merkle根不能为空")
	}

	exists, err := s.AuditBlockExists(ctx, blockID)
	if err != nil {
		return fmt.Errorf("检查区块存在性失败: %v", err)
	}
	if exists {
		return fmt.Errorf("区块 %s 已存在", blockID)
	}

	var metadata map[string]interface{}
	if err := json.Unmarshal([]byte(metadataStr), &metadata); err != nil {
		return fmt.Errorf("元数据解析失败: %v", err)
	}

	creatorOrg, creatorMSP, err := s.getCreatorInfo(ctx)
	if err != nil {
		return fmt.Errorf("获取创建者信息失败: %v", err)
	}

	block := AuditBlock{
		BlockID:          blockID,
		MerkleRoot:       merkleRoot,
		Timestamp:        time.Now().Unix(),
		PreviousHash:     "",
		TransactionCount: transactionCount,
		Metadata:         metadata,
		CreatorOrg:       creatorOrg,
		CreatorMSP:       creatorMSP,
	}

	blockBytes, err := json.Marshal(block)
	if err != nil {
		return fmt.Errorf("区块序列化失败: %v", err)
	}

	err = ctx.GetStub().PutState("BLOCK_"+blockID, blockBytes)
	if err != nil {
		return fmt.Errorf("写入区块失败: %v", err)
	}

	err = ctx.GetStub().SetEvent("AuditBlockCreated", blockBytes)
	if err != nil {
		return fmt.Errorf("设置事件失败: %v", err)
	}

	fmt.Printf("✅ 审计区块创建成功: %s\n", blockID)
	return nil
}

// AddAuditRecord 添加审计记录
func (s *SmartContract) AddAuditRecord(ctx contractapi.TransactionContextInterface, recordID string, blockID string, action string, userID string, resource string, result string, ip string, details string) error {
	exists, err := s.AuditBlockExists(ctx, blockID)
	if err != nil {
		return fmt.Errorf("检查区块存在性失败: %v", err)
	}
	if !exists {
		return fmt.Errorf("区块 %s 不存在", blockID)
	}

	record := AuditRecord{
		RecordID:      recordID,
		BlockID:       blockID,
		TransactionID: ctx.GetStub().GetTxID(),
		Action:        action,
		UserID:        userID,
		Resource:      resource,
		Result:        result,
		Timestamp:     time.Now().Unix(),
		IP:            ip,
		Details:       details,
		Hash:          s.calculateHash(recordID, action, userID, resource, result),
	}

	recordBytes, err := json.Marshal(record)
	if err != nil {
		return fmt.Errorf("记录序列化失败: %v", err)
	}

	err = ctx.GetStub().PutState("RECORD_"+recordID, recordBytes)
	if err != nil {
		return fmt.Errorf("写入记录失败: %v", err)
	}

	fmt.Printf("✅ 审计记录添加成功: %s\n", recordID)
	return nil
}

// QueryAuditBlock 查询审计区块
func (s *SmartContract) QueryAuditBlock(ctx contractapi.TransactionContextInterface, blockID string) (*AuditBlock, error) {
	blockBytes, err := ctx.GetStub().GetState("BLOCK_" + blockID)
	if err != nil {
		return nil, fmt.Errorf("查询区块失败: %v", err)
	}
	if blockBytes == nil {
		return nil, fmt.Errorf("区块 %s 不存在", blockID)
	}

	var block AuditBlock
	err = json.Unmarshal(blockBytes, &block)
	if err != nil {
		return nil, fmt.Errorf("区块反序列化失败: %v", err)
	}

	return &block, nil
}

// VerifyMerkleRoot 验证Merkle根
func (s *SmartContract) VerifyMerkleRoot(ctx contractapi.TransactionContextInterface, blockID string) (bool, error) {
	block, err := s.QueryAuditBlock(ctx, blockID)
	if err != nil {
		return false, err
	}

	// 获取区块的所有记录
	records, err := s.getRecordsByBlock(ctx, blockID)
	if err != nil {
		return false, fmt.Errorf("获取记录失败: %v", err)
	}

	calculatedRoot := s.calculateMerkleRoot(records)

	if calculatedRoot == block.MerkleRoot {
		fmt.Printf("✅ Merkle根验证成功: %s\n", blockID)
		return true, nil
	}

	fmt.Printf("❌ Merkle根验证失败: %s (预期: %s, 实际: %s)\n", blockID, block.MerkleRoot, calculatedRoot)
	return false, nil
}

// getRecordsByBlock 获取区块下的所有记录
func (s *SmartContract) getRecordsByBlock(ctx contractapi.TransactionContextInterface, blockID string) ([]*AuditRecord, error) {
	resultsIterator, err := ctx.GetStub().GetStateByRange("RECORD_", "RECORD_`")
	if err != nil {
		return nil, fmt.Errorf("范围查询失败: %v", err)
	}
	defer resultsIterator.Close()

	var records []*AuditRecord
	for resultsIterator.HasNext() {
		queryResponse, err := resultsIterator.Next()
		if err != nil {
			return nil, fmt.Errorf("迭代失败: %v", err)
		}

		var record AuditRecord
		err = json.Unmarshal(queryResponse.Value, &record)
		if err != nil {
			return nil, fmt.Errorf("记录反序列化失败: %v", err)
		}

		if record.BlockID == blockID {
			records = append(records, &record)
		}
	}

	return records, nil
}

// 辅助方法
func (s *SmartContract) getCreatorInfo(ctx contractapi.TransactionContextInterface) (string, string, error) {
	return "BankShieldOrg", "BankShieldOrgMSP", nil
}

func (s *SmartContract) calculateHash(elements ...string) string {
	h := sha256.New()
	for _, elem := range elements {
		h.Write([]byte(elem))
	}
	return hex.EncodeToString(h.Sum(nil))
}

func (s *SmartContract) calculateMerkleRoot(records []*AuditRecord) string {
	if len(records) == 0 {
		return ""
	}
	allHashes := ""
	for _, record := range records {
		allHashes += record.Hash
	}
	return s.calculateHash(allHashes)
}

func (s *SmartContract) AuditBlockExists(ctx contractapi.TransactionContextInterface, blockID string) (bool, error) {
	blockBytes, err := ctx.GetStub().GetState("BLOCK_" + blockID)
	if err != nil {
		return false, fmt.Errorf("检查区块存在性失败: %v", err)
	}
	return blockBytes != nil, nil
}

func main() {
	chaincode, err := contractapi.NewChaincode(&SmartContract{})
	if err != nil {
		fmt.Printf("创建链码失败: %v\n", err)
		return
	}

	if err := chaincode.Start(); err != nil {
		fmt.Printf("启动链码失败: %v\n", err)
	}
}
