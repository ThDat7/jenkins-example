pipeline {
    agent any

    tools { 
        maven 'maven'
    }

    environment {
        MYSQL_ROOT_LOGIN = credentials('mysql')
        DOCKER_HUB = 'rubik2k3'
        MYSQL_CONTAINER_NAME = 'jenkins_example-mysql'
        DATABASE_NAME = 'jenkins_example'
        BACKEND_IMAGE = 'jenkins_demo'
        EC2_HOST = 'ec2-54-227-104-197.compute-1.amazonaws.com'
    }

    stages {
        stage('Build with Maven') {
            steps {
                sh 'mvn --version'
                sh 'java -version'
                sh 'mvn clean package -Dmaven.test.failure.ignore=true'
            }
        }

        stage('Packaging/Pushing image') {

            steps {                
                withDockerRegistry(credentialsId: 'dockerhub', url: 'https://index.docker.io/v1/') {
                    sh "docker build -t ${DOCKER_HUB}/${BACKEND_IMAGE} ."
                    sh "docker push ${DOCKER_HUB}/${BACKEND_IMAGE}"
                }
            }
        }

        stage('Deploy MySql to EC2') {
            steps {
             script {
               def deployingScript = "#!/bin/bash\n"+
               "docker image pull mysql:8.0\n"+
               "docker network create dev || echo 'this network exists'\n"+
               "docker container stop ${MYSQL_CONTAINER_NAME} || echo 'this container does not exist'\n"+ 
               "echo y | docker container prune\n"+
               "docker volume rm ${MYSQL_CONTAINER_NAME}-data || echo 'no volume'\n"+
               "docker run --name ${MYSQL_CONTAINER_NAME} --rm --network dev -v ${MYSQL_CONTAINER_NAME}-data:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_LOGIN_PSW} -e MYSQL_DATABASE=${DATABASE_NAME}  -d mysql:8.0\n"+
               "sleep 20"
               sshagent(['ec2']) {
                   sh """
                   ssh -o StrictHostKeyChecking=no ubuntu@${EC2_HOST} "echo \\\"${deployingScript}\\\" > deploy.sh && chmod +x deploy.sh && sudo ./deploy.sh"
                   """
               }
              }
            }
        }

        stage('Deploy Spring Boot to EC2') {
            steps {
             script {
               def deployingScript = "#!/bin/bash\n"+
               "docker image pull ${DOCKER_HUB}/${BACKEND_IMAGE}\n"+
               "docker container stop rubik2k3-jenkins_demo || echo 'this container does not exist'\n"+
               "docker network create dev || echo 'this network exists'\n"+
               "echo y | docker container prune \n"+
               "docker container run -d --rm --name rubik2k3-jenkins_demo -p 80:8081 --network dev ${DOCKER_HUB}/${BACKEND_IMAGE}\n"

               sshagent(['ec2']) {
                   sh """
                   ssh -o StrictHostKeyChecking=no ubuntu@${EC2_HOST} "echo \\\"${deployingScript}\\\" > deploy.sh && chmod +x deploy.sh && sudo ./deploy.sh"
                   """
               }
              }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}