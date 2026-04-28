pipeline {
    agent any

    tools {
        maven 'Maven 3.9' 
        jdk 'Java 21'    
    }

    stages {
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

            }
        }
    }
    
    post {
        always {
            junit '**/target/surefire-reports/*.xml'
        }
    }
}
