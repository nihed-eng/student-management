pipeline {
    agent any

    stages {

        stage('Build Maven') {
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw clean package -DskipTests'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh './mvnw sonar:sonar -DskipTests'
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline exécutée avec succès'
        }
        failure {
            echo 'Pipeline échouée'
        }
    }
}
