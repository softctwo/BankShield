/*
 * 权限变更存证智能合约
 * 
 * 功能：
 * 1. 权限变更操作审计
 * 2. 权限继承关系链追踪
 * 3. 越权检测和告警
 */

package main

import (
	"encoding/json"
	"fmt"
	"strings"
	"time"

	"github.com/hyperledger/fabric-contract-api-go/contractapi"
)

// PermissionContract 权限变更合约
type PermissionContract struct {
	contractapi.Contract
}

// PermissionChangeRecord 权限变更记录
type PermissionChangeRecord struct {
	ChangeID      string   `json:"changeId"`
	UserID        string   `json:"userId"`
	RoleID        string   `json:"roleId"`
	Action        string   `json:"action"` // GRANT, REVOKE, UPDATE
	Permissions   []string `json:"permissions"`
	Resource      string   `json:"resource"`
	PreviousState string   `json:"previousState"`
	NewState      string   `json:"newState"`
	Timestamp     int64    `json:"timestamp"`
	Operator      string   `json:"operator"`
	Reason        string   `json:"reason"`
	Status        string   `json:"status"` // APPROVED, REJECTED, PENDING
	ApprovalFlow  []string `json:"approvalFlow"`
	TransactionID string   `json:"transactionId"`
}

// UserPermissionGraph 用户权限图
type UserPermissionGraph struct {
	UserID       string                 `json:"userId"`
	CurrentRoles []string               `json:"currentRoles"`
	Permissions  map[string]interface{} `json:"permissions"` // 权限树
	ChangeHistory []string              `json:"changeHistory"`
	UpdateTime   int64                  `json:"updateTime"`
}

// Init 初始化合约
func (p *PermissionContract) Init(ctx contractapi.TransactionContextInterface) error {
	fmt.Println("权限变更合约初始化完成")
	return nil
}

// LogPermissionChange 记录权限变更
func (p *PermissionContract) LogPermissionChange(ctx contractapi.TransactionContextInterface, changeID string, userID string, roleID string, action string, permissionsStr string, resource string, reason string) error {
	if changeID == "" || userID == "" || action == "" {
		return fmt.Errorf("变更ID、用户ID和操作类型不能为空")
	}

	// 解析权限列表
	var permissions []string
	if err := json.Unmarshal([]byte(permissionsStr), &permissions); err != nil {
		return fmt.Errorf("权限列表解析失败: %v", err)
	}

	// 获取创建者
	creator, err := ctx.GetStub().GetCreator()
	if err != nil {
		return fmt.Errorf("获取创建者失败: %v", err)
	}

	// 获取当前权限状态（简化为查询历史）
	previousState := ""
	if graph, err := p.GetUserPermissionGraph(ctx, userID); err == nil && graph != nil {
		if graph.CurrentRoles != nil && len(graph.CurrentRoles) > 0 {
			previousState = strings.Join(graph.CurrentRoles, ",")
		}
	}

	// 创建变更记录
	record := PermissionChangeRecord{
		ChangeID:      changeID,
		UserID:        userID,
		RoleID:        roleID,
		Action:        action,
		Permissions:   permissions,
		Resource:      resource,
		PreviousState: previousState,
		NewState:      roleID, // 简化
		Timestamp:     time.Now().Unix(),
		Operator:      string(creator),
		Reason:        reason,
		Status:        "PENDING", // 默认待审批
		ApprovalFlow:  []string{},
		TransactionID: ctx.GetStub().GetTxID(),
	}

	// 根据操作类型和风险等级自动审批
	if p.autoApprove(action, permissions) {
		record.Status = "APPROVED"
		record.ApprovalFlow = []string{"auto_approved"}
	
		// 更新用户权限图
		if err := p.updateUserPermissionGraph(ctx, userID, roleID, action, permissions); err != nil {
			return fmt.Errorf("更新用户权限图失败: %v", err)
		}
		
		// 触发安全告警（高风险操作）
		if p.isHighRisk(action, permissions) {
			p.triggerSecurityAlert(ctx, record)
		}
	}

	recordBytes, err := json.Marshal(record)
	if err != nil {
		return fmt.Errorf("变更记录序列化失败: %v", err)
	}

	err = ctx.GetStub().PutState("PERM_"+changeID, recordBytes)
	if err != nil {
		return fmt.Errorf("写入变更记录失败: %v", err)
	}

	fmt.Printf("✅ 权限变更记录成功 - 用户: %s, 操作: %s, 状态: %s\n", userID, action, record.Status)
	return nil
}

