package main

import (
	"encoding/json"
	"fmt"
	"time"

	"github.com/hyperledger/fabric-contract-api-go/contractapi"
)

// DataAccessRecord 数据访问记录完整定义
type DataAccessRecord struct {
	AccessID      string `json:"accessId"`
	UserID        string `json:"userId"`
	UserRole      string `json:"userRole"`
	DataID        string `json:"dataId"`
	DataType      string `json:"dataType"`
	DataOwner     string `json:"dataOwner"`
	AccessType    string `json:"accessType"`
	QuerySQL      string `json:"querySql"`
	Timestamp     int64  `json:"timestamp"`
	IP            string `json:"ip"`
	Location      string `json:"location"`
	Device        string `json:"device"`
	ResultCount   int    `json:"resultCount"`
	DataSize      int64  `json:"dataSize"`
	IsSensitive   bool   `json:"isSensitive"`
	Sensitivity   string `json:"sensitivity"`
	Purpose       string `json:"purpose"`
	ComplianceTag string `json:"complianceTag"`
	Status        string `json:"status"`
	ErrorMsg      string `json:"errorMsg"`
	TransactionID string `json:"transactionId"`
}

// Init 初始化合约
func (d *DataAccessContract) Init(ctx contractapi.TransactionContextInterface) error {
	fmt.Println("数据访问存证合约初始化完成")
	return nil
}

// RecordAccess 记录单次数据访问
func (d *DataAccessContract) RecordAccess(ctx contractapi.TransactionContextInterface, accessID string, userID string, dataID string, accessType string, querySQL string, resultCount int, dataSize int64) error {
	if accessID == "" || userID == "" || dataID == "" {
		return fmt.Errorf("访问ID、用户ID和数据ID不能为空")
	}

	// 检查是否存在
	exists, err := d.accessExists(ctx, accessID)
	if err != nil {
		return fmt.Errorf("检查访问记录失败: %v", err)
	}
	if exists {
		return fmt.Errorf("访问记录 %s 已存在", accessID)
	}

	// 创建访问记录
	record := DataAccessRecord{
		AccessID:      accessID,
		UserID:        userID,
		UserRole:      "user",
		DataID:        dataID,
		DataType:      "TABLE",
		DataOwner:     "system",
		AccessType:    accessType,
		QuerySQL:      querySQL,
		Timestamp:     time.Now().Unix(),
		IP:            "0.0.0.0",
		Location:      "unknown",
		Device:        "unknown",
		ResultCount:   resultCount,
		DataSize:      dataSize,
		IsSensitive:   dataSize > 1024*1024, // > 1MB视为敏感
		Sensitivity:   "MEDIUM",
		Purpose:       "business",
		ComplianceTag: "GDPR",
		Status:        "SUCCESS",
		ErrorMsg:      "",
		TransactionID: ctx.GetStub().GetTxID(),
	}

	// 评估风险等级
	record.Sensitivity = d.evaluateSensitivity(record)

	recordBytes, err := json.Marshal(record)
	if err != nil {
		return fmt.Errorf("记录序列化失败: %v", err)
	}

	err = ctx.GetStub().PutState("ACCESS_"+accessID, recordBytes)
	if err != nil {
		return fmt.Errorf("写入访问记录失败: %v", err)
	}

	// 更新统计数据
	d.updateStats(ctx, record)

	fmt.Printf("✅ 数据访问记录成功 - ID: %s, 用户: %s, 数据: %s\n", accessID, userID, dataID)
	return nil
}

// BatchRecordAccess 批量记录访问
func (d *DataAccessContract) BatchRecordAccess(ctx contractapi.TransactionContextInterface, batchID string, accessIDs string) error {
	if batchID == "" || accessIDs == "" {
		return fmt.Errorf("批次ID和访问ID列表不能为空")
	}

	// 解析访问ID列表
	var idList []string
	if err := json.Unmarshal([]byte(accessIDs), &idList); err != nil {
		return fmt.Errorf("解析访问ID列表失败: %v", err)
	}

	totalSize := int64(0)
	for _, accessID := range idList {
		recordBytes, err := ctx.GetStub().GetState("ACCESS_" + accessID)
		if err != nil || recordBytes == nil {
			continue
		}

		var record DataAccessRecord
		if err := json.Unmarshal(recordBytes, &record); err == nil {
			totalSize += record.DataSize
		}
	}

	// 评估批量操作风险
	riskLevel := "LOW"
	if len(idList) > 100 || totalSize > 100*1024*1024 {
		riskLevel = "HIGH"
	} else if len(idList) > 50 || totalSize > 10*1024*1024 {
		riskLevel = "MEDIUM"
	}

	batch := map[string]interface{}{
		"batchId":       batchID,
		"accessRecords": idList,
		"totalSize":     totalSize,
		"timestamp":     time.Now().Unix(),
		"status":        "SUCCESS",
		"riskLevel":     riskLevel,
	}

	batchBytes, err := json.Marshal(batch)
	if err != nil {
		return fmt.Errorf("批次记录序列化失败: %v", err)
	}

	err = ctx.GetStub().PutState("BATCH_"+batchID, batchBytes)
	if err != nil {
		return fmt.Errorf("写入批次记录失败: %v", err)
	}

	if riskLevel == "HIGH" {
		fmt.Printf("🚨 高风险批量访问 - 批次: %s, 记录数: %d, 大小: %d MB\n", 
			batchID, len(idList), totalSize/(1024*1024))
		// 触发告警
		d.triggerBatchAlert(ctx, batchID, riskLevel, len(idList), totalSize)
	}

	fmt.Printf("✅ 批量访问记录成功 - 批次: %s, 记录数: %d\n", batchID, len(idList))
	return nil
}

