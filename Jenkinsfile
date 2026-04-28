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
        TESTCONTAINERS_RYUK_DISABLED = 'false'
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
                    echo "Detecting changed services..."
                    // 1. Fetch thông tin để có cái mà so sánh
                    sh "git fetch origin main --quiet"

                    // 2. Lấy danh sách file thay đổi
                    def changedFiles = sh(script: "git diff --name-only origin/main...HEAD", returnStdout: true).trim()

                    if (!changedFiles) {
                        echo "Không tìm thấy thay đổi so với main. Mặc định xử lý service từ parameters."
                        env.TARGET_SERVICE = params.SERVICE == 'auto' ? 'root' : params.SERVICE
                    } else {
                        // 3. Tách lấy folder cấp 1
                        def folders = changedFiles.split("\n").collect { it.split("/")[0] }.unique()
                        echo "Các thư mục có thay đổi: ${folders}"

                        if (params.SERVICE != 'auto') {
                            env.TARGET_SERVICE = params.SERVICE
                        } else {
                            // Ưu tiên build common-library nếu có thay đổi trong đó
                            if (folders.contains('common-library')) {
                                env.TARGET_SERVICE = 'common-library'
                            } else {
                                // Tìm folder đầu tiên có chứa pom.xml (đúng nghĩa là 1 service Java)
                                def service = folders.find { folder -> 
                                    folder && fileExists("${folder}/pom.xml") 
                                }
                                env.TARGET_SERVICE = service ?: 'root'
                            }
                        }
                    }

                    env.SERVICE_PATH = env.TARGET_SERVICE == 'root' ? '.' : env.TARGET_SERVICE
                    echo ">>> Kết quả: TARGET_SERVICE = ${env.TARGET_SERVICE}"
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
        
        stage('Build - Maven Clean Install') {
            steps {
                echo "Building project from root to resolve variables..."
                sh '''
                    if [ "$TARGET_SERVICE" = "root" ]; then
                        mvn clean install -DskipTests -Dmaven.javadoc.skip=true
                    else
                        # Dùng $TARGET_SERVICE (cú pháp shell) để an toàn nhất
                        mvn clean install -pl common-library,$TARGET_SERVICE -am -DskipTests \
                            -Dmaven.javadoc.skip=true \
                            -Dorg.slf4j.simpleLogger.defaultLogLevel=WARN
                    fi
                '''
            }
        }
        
        stage('Test Phase - Unit & Integration Tests') {
            when {
                expression { !params.SKIP_TESTS }
            }
            steps {
                echo "Running tests for ${env.TARGET_SERVICE}..."
                script {
                    // Chuyển flag vào biến Groovy để dễ quản lý và nội suy
                    def commonFlags = "-Dmaven.javadoc.skip=true -Dorg.slf4j.simpleLogger.defaultLogLevel=WARN -V"
                    
                    sh """
                        if [ "$TARGET_SERVICE" = "root" ]; then
                            echo "Running unit tests only for root..."
                            # -DskipITs: Ngăn chặn hoàn toàn plugin Failsafe quét các class *IT.java
                            mvn test ${commonFlags} -Dit.enabled=false -DskipITs
                        else
                            echo "--- Step 1: Running Unit Tests for $TARGET_SERVICE ---"
                            # Ép buộc bỏ qua IT ở cả tầng Maven Plugin và tầng Spring Context
                            mvn test -pl $TARGET_SERVICE -am ${commonFlags} -Dit.enabled=false -DskipITs
                            
                            if [ "${params.SKIP_IT}" != "true" ]; then
                                echo "--- Step 2: Running Integration Tests for $TARGET_SERVICE ---"
                                # Chỉ khi chạy IT mới bật it.enabled=true và cho phép chạy verify
                                mvn verify -pl $TARGET_SERVICE -am -DskipUnitTests -Dit.enabled=true ${commonFlags}
                            else
                                echo ">>> SKIP_IT is true: Integration Tests and Docker containers are disabled."
                            fi
                        fi
                    """
                }
            }
            post {
                always {
                    echo "Collecting test results..."
                    script {
                        // Quét kết quả test dựa trên service path để chính xác hơn
                        def reportPattern = "${env.SERVICE_PATH}/**/target/surefire-reports/*.xml,${env.SERVICE_PATH}/**/target/failsafe-reports/*.xml"
                        junit testResults: reportPattern, allowEmptyResults: true
                    }
                }
            }
        }
        
        stage('Code Coverage Validation (JaCoCo 70% Threshold)') {
            when { expression { !params.SKIP_TESTS } }
            steps {
                echo "Kiểm tra độ phủ code cho ${env.TARGET_SERVICE}..."
                // Lệnh này sẽ quét file jacoco.exec vừa tạo ra ở stage Test
                // Nếu độ phủ < 0.7 (70%), Maven sẽ trả về lỗi và Jenkins sẽ dừng stage này ngay lập tức.
                sh "mvn jacoco:check -pl ${env.TARGET_SERVICE} -am -Djacoco.haltOnFailure=true -Djacoco.line.minimum=0.70"
            }
        }
        
        stage('SonarCloud Analysis - Security & Quality') {
            when {
                expression { !params.SKIP_SONAR && env.TARGET_SERVICE != 'root' && !params.SKIP_TESTS }
            }
            steps {
                echo "Running SonarCloud scan for $TARGET_SERVICE..."
                sh '''
                    mvn sonar:sonar -pl $TARGET_SERVICE -am \
                        -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                        -Dsonar.organization=${SONAR_ORGANIZATION} \
                        -Dsonar.host.url=https://sonarcloud.io \
                        -Dsonar.login=${SONAR_TOKEN} \
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
                    fileExists("${env.SERVICE_PATH}/Dockerfile") &&
                    env.TARGET_SERVICE != 'common-library' &&
                    !env.TARGET_SERVICE.contains('automation')
                }
            }
            steps {
                echo "Building Docker image for ${env.TARGET_SERVICE}..."
                sh '''
                    cd ${SERVICE_PATH}
                    IMAGE_NAME=$(echo ${TARGET_SERVICE} | tr '-' '_')
                    
                    docker build \
                        --tag ${REGISTRY_URL}/nashtech-garage/${IMAGE_NAME}:${BUILD_VERSION} \
                        --tag ${REGISTRY_URL}/nashtech-garage/${IMAGE_NAME}:latest \
                        --build-arg BUILD_DATE=$(date -u +'%Y-%m-%dT%H:%M:%SZ') \
                        --build-arg VCS_REF=${GIT_COMMIT_SHORT} \
                        --build-arg VERSION=${BUILD_VERSION} .
                    
                    echo "Docker image built successfully: ${REGISTRY_URL}/nashtech-garage/${IMAGE_NAME}:${BUILD_VERSION}"
                '''
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
                echo "Archiving artifacts..."
                // Dùng wildcard ** để quét tất cả các module đã build
                archiveArtifacts artifacts: "${env.SERVICE_PATH}/target/*.json, ${env.SERVICE_PATH}/target/surefire-reports/*.xml, ${env.SERVICE_PATH}/target/failsafe-reports/*.xml", 
                                 allowEmptyArchive: true
                
                if (fileExists("${env.SERVICE_PATH}/target/site/jacoco/index.html")) {
                    publishHTML([
                        allowMissing: true,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: "${env.SERVICE_PATH}/target/site/jacoco",
                        reportFiles: 'index.html',
                        reportName: 'JaCoCo Code Coverage Report'
                    ])
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
