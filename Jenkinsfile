pipeline {
    agent any

    tools {
        maven 'Maven 3.9'
    }

    environment {
        CHANGED_FILES = ''
        JAVA_HOME = '/usr/lib/jvm/java-21-openjdk-amd64'
        PATH = "${JAVA_HOME}/bin:${env.PATH}"
    }

    stages {
        stage('Detect Changes') {
            steps {
                script {
                    // Ensure we are on the correct branch and have the latest code
                    checkout scm
                    
                    // Get the list of changed files between the current commit and the previous one
                    def changedFilesList = sh(script: 'git diff --name-only HEAD~1 HEAD', returnStdout: true).trim().split('\n')
                    env.CHANGED_FILES = changedFilesList.join(',')
                    echo "Changed files: ${env.CHANGED_FILES}"
                }
            }
        }

        stage('Monorepo Build & Test') {
            parallel {
                stage('Media Service') {
                    when { expression { env.CHANGED_FILES.split(',').any { it.startsWith('media/') } } }
                    steps {
                        echo 'Changes detected in Media Service. Starting Build & Test...'
                        sh 'mvn clean test -pl media -am'
                    }
                }

                stage('Product Service') {
                    when { expression { env.CHANGED_FILES.split(',').any { it.startsWith('product/') } } }
                    steps {
                        echo 'Changes detected in Product Service. Starting Build & Test...'
                        sh 'mvn clean test -pl product -am'
                    }
                }

                stage('Cart Service') {
                    when { expression { env.CHANGED_FILES.split(',').any { it.startsWith('cart/') } } }
                    steps {
                        echo 'Changes detected in Cart Service. Starting Build & Test...'
                        sh 'mvn clean test -pl cart -am'
                    }
                }

                stage('Rating Service') {
                    when { expression { env.CHANGED_FILES.split(',').any { it.startsWith('rating/') } } }
                    steps {
                        echo 'Changes detected in Rating Service. Starting Build & Test...'
                        sh 'mvn clean test -pl rating -am'
                    }
                }

                stage('Tax Service') {
                    when { expression { env.CHANGED_FILES.split(',').any { it.startsWith('tax/') } } }
                    steps {
                        echo 'Changes detected in Tax Service. Starting Build & Test...'
                        sh 'mvn clean test -pl tax -am'
                    }
                }

                stage('Webhook Service') {
                    when { expression { env.CHANGED_FILES.split(',').any { it.startsWith('webhook/') } } }
                    steps {
                        echo 'Changes detected in Webhook Service. Starting Build & Test...'
                        sh 'mvn clean test -pl webhook -am'
                    }
                }

                stage('Promotion Service') {
                    when { expression { env.CHANGED_FILES.split(',').any { it.startsWith('promotion/') } } }
                    steps {
                        echo 'Changes detected in Promotion Service. Starting Build & Test...'
                        sh 'mvn clean test -pl promotion -am'
                    }
                }

                stage('Location Service') {
                    when { expression { env.CHANGED_FILES.split(',').any { it.startsWith('location/') } } }
                    steps {
                        echo 'Changes detected in Location Service. Starting Build & Test...'
                        sh 'mvn clean test -pl location -am'
                    }
                }

                stage('Inventory Service') {
                    when { expression { env.CHANGED_FILES.split(',').any { it.startsWith('inventory/') } } }
                    steps {
                        echo 'Changes detected in Inventory Service. Starting Build & Test...'
                        sh 'mvn clean test -pl inventory -am'
                    }
                }
            }
        }
    }
    
    post {
        always {
            junit '**/target/surefire-reports/*.xml'
        }
    }
}
