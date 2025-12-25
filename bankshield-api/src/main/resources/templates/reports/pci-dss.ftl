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
            border-bottom: 2px solid #52c41a;
            padding-bottom: 20px;
        }
        .score { 
            font-size: 72px; 
            color: #52c41a; 
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
            color: #52c41a;
            border-left: 4px solid #52c41a;
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
            background-color: #f6ffed;
            border: 1px solid #b7eb8f;
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
            color: #52c41a;
        }
        .metric-label {
            font-size: 14px;
            color: #666;
            margin-top: 5px;
        }
        .requirement-box {
            background-color: #f0f5ff;
            border: 1px solid #adc6ff;
            border-radius: 4px;
            padding: 15px;
            margin: 10px 0;
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
        PCI合规评分：${complianceScore!0}分
    </div>
    
    <#if complianceScore gte 85>
        <div class="info-box">
            <strong>✓ 优秀：</strong>系统PCI-DSS合规性表现优秀，符合支付卡行业数据安全标准要求。
        </div>
    <#elseif complianceScore gte 75>
        <div class="warning-box">
            <strong>⚠ 良好：</strong>系统PCI-DSS合规性表现良好，但仍有改进空间。
        </div>
    <#else>
        <div class="warning-box">
            <strong>⚠ 需改进：</strong>系统PCI-DSS合规性需要改进，请重点关注不合规项。
        </div>
    </#if>
    
    <div class="section">
        <h3>1. 防火墙配置与维护（要求1）</h3>
        <div class="requirement-box">
            <strong>PCI-DSS要求1：</strong>安装并维护防火墙配置以保护持卡人数据
        </div>
        
        <div class="chart-container">
            <div class="metric-card">
                <div class="metric-value">${firewallConfiguration.firewallEnabled?string('是','否')}</div>
                <div class="metric-label">防火墙启用</div>
            </div>
            <div class="metric-card">
                <div class="metric-value">${firewallConfiguration.defaultDeny?string('是','否')}</div>
                <div class="metric-label">默认拒绝策略</div>
            </div>
            <div class="metric-card">
                <div class="metric-value">${firewallConfiguration.rulesUpdated?string('是','否')}</div>
                <div class="metric-label">规则定期更新</div>
            </div>
        </div>
        
        <table class="table">
            <tr>
                <th>检查子项</th>
                <th>结果</th>
                <th>说明</th>
                <th>修复建议</th>
            </tr>
            <tr>
                <td>防火墙规则配置</td>
                <td class="${firewallConfiguration.firewallEnabled?string('pass', 'fail')}">${firewallConfiguration.firewallEnabled?string('通过', '未通过')}</td>
                <td>防火墙规则配置完善</td>
                <td>继续维护防火墙规则</td>
            </tr>
            <tr>
                <td>默认拒绝策略</td>
                <td class="${firewallConfiguration.defaultDeny?string('pass', 'fail')}">${firewallConfiguration.defaultDeny?string('通过', '未通过')}</td>
                <td>实施了默认拒绝策略</td>
                <td>保持默认拒绝策略</td>
            </tr>
            <tr>
                <td>规则定期审查</td>
                <td class="${firewallConfiguration.rulesUpdated?string('pass', 'fail')}">${firewallConfiguration.rulesUpdated?string('通过', '未通过')}</td>
                <td>定期审查和更新防火墙规则</td>
                <td>建立规则审查计划</td>
            </tr>
        </table>
    </div>
    
    <div class="section">
        <h3>2. 系统密码与安全参数（要求2）</h3>
        <div class="requirement-box">
            <strong>PCI-DSS要求2：</strong>不要使用供应商提供的默认系统密码和其他安全参数
        </div>
        
        <table class="table">
            <tr>
                <th>检查子项</th>
                <th>结果</th>
                <th>说明</th>
                <th>修复建议</th>
            </tr>
            <tr>
                <td>默认密码更改</td>
                <td class="${accessControl.defaultPasswordsChanged?string('pass', 'fail')}">${accessControl.defaultPasswordsChanged?string('通过', '未通过')}</td>
                <td>所有系统默认密码已更改</td>
                <td>定期检查新系统的默认密码</td>
            </tr>
            <tr>
                <td>安全参数配置</td>
                <td class="${accessControl.securityParamsConfigured?string('pass', 'fail')}">${accessControl.securityParamsConfigured?string('通过', '未通过')}</td>
                <td>系统安全参数配置正确</td>
                <td>定期审查安全参数配置</td>
            </tr>
            <tr>
                <td>加密参数管理</td>
                <td class="${accessControl.encryptionParamsManaged?string('pass', 'fail')}">${accessControl.encryptionParamsManaged?string('通过', '未通过')}</td>
                <td>加密参数管理规范</td>
                <td>建立加密参数管理流程</td>
            </tr>
        </table>
    </div>
    
    <div class="section">
        <h3>3. 持卡人数据保护（要求3）</h3>
        <div class="requirement-box">
            <strong>PCI-DSS要求3：</strong>保护存储的持卡人数据
        </div>
        
        <div class="chart-container">
            <div class="metric-card">
                <div class="metric-value">${cardholderData.sensitiveDataEncrypted?string('是','否')}</div>
                <div class="metric-label">敏感数据加密</div>
            </div>
            <div class="metric-card">
                <div class="metric-value">${cardholderData.encryptionAlgorithm!""}</div>
                <div class="metric-label">加密算法</div>
            </div>
            <div class="metric-card">
                <div class="metric-value">${cardholderData.keyManagementCompliant?string('是','否')}</div>
                <div class="metric-label">密钥管理合规</div>
            </div>
        </div>
        
        <table class="table">
            <tr>
                <th>检查子项</th>
                <th>结果</th>
                <th>说明</th>
                <th>修复建议</th>
            </tr>
            <tr>
                <td>数据加密存储</td>
                <td class="${cardholderData.sensitiveDataEncrypted?string('pass', 'fail')}">${cardholderData.sensitiveDataEncrypted?string('通过', '未通过')}</td>
                <td>持卡人数据加密存储</td>
                <td>确保所有敏感数据加密</td>
            </tr>
            <tr>
                <td>强加密算法</td>
                <td class="${cardholderData.strongEncryption?string('pass', 'fail')}">${cardholderData.strongEncryption?string('通过', '未通过')}</td>
                <td>使用符合PCI-DSS要求的强加密算法</td>
                <td>使用AES-256等强加密算法</td>
            </tr>
            <tr>
                <td>密钥管理</td>
                <td class="${cardholderData.keyManagementCompliant?string('pass', 'fail')}">${cardholderData.keyManagementCompliant?string('通过', '未通过')}</td>
                <td>密钥管理符合PCI-DSS要求</td>
                <td>建立完善的密钥管理流程</td>
            </tr>
            <tr>
                <td>数据屏蔽</td>
                <td class="${cardholderData.dataMaskingImplemented?string('pass', 'fail')}">${cardholderData.dataMaskingImplemented?string('通过', '未通过')}</td>
                <td>实施了数据屏蔽措施</td>
                <td>对显示的数据进行屏蔽处理</td>
            </tr>
        </table>
    </div>
    
    <div class="section">
        <h3>4. 加密传输（要求4）</h3>
        <div class="requirement-box">
            <strong>PCI-DSS要求4：</strong>在开放的公共网络中加密持卡人数据传输
        </div>
        
        <div class="chart-container">
            <div class="metric-card">
                <div class="metric-value">${encryptedTransmission.sslEnabled?string('是','否')}</div>
                <div class="metric-label">SSL/TLS启用</div>
            </div>
            <div class="metric-card">
                <div class="metric-value">${encryptedTransmission.strongCiphers?string('是','否')}</div>
                <div class="metric-label">强加密套件</div>
            </div>
            <div class="metric-card">
                <div class="metric-value">${encryptedTransmission.certificateValid?string('是','否')}</div>
                <div class="metric-label">证书有效</div>
            </div>
        </div>
        
        <table class="table">
            <tr>
                <th>检查子项</th>
                <th>结果</th>
                <th>说明</th>
                <th>修复建议</th>
            </tr>
            <tr>
                <td>SSL/TLS配置</td>
                <td class="${encryptedTransmission.sslEnabled?string('pass', 'fail')}">${encryptedTransmission.sslEnabled?string('通过', '未通过')}</td>
                <td>SSL/TLS正确配置和启用</td>
                <td>确保SSL/TLS正确配置</td>
            </tr>
            <tr>
                <td>强加密套件</td>
                <td class="${encryptedTransmission.strongCiphers?string('pass', 'fail')}">${encryptedTransmission.strongCiphers?string('通过', '未通过')}</td>
                <td>使用强加密算法和密钥长度</td>
                <td>禁用弱加密算法</td>
            </tr>
            <tr>
                <td>证书管理</td>
                <td class="${encryptedTransmission.certificateValid?string('pass', 'fail')}">${encryptedTransmission.certificateValid?string('通过', '未通过')}</td>
                <td>SSL证书有效且未过期</td>
                <td>定期检查和更新证书</td>
            </tr>
        </table>
    </div>
    
    <div class="section">
        <h3>5. 访问控制与身份管理（要求8）</h3>
        <div class="requirement-box">
            <strong>PCI-DSS要求8：</strong>为每个具有计算机访问权限的人分配一个唯一的ID
        </div>
        
        <table class="table">
            <tr>
                <th>检查子项</th>
                <th>结果</th>
                <th>说明</th>
                <th>修复建议</th>
            </tr>
            <tr>
                <td>唯一用户ID</td>
                <td class="${accessControl.uniqueIds?string('pass', 'fail')}">${accessControl.uniqueIds?string('通过', '未通过')}</td>
                <td>每个用户分配唯一ID</td>
                <td>建立用户ID管理规范</td>
            </tr>
            <tr>
                <td>强密码策略</td>
                <td class="${accessControl.passwordPolicy?string('pass', 'fail')}">${accessControl.passwordPolicy?string('通过', '未通过')}</td>
                <td>实施了强密码策略</td>
                <td>定期审查密码策略</td>
            </tr>
            <tr>
                <td>双因素认证</td>
                <td class="${accessControl.twoFactorAuth?string('pass', 'fail')}">${accessControl.twoFactorAuth?string('通过', '未通过')}</td>
                <td>关键系统启用双因素认证</td>
                <td>为管理员账户启用双因素认证</td>
            </tr>
        </table>
    </div>
    
    <div class="section">
        <h3>6. 不合规项汇总</h3>
        <#if nonCompliances?? && nonCompliances?size gt 0>
        <table class="table">
            <tr>
                <th>检查项</th>
                <th>PCI要求</th>
                <th>状态</th>
                <th>检查结果</th>
                <th>修复建议</th>
                <th>负责人</th>
            </tr>
            <#list nonCompliances as item>
            <tr>
                <td>${item.checkItemName!""}</td>
                <td>${item.checkType!""}</td>
                <td class="${item.passStatus?string('pass','fail')?replace('FAIL','fail')?replace('UNKNOWN','warning')}">${item.passStatus!""}</td>
                <td>${item.checkResult!""}</td>
                <td>${item.remediation!""}</td>
                <td>${item.responsiblePerson!""}</td>
            </tr>
            </#list>
        </table>
        <#else>
        <div class="info-box">
            <strong>✓ 恭喜：</strong>本次PCI-DSS检查未发现不合规项，系统符合支付卡行业数据安全标准要求。
        </div>
        </#if>
    </div>
    
    <div class="section">
        <h3>7. 改进建议</h3>
        <div class="warning-box">
            <h4>短期改进建议（30天内）：</h4>
            <ul>
                <li>定期审查防火墙规则，确保规则有效性</li>
                <li>检查SSL/TLS证书有效期，及时更新即将过期的证书</li>
                <li>验证所有系统默认密码已更改</li>
                <li>检查持卡人数据加密状态</li>
            </ul>
        </div>
        
        <div class="info-box">
            <h4>长期改进建议（90天内）：</h4>
            <ul>
                <li>建立防火墙规则定期审查机制</li>
                <li>完善密钥管理流程，确保符合PCI-DSS要求</li>
                <li>加强访问控制，为所有管理员启用双因素认证</li>
                <li>建立PCI-DSS合规性持续监控机制</li>
                <li>定期进行PCI-DSS合规性培训</li>
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