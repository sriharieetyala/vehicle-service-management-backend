pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    environment {
        SONAR_TOKEN = '74593bc7c25686ec55108e653e3873da14e47f9d'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/sriharieetyala/vehicle-service-management-backend.git'
            }
        }

        stage('Build & Test') {
            steps {
                bat 'mvn package'
            }
        }

        stage('SonarCloud Analysis') {
            steps {
                bat 'mvn sonar:sonar -Dsonar.token=%SONAR_TOKEN%'
            }
        }

        stage('Build Docker Images') {
            steps {
                bat 'docker-compose build'
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed.'
        }
    }
}