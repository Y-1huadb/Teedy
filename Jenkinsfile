pipeline {
    agent any
    environment {
        // define environment variable
        // Jenkins credentials configuration
        DOCKER_HUB_CREDENTIALS = credentials('Docker-Hub') // Docker Hub credentials ID store in Jenkins
        // Docker Hub Repository's name
        DOCKER_IMAGE = 'y261/teedy' // your Docker Hub user name and Repository's name
        DOCKER_TAG = "${env.BUILD_NUMBER}" // use build number as tag
        DOCKER_USER = 'y261'
        DOCKER_PASS = '12syy.Sustech'
    }
    stages {
        stage('Build') {
            steps {
                checkout scmGit(
                    branches: [[name: '*/master']],
                    extensions: [],
                    userRemoteConfigs: [[url: 'https://github.com/Y-1huadb/Teedy.git']] // your github Repository
                )
                sh 'mvn -B -DskipTests clean package'
            }
        }
        // Building Docker images
        stage('Building image') {
            steps {
                script {
                    // assume Dockerfile locate at root
                    docker.build("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}")
                }
            }
        }
        // Uploading Docker images into Docker Hub
        stage('Upload image') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'Docker-Hub', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh '''
                        # 登录到 dockerpull.cn
                        echo $DOCKER_PASS | docker login https://dockerpull.cn -u $DOCKER_USER --password-stdin
                        
                        # 推送镜像到 dockerpull.cn
                        docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} dockerpull.cn/${DOCKER_IMAGE}:${DOCKER_TAG}
                        docker push dockerpull.cn/${DOCKER_IMAGE}:${DOCKER_TAG}
                        
                        # 推送 latest 标签到 dockerpull.cn
                        docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} dockerpull.cn/${DOCKER_IMAGE}:latest
                        docker push dockerpull.cn/${DOCKER_IMAGE}:latest
                    '''
                }
            }
        }
        // Running Docker container
        stage('Run containers') {
            steps {
                script {
                    // stop then remove containers if exists
                    sh 'docker stop teedy-container-8081 || true'
                    sh 'docker rm teedy-container-8081 || true'
                    // run Container
                    docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").run('--name teedy-container-8081 -d -p 8081:8080')
                    // Optional: list all teedy-containers
                    sh 'docker ps --filter "name=teedy-container"'
                }
            }
        }
    }
}