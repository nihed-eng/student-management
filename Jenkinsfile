pipeline {
    agent any

    stages {

        stage('Build Maven') {
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw clean verify'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh './mvnw sonar:sonar'
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
