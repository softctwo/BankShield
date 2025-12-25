// 自定义Cypress命令

// 登录命令
Cypress.Commands.add('login', (username, password) => {
  cy.session([username, password], () => {
    cy.visit('/login')
    cy.get('input[name="username"]').type(username)
    cy.get('input[name="password"]').type(password)
    cy.get('button[type="submit"]').click()
    
    // 等待登录成功
    cy.url().should('include', '/dashboard')
    cy.get('.user-info').should('contain', username)
  })
})

// 管理员登录命令
Cypress.Commands.add('adminLogin', () => {
  cy.login(Cypress.env('testUser'), Cypress.env('testPassword'))
})

// API登录命令（用于获取token）
Cypress.Commands.add('apiLogin', (username, password) => {
  return cy.request({
    method: 'POST',
    url: `${Cypress.env('apiBaseUrl')}/api/auth/login`,
    body: {
      username: username,
      password: password
    }
  }).then((response) => {
    expect(response.status).to.equal(200)
    expect(response.body.success).to.be.true
    return response.body.data.accessToken
  })
})

// 带认证的请求命令
Cypress.Commands.add('authenticatedRequest', (method, url, body = null) => {
  return cy.apiLogin(Cypress.env('testUser'), Cypress.env('testPassword')).then((token) => {
    const requestOptions = {
      method: method,
      url: `${Cypress.env('apiBaseUrl')}${url}`,
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    }
    
    if (body) {
      requestOptions.body = body
    }
    
    return cy.request(requestOptions)
  })
})

// 检查元素是否存在的命令
Cypress.Commands.add('elementExists', (selector) => {
  return cy.get('body').then(($body) => {
    return $body.find(selector).length > 0
  })
})

// 等待加载完成的命令
Cypress.Commands.add('waitForLoading', () => {
  cy.get('.loading-spinner', { timeout: 10000 }).should('not.exist')
})

// 等待表格加载完成
Cypress.Commands.add('waitForTable', () => {
  cy.get('.el-table__body-wrapper', { timeout: 10000 }).should('be.visible')
})

// 生成随机字符串
Cypress.Commands.add('generateRandomString', (length = 8) => {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
  let result = ''
  for (let i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  return result
})

// 生成随机手机号
Cypress.Commands.add('generateRandomPhone', () => {
  const prefix = ['130', '131', '132', '133', '134', '135', '136', '137', '138', '139', '150', '151', '152', '153', '155', '156', '157', '158', '159', '180', '181', '182', '183', '184', '185', '186', '187', '188', '189']
  const randomPrefix = prefix[Math.floor(Math.random() * prefix.length)]
  const randomSuffix = Math.floor(Math.random() * 100000000).toString().padStart(8, '0')
  return randomPrefix + randomSuffix
})

// 生成随机邮箱
Cypress.Commands.add('generateRandomEmail', () => {
  const domains = ['test.com', 'example.com', 'mock.com', 'demo.com']
  const randomDomain = domains[Math.floor(Math.random() * domains.length)]
  const randomString = Math.random().toString(36).substring(2, 10)
  return `${randomString}@${randomDomain}`
})

// 检查页面是否包含错误信息
Cypress.Commands.add('checkNoErrors', () => {
  cy.get('.el-message--error').should('not.exist')
  cy.get('.error-message').should('not.exist')
})

// 检查操作是否成功
Cypress.Commands.add('checkOperationSuccess', (message) => {
  cy.get('.el-message--success').should('be.visible').and('contain', message)
})

// 截图并保存
Cypress.Commands.add('screenshotWithName', (name) => {
  cy.screenshot(name, {
    capture: 'fullPage',
    overwrite: true
  })
})

// 等待元素可点击
Cypress.Commands.add('waitForClickable', (selector, timeout = 10000) => {
  cy.get(selector, { timeout: timeout }).should('be.visible').and('not.be.disabled')
})

// 清除并输入文本
Cypress.Commands.add('clearAndType', (selector, text) => {
  cy.get(selector).clear().type(text)
})

// 选择下拉框选项
Cypress.Commands.add('selectOption', (selectSelector, optionText) => {
  cy.get(selectSelector).click()
  cy.get('.el-select-dropdown').should('be.visible')
  cy.get('.el-select-dropdown').contains(optionText).click()
})

// 处理确认对话框
Cypress.Commands.add('confirmDialog', (confirm = true) => {
  cy.get('.el-message-box').should('be.visible')
  if (confirm) {
    cy.get('.el-message-box__btns').contains('确定').click()
  } else {
    cy.get('.el-message-box__btns').contains('取消').click()
  }
})

// 检查表格数据
Cypress.Commands.add('checkTableData', (expectedData) => {
  cy.waitForTable()
  cy.get('.el-table__body-wrapper tbody tr').should('have.length.at.least', expectedData.length)
  
  expectedData.forEach((row, index) => {
    Object.keys(row).forEach(key => {
      cy.get(`.el-table__body-wrapper tbody tr:eq(${index})`).should('contain', row[key])
    })
  })
})

// 等待API响应
Cypress.Commands.add('waitForApiResponse', (method, urlPattern, timeout = 10000) => {
  cy.intercept(method, urlPattern).as('apiCall')
  cy.wait('@apiCall', { timeout: timeout })
})