// QueryAccessByUser 查询用户访问历史
func (d *DataAccessContract) QueryAccessByUser(ctx contractapi.TransactionContextInterface, userID string, startTime int64, endTime int64) ([]*DataAccessRecord, error) {
	resultsIterator, err := ctx.GetStub().GetStateByRange("ACCESS_", "ACCESS_`")
	if err != nil {
		return nil, fmt.Errorf("范围查询失败: %v", err)
	}
	defer resultsIterator.Close()

	var accesses []*DataAccessRecord
	for resultsIterator.HasNext() {
		queryResponse, err := resultsIterator.Next()
		if err != nil {
			continue
		}

		var record DataAccessRecord
		if err := json.Unmarshal(queryResponse.Value, &record); err != nil {
			continue
		}

		if record.UserID == userID && record.Timestamp >= startTime && record.Timestamp <= endTime {
			accesses = append(accesses, &record)
		}
	}

	return accesses, nil
}

// QueryHighRiskAccess 查询高风险访问
func (d *DataAccessContract) QueryHighRiskAccess(ctx contractapi.TransactionContextInterface, minSize int64) ([]*DataAccessRecord, error) {
	resultsIterator, err := ctx.GetStub().GetStateByRange("ACCESS_", "ACCESS_`")
	if err != nil {
		return nil, fmt.Errorf("范围查询失败: %v", err)
	}
	defer resultsIterator.Close()

	var highRiskAccesses []*DataAccessRecord
	for resultsIterator.HasNext() {
		queryResponse, err := resultsIterator.Next()
		if err != nil {
			continue
		}

		var record DataAccessRecord
		if err := json.Unmarshal(queryResponse.Value, &record); err != nil {
			continue
		}

		// 判断高风险：数据量大、敏感数据、失败访问
		if record.DataSize >= minSize || record.IsSensitive || record.Status == "FAILED" {
			highRiskAccesses = append(highRiskAccesses, &record)
		}
	}

	return highRiskAccesses, nil
}

// GetAccessStats 获取访问统计
func (d *DataAccessContract) GetAccessStats(ctx contractapi.TransactionContextInterface) (map[string]interface{}, error) {
	stats := map[string]interface{}{
		"totalAccesses":   0,
		"sensitiveAccesses": 0,
		"failedAccesses":  0,
		"highRiskAccesses": 0,
		"totalDataSize":   int64(0),
		"uniqueUsers":     make(map[string]bool),
		"uniqueData":      make(map[string]bool),
	}

	resultsIterator, err := ctx.GetStub().GetStateByRange("ACCESS_", "ACCESS_`")
	if err != nil {
		return nil, fmt.Errorf("范围查询失败: %v", err)
	}
	defer resultsIterator.Close()

	for resultsIterator.HasNext() {
		queryResponse, err := resultsIterator.Next()
		if err != nil {
			continue
		}

		var record DataAccessRecord
		if err := json.Unmarshal(queryResponse.Value, &record); err != nil {
			continue
		}

		stats["totalAccesses"] = stats["totalAccesses"].(int) + 1
		stats["totalDataSize"] = stats["totalDataSize"].(int64) + record.DataSize
		
		stats["uniqueUsers"].(map[string]bool)[record.UserID] = true
		stats["uniqueData"].(map[string]bool)[record.DataID] = true

		if record.IsSensitive {
			stats["sensitiveAccesses"] = stats["sensitiveAccesses"].(int) + 1
		}
		if record.Status == "FAILED" {
			stats["failedAccesses"] = stats["failedAccesses"].(int) + 1
		}
		if record.Sensitivity == "HIGH" || record.Sensitivity == "CRITICAL" {
			stats["highRiskAccesses"] = stats["highRiskAccesses"].(int) + 1
		}
	}

	stats["uniqueUserCount"] = len(stats["uniqueUsers"].(map[string]bool))
	stats["uniqueDataCount"] = len(stats["uniqueData"].(map[string]bool))
	delete(stats, "uniqueUsers")
	delete(stats, "uniqueData")

	return stats, nil
}

