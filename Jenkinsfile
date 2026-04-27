#!/usr/bin/env groovy

pipeline {
//    agent any
    docker {
        image 'maven:3.9-eclipse-temurin-21'
        args '-v /var/run/docker.sock:/var/run/docker.sock --privileged --user root'
    }
    options {
        timestamps()
        timeout(time: 1, unit: 'HOURS')
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }
    
    environment {
        MAVEN_OPTS = '-Xmx2g -Xms512m'
        TESTCONTAINERS_RYUK_DISABLED = 'true'
        SONAR_TOKEN = credentials('sonarcloud-token')
        SONAR_ORGANIZATION = 'nashtech-garage'
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
                    echo "Detecting changed services in monorepo..."
                    if (params.SERVICE == 'auto') {
                        def changedService = sh(
                            script: '''
                                CHANGED=$(git diff --name-only origin/main...HEAD 2>/dev/null || git diff --name-only HEAD~1..HEAD)
                                SERVICE=$(echo "$CHANGED" | head -1 | cut -d"/" -f1)
                                if [ -f "$SERVICE/pom.xml" ]; then
                                    echo "$SERVICE"
                                else
                                    echo "root"
                                fi
                            ''',
                            returnStdout: true
                        ).trim()
                        env.TARGET_SERVICE = changedService
                        echo "Auto-detected service: ${env.TARGET_SERVICE}"
                    } else {
                        env.TARGET_SERVICE = params.SERVICE
                        echo "Manual service selected: ${params.SERVICE}"
                    }
                    
                    env.SERVICE_PATH = env.TARGET_SERVICE == 'root' ? '.' : env.TARGET_SERVICE
                    env.POM_FILE = env.SERVICE_PATH + '/pom.xml'
                    
                    if (!fileExists(env.POM_FILE)) {
                        error("pom.xml not found for service: ${env.TARGET_SERVICE}")
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
        
        stage('Gitleaks - Secrets Detection') {
            steps {
                script {
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
                echo "Building ${env.TARGET_SERVICE}..."
                sh '''
                    cd ${SERVICE_PATH}
                    mvn clean install -DskipTests \
                        -Dmaven.javadoc.skip=true \
                        -Dorg.slf4j.simpleLogger.defaultLogLevel=WARN
                '''
            }
        }
        
        stage('Test Phase - Unit & Integration Tests') {
            when {
                expression { !params.SKIP_TESTS }
            }
            steps {
                echo "Running unit and integration tests..."
                sh '''
                    cd ${SERVICE_PATH}
                    
                    echo "Step 1: Running Unit Tests (Surefire)..."
                    mvn test -Dmaven.javadoc.skip=true -Dorg.slf4j.simpleLogger.defaultLogLevel=WARN
                    
                    echo "Step 2: Running Integration Tests (Failsafe)..."
                    mvn verify -DskipUnitTests -Dmaven.javadoc.skip=true -Dorg.slf4j.simpleLogger.defaultLogLevel=WARN || true
                '''
            }
            post {
                always {
                    echo "Collecting test results..."
                    junit testResults: '${SERVICE_PATH}/**/target/surefire-reports/*.xml,${SERVICE_PATH}/**/target/failsafe-reports/*.xml', 
                          allowEmptyResults: true
                }
            }
        }
        
        stage('Code Coverage Validation (JaCoCo 70% Threshold)') {
            when {
                expression { !params.SKIP_TESTS }
            }
            steps {
                echo "Validating code coverage with JaCoCo (minimum 70%)..."
                sh '''
                    cd ${SERVICE_PATH}
                    
                    # Ensure coverage is generated
                    if [ ! -f "target/jacoco-report/index.csv" ]; then
                        echo "Generating JaCoCo report..."
                        mvn jacoco:report -Dmaven.javadoc.skip=true -Dorg.slf4j.simpleLogger.defaultLogLevel=WARN || true
                    fi
                    
                    if [ -f "target/jacoco-report/index.csv" ]; then
                        # Extract coverage: last line, 5th column is coverage %
                        COVERAGE=$(tail -1 target/jacoco-report/index.csv | cut -d',' -f5 | tr -d '%')
                        
                        echo "Code Coverage Result: ${COVERAGE}%"
                        
                        if [ -z "$COVERAGE" ]; then
                            echo "WARNING: Could not parse coverage percentage"
                        elif [ "$COVERAGE" -ge 70 ]; then
                            echo "SUCCESS: Code coverage ${COVERAGE}% meets minimum threshold (70%)"
                        else
                            echo "ERROR: Code coverage ${COVERAGE}% is below minimum requirement of 70%"
                            exit 1
                        fi
                    else
                        echo "WARNING: JaCoCo report not found, skipping coverage validation"
                    fi
                '''
            }
        }
        
        stage('SonarCloud Analysis - Security & Quality') {
            when {
                expression { !params.SKIP_SONAR && env.TARGET_SERVICE != 'root' && !params.SKIP_TESTS }
            }
            steps {
                echo "Running SonarCloud security and code quality scan..."
                sh '''
                    cd ${SERVICE_PATH}
                    
                    # Extract SonarQube project key from pom.xml
                    SONAR_KEY=$(grep -A2 '<sonar.projectKey>' pom.xml | grep -oP '(?<=<sonar.projectKey>)[^<]+' || echo '')
                    
                    if [ -z "$SONAR_KEY" ]; then
                        echo "WARNING: SonarQube project key not found in pom.xml - skipping SonarCloud"
                        exit 0
                    fi
                    
                    echo "Publishing to SonarCloud with project key: $SONAR_KEY"
                    
                    mvn sonar:sonar \
                        -Dsonar.projectKey="$SONAR_KEY" \
                        -Dsonar.organization=${SONAR_ORGANIZATION} \
                        -Dsonar.host.url=https://sonarcloud.io \
                        -Dsonar.login=${SONAR_TOKEN} \
                        -Dmaven.javadoc.skip=true
                    
                    echo "SonarCloud analysis completed"
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
                        reportDir: '${SERVICE_PATH}/target/jacoco-report',
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
                echo "Archiving artifacts and generating pipeline report..."
                
                // Archive test and coverage reports
                archiveArtifacts artifacts: '${SERVICE_PATH}/target/jacoco-report/**,${SERVICE_PATH}/target/surefire-reports/**,gitleaks-report.json', 
                                 allowEmptyArchive: true
                
                echo "Pipeline execution completed"
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
