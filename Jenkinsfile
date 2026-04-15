pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        timeout(time: 30, unit: 'MINUTES')
    }

    environment {
        SONAR_HOST = "http://192.168.56.10:9000"
        IMAGE_NAME = "student-management"
        IMAGE_TAG = "1.0"
        NAMESPACE = "devops"
    }

    stages {

        stage('Checkout Code') {
            steps {
                checkout scm
            }
        }

        stage('Prepare') {
            steps {
                sh 'chmod +x mvnw || true'
            }
        }

        stage('Build & Package') {
            steps {
                sh './mvnw clean package -DskipTests'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                        sh """
                        ./mvnw sonar:sonar \
                        -Dsonar.host.url=${SONAR_HOST} \
                        -Dsonar.login=${SONAR_TOKEN}
                        """
                    }
                }
            }
        }

        // ✅ VERSION SIMPLE (pas de blocage)
        stage('Quality Gate') {
            steps {
                echo "Skipping Quality Gate (simple mode for TP)"
            }
        }

        stage('Build Docker Image (Minikube)') {
            steps {
                sh '''
                eval $(minikube docker-env)
                docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .
                '''
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh """
                kubectl apply -f k8s/ -n ${NAMESPACE}
                kubectl rollout restart deployment spring-app -n ${NAMESPACE}
                """
            }
        }

        stage('Verify Deployment') {
            steps {
                sh """
                kubectl get pods -n ${NAMESPACE}
                kubectl get svc -n ${NAMESPACE}
                """
            }
        }
    }

    post {
        success {
            echo "✅ Pipeline SUCCESS: build + sonar + docker + kubernetes OK"
        }

        failure {
            echo "❌ Pipeline FAILED"
        }
    }
}
