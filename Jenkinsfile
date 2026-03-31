pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        timeout(time: 20, unit: 'MINUTES')
    }

    environment {
        SONAR_HOST = "http://192.168.49.2:9000"
        IMAGE_NAME = "student-app"
        IMAGE_TAG = "1.0"
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main',
                url: 'git@github.com:nihed-eng/student-management.git'
            }
        }

        stage('Build & Test') {
            steps {
                sh """
                    chmod +x mvnw
                    ./mvnw clean package -DskipTests=false
                """
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                    withSonarQubeEnv('SonarQube') {
                        sh """
                            ./mvnw sonar:sonar \
                            -Dsonar.host.url=${SONAR_HOST} \
                            -Dsonar.login=${SONAR_TOKEN} \
                            -Dsonar.qualitygate.wait=true
                        """
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh """
                    docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .
                """
            }
        }

        stage('Run Container') {
            steps {
                sh """
                    docker rm -f student-app || true
                    docker run -d --name student-app -p 8089:8089 ${IMAGE_NAME}:${IMAGE_TAG}
                """
            }
        }
    }

    post {
        success {
            echo "✅ Pipeline terminé avec succès"
        }

        failure {
            echo "❌ Pipeline échoué"
        }
    }
}
