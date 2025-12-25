pipeline {
    agent any
    
    environment {
        REGISTRY = 'harbor.bankshield.com'
        IMAGE_NAME = 'bankshield/api'
        DOCKER_CREDENTIALS = 'docker-hub-credentials'
        KUBE_CONFIG = 'kube-config-prod'
    }
    
    tools {
        maven 'Maven-3.8'
        jdk 'JDK-1.8'
        nodejs 'Node-16'
    }
    
    stages {
        stage('检出') {
            steps {
                checkout scm
            }
        }
        
        stage('代码质量检查') {
            parallel {
                stage('SonarQube分析') {
                    steps {
                        withSonarQubeEnv('SonarQube') {
                            sh 'mvn clean verify sonar:sonar'
                        }
                    }
                }
                
                stage('依赖检查') {
                    steps {
                        sh 'mvn dependency-check:check'
                    }
                    post {
                        always {
                            publishHTML([
                                allowMissing: false,
                                alwaysLinkToLastBuild: true,
                                keepAll: true,
                                reportDir: 'target',
                                reportFiles: 'dependency-check-report.html',
                                reportName: 'Dependency Check Report'
                            ])
                        }
                    }
                }
            }
        }
        
        stage('测试') {
            parallel {
                stage('单元测试') {
                    steps {
                        sh 'mvn clean test -Dspring.profiles.active=test'
                    }
                    post {
                        always {
                            junit 'target/surefire-reports/*.xml'
                            publishHTML([
                                allowMissing: false,
                                alwaysLinkToLastBuild: true,
                                keepAll: true,
                                reportDir: 'target/site/jacoco',
                                reportFiles: 'index.html',
                                reportName: 'Coverage Report'
                            ])
                        }
                    }
                }
                
                stage('前端测试') {
                    steps {
                        dir('bankshield-ui') {
                            sh 'npm install'
                            sh 'npm run test:unit'
                        }
                    }
                }
            }
        }
        
        stage('构建') {
            parallel {
                stage('构建后端') {
                    steps {
                        sh 'mvn clean package -DskipTests'
                    }
                }
                
                stage('构建前端') {
                    steps {
                        dir('bankshield-ui') {
                            sh 'npm run build'
                        }
                    }
                }
            }
        }
        
        stage('Docker镜像') {
            steps {
                script {
                    def imageTag = "${env.REGISTRY}/${env.IMAGE_NAME}:${env.BUILD_NUMBER}"
                    
                    docker.withRegistry("https://${env.REGISTRY}", env.DOCKER_CREDENTIALS) {
                        def apiImage = docker.build("${imageTag}", "./bankshield-api")
                        apiImage.push()
                        
                        def uiImage = docker.build("${imageTag}-ui", "./bankshield-ui")
                        uiImage.push()
                    }
                }
            }
        }
        
        stage('E2E测试') {
            steps {
                dir('bankshield-ui') {
                    sh 'npm run serve &'
                    sh 'sleep 30'
                    sh 'npx cypress run'
                }
            }
        }
        
        stage('部署') {
            when {
                branch 'main'
            }
            steps {
                script {
                    def imageTag = "${env.REGISTRY}/${env.IMAGE_NAME}:${env.BUILD_NUMBER}"
                    
                    withKubeConfig([credentialsId: env.KUBE_CONFIG]) {
                        sh """
                            kubectl set image deployment/bankshield-api \
                              bankshield-api=${imageTag} \
                              -n bankshield-prod
                            
                            kubectl set image deployment/bankshield-ui \
                              bankshield-ui=${imageTag}-ui \
                              -n bankshield-prod
                            
                            kubectl rollout status deployment/bankshield-api -n bankshield-prod
                            kubectl rollout status deployment/bankshield-ui -n bankshield-prod
                        """
                    }
                }
            }
        }
        
        stage('验证') {
            steps {
                script {
                    sh './scripts/health-check.sh https://app.bankshield.com'
                }
            }
        }
    }
    
    post {
        always {
            // 清理工作空间
            cleanWs()
        }
        
        success {
            slackSend(
                channel: '#deployments',
                color: 'good',
                message: """
                    ✅ BankShield部署成功！
                    版本: ${env.BUILD_NUMBER}
                    环境: ${env.BRANCH_NAME}
                    查看: ${env.BUILD_URL}
                """
            )
        }
        
        failure {
            slackSend(
                channel: '#deployments',
                color: 'danger',
                message: """
                    ❌ BankShield部署失败！
                    版本: ${env.BUILD_NUMBER}
                    环境: ${env.BRANCH_NAME}
                    查看: ${env.BUILD_URL}
                """
            )
        }
    }
}