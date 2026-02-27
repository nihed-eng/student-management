pipeline {
    agent any

    environment {
        IMAGE_NAME = "student-management"
    }

    stages {

        stage('Checkout Code') {
            steps {
                git credentialsId: 'token',
                url: 'https://github.com/nihed-eng/student-management.git',
                branch: 'main'
            }
        }

        stage('Build Maven') {
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw clean package'
            }
        }

      stage('Run Tests') {
    steps {
        sh './mvnw test -Dspring.profiles.active=test ' +
           '-Dspring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL ' +
           '-Dspring.datasource.driverClassName=org.h2.Driver ' +
           '-Dspring.datasource.username=sa ' +
           '-Dspring.datasource.password= ' +
           '-Dspring.jpa.database-platform=org.hibernate.dialect.H2Dialect'
    }
}

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh './mvnw sonar:sonar'
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 2, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
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
