pipeline {
    agent any

    environment {
        DOCKER_IMAGE = "rudska6/worket-server"
        DOCKER_TAG = "latest"
        EC2_HOST = "ubuntu@${SERVER_IP}"        // 백엔드 서버 IP
        EC2_KEY = "deploy-key"                  // Jenkins SSH key ID
        COMPOSE_FILE = "docker-compose.yml"
        ENV_FILE = ".env"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main',
                    credentialsId: 'github',
                    url: 'https://github.com/Team-gighub/worket-server.git'
            }
        }

        stage('Build JAR') {
            steps {
                sh 'chmod +x gradlew'
                sh './gradlew clean build -x test'
            }
        }

        stage('Docker Build') {
            steps {
                sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
            }
        }

        stage('Docker Login') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-login',
                                usernameVariable: 'DOCKER_USER',
                                passwordVariable: 'DOCKER_PASS')]) {
                    sh "echo \"$DOCKER_PASS\" | docker login -u \"$DOCKER_USER\" --password-stdin"
                }
            }
        }

        stage('Docker Push') {
            steps {
                sh "docker push ${DOCKER_IMAGE}:${DOCKER_TAG}"
            }
        }

        stage('Deploy to EC2 (docker-compose)') {
            steps {
                sshagent(credentials: ['deploy-key']) {
                    sh """
                    ssh -v -o StrictHostKeyChecking=no ${EC2_HOST} '
                        cd ~/worket-server || mkdir ~/worket-server && cd ~/worket-server;

                        # 최신 이미지 pull
                        docker pull ${DOCKER_IMAGE}:${DOCKER_TAG};

                        docker rm -f worket-server || true

                        docker compose up -d;
                    '
                    """
                }
            }
        }
    }
}
