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

      stage('Setup MySQL Container') {
    steps {
        sh '''
        docker rm -f student-mysql || true

        docker run -d --name student-mysql --network host \
        -e MYSQL_ROOT_PASSWORD= \
        -e MYSQL_ALLOW_EMPTY_PASSWORD=yes \
        -e MYSQL_DATABASE=studentdb \
        mysql:8.0
        '''
    }
}

        stage('Build & Test Maven') {
            steps {
                sh 'chmod +x mvnw'
                // On lance le build. Spring trouvera MySQL sur localhost:3306 (grâce au network host de Docker)
                sh './mvnw clean package'
            }
            post {
                always {
                    // TRÈS IMPORTANT : On supprime le container après le test, même si ça échoue
                    sh 'docker rm -f student-mysql || true'
                }
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
