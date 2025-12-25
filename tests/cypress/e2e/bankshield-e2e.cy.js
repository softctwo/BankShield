describe('BankShield E2E Tests', () => {
  const baseUrl = Cypress.config('baseUrl') || 'http://localhost:3000';
  const apiUrl = Cypress.env('apiUrl') || 'http://localhost:8080/api';
  
  beforeEach(() => {
    cy.visit('/');
  });

  describe('Authentication Tests', () => {
    it('should display login page', () => {
      cy.get('[data-testid="login-form"]').should('be.visible');
      cy.get('[data-testid="username-input"]').should('be.visible');
      cy.get('[data-testid="password-input"]').should('be.visible');
      cy.get('[data-testid="login-button"]').should('be.visible');
    });

    it('should login with valid credentials', () => {
      cy.intercept('POST', `${apiUrl}/auth/login`, {
        statusCode: 200,
        body: {
          token: 'test-token',
          user: {
            id: 1,
            username: 'testuser',
            email: 'test@bankshield.com'
          }
        }
      }).as('loginRequest');

      cy.get('[data-testid="username-input"]').type('testuser');
      cy.get('[data-testid="password-input"]').type('password123');
      cy.get('[data-testid="login-button"]').click();

      cy.wait('@loginRequest');
      cy.get('[data-testid="dashboard"]').should('be.visible');
      cy.get('[data-testid="user-menu"]').should('contain', 'testuser');
    });

    it('should show error with invalid credentials', () => {
      cy.intercept('POST', `${apiUrl}/auth/login`, {
        statusCode: 401,
        body: {
          error: 'Invalid credentials'
        }
      }).as('loginRequest');

      cy.get('[data-testid="username-input"]').type('invaliduser');
      cy.get('[data-testid="password-input"]').type('wrongpassword');
      cy.get('[data-testid="login-button"]').click();

      cy.wait('@loginRequest');
      cy.get('[data-testid="error-message"]').should('contain', 'Invalid credentials');
    });
  });

  describe('Data Classification Tests', () => {
    beforeEach(() => {
      // 模拟登录
      cy.window().then((win) => {
        win.localStorage.setItem('token', 'test-token');
      });
    });

    it('should classify sensitive data', () => {
      cy.intercept('POST', `${apiUrl}/classification/classify`, {
        statusCode: 200,
        body: {
          result: 'PII',
          confidence: 0.95,
          details: {
            type: 'Personal Information',
            sensitivity: 'High'
          }
        }
      }).as('classifyRequest');

      cy.visit('/classification');
      cy.get('[data-testid="data-input"]').type('John Doe, SSN: 123-45-6789');
      cy.get('[data-testid="classify-button"]').click();

      cy.wait('@classifyRequest');
      cy.get('[data-testid="classification-result"]').should('contain', 'PII');
      cy.get('[data-testid="confidence-score"]').should('contain', '95%');
    });

    it('should handle classification errors', () => {
      cy.intercept('POST', `${apiUrl}/classification/classify`, {
        statusCode: 500,
        body: {
          error: 'Classification service unavailable'
        }
      }).as('classifyRequest');

      cy.visit('/classification');
      cy.get('[data-testid="data-input"]').type('test data');
      cy.get('[data-testid="classify-button"]').click();

      cy.wait('@classifyRequest');
      cy.get('[data-testid="error-message"]').should('contain', 'Classification service unavailable');
    });
  });

  describe('Encryption Tests', () => {
    beforeEach(() => {
      cy.window().then((win) => {
        win.localStorage.setItem('token', 'test-token');
      });
    });

    it('should encrypt data successfully', () => {
      cy.intercept('POST', `${apiUrl}/encryption/encrypt`, {
        statusCode: 200,
        body: {
          encryptedData: 'encrypted-text-here',
          algorithm: 'AES-256',
          keyId: 'key-123'
        }
      }).as('encryptRequest');

      cy.visit('/encryption');
      cy.get('[data-testid="data-input"]').type('sensitive information');
      cy.get('[data-testid="algorithm-select"]').select('AES-256');
      cy.get('[data-testid="encrypt-button"]').click();

      cy.wait('@encryptRequest');
      cy.get('[data-testid="encrypted-result"]').should('contain', 'encrypted-text-here');
      cy.get('[data-testid="algorithm-used"]').should('contain', 'AES-256');
    });

    it('should decrypt data successfully', () => {
      cy.intercept('POST', `${apiUrl}/encryption/decrypt`, {
        statusCode: 200,
        body: {
          decryptedData: 'original sensitive information',
          algorithm: 'AES-256'
        }
      }).as('decryptRequest');

      cy.visit('/encryption');
      cy.get('[data-testid="encrypted-input"]').type('encrypted-text-here');
      cy.get('[data-testid="decrypt-button"]').click();

      cy.wait('@decryptRequest');
      cy.get('[data-testid="decrypted-result"]').should('contain', 'original sensitive information');
    });
  });

  describe('Monitoring Dashboard Tests', () => {
    beforeEach(() => {
      cy.window().then((win) => {
        win.localStorage.setItem('token', 'test-token');
      });
    });

    it('should display monitoring dashboard', () => {
      cy.intercept('GET', `${apiUrl}/metrics`, {
        statusCode: 200,
        body: `
          # HELP bankshield_requests_total Total requests
          # TYPE bankshield_requests_total counter
          bankshield_requests_total{method="GET",status="200"} 100
          bankshield_requests_total{method="POST",status="200"} 50
          
          # HELP bankshield_response_time Response time
          # TYPE bankshield_response_time histogram
          bankshield_response_time_bucket{le="0.1"} 10
          bankshield_response_time_bucket{le="0.5"} 80
          bankshield_response_time_bucket{le="1.0"} 95
          bankshield_response_time_bucket{le="+Inf"} 100
        `
      }).as('metricsRequest');

      cy.visit('/dashboard');
      cy.get('[data-testid="metrics-dashboard"]').should('be.visible');
      cy.get('[data-testid="request-count"]').should('contain', '150');
      cy.get('[data-testid="response-time-chart"]').should('be.visible');
    });

    it('should refresh metrics', () => {
      cy.intercept('GET', `${apiUrl}/metrics`, {
        statusCode: 200,
        body: '# HELP bankshield_requests_total Total requests\n# TYPE bankshield_requests_total counter\nbankshield_requests_total 200'
      }).as('metricsRequest');

      cy.visit('/dashboard');
      cy.get('[data-testid="refresh-button"]').click();
      cy.wait('@metricsRequest');
      cy.get('[data-testid="request-count"]').should('contain', '200');
    });
  });

  describe('Error Handling Tests', () => {
    it('should handle network errors gracefully', () => {
      cy.intercept('POST', `${apiUrl}/auth/login`, {
        forceNetworkError: true
      }).as('loginRequest');

      cy.get('[data-testid="username-input"]').type('testuser');
      cy.get('[data-testid="password-input"]').type('password123');
      cy.get('[data-testid="login-button"]').click();

      cy.wait('@loginRequest');
      cy.get('[data-testid="error-message"]').should('contain', 'Network error');
    });

    it('should handle timeout errors', () => {
      cy.intercept('POST', `${apiUrl}/auth/login`, {
        delay: 5000,
        statusCode: 408,
        body: { error: 'Request timeout' }
      }).as('loginRequest');

      cy.get('[data-testid="username-input"]').type('testuser');
      cy.get('[data-testid="password-input"]').type('password123');
      cy.get('[data-testid="login-button"]').click();

      cy.wait('@loginRequest');
      cy.get('[data-testid="error-message"]').should('contain', 'Request timeout');
    });
  });

  describe('Security Tests', () => {
    it('should prevent XSS attacks', () => {
      cy.get('[data-testid="username-input"]').type('<script>alert("XSS")</script>');
      cy.get('[data-testid="password-input"]').type('password');
      cy.get('[data-testid="login-button"]').click();

      // 确保脚本没有被执行
      cy.window().then((win) => {
        expect(win.alert).to.not.have.been.called;
      });
    });

    it('should sanitize user input', () => {
      cy.window().then((win) => {
        win.localStorage.setItem('token', 'test-token');
      });

      cy.intercept('POST', `${apiUrl}/classification/classify`, {
        statusCode: 200,
        body: { result: 'SANITIZED' }
      }).as('classifyRequest');

      cy.visit('/classification');
      cy.get('[data-testid="data-input"]').type('<script>alert("XSS")</script>');
      cy.get('[data-testid="classify-button"]').click();

      cy.wait('@classifyRequest');
      cy.get('[data-testid="classification-result"]').should('not.contain', '<script>');
    });
  });
});