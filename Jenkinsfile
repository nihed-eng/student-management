pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        timeout(time: 20, unit: 'MINUTES')
    }

    environment {
        SONAR_HOST = "http://192.168.56.10:9000"
        IMAGE_NAME = "student-app"
        IMAGE_TAG = "1.0"
        CONTAINER_NAME = "student-app"
    }

    stages {

        stage('Prepare') {
            steps {
                sh '''
                    echo "🔧 Fix permissions mvnw"
                    chmod +x mvnw || true
                '''
            }
        }

        stage('Build & Package') {
            steps {
                sh '''
                    ./mvnw clean package -DskipTests
                '''
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                    withSonarQubeEnv('SonarQube') {
                        sh """
                            ./mvnw sonar:sonar \
                            -Dsonar.host.url=${SONAR_HOST} \
                            -Dsonar.login=$SONAR_TOKEN
                        """
                    }
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
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

        stage('Deploy Container') {
            steps {
                sh """
                    docker rm -f ${CONTAINER_NAME} || true
                    docker run -d \
                        --name ${CONTAINER_NAME} \
                        -p 8089:8089 \
                        ${IMAGE_NAME}:${IMAGE_TAG}
                """
            }
        }
    }

    post {
        always {
            echo "📌 Nettoyage post-build"
            sh "docker image prune -f || true"
        }

        success {
            echo "✅ Pipeline réussi"
        }

        failure {
            echo "❌ Pipeline échoué - vérifier logs"
        }
    }
}
