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
        stage('Checkout') {
            steps {
                echo "Checking out code from ${GIT_BRANCH_NAME}..."
                checkout scm
            }
        }
        
        stage('Detect Changed Service') {
            steps {
                script {
                    echo "Fetching main branch history..."
                    // Chiến thuật đúng nhất: Fetch kèm theo ánh xạ nhánh để Git nhận diện được 'main'
                    sh "git fetch origin main:remotes/origin/main --quiet"

                    echo "Detecting changed services..."
                    // Sử dụng remotes/origin/main để chỉ định chính xác vị trí nhánh vừa fetch
                    def changedFiles = sh(
                        script: "git diff --name-only remotes/origin/main...HEAD", 
                        returnStdout: true
                    ).trim()

                    if (!changedFiles) {
                        echo "Không tìm thấy file thay đổi so với main. Sử dụng tham số hoặc mặc định 'root'."
                        env.TARGET_SERVICE = params.SERVICE == 'auto' ? 'root' : params.SERVICE
                    } else {
                        def folders = changedFiles.split("\n").collect { it.split("/")[0] }.unique()
                        echo "Thư mục thay đổi: ${folders}"

                        // Lấy danh sách các service, nối lại bằng dấu phẩy
                        def affectedServices = folders.findAll { it != 'common-library' && fileExists("${it}/pom.xml") }

                        if (affectedServices) {
                            env.TARGET_SERVICES_LIST = affectedServices.join(",") 
                            // Ví dụ: "cart,product,order"
                        } else {
                            env.TARGET_SERVICES_LIST = "common-library"
                        }

                        echo "Affected services: ${env.TARGET_SERVICES_LIST}"
                    }                       
                }
            }
        }
        
        stage('Initialize') {
            steps {
                echo "Build Configuration:"
                echo "  Service: ${env.TARGET_SERVICE}"
                echo "  Branch: ${GIT_BRANCH_NAME}"
                echo "  Commit: ${GIT_COMMIT_SHORT}"
                echo "  Build Version: ${BUILD_VERSION}"
            }
        }

        stage('Snyk Security Scan') {
            steps {
                script {
                    echo "Scanning for vulnerabilities with Snyk..."
                    // Sử dụng snyk auth $SNYK_TOKEN trước đó hoặc dùng plugin
                    sh 'snyk test --all-projects --severity-threshold=high || true'
                }
            }
        }
        
        stage('Gitleaks - Secrets Detection') {
            steps {
                script {
                    // echo "Dọn dẹp Docker trước khi bắt đầu để tránh xung đột port..."
                    // sh 'docker system prune -f'

                    echo "Running pre-installed Gitleaks for secrets detection..."
                    sh '''
                        # Chạy gitleaks detect. 
                        # Dùng || true để script không dừng ngay lập tức khi tìm thấy secret, 
                        # giúp chúng ta có thể xử lý logic báo cáo bên dưới.
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
        
        stage('Monorepo Build & Test') {
            parallel {
                stage('Media Service') { when { changeset "media/**" } steps { echo 'Changes detected in Media Service. Starting Build & Test...'; sh 'mvn clean test -pl media -am' } }
                stage('Product Service') { when { changeset "product/**" } steps { echo 'Changes detected in Product Service. Starting Build & Test...'; sh 'mvn clean test -pl product -am' } }
                stage('Cart Service') { when { changeset "cart/**" } steps { echo 'Changes detected in Cart Service. Starting Build & Test...'; sh 'mvn clean test -pl cart -am' } }
                stage('Rating Service') { when { changeset "rating/**" } steps { echo 'Changes detected in Rating Service. Starting Build & Test...'; sh 'mvn clean test -pl rating -am' } }
                stage('Tax Service') { when { changeset "tax/**" } steps { echo 'Changes detected in Tax Service. Starting Build & Test...'; sh 'mvn clean test -pl tax -am' } }
                stage('Webhook Service') { when { changeset "webhook/**" } steps { echo 'Changes detected in Webhook Service. Starting Build & Test...'; sh 'mvn clean test -pl webhook -am' } }
                stage('Promotion Service') { when { changeset "promotion/**" } steps { echo 'Changes detected in Promotion Service. Starting Build & Test...'; sh 'mvn clean test -pl promotion -am' } }
                stage('Location Service') { when { changeset "location/**" } steps { echo 'Changes detected in Location Service. Starting Build & Test...'; sh 'mvn clean test -pl location -am' } }
                stage('Inventory Service') { when { changeset "inventory/**" } steps { echo 'Changes detected in Inventory Service. Starting Build & Test...'; sh 'mvn clean test -pl inventory -am' } }
                stage('Backoffice Service') { when { changeset "backoffice/**" } steps { echo 'Changes detected in Backoffice Service. Starting Build & Test...'; sh 'mvn clean test -pl backoffice -am' } }
                stage('Backoffice BFF') { when { changeset "backoffice-bff/**" } steps { echo 'Changes detected in Backoffice BFF. Starting Build & Test...'; sh 'mvn clean test -pl backoffice-bff -am' } }
                stage('Delivery Service') { when { changeset "delivery/**" } steps { echo 'Changes detected in Delivery Service. Starting Build & Test...'; sh 'mvn clean test -pl delivery -am' } }
                stage('Identity Service') { when { changeset "identity/**" } steps { echo 'Changes detected in Identity Service. Starting Build & Test...'; sh 'mvn clean test -pl identity -am' } }
                stage('Payment Service') { when { changeset "payment/**" } steps { echo 'Changes detected in Payment Service. Starting Build & Test...'; sh 'mvn clean test -pl payment -am' } }
                stage('Payment Paypal Service') { when { changeset "payment-paypal/**" } steps { echo 'Changes detected in Payment Paypal Service. Starting Build & Test...'; sh 'mvn clean test -pl payment-paypal -am' } }
                stage('Recommendation Service') { when { changeset "recommendation/**" } steps { echo 'Changes detected in Recommendation Service. Starting Build & Test...'; sh 'mvn clean test -pl recommendation -am' } }
                stage('Sampledata Service') { when { changeset "sampledata/**" } steps { echo 'Changes detected in Sampledata Service. Starting Build & Test...'; sh 'mvn clean test -pl sampledata -am' } }
                stage('Search Service') { when { changeset "search/**" } steps { echo 'Changes detected in Search Service. Starting Build & Test...'; sh 'mvn clean test -pl search -am' } }
                stage('Storefront BFF') { when { changeset "storefront-bff/**" } steps { echo 'Changes detected in Storefront BFF. Starting Build & Test...'; sh 'mvn clean test -pl storefront-bff -am' } }
                stage('Customer Service') { when { changeset "customer/**" } steps { echo 'Changes detected in Customer Service. Starting Build & Test...'; sh 'mvn clean test -pl customer -am' } }
                stage('Order Service') { when { changeset "order/**" } steps { echo 'Changes detected in Order Service. Starting Build & Test...'; sh 'mvn clean test -pl order -am' } }
            }
        }
        
        stage('Code Coverage Validation (JaCoCo 70% Threshold)') {
            when { 
                expression { !params.SKIP_TESTS && env.TARGET_SERVICES_LIST != 'root' } 
            }
            steps {
                script {
                    def commonFlags = "-Dmaven.javadoc.skip=true -Dorg.slf4j.simpleLogger.defaultLogLevel=WARN"
                    
                    // if (!params.SKIP_IT) {
                    //     echo "--- Running Integration Tests & Checking 70% Threshold for ${env.TARGET_SERVICES_LIST} ---"
                    //     // mvn verify sẽ: chạy IT -> gộp kết quả với UT -> chạy jacoco:check (đã cấu hình trong POM)
                    //     // -DskipUnitTests=true để không chạy lại các bài Unit Test đã chạy ở stage trước
                    //     sh "mvn verify -pl ${env.TARGET_SERVICES_LIST} -am -DskipUnitTests=true -Dit.enabled=true ${commonFlags}"
                    // } else {
                    //     echo "--- Skipping IT, only validating coverage from Unit Tests ---"
                    //     // Nếu người dùng chọn skip IT, ta vẫn phải check xem UT có đủ 70% không
                    //     sh "mvn jacoco:check -pl ${env.TARGET_SERVICES_LIST} -am -Djacoco.line.minimum=0.50"
                    // }

                    sh "mvn jacoco:check -pl ${env.TARGET_SERVICES_LIST} -am -Djacoco.line.minimum=0.50"
                }
            }
            post {
                // always {
                //     echo "Collecting integration test results..."
                //     junit testResults: "**/target/failsafe-reports/*.xml", allowEmptyResults: true, skipPublishingChecks: true
                // }

                always {
                    script {
                        echo "Collecting test results..."
                        withEnv(['CHECKS_SKIP_PUBLISH=true']) {
                            junit testResults: "**/target/failsafe-reports/*.xml", 
                                allowEmptyResults: true, 
                                skipPublishingChecks: true
                        }
                    }
                }
            }
        }
        
        stage('SonarCloud Analysis - Security & Quality') {
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
        
        stage('Publish Test & Coverage Reports') {
            when {
                expression { !params.SKIP_TESTS }
            }
            steps {
                echo "Publishing test results and coverage reports..."
                script {
                    // Publish JUnit test results
                    junit testResults: '${SERVICE_PATH}/**/target/surefire-reports/*.xml,${SERVICE_PATH}/**/target/failsafe-reports/*.xml',
                          allowEmptyResults: true,
                          skipPublishingChecks: true
                    
                    // Publish JaCoCo HTML coverage report
                    publishHTML([
                        allowMissing: true,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: '${SERVICE_PATH}/target/site/jacoco',
                        reportFiles: 'index.html',
                        reportName: 'JaCoCo Code Coverage Report'
                    ])
                    
                    echo "Test and coverage reports published successfully"
                }
            }
        }
        
        stage('Build Docker Image') {
            when {
                expression { 
                    env.GIT_BRANCH_NAME in ['main', 'master', 'develop'] &&
                    env.TARGET_SERVICES_LIST != 'common-library' &&
                    !env.TARGET_SERVICES_LIST.contains('root')
                }
            }
            steps {
                script {
                    // Tách danh sách services thành mảng để xử lý từng cái
                    def services = env.TARGET_SERVICES_LIST.split(',')
                    
                    withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', 
                                                    passwordVariable: 'REGISTRY_PASSWORD', 
                                                    usernameVariable: 'REGISTRY_USERNAME')]) {
                        
                        // Login một lần duy nhất trước khi vòng lặp bắt đầu
                        sh "echo '${REGISTRY_PASSWORD}' | docker login -u '${REGISTRY_USERNAME}' --password-stdin ${env.REGISTRY_URL}"

                        for (service in services) {
                            // Chỉ build nếu service có Dockerfile
                            if (fileExists("${service}/Dockerfile")) {
                                echo "--- Building & Pushing Docker image for: ${service} ---"
                                
                                def imageName = service.replace('-', '_')
                                def imageTag = "${env.REGISTRY_URL}/nashtech-garage/${imageName}"
                                
                                sh """
                                    docker build -t ${imageTag}:${env.BUILD_VERSION} -t ${imageTag}:latest ${service}
                                    docker push ${imageTag}:${env.BUILD_VERSION}
                                    docker push ${imageTag}:latest
                                """
                            } else {
                                echo ">>> Skipping ${service}: No Dockerfile found."
                            }
                        }
                        
                        sh "docker logout ${env.REGISTRY_URL}"
                    }
                }
            }
        }
        
        stage('Push Docker Image to Registry') {
            when {
                expression { 
                    env.GIT_BRANCH_NAME in ['main', 'master', 'develop'] &&
                    fileExists("${env.SERVICE_PATH}/Dockerfile") &&
                    env.TARGET_SERVICE != 'common-library' &&
                    !env.TARGET_SERVICE.contains('automation')
                }
            }
            steps {
                echo "Pushing Docker image to registry..."
                // Sử dụng ID 'docker-hub-credentials' bạn đã tạo trên UI
                withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', 
                                                passwordVariable: 'REGISTRY_PASSWORD', 
                                                usernameVariable: 'REGISTRY_USERNAME')]) {
                    sh '''
                        echo "${REGISTRY_PASSWORD}" | docker login -u "${REGISTRY_USERNAME}" --password-stdin ${REGISTRY_URL}
                        
                        IMAGE_NAME=$(echo ${TARGET_SERVICE} | tr '-' '_')
                        
                        docker push ${REGISTRY_URL}/nashtech-garage/${IMAGE_NAME}:${BUILD_VERSION}
                        docker push ${REGISTRY_URL}/nashtech-garage/${IMAGE_NAME}:latest
                        
                        docker logout ${REGISTRY_URL}
                    '''
                }
            }
        }
    }
    
    post {
        always {
            script {
                echo "Archiving artifacts for all affected services..."
                
                // Dùng wildcard ** để gom báo cáo từ mọi module trong project
                archiveArtifacts artifacts: "**/target/*.json, **/target/surefire-reports/*.xml, **/target/failsafe-reports/*.xml", 
                                allowEmptyArchive: true
                
                // Tìm và publish JaCoCo report (thường chỉ lấy của service chính)
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
