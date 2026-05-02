#!/usr/bin/env groovy

pipeline {
//    agent any
    agent {
        docker {
            image 'maven:3.9-eclipse-temurin-21'
            args '-v /var/run/docker.sock:/var/run/docker.sock -v $HOME/.m2:/root/.m2 --network host --privileged --user root'
        }
    }

    options {
        timestamps()
        timeout(time: 1, unit: 'HOURS')
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }
    
    environment {
        MAVEN_OPTS = '-Xmx1g -Xms512m'
        TESTCONTAINERS_CONTAINER_STARTUP_TIMEOUT = '300'
        TESTCONTAINERS_RYUK_DISABLED = 'true'
        SONAR_TOKEN = credentials('sonarcloud-token')
        SONAR_ORGANIZATION = 'devops-yas'
        SONAR_PROJECT_KEY = 'devops-yas_yas'
        DOCKER_REGISTRY_CREDS = credentials('docker-hub-credentials')
        REGISTRY_URL = 'docker.io'
        GIT_COMMIT_SHORT = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
        GIT_BRANCH_NAME = sh(script: "git rev-parse --abbrev-ref HEAD", returnStdout: true).trim()
        BUILD_VERSION = "${env.BUILD_NUMBER}-${GIT_COMMIT_SHORT}"
    }

    tools {
        maven 'maven-3.9' 
        jdk 'jdk-21'    
    }
    
    parameters {
        choice(
            name: 'SERVICE',
            choices: ['auto', 'product', 'order', 'customer', 'inventory', 'location', 'media', 
                     'payment', 'payment-paypal', 'promotion', 'rating', 'search', 'cart', 
                     'recommendation', 'delivery', 'sampledata', 'common-library', 
                     'backoffice-bff', 'storefront-bff', 'webhook', 'tax'],
            description: 'Service to build (auto = detect changes)'
        )
        booleanParam(name: 'SKIP_TESTS', defaultValue: false, description: 'Skip test execution')
        booleanParam(name: 'SKIP_SONAR', defaultValue: false, description: 'Skip SonarCloud scan')
        booleanParam(name: 'SKIP_IT', defaultValue: true, description: 'Tạm thời bỏ qua Integration Tests')
    }
    
    stages {
        stage('Checkout & Detect') {
            steps {
                echo "Checking out code from ${GIT_BRANCH_NAME}..."
                checkout scm
                script {
                    def affected = []
                    def changeLogSets = currentBuild.changeSets
                    
                    // Quét file thay đổi
                    for (int i = 0; i < changeLogSets.size(); i++) {
                        def entries = changeLogSets[i].items
                        for (int j = 0; j < entries.size(); j++) {
                            def entry = entries[j]
                            entry.affectedFiles.each { file ->
                                def pathParts = file.path.split('/')
                                if (pathParts.size() > 1) {
                                    def serviceName = pathParts[0]
                                    // Dùng try-catch để tránh lỗi fileExists làm crash pipeline
                                    try {
                                        if (serviceName != 'common-library' && fileExists("${serviceName}/pom.xml")) {
                                            affected << serviceName
                                        }
                                    } catch (Exception e) { /* Bỏ qua nếu lỗi check file */ }
                                }
                            }
                        }
                    }
                    
                    // Gán lại vào env và đảm bảo nó không bao giờ null
                    def finalResult = affected.unique().join(",")
                    if (params.SERVICE != 'auto') {
                        env.TARGET_SERVICES_LIST = params.SERVICE
                    } else {
                        env.TARGET_SERVICES_LIST = finalResult ?: "common-library"
                    }
                    echo "Services detected: ${env.TARGET_SERVICES_LIST}"
                }
            }
        }

        stage('Snyk Security Scan') {
            steps {
                script {
                    echo "Downloading Snyk Binary and scanning..."
                    withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
                        sh '''
                            # 1. Tải Snyk binary chính thức từ GitHub
                            curl https://static.snyk.io/cli/latest/snyk-linux -o snyk
                            chmod +x ./snyk
                            
                            # 2. Chạy quét toàn bộ dự án YAS
                            ./snyk test --all-projects --severity-threshold=high --token=$SNYK_TOKEN --json > snyk-report.json || true
                        '''
                        // Lưu artifact để nộp báo cáo
                        archiveArtifacts artifacts: 'snyk-report.json', allowEmptyArchive: true
                    }
                }
            }
        }
        
        stage('Gitleaks - Secrets Detection') {
            steps {
                script {
                    // echo "Clean up Docker before starting to avoid port conflicts..."
                    // echo "Dọn dẹp Docker trước khi bắt đầu để tránh xung đột port..."
                    // sh 'docker system prune -f'

                    echo "Running pre-installed Gitleaks for secrets detection..."
                    sh '''
                        # Run gitleaks detect. 
                        # Use || true so the script doesn't stop immediately when a secret is found, 
                        # allowing us to handle reporting logic below.
                        gitleaks detect --source . \
                        --config gitleaks.toml \
                        --report-format json \
                        --report-path gitleaks-report.json \
                        --verbose || true
                        
                        # Kiểm tra nếu file báo cáo tồn tại
                        if [ -f "gitleaks-report.json" ]; then
                            # -i giúp tìm không phân biệt hoa thường (bắt được cả critical và CRITICAL)
                            CRITICAL=$(grep -ic '"severity":"critical"' gitleaks-report.json || echo 0)
                            
                            if [ "$CRITICAL" -gt 0 ]; then
                                echo "-------------------------------------------------------"
                                echo "ERROR: Found $CRITICAL CRITICAL secrets in your code!"
                                echo "Please check gitleaks-report.json in Build Artifacts."
                                echo "-------------------------------------------------------"
                                # cat gitleaks-report.json # Chỉ nên cat nếu file nhỏ, nếu lớn sẽ làm rối log
                                exit 1
                            fi
                        fi
                        echo "Gitleaks scan passed - no critical secrets found."
                    '''
                }
            }
        }

        stage('Monorepo Build') {
            parallel {

                stage('Build Media Service') {
                    when { changeset "media/**" }
                    steps {
                        echo 'Changes detected in Media Service. Starting Build...'
                        sh 'mvn clean install -pl media -am -DskipTests'
                    }
                }

                stage('Build Product Service') {
                    when { changeset "product/**" }
                    steps {
                        echo 'Changes detected in Product Service. Starting Build...'
                        sh 'mvn clean install -pl product -am -DskipTests'
                    }
                }

                stage('Build Cart Service') {
                    when { changeset "cart/**" }
                    steps {
                        echo 'Changes detected in Cart Service. Starting Build...'
                        sh 'mvn clean install -pl cart -am -DskipTests'
                    }
                }

                stage('Build Rating Service') {
                    when { changeset "rating/**" }
                    steps {
                        echo 'Changes detected in Rating Service. Starting Build...'
                        sh 'mvn clean install -pl rating -am -DskipTests'
                    }
                }

                stage('Build Tax Service') {
                    when { changeset "tax/**" }
                    steps {
                        echo 'Changes detected in Tax Service. Starting Build...'
                        sh 'mvn clean install -pl tax -am -DskipTests'
                    }
                }

                stage('Build Webhook Service') {
                    when { changeset "webhook/**" }
                    steps {
                        echo 'Changes detected in Webhook Service. Starting Build...'
                        sh 'mvn clean install -pl webhook -am -DskipTests'
                    }
                }

                stage('Build Promotion Service') {
                    when { changeset "promotion/**" }
                    steps {
                        echo 'Changes detected in Promotion Service. Starting Build...'
                        sh 'mvn clean install -pl promotion -am -DskipTests'
                    }
                }

                stage('Build Location Service') {
                    when { changeset "location/**" }
                    steps {
                        echo 'Changes detected in Location Service. Starting Build...'
                        sh 'mvn clean install -pl location -am -DskipTests'
                    }
                }

                stage('Build Inventory Service') {
                    when { changeset "inventory/**" }
                    steps {
                        echo 'Changes detected in Inventory Service. Starting Build...'
                        sh 'mvn clean install -pl inventory -am -DskipTests'
                    }
                }

                stage('Build Backoffice Service') {
                    when { changeset "backoffice/**" }
                    steps {
                        echo 'Changes detected in Backoffice Service. Starting Build...'
                        sh 'mvn clean install -pl backoffice -am -DskipTests'
                    }
                }

                stage('Build Backoffice BFF') {
                    when { changeset "backoffice-bff/**" }
                    steps {
                        echo 'Changes detected in Backoffice BFF. Starting Build...'
                        sh 'mvn clean install -pl backoffice-bff -am -DskipTests'
                    }
                }

                stage('Build Delivery Service') {
                    when { changeset "delivery/**" }
                    steps {
                        echo 'Changes detected in Delivery Service. Starting Build...'
                        sh 'mvn clean install -pl delivery -am -DskipTests'
                    }
                }

                stage('Build Identity Service') {
                    when { changeset "identity/**" }
                    steps {
                        echo 'Changes detected in Identity Service. Starting Build...'
                        sh 'mvn clean install -pl identity -am -DskipTests'
                    }
                }

                stage('Build Payment Service') {
                    when { changeset "payment/**" }
                    steps {
                        echo 'Changes detected in Payment Service. Starting Build...'
                        sh 'mvn clean install -pl payment -am -DskipTests'
                    }
                }

                stage('Build Payment Paypal Service') {
                    when { changeset "payment-paypal/**" }
                    steps {
                        echo 'Changes detected in Payment Paypal Service. Starting Build...'
                        sh 'mvn clean install -pl payment-paypal -am -DskipTests'
                    }
                }

                stage('Build Recommendation Service') {
                    when { changeset "recommendation/**" }
                    steps {
                        echo 'Changes detected in Recommendation Service. Starting Build...'
                        sh 'mvn clean install -pl recommendation -am -DskipTests'
                    }
                }

                stage('Build Sampledata Service') {
                    when { changeset "sampledata/**" }
                    steps {
                        echo 'Changes detected in Sampledata Service. Starting Build...'
                        sh 'mvn clean install -pl sampledata -am -DskipTests'
                    }
                }

                stage('Build Search Service') {
                    when { changeset "search/**" }
                    steps {
                        echo 'Changes detected in Search Service. Starting Build...'
                        sh 'mvn clean install -pl search -am -DskipTests'
                    }
                }

                stage('Build Storefront BFF') {
                    when { changeset "storefront-bff/**" }
                    steps {
                        echo 'Changes detected in Storefront BFF. Starting Build...'
                        sh 'mvn clean install -pl storefront-bff -am -DskipTests'
                    }
                }

                stage('Build Customer Service') {
                    when { changeset "customer/**" }
                    steps {
                        echo 'Changes detected in Customer Service. Starting Build...'
                        sh 'mvn clean install -pl customer -am -DskipTests'
                    }
                }

                stage('Build Order Service') {
                    when { changeset "order/**" }
                    steps {
                        echo 'Changes detected in Order Service. Starting Build...'
                        sh 'mvn clean install -pl order -am -DskipTests'
                    }
                }

            }
        }

        stage('Monorepo Test & Coverage') {
            parallel {

                stage('Test Media Service') {
                    when { changeset "media/**" }
                    steps {
                        echo 'Changes detected in Media Service. Starting Tests...'
                        sh 'mvn test -pl media -am -Djacoco.line.minimum=0.70'
                    }
                }

                stage('Test Product Service') {
                    when { changeset "product/**" }
                    steps {
                        echo 'Changes detected in Product Service. Starting Tests...'
                        sh 'mvn test -pl product -am -Djacoco.line.minimum=0.70'
                    }
                }

                stage('Test Cart Service') {
                    when { changeset "cart/**" }
                    steps {
                        echo 'Changes detected in Cart Service. Starting Tests...'
                        sh 'mvn test -pl cart -am -Djacoco.line.minimum=0.70'
                    }
                }

                stage('Test Rating Service') {
                    when { changeset "rating/**" }
                    steps {
                        echo 'Changes detected in Rating Service. Starting Tests...'
                        sh 'mvn test -pl rating -am -Djacoco.line.minimum=0.70'
                    }
                }

                stage('Test Tax Service') {
                    when { changeset "tax/**" }
                    steps {
                        echo 'Changes detected in Tax Service. Starting Tests...'
                        sh 'mvn test -pl tax -am -Djacoco.line.minimum=0.70'
                    }
                }

                stage('Test Webhook Service') {
                    when { changeset "webhook/**" }
                    steps {
                        echo 'Changes detected in Webhook Service. Starting Tests...'
                        sh 'mvn test -pl webhook -am -Djacoco.line.minimum=0.70'
                    }
                }

                stage('Test Promotion Service') {
                    when { changeset "promotion/**" }
                    steps {
                        echo 'Changes detected in Promotion Service. Starting Tests...'
                        sh 'mvn test -pl promotion -am -Djacoco.line.minimum=0.70'
                    }
                }

                stage('Test Location Service') {
                    when { changeset "location/**" }
                    steps {
                        echo 'Changes detected in Location Service. Starting Tests...'
                        sh 'mvn test -pl location -am -Djacoco.line.minimum=0.70'
                    }
                }

                stage('Test Inventory Service') {
                    when { changeset "inventory/**" }
                    steps {
                        echo 'Changes detected in Inventory Service. Starting Tests...'
                        sh 'mvn test -pl inventory -am -Djacoco.line.minimum=0.70'
                    }
                }

                stage('Test Backoffice Service') {
                    when { changeset "backoffice/**" }
                    steps {
                        echo 'Changes detected in Backoffice Service. Starting Tests...'
                        sh 'mvn test -pl backoffice -am -Djacoco.line.minimum=0.70'
                    }
                }

                stage('Test Backoffice BFF') {
                    when { changeset "backoffice-bff/**" }
                    steps {
                        echo 'Changes detected in Backoffice BFF. Starting Tests...'
                        sh 'mvn test -pl backoffice-bff -am -Djacoco.line.minimum=0.70'
                    }
                }

                stage('Test Delivery Service') {
                    when { changeset "delivery/**" }
                    steps {
                        echo 'Changes detected in Delivery Service. Starting Tests...'
                        sh 'mvn test -pl delivery -am -Djacoco.line.minimum=0.70'
                    }
                }

                stage('Test Identity Service') {
                    when { changeset "identity/**" }
                    steps {
                        echo 'Changes detected in Identity Service. Starting Tests...'
                        sh 'mvn test -pl identity -am -Djacoco.line.minimum=0.70'
                    }
                }

                stage('Test Payment Service') {
                    when { changeset "payment/**" }
                    steps {
                        echo 'Changes detected in Payment Service. Starting Tests...'
                        sh 'mvn test -pl payment -am -Djacoco.line.minimum=0.70'
                    }
                }

                stage('Test Payment Paypal Service') {
                    when { changeset "payment-paypal/**" }
                    steps {
                        echo 'Changes detected in Payment Paypal Service. Starting Tests...'
                        sh 'mvn test -pl payment-paypal -am -Djacoco.line.minimum=0.70'
                    }
                }

                stage('Test Recommendation Service') {
                    when { changeset "recommendation/**" }
                    steps {
                        echo 'Changes detected in Recommendation Service. Starting Tests...'
                        sh 'mvn test -pl recommendation -am -Djacoco.line.minimum=0.70'
                    }
                }

                stage('Test Sampledata Service') {
                    when { changeset "sampledata/**" }
                    steps {
                        echo 'Changes detected in Sampledata Service. Starting Tests...'
                        sh 'mvn test -pl sampledata -am -Djacoco.line.minimum=0.70'
                    }
                }

                stage('Test Search Service') {
                    when { changeset "search/**" }
                    steps {
                        echo 'Changes detected in Search Service. Starting Tests...'
                        sh 'mvn test -pl search -am -Djacoco.line.minimum=0.70'
                    }
                }

                stage('Test Storefront BFF') {
                    when { changeset "storefront-bff/**" }
                    steps {
                        echo 'Changes detected in Storefront BFF. Starting Tests...'
                        sh 'mvn test -pl storefront-bff -am -Djacoco.line.minimum=0.70'
                    }
                }

                stage('Test Customer Service') {
                    when { changeset "customer/**" }
                    steps {
                        echo 'Changes detected in Customer Service. Starting Tests...'
                        sh 'mvn test -pl customer -am -Djacoco.line.minimum=0.70'
                    }
                }

                stage('Test Order Service') {
                    when { changeset "order/**" }
                    steps {
                        echo 'Changes detected in Order Service. Starting Tests...'
                        sh 'mvn test -pl order -am -Djacoco.line.minimum=0.70'
                    }
                }

            }
        }
        
        stage('SonarCloud Analysis') {
            when {
                expression { !params.SKIP_SONAR && env.TARGET_SERVICES_LIST != 'root' && !params.SKIP_TESTS }
            }
            steps {
                echo "Running SonarCloud scan for ${env.TARGET_SERVICES_LIST}..."
                sh '''
                    mvn sonar:sonar -pl $TARGET_SERVICES_LIST -am \
                        -Dsonar.projectKey=$SONAR_PROJECT_KEY \
                        -Dsonar.organization=$SONAR_ORGANIZATION \
                        -Dsonar.host.url=https://sonarcloud.io \
                        -Dsonar.token=$SONAR_TOKEN \
                        -Dsonar.maven.scanAll=false \
                        -Dsonar.qualitygate.wait=true \
                        -Dmaven.javadoc.skip=true
                '''
            }
        }
        
        stage('Build & Push Docker') {
            when { expression { env.GIT_BRANCH_NAME == 'main' } }
            steps {
                script {
                    def services = env.TARGET_SERVICES_LIST.split(',')
                    withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', 
                                    passwordVariable: 'REGISTRY_PASSWORD', usernameVariable: 'REGISTRY_USERNAME')]) {
                        sh "echo '${REGISTRY_PASSWORD}' | docker login -u '${REGISTRY_USERNAME}' --password-stdin ${env.REGISTRY_URL}"
                        for (service in services) {
                            if (fileExists("${service}/Dockerfile")) {
                                def imageName = service.replace('-', '_')
                                sh "docker build -t ${env.REGISTRY_URL}/nashtech-garage/${imageName}:${env.BUILD_VERSION} ${service}"
                                sh "docker push ${env.REGISTRY_URL}/nashtech-garage/${imageName}:${env.BUILD_VERSION}"
                            }
                        }
                    }
                }
            }
        }
    }
    
    post {
        always {
            junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
            script {
                echo "Archiving artifacts for all affected services..."
                
                // Dùng wildcard ** để gom báo cáo từ mọi module trong project
                archiveArtifacts artifacts: "**/target/*.json, **/target/surefire-reports/*.xml, **/target/failsafe-reports/*.xml", 
                                allowEmptyArchive: true
                
                // Tìm và publish JaCoCo report (thường chỉ lấy của service chính)
                if (env.TARGET_SERVICES_LIST != null)
                {
                    def services = env.TARGET_SERVICES_LIST.split(',')
                    for (service in services) {
                        def reportPath = "${service}/target/site/jacoco/index.html"
                        if (fileExists(reportPath)) {
                            publishHTML([
                                allowMissing: true,
                                alwaysLinkToLastBuild: true,
                                keepAll: true,
                                reportDir: "${service}/target/site/jacoco",
                                reportFiles: 'index.html',
                                reportName: "JaCoCo Coverage - ${service}"
                            ])
                        }
                    }
                }               
            }
        }
        
        success {
            echo "SUCCESS: Pipeline completed successfully!"
            echo "Available Reports:"
            echo "  - Test Results: ${BUILD_URL}testReport/"
            echo "  - Coverage Report: ${BUILD_URL}JaCoCo_Code_Coverage_Report/"
        }
        
        failure {
            echo "FAILURE: Pipeline encountered errors!"
            echo "Check logs at: ${BUILD_URL}console"
        }
        
        unstable {
            echo "WARNING: Pipeline is unstable - review warnings above"
        }
    }
}