// ApprovePermissionChange 审批权限变更
func (p *PermissionContract) ApprovePermissionChange(ctx contractapi.TransactionContextInterface, changeID string, approver string, approved bool, comment string) error {
	changeBytes, err := ctx.GetStub().GetState("PERM_" + changeID)
	if err != nil {
		return fmt.Errorf("查询变更记录失败: %v", err)
	}

	var record PermissionChangeRecord
	err = json.Unmarshal(changeBytes, &record)
	if err != nil {
		return fmt.Errorf("记录反序列化失败: %v", err)
	}

	if record.Status != "PENDING" {
		return fmt.Errorf("只能审批PENDING状态的变更")
	}

	// 添加审批记录
	record.ApprovalFlow = append(record.ApprovalFlow, fmt.Sprintf("%s:%v:%s", approver, approved, comment))

	if approved {
		record.Status = "APPROVED"
		
		// 更新用户权限图
		if err := p.updateUserPermissionGraph(ctx, record.UserID, record.RoleID, record.Action, record.Permissions); err != nil {
			return fmt.Errorf("更新用户权限图失败: %v", err)
		}
		
		// 触发安全告警（高风险操作）
		if p.isHighRisk(record.Action, record.Permissions) {
			p.triggerSecurityAlert(ctx, record)
		}
		
		fmt.Printf("✅ 权限变更已批准 - ID: %s, 用户: %s\n", changeID, record.UserID)
	} else {
		record.Status = "REJECTED"
		fmt.Printf("❌ 权限变更已拒绝 - ID: %s, 用户: %s\n", changeID, record.UserID)
	}

	// 保存更新
	updatedBytes, _ := json.Marshal(record)
	err = ctx.GetStub().PutState("PERM_"+changeID, updatedBytes)
	if err != nil {
		return fmt.Errorf("更新变更记录失败: %v", err)
	}

	return nil
}

// updateUserPermissionGraph 更新用户权限图
func (p *PermissionContract) updateUserPermissionGraph(ctx contractapi.TransactionContextInterface, userID string, roleID string, action string, permissions []string) error {
	graphKey := "GRAPH_" + userID
	graphBytes, err := ctx.GetStub().GetState(graphKey)
	
	var graph UserPermissionGraph
	if err == nil && graphBytes != nil {
		// 更新现有图
		err = json.Unmarshal(graphBytes, &graph)
		if err != nil {
			return fmt.Errorf("权限图反序列化失败: %v", err)
		}
	} else {
		// 创建新图
		graph = UserPermissionGraph{
			UserID:        userID,
			CurrentRoles:  []string{},
			Permissions:   make(map[string]interface{}),
			ChangeHistory: []string{},
			UpdateTime:    time.Now().Unix(),
		}
	}

	// 更新角色
	if action == "GRANT" {
		if !contains(graph.CurrentRoles, roleID) {
			graph.CurrentRoles = append(graph.CurrentRoles, roleID)
		}
	} else if action == "REVOKE" {
		graph.CurrentRoles = remove(graph.CurrentRoles, roleID)
	}

	// 更新权限
	for _, perm := range permissions {
		if action == "GRANT" || action == "UPDATE" {
			graph.Permissions[perm] = true
		} else if action == "REVOKE" {
			delete(graph.Permissions, perm)
		}
	}

	// 添加历史记录
	graph.ChangeHistory = append(graph.ChangeHistory, fmt.Sprintf("%d:%s:%s", time.Now().Unix(), action, roleID))
	graph.UpdateTime = time.Now().Unix()

	// 保存
	graphBytes, err = json.Marshal(graph)
	if err != nil {
		return fmt.Errorf("权限图序列化失败: %v", err)
	}

	err = ctx.GetStub().PutState(graphKey, graphBytes)
	if err != nil {
		return fmt.Errorf("保存权限图失败: %v", err)
	}

	fmt.Printf("✅ 用户权限图更新成功 - 用户: %s\n", userID)
	return nil
}

