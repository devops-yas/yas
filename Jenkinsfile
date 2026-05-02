pipeline {
    agent {
        docker {
            image 'maven:3.9-eclipse-temurin-21'
            args '-v /var/run/docker.sock:/var/run/docker.sock -v $HOME/.m2:/root/.m2 --network host --privileged --user root'
        }
    }

    environment {
        MAVEN_OPTS = '-Xmx1g -Xms512m'
        TESTCONTAINERS_CONTAINER_STARTUP_TIMEOUT = '300'
        TESTCONTAINERS_RYUK_DISABLED = 'true'
    }

    tools {
        maven 'maven-3.9' 
        jdk 'jdk-21'    
    }

    stages {
        stage('Security Scan: Gitleaks') {
            steps {
                echo 'Scanning for hardcoded secrets with Gitleaks...'
                catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                    sh 'docker run --rm -v ${WORKSPACE}:/path zricethezav/gitleaks:latest detect --source="/path" -v'
                }
            }
        }

        stage('SCA Scan: Snyk') {
            steps {
                echo 'Scanning dependencies for vulnerabilities with Snyk...'
                catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                    withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
                        sh 'docker run --rm -e SNYK_TOKEN=${SNYK_TOKEN} -v ${WORKSPACE}:/app snyk/snyk:maven snyk test --all-projects'
                    }
                }
            }
        }

        stage('Monorepo Build & Test') {
            parallel {

                stage('Media Service') {
                    when { changeset "media/**" }
                    steps {
                        echo 'Changes detected in Media Service. Starting Build & Test...'
                        sh 'mvn clean test -pl media -am'
                    }
                }

                stage('Product Service') {
                    when { changeset "product/**" }
                    steps {
                        echo 'Changes detected in Product Service. Starting Build & Test...'
                        sh 'mvn clean test -pl product -am'
                    }
                }

                stage('Cart Service') {
                    when { changeset "cart/**" }
                    steps {
                        echo 'Changes detected in Cart Service. Starting Build & Test...'
                        sh 'mvn clean test -pl cart -am'
                    }
                }

                stage('Rating Service') {
                    when { changeset "rating/**" }
                    steps {
                        echo 'Changes detected in Rating Service. Starting Build & Test...'
                        sh 'mvn clean test -pl rating -am'
                    }
                }

                stage('Tax Service') {
                    when { changeset "tax/**" }
                    steps {
                        echo 'Changes detected in Tax Service. Starting Build & Test...'
                        sh 'mvn clean test -pl tax -am'
                    }
                }

                stage('Webhook Service') {
                    when { changeset "webhook/**" }
                    steps {
                        echo 'Changes detected in Webhook Service. Starting Build & Test...'
                        sh 'mvn clean test -pl webhook -am'
                    }
                }

                stage('Promotion Service') {
                    when { changeset "promotion/**" }
                    steps {
                        echo 'Changes detected in Promotion Service. Starting Build & Test...'
                        sh 'mvn clean test -pl promotion -am'
                    }
                }

                stage('Location Service') {
                    when { changeset "location/**" }
                    steps {
                        echo 'Changes detected in Location Service. Starting Build & Test...'
                        sh 'mvn clean test -pl location -am'
                    }
                }

                stage('Inventory Service') {
                    when { changeset "inventory/**" }
                    steps {
                        echo 'Changes detected in Inventory Service. Starting Build & Test...'
                        sh 'mvn clean test -pl inventory -am'
                    }
                }

                stage('Backoffice Service') {
                    when { changeset "backoffice/**" }
                    steps {
                        echo 'Changes detected in Backoffice Service. Starting Build & Test...'
                        sh 'mvn clean test -pl backoffice -am'
                    }
                }

                stage('Backoffice BFF') {
                    when { changeset "backoffice-bff/**" }
                    steps {
                        echo 'Changes detected in Backoffice BFF. Starting Build & Test...'
                        sh 'mvn clean test -pl backoffice-bff -am'
                    }
                }

                stage('Delivery Service') {
                    when { changeset "delivery/**" }
                    steps {
                        echo 'Changes detected in Delivery Service. Starting Build & Test...'
                        sh 'mvn clean test -pl delivery -am'
                    }
                }

                stage('Identity Service') {
                    when { changeset "identity/**" }
                    steps {
                        echo 'Changes detected in Identity Service. Starting Build & Test...'
                        sh 'mvn clean test -pl identity -am'
                    }
                }

                stage('Payment Service') {
                    when { changeset "payment/**" }
                    steps {
                        echo 'Changes detected in Payment Service. Starting Build & Test...'
                        sh 'mvn clean test -pl payment -am'
                    }
                }

                stage('Payment Paypal Service') {
                    when { changeset "payment-paypal/**" }
                    steps {
                        echo 'Changes detected in Payment Paypal Service. Starting Build & Test...'
                        sh 'mvn clean test -pl payment-paypal -am'
                    }
                }

                stage('Recommendation Service') {
                    when { changeset "recommendation/**" }
                    steps {
                        echo 'Changes detected in Recommendation Service. Starting Build & Test...'
                        sh 'mvn clean test -pl recommendation -am'
                    }
                }

                stage('Sampledata Service') {
                    when { changeset "sampledata/**" }
                    steps {
                        echo 'Changes detected in Sampledata Service. Starting Build & Test...'
                        sh 'mvn clean test -pl sampledata -am'
                    }
                }

                stage('Search Service') {
                    when { changeset "search/**" }
                    steps {
                        echo 'Changes detected in Search Service. Starting Build & Test...'
                        sh 'mvn clean test -pl search -am'
                    }
                }

                stage('Storefront BFF') {
                    when { changeset "storefront-bff/**" }
                    steps {
                        echo 'Changes detected in Storefront BFF. Starting Build & Test...'
                        sh 'mvn clean test -pl storefront-bff -am'
                    }
                }

                stage('Customer Service') {
                    when { changeset "customer/**" }
                    steps {
                        echo 'Changes detected in Customer Service. Starting Build & Test...'
                        sh 'mvn clean test -pl customer -am'
                    }
                }

                stage('Order Service') {
                    when { changeset "order/**" }
                    steps {
                        echo 'Changes detected in Order Service. Starting Build & Test...'
                        sh 'mvn clean test -pl order -am'
                    }
                }

            }
        }

        stage('Code Quality: SonarQube') {
            steps {
                echo 'Running SonarQube analysis...'
                catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                    withCredentials([string(credentialsId: 'sonarcloud-token', variable: 'SONAR_TOKEN')]) {
                        sh 'mvn sonar:sonar -Dsonar.projectKey=yas-monorepo -Dsonar.host.url=http://your-sonarqube-server:9000 -Dsonar.login=${SONAR_TOKEN}'
                    }
                }
            }
        }
    }
    
    post {
        always {
            junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
        }
    }
}
