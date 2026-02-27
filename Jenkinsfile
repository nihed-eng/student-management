pipeline {
    agent any

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

        stage('SonarQube') {
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
