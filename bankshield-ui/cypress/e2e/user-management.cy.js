// 用户管理E2E测试
describe('用户管理完整生命周期测试', () => {
  beforeEach(() => {
    cy.adminLogin()
    cy.visit('/system/user')
    cy.waitForLoading()
  })

  it('创建-查询-更新-删除用户', () => {
    const timestamp = Date.now()
    const username = `e2e_user_${timestamp}`
    const phone = cy.generateRandomPhone()
    const email = cy.generateRandomEmail()

    // 1. 创建用户
    cy.get('.add-user-btn').click()
    cy.get('.el-dialog').should('be.visible')
    
    cy.get('input[name="username"]').type(username)
    cy.get('input[name="password"]').type('Test@123456')
    cy.get('input[name="name"]').type('E2E测试用户')
    cy.get('input[name="phone"]').type(phone)
    cy.get('input[name="email"]').type(email)
    
    // 分配角色
    cy.get('.role-transfer').within(() => {
      cy.get('.available-roles').contains('普通用户').click()
      cy.get('.add-role-btn').click()
    })
    
    cy.get('.el-dialog__footer').contains('确定').click()
    
    // 验证创建成功
    cy.checkOperationSuccess('创建成功')
    cy.waitForTable()
    
    // 2. 查询用户
    cy.get('.search-input').type(username)
    cy.get('.search-btn').click()
    cy.waitForLoading()
    
    // 验证查询结果
    cy.get('.el-table__body-wrapper').should('contain', username)
    cy.get('.el-table__body-wrapper tr').should('have.length', 1)
    
    // 3. 编辑用户
    cy.get('.el-table__body-wrapper')
      .contains(username)
      .parents('tr')
      .find('.edit-btn')
      .click()
    
    cy.get('.el-dialog').should('be.visible')
    cy.get('input[name="name"]').clear().type('更新后的E2E测试用户')
    cy.get('input[name="phone"]').clear().type(cy.generateRandomPhone())
    
    cy.get('.el-dialog__footer').contains('确定').click()
    
    // 验证更新成功
    cy.checkOperationSuccess('更新成功')
    
    // 4. 禁用用户
    cy.get('.el-table__body-wrapper')
      .contains(username)
      .parents('tr')
      .find('.disable-btn')
      .click()
    
    cy.confirmDialog(true)
    
    // 验证禁用成功
    cy.checkOperationSuccess('禁用成功')
    
    // 5. 启用用户
    cy.get('.el-table__body-wrapper')
      .contains(username)
      .parents('tr')
      .find('.enable-btn')
      .click()
    
    cy.confirmDialog(true)
    
    // 验证启用成功
    cy.checkOperationSuccess('启用成功')
    
    // 6. 删除用户
    cy.get('.el-table__body-wrapper')
      .contains(username)
      .parents('tr')
      .find('.delete-btn')
      .click()
    
    cy.confirmDialog(true)
    
    // 验证删除成功
    cy.checkOperationSuccess('删除成功')
    
    // 验证用户已删除
    cy.get('.search-input').clear().type(username)
    cy.get('.search-btn').click()
    cy.waitForLoading()
    
    cy.get('.el-table__empty-text').should('be.visible')
  })

  it('用户权限控制验证', () => {
    // 创建普通用户
    const normalUser = `normal_user_${Date.now()}`
    const normalPass = 'Test@123456'
    
    // 先创建普通用户
    cy.authenticatedRequest('POST', '/api/user', {
      username: normalUser,
      password: normalPass,
      name: '普通测试用户',
      phone: cy.generateRandomPhone(),
      email: cy.generateRandomEmail(),
      deptId: 1,
      status: 1
    }).then((response) => {
      expect(response.status).to.equal(200)
      expect(response.body.success).to.be.true
    })
    
    // 退出管理员登录
    cy.get('.logout-btn').click()
    cy.confirmDialog(true)
    
    // 使用普通用户登录
    cy.login(normalUser, normalPass)
    
    // 尝试访问用户管理页面
    cy.visit('/system/user')
    
    // 验证无权限提示
    cy.get('.el-message--error').should('be.visible').and('contain', '权限不足')
    cy.url().should('not.include', '/system/user')
  })

  it('用户搜索和筛选', () => {
    // 创建测试用户
    const testUsers = []
    for (let i = 0; i < 5; i++) {
      const username = `search_test_${i}_${Date.now()}`
      testUsers.push(username)
      
      cy.authenticatedRequest('POST', '/api/user', {
        username: username,
        password: 'Test@123456',
        name: `搜索测试用户${i}`,
        phone: cy.generateRandomPhone(),
        email: cy.generateRandomEmail(),
        deptId: 1,
        status: 1
      })
    }
    
    // 刷新页面
    cy.visit('/system/user')
    cy.waitForLoading()
    
    // 按用户名搜索
    cy.get('.search-input').type(testUsers[0])
    cy.get('.search-btn').click()
    cy.waitForLoading()
    
    // 验证查询结果
    cy.get('.el-table__body-wrapper').should('contain', testUsers[0])
    cy.get('.el-table__body-wrapper tr').should('have.length', 1)
    
    // 清除搜索条件
    cy.get('.reset-btn').click()
    cy.waitForLoading()
    
    // 按部门筛选
    cy.get('.dept-filter').click()
    cy.get('.el-select-dropdown').contains('技术部').click()
    cy.get('.search-btn').click()
    cy.waitForLoading()
    
    // 验证筛选结果
    cy.get('.el-table__body-wrapper').should('be.visible')
  })

  it('批量操作功能', () => {
    // 创建多个测试用户
    const batchUsers = []
    for (let i = 0; i < 3; i++) {
      const username = `batch_test_${i}_${Date.now()}`
      batchUsers.push(username)
      
      cy.authenticatedRequest('POST', '/api/user', {
        username: username,
        password: 'Test@123456',
        name: `批量测试用户${i}`,
        phone: cy.generateRandomPhone(),
        email: cy.generateRandomEmail(),
        deptId: 1,
        status: 1
      })
    }
    
    // 刷新页面
    cy.visit('/system/user')
    cy.waitForLoading()
    
    // 选择所有用户
    cy.get('.el-table__header-wrapper').find('.el-checkbox').click()
    
    // 批量删除
    cy.get('.batch-delete-btn').click()
    cy.confirmDialog(true)
    
    // 验证批量删除成功
    cy.checkOperationSuccess('批量删除成功')
  })

  it('用户详情查看', () => {
    // 创建测试用户
    const testUser = `detail_test_${Date.now()}`
    cy.authenticatedRequest('POST', '/api/user', {
      username: testUser,
      password: 'Test@123456',
      name: '详情测试用户',
      phone: cy.generateRandomPhone(),
      email: cy.generateRandomEmail(),
      deptId: 1,
      status: 1,
      remark: '用于详情查看测试'
    })
    
    // 刷新页面
    cy.visit('/system/user')
    cy.waitForLoading()
    
    // 搜索测试用户
    cy.get('.search-input').type(testUser)
    cy.get('.search-btn').click()
    cy.waitForLoading()
    
    // 点击查看详情
    cy.get('.el-table__body-wrapper')
      .contains(testUser)
      .parents('tr')
      .find('.detail-btn')
      .click()
    
    // 验证详情弹窗
    cy.get('.user-detail-dialog').should('be.visible')
    cy.get('.user-detail-dialog').should('contain', testUser)
    cy.get('.user-detail-dialog').should('contain', '详情测试用户')
    cy.get('.user-detail-dialog').should('contain', '用于详情查看测试')
    
    // 关闭详情弹窗
    cy.get('.user-detail-dialog').find('.el-dialog__close').click()
  })

  it('用户密码重置', () => {
    // 创建测试用户
    const testUser = `reset_test_${Date.now()}`
    cy.authenticatedRequest('POST', '/api/user', {
      username: testUser,
      password: 'OldPassword123',
      name: '密码重置测试用户',
      phone: cy.generateRandomPhone(),
      email: cy.generateRandomEmail(),
      deptId: 1,
      status: 1
    })
    
    // 刷新页面
    cy.visit('/system/user')
    cy.waitForLoading()
    
    // 搜索测试用户
    cy.get('.search-input').type(testUser)
    cy.get('.search-btn').click()
    cy.waitForLoading()
    
    // 点击重置密码
    cy.get('.el-table__body-wrapper')
      .contains(testUser)
      .parents('tr')
      .find('.reset-password-btn')
      .click()
    
    // 输入新密码
    cy.get('.reset-password-dialog').should('be.visible')
    cy.get('input[name="newPassword"]').type('NewPassword123')
    cy.get('input[name="confirmPassword"]').type('NewPassword123')
    
    cy.get('.reset-password-dialog').contains('确定').click()
    
    // 验证密码重置成功
    cy.checkOperationSuccess('密码重置成功')
    
    // 验证新密码可以登录
    cy.apiLogin(testUser, 'NewPassword123').then((token) => {
      expect(token).to.be.a('string')
      expect(token).to.have.length.greaterThan(0)
    })
  })

  it('用户角色分配', () => {
    // 创建测试用户
    const testUser = `role_test_${Date.now()}`
    cy.authenticatedRequest('POST', '/api/user', {
      username: testUser,
      password: 'Test@123456',
      name: '角色测试用户',
      phone: cy.generateRandomPhone(),
      email: cy.generateRandomEmail(),
      deptId: 1,
      status: 1
    })
    
    // 刷新页面
    cy.visit('/system/user')
    cy.waitForLoading()
    
    // 搜索测试用户
    cy.get('.search-input').type(testUser)
    cy.get('.search-btn').click()
    cy.waitForLoading()
    
    // 点击角色分配
    cy.get('.el-table__body-wrapper')
      .contains(testUser)
      .parents('tr')
      .find('.assign-role-btn')
      .click()
    
    // 角色分配弹窗
    cy.get('.role-assign-dialog').should('be.visible')
    
    // 分配多个角色
    const roles = ['普通用户', '数据分析师', '审计员']
    roles.forEach(role => {
      cy.get('.available-roles').contains(role).click()
      cy.get('.add-role-btn').click()
    })
    
    cy.get('.role-assign-dialog').contains('确定').click()
    
    // 验证角色分配成功
    cy.checkOperationSuccess('角色分配成功')
  })

  it('用户导入导出', () => {
    // 点击导出按钮
    cy.get('.export-btn').click()
    
    // 选择导出选项
    cy.get('.export-dialog').should('be.visible')
    cy.get('input[name="exportAll"]').check()
    cy.get('.export-dialog').contains('确定').click()
    
    // 验证导出成功
    cy.checkOperationSuccess('导出成功')
    
    // 验证文件下载
    cy.readFile('cypress/downloads/users.xlsx').should('exist')
    
    // 点击导入按钮
    cy.get('.import-btn').click()
    
    // 上传文件
    cy.get('.import-dialog').should('be.visible')
    cy.get('input[type="file"]').selectFile('cypress/fixtures/test-users.xlsx')
    
    cy.get('.import-dialog').contains('确定').click()
    
    // 验证导入成功
    cy.checkOperationSuccess('导入成功')
    cy.waitForLoading()
  })

  it('用户操作审计', () => {
    // 执行一些用户操作
    const testUser = `audit_test_${Date.now()}`
    
    // 创建用户
    cy.get('.add-user-btn').click()
    cy.get('input[name="username"]').type(testUser)
    cy.get('input[name="password"]').type('Test@123456')
    cy.get('input[name="name"]').type('审计测试用户')
    cy.get('input[name="phone"]').type(cy.generateRandomPhone())
    cy.get('input[name="email"]').type(cy.generateRandomEmail())
    cy.get('.el-dialog__footer').contains('确定').click()
    
    cy.checkOperationSuccess('创建成功')
    
    // 查看审计日志
    cy.visit('/system/audit')
    cy.waitForLoading()
    
    // 筛选用户管理相关的操作
    cy.get('.module-filter').click()
    cy.get('.el-select-dropdown').contains('用户管理').click()
    cy.get('.search-btn').click()
    cy.waitForLoading()
    
    // 验证审计记录
    cy.get('.el-table__body-wrapper').should('contain', 'CREATE_USER')
    cy.get('.el-table__body-wrapper').should('contain', testUser)
  })

  it('页面响应性能测试', () => {
    // 测量页面加载时间
    const startTime = performance.now()
    
    cy.visit('/system/user')
    cy.waitForLoading()
    
    const endTime = performance.now()
    const loadTime = endTime - startTime
    
    // 验证加载时间小于2秒
    expect(loadTime).to.be.lessThan(2000)
    
    // 测量搜索响应时间
    const searchStartTime = performance.now()
    
    cy.get('.search-input').type('test')
    cy.get('.search-btn').click()
    cy.waitForLoading()
    
    const searchEndTime = performance.now()
    const searchTime = searchEndTime - searchStartTime
    
    // 验证搜索时间小于1秒
    expect(searchTime).to.be.lessThan(1000)
    
    cy.log(`页面加载时间: ${loadTime}ms`)
    cy.log(`搜索响应时间: ${searchTime}ms`)
  })

  it('错误处理和边界情况', () => {
    // 测试超长用户名
    const longUsername = 'a'.repeat(100)
    
    cy.get('.add-user-btn').click()
    cy.get('input[name="username"]').type(longUsername)
    cy.get('input[name="password"]').type('Test@123456')
    cy.get('input[name="name"]').type('长用户名测试')
    cy.get('input[name="phone"]').type(cy.generateRandomPhone())
    cy.get('input[name="email"]').type(cy.generateRandomEmail())
    
    cy.get('.el-dialog__footer').contains('确定').click()
    
    // 验证错误提示
    cy.get('.el-message--error').should('be.visible')
    
    // 关闭弹窗
    cy.get('.el-dialog__close').click()
    
    // 测试特殊字符
    cy.get('.add-user-btn').click()
    cy.get('input[name="username"]').type('user<>|\\/*?')
    cy.get('input[name="password"]').type('Test@123456')
    cy.get('input[name="name"]').type('特殊字符测试')
    cy.get('input[name="phone"]').type(cy.generateRandomPhone())
    cy.get('input[name="email"]').type(cy.generateRandomEmail())
    
    cy.get('.el-dialog__footer').contains('确定').click()
    
    // 验证错误提示
    cy.get('.el-message--error').should('be.visible')
  })
})