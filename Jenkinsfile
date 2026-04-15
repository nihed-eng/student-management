pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
    }

    environment {
        SONAR_HOST = "http://192.168.56.10:9000"

        IMAGE_NAME = "nihed/student-management"
        IMAGE_TAG = "${BUILD_NUMBER}"

        K8S_NAMESPACE = "devops"
        DEPLOYMENT_NAME = "spring-app"
        CONTAINER_NAME = "spring-app"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Prepare Maven') {
            steps {
                sh 'chmod +x mvnw'
            }
        }

        stage('Build & Test') {
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

        stage('Quality Gate') {
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    script {
                        def qg = waitForQualityGate()
                        if (qg.status != 'OK') {
                            error "❌ Quality Gate failed: ${qg.status}"
                        }
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    sh """
                    docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .
                    """
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh """
                kubectl apply -f k8s/ -n ${K8S_NAMESPACE}

                kubectl set image deployment/${DEPLOYMENT_NAME} \
                ${CONTAINER_NAME}=${IMAGE_NAME}:${IMAGE_TAG} \
                -n ${K8S_NAMESPACE}

                kubectl rollout status deployment/${DEPLOYMENT_NAME} -n ${K8S_NAMESPACE}
                """
            }
        }

        stage('Verify Deployment') {
            steps {
                sh """
                kubectl get pods -n ${K8S_NAMESPACE}
                kubectl get svc -n ${K8S_NAMESPACE}
                """
            }
        }
    }

    post {
        success {
            echo "✅ PIPELINE SUCCESS: Build → Sonar → Docker → Kubernetes deployed"
        }

        failure {
            echo "❌ PIPELINE FAILED: check logs"
        }

        always {
            cleanWs()
        }
    }
}
