pipeline {
    agent any

    tools { 
        maven 'maven'
    }

    environment {
        MYSQL_ROOT_LOGIN = credentials('mysql')
    }

    stages {
        stage('Build with Maven') {
            steps {
                sh 'mvn --version'
                sh 'java -version'
                sh 'mvn clean package -Dmaven.test.failure.ignore=true'
            }
        }

        stage('Packaging/Pushing imagae') {

            steps {                
                withDockerRegistry(credentialsId: 'dockerhub', url: 'https://index.docker.io/v1/') {
                    sh 'docker build -t rubik2k3/jenkins_demo .'
                    sh 'docker push rubik2k3/jenkins_demo'
                }
            }
        }

        stage('Deploy MySql to EC2') {
            steps {
             script {
               def deployingScript = "#!/bin/bash\n"+
               "sudo -i\n"+
               "docker image pull mysql:8.0\n"+
               "docker network create dev || echo 'this network exists'\n"+
               "docker container stop jenkins_example-mysql || echo 'this container does not exist'\n"+ 
               "echo y | docker container prune\n"+
               "docker volume rm jenkins_example-mysql-data || echo 'no volume'\n"+
               "docker run --name jenkins_example-mysql --rm --network dev -v jenkins_example-mysql-data:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_LOGIN_PSW} -e MYSQL_DATABASE=jenkins_example  -d mysql:8.0\n"+
               "sleep 20"
               sshagent(['ec2']) {
                   sh """
                   ssh -o StrictHostKeyChecking=no ubuntu@ec2-184-72-179-221.compute-1.amazonaws.com "echo \\\"${deployingScript}\\\" > deploy.sh && chmod +x deploy.sh && ./deploy.sh"
                   """
               }
              }
            }
        }

        stage('Deploy Spring Boot to EC2') {
            steps {
             script {
               def deployingScript = "#!/bin/bash\n"+
               "sudo -i\n"+
               "docker image pull rubik2k3/jenkins_demo\n"+
               "docker container stop rubik2k3-jenkins_demo || echo 'this container does not exist'\n"+
               "docker network create dev || echo 'this network exists'\n"+
               "echo y | docker container prune \n"+
               "docker container run -d --rm --name rubik2k3-jenkins_demo -p 8081:8080 --network dev rubik2k3/jenkins_demo\n"

               sshagent(['ec2']) {
                   sh """
                   ssh -o StrictHostKeyChecking=no ubuntu@ec2-184-72-179-221.compute-1.amazonaws.com "echo \\\"${deployingScript}\\\" > deploy.sh && chmod +x deploy.sh && ./deploy.sh"
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