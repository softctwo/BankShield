const { defineConfig } = require('cypress')

module.exports = defineConfig({
  e2e: {
    baseUrl: 'http://localhost:3000',
    specPattern: 'cypress/e2e/**/*.cy.js',
    viewportWidth: 1920,
    viewportHeight: 1080,
    video: true,
    screenshotOnRunFailure: true,
    defaultCommandTimeout: 10000,
    requestTimeout: 10000,
    responseTimeout: 10000,
    
    setupNodeEvents(on, config) {
      // 数据库种子任务
      on('task', {
        'db:seed': async (data) => {
          // 准备测试数据
          return await seedDatabase(data)
        },
        'db:cleanup': async () => {
          // 清理测试数据
          return await cleanupDatabase()
        },
        'log': (message) => {
          console.log(message)
          return null
        }
      })
      
      // 环境变量配置
      config.env.apiBaseUrl = process.env.API_BASE_URL || 'http://localhost:8080'
      config.env.testUser = process.env.TEST_USER || 'admin'
      config.env.testPassword = process.env.TEST_PASSWORD || '123456'
      
      return config
    },
    
    env: {
      apiBaseUrl: 'http://localhost:8080',
      testUser: 'admin',
      testPassword: '123456',
      coverage: true
    }
  },
  
  component: {
    devServer: {
      framework: 'vue',
      bundler: 'vite',
    },
  },
})

// 数据库种子函数
async function seedDatabase(data) {
  // 这里可以集成数据库操作，准备测试数据
  console.log('Seeding database with:', data)
  return { success: true }
}

// 数据库清理函数
async function cleanupDatabase() {
  // 清理测试数据
  console.log('Cleaning up database')
  return { success: true }
}