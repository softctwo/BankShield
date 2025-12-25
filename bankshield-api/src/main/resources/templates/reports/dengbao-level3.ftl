<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>${title}</title>
    <style>
        body { 
            font-family: 'SimSun', serif; 
            margin: 20px;
            line-height: 1.6;
        }
        .header { 
            text-align: center; 
            margin-bottom: 30px; 
            border-bottom: 2px solid #1890ff;
            padding-bottom: 20px;
        }
        .score { 
            font-size: 72px; 
            color: #1890ff; 
            text-align: center; 
            margin: 20px 0;
            font-weight: bold;
        }
        .section { 
            margin: 30px 0; 
            border: 1px solid #e8e8e8;
            border-radius: 4px;
            padding: 20px;
        }
        .section h3 {
            color: #1890ff;
            border-left: 4px solid #1890ff;
            padding-left: 10px;
            margin-bottom: 15px;
        }
        .table { 
            width: 100%; 
            border-collapse: collapse; 
            margin: 15px 0;
        }
        .table th, .table td { 
            border: 1px solid #ddd; 
            padding: 12px; 
            text-align: left;
        }
        .table th {
            background-color: #f5f5f5;
            font-weight: bold;
        }
        .pass { 
            color: #52c41a; 
            font-weight: bold; 
        }
        .fail { 
            color: #ff4d4f; 
            font-weight: bold; 
        }
        .warning {
            color: #faad14;
            font-weight: bold;
        }
        .info-box {
            background-color: #e6f7ff;
            border: 1px solid #91d5ff;
            border-radius: 4px;
            padding: 15px;
            margin: 10px 0;
        }
        .warning-box {
            background-color: #fffbe6;
            border: 1px solid #ffe58f;
            border-radius: 4px;
            padding: 15px;
            margin: 10px 0;
        }
        .footer {
            margin-top: 50px;
            text-align: center;
            color: #666;
            font-size: 12px;
            border-top: 1px solid #e8e8e8;
            padding-top: 20px;
        }
        .chart-container {
            text-align: center;
            margin: 20px 0;
        }
        .metric-card {
            display: inline-block;
            background: #f0f2f5;
            border-radius: 8px;
            padding: 20px;
            margin: 10px;
            text-align: center;
            min-width: 150px;
        }
        .metric-value {
            font-size: 24px;
            font-weight: bold;
            color: #1890ff;
        }
        .metric-label {
            font-size: 14px;
            color: #666;
            margin-top: 5px;
        }
    </style>
