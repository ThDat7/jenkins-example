pipeline {
    agent any

    tools { 
        maven 'maven'
        dockerTool 'docker'
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
                script {
                    docker.build('rubik2k3/jenkins_demo')
                    docker.withRegistry(url: 'https://index.docker.io/v1/', credentialsId: 'dockerhub') {
                        docker.image('rubik2k3/jenkins_demo').push('latest')
                    }
                }
                
                // withDockerRegistry(credentialsId: 'dockerhub', url: 'https://index.docker.io/v1/') {
                //     sh 'docker build -t rubik2k3/jenkins_demo .'
                //     sh 'docker push rubik2k3/jenkins_demo'
                // }
            }
        }

        stage('Deploy MySql to EC2') {
            steps {
             script {
               def deployingScript = '''#!/bin/bash
               docker image pull mysql:8.0
               docker network create dev || echo "this network exists
               docker container stop jenkins_example-mysql || echo "this container does not exist" 
               echo y | docker container prune 
               docker volume rm jenkins_example-mysql-data || echo "no volume"
               docker run --name jenkins_example-mysql --rm --network dev -v jenkins_example-mysql-data:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_LOGIN_PSW} -e MYSQL_DATABASE=jenkins_example  -d mysql:8.0 
               sleep 20               
               '''
               sshagent(['ec2']) {
                   sh '''ssh -o StrictHostKeyChecking=no ubuntu@ec2-184-72-179-221.compute-1.amazonaws.com "echo \\\${deployingScript} > /tmp/deployingScript.sh && chmod +x /tmp/deployingScript.sh && /tmp/deployingScript.sh"'''
               }
              }
            }
        }

        stage('Deploy Spring Boot to EC2') {
            steps {
             script {
               def deployingScript = '''#!/bin/bash
               docker image pull rubik2k3/jenkins_demo
               docker container stop rubik2k3-jenkins_demo || echo "this container does not exist"
               docker network create dev || echo "this network exists"
               echo y | docker container prune 
               docker container run -d --rm --name rubik2k3-jenkins_demo -p 8081:8080 --network dev rubik2k3/jenkins_demo
               '''
               sshagent(['ec2']) {
                   sh '''ssh -o StrictHostKeyChecking=no ubuntu@ec2-184-72-179-221.compute-1.amazonaws.com "echo \\\${deployingScript} > /tmp/deployingScript.sh && chmod +x /tmp/deployingScript.sh && /tmp/deployingScript.sh"'''
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