// GetUserPermissionGraph 获取用户权限图
func (p *PermissionContract) GetUserPermissionGraph(ctx contractapi.TransactionContextInterface, userID string) (*UserPermissionGraph, error) {
	graphBytes, err := ctx.GetStub().GetState("GRAPH_" + userID)
	if err != nil {
		return nil, fmt.Errorf("查询权限图失败: %v", err)
	}

	if graphBytes == nil {
		// 返回空图
		return &UserPermissionGraph{
			UserID:        userID,
			CurrentRoles:  []string{},
			Permissions:   make(map[string]interface{}),
			ChangeHistory: []string{},
			UpdateTime:    time.Now().Unix(),
		}, nil
	}

	var graph UserPermissionGraph
	err = json.Unmarshal(graphBytes, &graph)
	if err != nil {
		return nil, fmt.Errorf("权限图反序列化失败: %v", err)
	}

	return &graph, nil
}

// GetUserPermissions 获取用户所有权限
func (p *PermissionContract) GetUserPermissions(ctx contractapi.TransactionContextInterface, userID string) ([]string, error) {
	graph, err := p.GetUserPermissionGraph(ctx, userID)
	if err != nil {
		return nil, err
	}

	permissions := make([]string, 0, len(graph.Permissions))
	for perm := range graph.Permissions {
		permissions = append(permissions, perm)
	}

	return permissions, nil
}

// CheckPermission 检查用户是否有权限
func (p *PermissionContract) CheckPermission(ctx contractapi.TransactionContextInterface, userID string, permission string, resource string) (bool, error) {
	graph, err := p.GetUserPermissionGraph(ctx, userID)
	if err != nil {
		return false, err
	}

	// 检查权限
	if _, exists := graph.Permissions[permission]; exists {
		return true, nil
	}

	return false, nil
}

// GetPermissionChangeHistory 获取权限变更历史
func (p *PermissionContract) GetPermissionChangeHistory(ctx contractapi.TransactionContextInterface, userID string, limit int) ([]*PermissionChangeRecord, error) {
	resultsIterator, err := ctx.GetStub().GetStateByRange("PERM_", "PERM_`")
	if err != nil {
		return nil, fmt.Errorf("范围查询失败: %v", err)
	}
	defer resultsIterator.Close()

	var history []*PermissionChangeRecord
	count := 0
	for resultsIterator.HasNext() && count < limit {
		queryResponse, err := resultsIterator.Next()
		if err != nil {
			return nil, fmt.Errorf("迭代失败: %v", err)
		}

		var record PermissionChangeRecord
		err = json.Unmarshal(queryResponse.Value, &record)
		if err != nil {
			return nil, fmt.Errorf("记录反序列化失败: %v", err)
		}

		if record.UserID == userID {
			history = append(history, &record)
			count++
		}
	}

	return history, nil
}

// GetPermissionStats 获取权限统计
func (p *PermissionContract) GetPermissionStats(ctx contractapi.TransactionContextInterface) (map[string]interface{}, error) {
	stats := map[string]interface{}{
		"totalChanges": 0,
		"approvedChanges": 0,
		"pendingChanges": 0,
		"rejectedChanges": 0,
		"highRiskChanges": 0,
	}

	resultsIterator, err := ctx.GetStub().GetStateByRange("PERM_", "PERM_`")
	if err != nil {
		return nil, fmt.Errorf("范围查询失败: %v", err)
	}
	defer resultsIterator.Close()

	for resultsIterator.HasNext() {
		queryResponse, err := resultsIterator.Next()
		if err != nil {
			continue
		}

		var record PermissionChangeRecord
		err = json.Unmarshal(queryResponse.Value, &record)
		if err != nil {
			continue
		}

		stats["totalChanges"] = stats["totalChanges"].(int) + 1

		switch record.Status {
		case "APPROVED":
			stats["approvedChanges"] = stats["approvedChanges"].(int) + 1
		case "PENDING":
			stats["pendingChanges"] = stats["pendingChanges"].(int) + 1
		case "REJECTED":
			stats["rejectedChanges"] = stats["rejectedChanges"].(int) + 1
		}

		// 统计高风险操作
		if p.isHighRisk(record.Action, record.Permissions) {
			stats["highRiskChanges"] = stats["highRiskChanges"].(int) + 1
		}
	}

	return stats, nil
}

