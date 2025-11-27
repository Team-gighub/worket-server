pipeline {
    agent any

    environment {
        DOCKER_IMAGE = "rudska6/worket-server"
        DOCKER_TAG = "dev"
        EC2_HOST = "ubuntu@13.210.31.24"        // 백엔드 서버 IP
        EC2_KEY = "ubuntu-ssh"                  // Jenkins SSH key ID
        COMPOSE_FILE = "docker-compose.prod.yml"
        ENV_FILE = ".env.prod"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'feature/deploy-setup',
                    credentialsId: 'github-credentials',
                    url: 'https://github.com/Team-gighub/worket-server.git'
            }
        }

        stage('Build JAR') {
            steps {
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
                withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials',
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
                sshagent(credentials: ['ubuntu-ssh']) {
                    sh """
                    ssh -o StrictHostKeyChecking=no ${EC2_HOST} '
                        cd ~/worket-server || mkdir ~/worket-server && cd ~/worket-server;

                        # Git pull 최신 compose & env 파일 유지
                        git pull || true;

                        # compose 환경변수 파일 저장
                        echo "IMAGE_NAME=${DOCKER_IMAGE}" > ${ENV_FILE}
                        echo "IMAGE_TAG=${DOCKER_TAG}" >> ${ENV_FILE}

                        # 최신 이미지 pull
                        docker pull ${DOCKER_IMAGE}:${DOCKER_TAG};

                        # compose 재시작
                        docker compose --env-file ${ENV_FILE} -f ${COMPOSE_FILE} down || true;
                        docker compose --env-file ${ENV_FILE} -f ${COMPOSE_FILE} up -d;
                    '
                    """
                }
            }
        }
    }
}
