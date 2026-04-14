pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '15'))
    }

    environment {
        TF_IN_AUTOMATION = 'true'
        VM_USER = 'yc-user'
        YC_ZONE = 'ru-central1-b'
        ANSIBLE_HOST_KEY_CHECKING = 'False'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh '''
                    set -eux
                    /usr/share/maven/bin/mvn -B clean package -DskipTests
                    ls -lah target
                    test -f target/WeatherBot.jar
                '''
            }
        }

        stage('Test') {
            steps {
                sh '''
                    set -eux
                    /usr/share/maven/bin/mvn -B test
                '''
            }
        }

        stage('Terraform Init') {
            steps {
                dir('infra') {
                    withCredentials([
                        string(credentialsId: 'yc-cloud-id', variable: 'YC_CLOUD_ID'),
                        string(credentialsId: 'yc-folder-id', variable: 'YC_FOLDER_ID'),
                        string(credentialsId: 'yc-token', variable: 'YC_TOKEN'),
                        file(credentialsId: 'ssh-public-key', variable: 'SSH_PUBLIC_KEY_FILE')
                    ]) {
                        sh '''
                            set -eux
                            rm -rf .jenkins-secrets
                            mkdir -p .jenkins-secrets
                            cp "$SSH_PUBLIC_KEY_FILE" .jenkins-secrets/id_rsa.pub

                            terraform -chdir=tf init
                            terraform -chdir=tf validate
                            terraform -chdir=tf plan \
                              -var="cloud_id=$YC_CLOUD_ID" \
                              -var="folder_id=$YC_FOLDER_ID" \
                              -var="token=$YC_TOKEN" \
                              -var="zone=$YC_ZONE" \
                              -var="ssh_public_key_path=$(pwd)/.jenkins-secrets/id_rsa.pub" \
                              -var="vm_user=$VM_USER" \
                              -out=tfplan
                        '''
                    }
                }
            }
        }

        stage('Terraform Apply') {
            steps {
                dir('infra') {
                    withCredentials([
                        string(credentialsId: 'yc-cloud-id', variable: 'YC_CLOUD_ID'),
                        string(credentialsId: 'yc-folder-id', variable: 'YC_FOLDER_ID'),
                        string(credentialsId: 'yc-token', variable: 'YC_TOKEN'),
                        file(credentialsId: 'ssh-public-key', variable: 'SSH_PUBLIC_KEY_FILE')
                    ]) {
                       sh '''
                           set -eux
                           rm -rf .jenkins-secrets
                           mkdir -p .jenkins-secrets
                           cp "$SSH_PUBLIC_KEY_FILE" .jenkins-secrets/id_rsa.pub

                           terraform -chdir=tf apply -auto-approve \
                             -var="cloud_id=$YC_CLOUD_ID" \
                             -var="folder_id=$YC_FOLDER_ID" \
                             -var="token=$YC_TOKEN" \
                             -var="zone=$YC_ZONE" \
                             -var="ssh_public_key_path=$(pwd)/.jenkins-secrets/id_rsa.pub" \
                             -var="vm_user=$VM_USER"

                           terraform -chdir=tf output
                       '''
                    }
                }
            }
        }

        stage('Deploy with Ansible') {
            steps {
                dir('infra') {
                    withCredentials([
                        string(credentialsId: 'yc-cloud-id', variable: 'YC_CLOUD_ID'),
                        string(credentialsId: 'yc-folder-id', variable: 'YC_FOLDER_ID'),
                        string(credentialsId: 'yc-token', variable: 'YC_TOKEN'),
                        string(credentialsId: 'bot-name', variable: 'BOT_NAME'),
                        string(credentialsId: 'bot-token', variable: 'BOT_TOKEN'),
                        string(credentialsId: 'weather-api-key', variable: 'WEATHER_API_KEY'),
                        string(credentialsId: 'db-password', variable: 'DB_PASSWORD'),
                        file(credentialsId: 'ssh-public-key', variable: 'SSH_PUBLIC_KEY_FILE'),
                        file(credentialsId: 'ssh-private-key', variable: 'SSH_PRIVATE_KEY_FILE')
                    ]) {
                        sh '''
                            set -eux
                            rm -rf .jenkins-secrets
                            mkdir -p .jenkins-secrets
                            cp "$SSH_PUBLIC_KEY_FILE" .jenkins-secrets/id_rsa.pub
                            cp "$SSH_PRIVATE_KEY_FILE" .jenkins-secrets/id_rsa
                            chmod 600 .jenkins-secrets/id_rsa

                            export SSH_PUBLIC_KEY_PATH="$(pwd)/.jenkins-secrets/id_rsa.pub"
                            export SSH_PRIVATE_KEY_PATH="$(pwd)/.jenkins-secrets/id_rsa"

                            export YC_CLOUD_ID="$YC_CLOUD_ID"
                            export YC_FOLDER_ID="$YC_FOLDER_ID"
                            export YC_TOKEN="$YC_TOKEN"
                            export YC_ZONE="$YC_ZONE"
                            export VM_USER="$VM_USER"

                            export BOT_NAME="$BOT_NAME"
                            export BOT_TOKEN="$BOT_TOKEN"
                            export WEATHER_API_KEY="$WEATHER_API_KEY"
                            export DB_PASSWORD="$DB_PASSWORD"

                            export APP_JAR_PATH="$WORKSPACE/target/WeatherBot.jar"

                            ansible-playbook deploy-yc-fatjar.yaml
                        '''
                    }
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true, allowEmptyArchive: true
            junit testResults: '**/surefire-reports/*.xml', allowEmptyResults: true
            sh 'rm -rf infra/.jenkins-secrets || true'
        }
    }
}