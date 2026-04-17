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

        IMAGE_NAME = "nihedmath/student-management"
        IMAGE_TAG  = "${BUILD_NUMBER}"

        K8S_NAMESPACE   = "devops"
        DEPLOYMENT_NAME = "spring-app"
        CONTAINER_NAME  = "spring"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh '''
                    chmod +x mvnw
                    ./mvnw clean package -DskipTests
                '''
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                        sh '''
                            ./mvnw sonar:sonar \
                              -Dsonar.host.url=$SONAR_HOST \
                              -Dsonar.token=$SONAR_TOKEN
                        '''
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
                sh '''
                    echo "🐳 Building Docker image..."
                    docker build -t $IMAGE_NAME:$IMAGE_TAG .
                    docker tag $IMAGE_NAME:$IMAGE_TAG $IMAGE_NAME:latest
                '''
            }
        }

        stage('Push Docker Image') {
            steps {
                echo "⏭️ Skipping Docker push (local Minikube)"
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh '''
                    set -e

                    echo "📦 Checking namespace..."
                    kubectl get namespace $K8S_NAMESPACE || kubectl create namespace $K8S_NAMESPACE

                    echo "🧹 Deleting old deployment (fix immutable selector)..."
                    kubectl delete deployment $DEPLOYMENT_NAME -n $K8S_NAMESPACE || true

                    echo "📂 Applying Kubernetes manifests..."
                    kubectl apply -f k8s/ -n $K8S_NAMESPACE

                    echo "🚀 Updating deployment image..."
                    kubectl set image deployment/$DEPLOYMENT_NAME \
                    $CONTAINER_NAME=$IMAGE_NAME:$IMAGE_TAG \
                    -n $K8S_NAMESPACE

                    echo "⏳ Waiting for rollout..."
                    kubectl rollout status deployment/$DEPLOYMENT_NAME \
                    -n $K8S_NAMESPACE --timeout=120s
                '''
            }
        }

        stage('Verify Deployment') {
            steps {
                sh '''
                    echo "📊 Pods:"
                    kubectl get pods -n $K8S_NAMESPACE

                    echo "🌐 Services:"
                    kubectl get svc -n $K8S_NAMESPACE
                '''
            }
        }
    }

    post {
        success {
            echo "✅ PIPELINE SUCCESS: Build → Sonar → Docker → Kubernetes"
        }
        failure {
            echo "❌ PIPELINE FAILED"
        }
        always {
            cleanWs()
        }
    }
}