// 辅助方法

// autoApprove 自动审批判断
func (p *PermissionContract) autoApprove(action string, permissions []string) bool {
	// 简化规则：低权限自动审批
	lowRiskActions := []string{"QUERY", "READ", "LIST"}
	for _, lowRisk := range lowRiskActions {
		if action == lowRisk {
			return true
		}
	}
	return false
}

// isHighRisk 判断是否为高风险操作
func (p *PermissionContract) isHighRisk(action string, permissions []string) bool {
	highRiskActions := []string{"DELETE", "ADMIN", "GRANT", "REVOKE"}
	highRiskPerms := []string{"*", "admin", "delete", "sudo"}

	// 检查操作类型
	for _, risky := range highRiskActions {
		if action == risky {
			return true
		}
	}

	// 检查权限级别
	for _, perm := range permissions {
		for _, risky := range highRiskPerms {
			if perm == risky || strings.Contains(perm, risky) {
				return true
			}
		}
	}

	return false
}

// triggerSecurityAlert 触发安全告警
func (p *PermissionContract) triggerSecurityAlert(ctx contractapi.TransactionContextInterface, record PermissionChangeRecord) {
	alertMsg := fmt.Sprintf("🚨 高风险权限变更 - 用户: %s, 操作: %s, 权限: %v", 
		record.UserID, record.Action, record.Permissions)
	
	fmt.Println(alertMsg)

	// 记录到告警日志
	alertKey := fmt.Sprintf("ALERT_%d_%s", time.Now().Unix(), record.ChangeID)
	alert := map[string]interface{}{
		"timestamp": time.Now().Unix(),
		"severity":  "HIGH",
		"type":      "PERMISSION_CHANGE",
		"message":   alertMsg,
		"details":   record,
	}
	
	alertBytes, _ := json.Marshal(alert)
	ctx.GetStub().PutState(alertKey, alertBytes)
}

func (p *PermissionContract) autoApprove(action string, permissions []string) bool {
	lowRiskActions := []string{"QUERY", "READ", "LIST"}
	for _, lowRisk := range lowRiskActions {
		if action == lowRisk {
			return true
		}
	}
	return false
}

func (p *PermissionContract) isHighRisk(action string, permissions []string) bool {
	highRiskActions := []string{"DELETE", "ADMIN", "GRANT", "REVOKE"}
	highRiskPerms := []string{"*", "admin", "delete", "sudo"}

	for _, risky := range highRiskActions {
		if action == risky {
			return true
		}
	}

	for _, perm := range permissions {
		for _, risky := range highRiskPerms {
			if perm == risky {
				return true
			}
		}
	}

	return false
}

func (p *PermissionContract) triggerSecurityAlert(ctx contractapi.TransactionContextInterface, record PermissionChangeRecord) {
	alertMsg := fmt.Sprintf("🚨 高风险权限变更 - 用户: %s, 操作: %s, 权限: %v", 
		record.UserID, record.Action, record.Permissions)
	
	fmt.Println(alertMsg)

	alertKey := fmt.Sprintf("ALERT_%d_%s", time.Now().Unix(), record.ChangeID)
	alert := map[string]interface{}{
		"timestamp": time.Now().Unix(),
		"severity":  "HIGH",
		"type":      "PERMISSION_CHANGE",
		"message":   alertMsg,
		"details":   record,
	}
	
	alertBytes, _ := json.Marshal(alert)
	ctx.GetStub().PutState(alertKey, alertBytes)
}

func contains(slice []string, item string) bool {
	for _, s := range slice {
		if s == item {
			return true
		}
	}
	return false
}

func remove(slice []string, item string) []string {
	result := []string{}
	for _, s := range slice {
		if s != item {
			result = append(result, s)
		}
	}
	return result
}

func main() {
	chaincode, err := contractapi.NewChaincode(&PermissionContract{})
	if err != nil {
		fmt.Printf("创建链码失败: %v\n", err)
		return
	}

	if err := chaincode.Start(); err != nil {
		fmt.Printf("启动链码失败: %v\n", err)
	}
}