</head>
<body>
    <div class="header">
        <h1>银行数据安全管理系统</h1>
        <h2>${title}</h2>
        <p><strong>报告周期：</strong>${reportPeriod}</p>
        <p><strong>生成时间：</strong>${.now?string['yyyy-MM-dd HH:mm:ss']}</p>
        <p><strong>生成人：</strong>${generatedBy!"系统"}</p>
    </div>
    
    <div class="score">
        合规评分：${complianceScore!0}分
    </div>
    
    <#if complianceScore gte 90>
        <div class="info-box">
            <strong>✓ 优秀：</strong>系统合规性表现优秀，符合等保三级要求。
        </div>
    <#elseif complianceScore gte 80>
        <div class="warning-box">
            <strong>⚠ 良好：</strong>系统合规性表现良好，但仍有改进空间。
        </div>
    <#else>
        <div class="warning-box">
            <strong>⚠ 需改进：</strong>系统合规性需要改进，请重点关注不合规项。
        </div>
    </#if>
    
    <div class="section">
        <h3>1. 访问控制</h3>
        <div class="chart-container">
            <div class="metric-card">
                <div class="metric-value">${accessControl.totalUsers!0}</div>
                <div class="metric-label">总用户数</div>
            </div>
            <div class="metric-card">
                <div class="metric-value">${accessControl.activeUsers!0}</div>
                <div class="metric-label">活跃用户</div>
            </div>
            <div class="metric-card">
                <div class="metric-value">${accessControl.roleBasedAccess?string('是','否')}</div>
                <div class="metric-label">基于角色访问</div>
            </div>
            <div class="metric-card">
                <div class="metric-value">${accessControl.minPrivilege?string('是','否')}</div>
                <div class="metric-label">最小权限原则</div>
            </div>
        </div>
        
        <table class="table">
            <tr>
                <th>检查项</th>
                <th>结果</th>
                <th>说明</th>
                <th>修复建议</th>
            </tr>
            <tr>
                <td>用户身份验证</td>
                <td class="${securityData.authCheck.pass?string('pass', 'fail')}">${securityData.authCheck.pass?string('通过', '未通过')}</td>
                <td>${securityData.authCheck.description!""}</td>
                <td>${securityData.authCheck.remediation!""}</td>
            </tr>
            <tr>
                <td>权限最小化</td>
                <td class="${accessControl.minPrivilege?string('pass', 'fail')}">${accessControl.minPrivilege?string('通过', '未通过')}</td>
                <td>系统实施了最小权限原则</td>
                <td>继续维护和优化权限分配</td>
            </tr>
        </table>
    </div>
    
    <div class="section">
        <h3>2. 安全审计</h3>
        <div class="chart-container">
            <div class="metric-card">
                <div class="metric-value">${auditData.auditLogIntegrity!0}%</div>
                <div class="metric-label">审计日志完整性</div>
            </div>
            <div class="metric-card">
                <div class="metric-value">${auditData.auditLogRetentionDays!0}天</div>
                <div class="metric-label">日志保留天数</div>
            </div>
            <div class="metric-card">
                <div class="metric-value">${auditData.totalOperationCount!0}</div>
                <div class="metric-label">本月操作审计</div>
            </div>
            <div class="metric-card">
                <div class="metric-value">${auditData.totalLoginCount!0}</div>
                <div class="metric-label">本月登录审计</div>
            </div>
        </div>
        
        <div class="info-box">
            <strong>审计概况：</strong>
            <ul>
                <li>审计日志完整性：${auditData.auditLogIntegrity!0}%</li>
                <li>审计日志存储天数：${auditData.auditLogRetentionDays!180}天（符合等保要求）</li>
                <li>本月操作审计总数：${auditData.totalOperationCount!0}</li>
                <li>本月登录审计总数：${auditData.totalLoginCount!0}</li>
                <li>失败操作数：${auditData.failedOperationCount!0}</li>
                <li>失败登录数：${auditData.failedLoginCount!0}</li>
            </ul>
        </div>
        
        <#if auditData.topOperationUsers?? && auditData.topOperationUsers?size gt 0>
        <h4>活跃用户TOP 5</h4>
        <table class="table">
            <tr>
                <th>用户名</th>
                <th>操作次数</th>
                <th>最后操作时间</th>
            </tr>
            <#list auditData.topOperationUsers as user>
            <tr>
                <td>${user.username!""}</td>
                <td>${user.operationCount!0}</td>
                <td>${user.lastOperationTime!""}</td>
            </tr>
            </#list>
        </table>
        </#if>
    </div>
    
    <div class="section">
        <h3>3. 数据完整性</h3>
        <div class="chart-container">
            <div class="metric-card">
                <div class="metric-value">${keyData.totalKeyCount!0}</div>
                <div class="metric-label">密钥总数</div>
            </div>
            <div class="metric-card">
                <div class="metric-value">${keyData.activeKeyCount!0}</div>
                <div class="metric-label">活跃密钥</div>
            </div>
            <div class="metric-card">
                <div class="metric-value">${keyData.expiredKeyCount!0}</div>
                <div class="metric-label">过期密钥</div>
            </div>
            <div class="metric-card">
                <div class="metric-value">${keyData.keysNeedRotation!0}</div>
                <div class="metric-label">需轮换密钥</div>
            </div>
        </div>
        
        <table class="table">
            <tr>
                <th>检查项</th>
                <th>结果</th>
                <th>说明</th>
            </tr>
            <tr>
                <td>敏感数据加密</td>
                <td class="${securityData.sensitiveDataEncryption?string('pass', 'fail')}">${securityData.sensitiveDataEncryption?string('已启用', '未启用')}</td>
                <td>敏感数据使用国密算法加密存储</td>
            </tr>
            <tr>
                <td>密钥管理合规</td>
                <td class="${keyData.keyManagementCompliance?string('pass', 'fail')}">${keyData.keyManagementCompliance?string('合规', '不合规')}</td>
                <td>密钥生命周期管理符合标准要求</td>
            </tr>
            <tr>
                <td>密钥轮换</td>
                <td class="${(keyData.keysNeedRotation == 0)?string('pass', 'warning')}">${(keyData.keysNeedRotation == 0)?string('正常', keyData.keysNeedRotation + '个密钥需轮换')}</td>
                <td>定期检查密钥轮换需求</td>
            </tr>
        </table>
    </div>
    
    <div class="section">
        <h3>4. 监控告警</h3>
        <div class="chart-container">
            <div class="metric-card">
                <div class="metric-value">${monitorData.totalAlertCount!0}</div>
                <div class="metric-label">本月告警总数</div>
            </div>
            <div class="metric-card">
                <div class="metric-value">${monitorData.unresolvedAlertCount!0}</div>
                <div class="metric-label">未处理告警</div>
            </div>
            <div class="metric-card">
                <div class="metric-value">${monitorData.criticalAlertCount!0}</div>
                <div class="metric-label">严重告警</div>
            </div>
            <div class="metric-card">
                <div class="metric-value">${monitorData.warningAlertCount!0}</div>
                <div class="metric-label">警告告警</div>
            </div>
        </div>
        
        <div class="info-box">
            <strong>告警概况：</strong>
            <ul>
                <li>本月告警总数：${monitorData.totalAlertCount!0}</li>
                <li>未处理告警：${monitorData.unresolvedAlertCount!0}</li>
                <li>严重告警：${monitorData.criticalAlertCount!0}</li>
                <li>警告告警：${monitorData.warningAlertCount!0}</li>
                <li>信息告警：${monitorData.infoAlertCount!0}</li>
            </ul>
        </div>
    </div>
    
    <div class="section">
        <h3>5. 不合规项汇总</h3>
        <#if nonCompliances?? && nonCompliances?size gt 0>
        <table class="table">
            <tr>
                <th>检查项</th>
                <th>标准</th>
                <th>状态</th>
                <th>检查结果</th>
                <th>修复建议</th>
                <th>负责人</th>
            </tr>
            <#list nonCompliances as item>
            <tr>
                <td>${item.checkItemName!""}</td>
                <td>${item.complianceStandard!""}</td>
                <td class="${item.passStatus?string('pass','fail')?replace('FAIL','fail')?replace('UNKNOWN','warning')}">${item.passStatus!""}</td>
                <td>${item.checkResult!""}</td>
                <td>${item.remediation!""}</td>
                <td>${item.responsiblePerson!""}</td>
            </tr>
            </#list>
        </table>
        <#else>
        <div class="info-box">
            <strong>✓ 恭喜：</strong>本次检查未发现不合规项，系统运行状态良好。
        </div>
        </#if>
    </div>
    
    <div class="section">
        <h3>6. 改进建议</h3>
        <div class="warning-box">
            <h4>短期改进建议（30天内）：</h4>
            <ul>
                <li>定期检查密钥轮换状态，及时处理需要轮换的密钥</li>
                <li>监控审计日志完整性，确保达到100%</li>
                <li>关注未处理告警，优先处理严重告警</li>
            </ul>
        </div>
        
        <div class="info-box">
            <h4>长期改进建议（90天内）：</h4>
            <ul>
                <li>持续优化访问控制策略，定期审查用户权限</li>
                <li>加强安全审计，扩展审计覆盖面</li>
                <li>完善数据完整性保护措施，提升数据安全等级</li>
                <li>建立自动化合规检查机制，提高检查效率</li>
            </ul>
        </div>
    </div>
    
    <div class="footer">
        <p>本报告由 BankShield 银行数据安全管理系统自动生成</p>
        <p>报告生成时间：${.now?string['yyyy-MM-dd HH:mm:ss']}</p>
        <p>系统版本：${systemVersion!"1.0.0"}</p>
    </div>
</body>
</html>