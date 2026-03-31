pipeline {
    agent any

    environment {
        DOCKER_IMAGE = "nihedmath/student-app"
        DOCKER_TAG = "${BUILD_NUMBER}"
    }

    stages {

        stage('Checkout') {
            steps {
                git credentialsId: 'token',
                    url: 'https://github.com/nihed-eng/student-management.git',
                    branch: 'main'
            }
        }

        stage('Build & Test') {
            steps {
                sh 'chmod +x mvnw'
                sh """
                ./mvnw clean package \
                -Dspring.datasource.url="jdbc:h2:mem:testdb;MODE=MySQL" \
                -Dspring.datasource.driverClassName=org.h2.Driver \
                -Dspring.datasource.username=sa \
                -Dspring.datasource.password= \
                -Dspring.jpa.database-platform=org.hibernate.dialect.H2Dialect
                """
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                    withSonarQubeEnv('SonarQube') {
                        sh """
                        ./mvnw sonar:sonar \
                        -Dsonar.login=${SONAR_TOKEN} \
                        -Dsonar.host.url=http://192.168.56.10:9000
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
                sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
                sh "docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest"
            }
        }

        stage('Push Docker Image') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh "echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin"
                    sh "docker push ${DOCKER_IMAGE}:${DOCKER_TAG}"
                    sh "docker push ${DOCKER_IMAGE}:latest"
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh "kubectl apply -f k8s/ -n devops"
                sh "kubectl set image deployment/spring-app spring=${DOCKER_IMAGE}:${DOCKER_TAG} -n devops"
                sh "kubectl rollout status deployment/spring-app -n devops"
            }
        }
    }

    post {
        success {
            echo '✅ Pipeline exécutée avec succès'
        }
        failure {
            echo '❌ Pipeline échouée'
        }
    }
}
