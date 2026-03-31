pipeline {
    agent any

    environment {
        DOCKER_IMAGE = "nihedmath/student-app"
        DOCKER_TAG = "${BUILD_NUMBER}"
        SONAR_HOST = "http://192.168.56.10:9000"
    }

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {

        stage('Checkout') {
            steps {
                cleanWs()
                git credentialsId: 'token',
                    url: 'https://github.com/nihed-eng/student-management.git',
                    branch: 'main'
            }
        }

        stage('Build & Test') {
            steps {
                sh 'chmod +x mvnw'
                sh """
                    ./mvnw clean package -DskipTests
                """
            }
        }

        stage('Unit Tests') {
            steps {
                sh "./mvnw test"
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                    withSonarQubeEnv('SonarQube') {
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
                    waitForQualityGate abortPipeline: false
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh """
                    docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} .
                    docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest
                """
            }
        }

        stage('Push Docker Image') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh """
                        echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin
                        docker push ${DOCKER_IMAGE}:${DOCKER_TAG}
                        docker push ${DOCKER_IMAGE}:latest
                    """
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh """
                    kubectl apply -f k8s/ -n devops
                    kubectl set image deployment/spring-app \
                        spring=${DOCKER_IMAGE}:${DOCKER_TAG} -n devops
                    kubectl rollout status deployment/spring-app -n devops --timeout=120s
                """
            }
        }
    }

    post {
        success {
            echo "✅ Pipeline réussie - image ${DOCKER_TAG} déployée"
        }

        failure {
            echo "❌ Pipeline échouée - vérifier logs"
        }

        always {
            cleanWs()
        }
    }
}