// evaluateSensitivity 评估数据敏感度
func (d *DataAccessContract) evaluateSensitivity(record DataAccessRecord) string {
	// 基于数据量、访问类型、用户角色等评估
	score := 0

	if record.DataSize > 10*1024*1024 {
		score += 30
	} else if record.DataSize > 1024*1024 {
		score += 20
	} else if record.DataSize > 100*1024 {
		score += 10
	}

	if record.AccessType == "DELETE" || record.AccessType == "UPDATE" {
		score += 25
	} else if record.AccessType == "DOWNLOAD" {
		score += 15
	}

	if record.ResultCount > 10000 {
		score += 20
	} else if record.ResultCount > 1000 {
		score += 10
	}

	if record.UserRole == "admin" {
		score += 5
	}

	if score >= 60 {
		return "CRITICAL"
	} else if score >= 40 {
		return "HIGH"
	} else if score >= 20 {
		return "MEDIUM"
	}
	return "LOW"
}

// updateStats 更新统计数据
func (d *DataAccessContract) updateStats(ctx contractapi.TransactionContextInterface, record DataAccessRecord) {
	// 更新用户访问计数
	userKey := "USER_STATS_" + record.UserID
	userStats := map[string]interface{}{
		"userId":         record.UserID,
		"totalAccesses":  0,
		"totalDataSize":  int64(0),
		"lastAccessTime": record.Timestamp,
	}
	
	if statsBytes, err := ctx.GetStub().GetState(userKey); err == nil && statsBytes != nil {
		json.Unmarshal(statsBytes, &userStats)
	}
	
	userStats["totalAccesses"] = userStats["totalAccesses"].(int) + 1
	userStats["totalDataSize"] = userStats["totalDataSize"].(int64) + record.DataSize
	
	if updatedBytes, err := json.Marshal(userStats); err == nil {
		ctx.GetStub().PutState(userKey, updatedBytes)
	}

	// 更新数据访问计数
	dataKey := "DATA_STATS_" + record.DataID
	dataStats := map[string]interface{}{
		"dataId":         record.DataID,
		"accessCount":    0,
		"lastAccessTime": record.Timestamp,
		"uniqueUsers":    make(map[string]bool),
	}
	
	if dataBytes, err := ctx.GetStub().GetState(dataKey); err == nil && dataBytes != nil {
		json.Unmarshal(dataBytes, &dataStats)
	}
	
	dataStats["accessCount"] = dataStats["accessCount"].(int) + 1
	dataStats["uniqueUsers"].(map[string]bool)[record.UserID] = true
	
	if updatedBytes, err := json.Marshal(dataStats); err == nil {
		ctx.GetStub().PutState(dataKey, updatedBytes)
	}
}

// triggerBatchAlert 触发批量访问告警
func (d *DataAccessContract) triggerBatchAlert(ctx contractapi.TransactionContextInterface, batchID string, riskLevel string, recordCount int, totalSize int64) {
	alert := map[string]interface{}{
		"alertType":     "BATCH_ACCESS",
		"severity":      riskLevel,
		"batchId":       batchID,
		"recordCount":   recordCount,
		"totalSize":     totalSize,
		"timestamp":     time.Now().Unix(),
		"transactionId": ctx.GetStub().GetTxID(),
	}
	
	alertBytes, _ := json.Marshal(alert)
	alertKey := "ALERT_" + fmt.Sprintf("%d", time.Now().Unix()) + "_" + batchID
	ctx.GetStub().PutState(alertKey, alertBytes)
	
	fmt.Printf("🚨 高风险批量访问告警已触发 - 批次: %s, 风险等级: %s\n", batchID, riskLevel)
}

// accessExists 检查访问记录是否存在
func (d *DataAccessContract) accessExists(ctx contractapi.TransactionContextInterface, accessID string) (bool, error) {
	accessBytes, err := ctx.GetStub().GetState("ACCESS_" + accessID)
	if err != nil {
		return false, fmt.Errorf("检查访问记录失败: %v", err)
	}
	return accessBytes != nil, nil
}

func main() {
	contract, err := contractapi.NewChaincode(&DataAccessContract{})
	if err != nil {
		fmt.Printf("创建链码失败: %v\n", err)
		return
	}

	if err := contract.Start(); err != nil {
		fmt.Printf("启动链码失败: %v\n", err)
	}
}
