pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '15'))
    }

    environment {
        KUBE_NAMESPACE = 'weather-bot'
        KIND_CLUSTER = 'lab7'
        APP_NAME = 'weather-bot'
        IMAGE_NAME = "weather-bot:${BUILD_NUMBER}"
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

        stage('Build Docker Image') {
            steps {
                sh '''
                    set -eux
                    docker build -t "$IMAGE_NAME" .
                    docker image inspect "$IMAGE_NAME" >/dev/null
                '''
            }
        }

        stage('Load Image To Kubernetes') {
            steps {
                sh '''
                    set -eux
                    kind get clusters | grep -qx "$KIND_CLUSTER"
                    kind load docker-image "$IMAGE_NAME" --name "$KIND_CLUSTER"
                '''
            }
        }

        stage('Deploy To Kubernetes') {
            steps {
                withCredentials([
                    string(credentialsId: 'bot-name', variable: 'BOT_NAME'),
                    string(credentialsId: 'bot-token', variable: 'BOT_TOKEN'),
                    string(credentialsId: 'weather-api-key', variable: 'WEATHER_API_KEY'),
                    string(credentialsId: 'db-password', variable: 'DB_PASSWORD')
                ]) {
                    sh '''
                        set -eux

                        kubectl apply -f k8s/00-namespace.yaml

                        kubectl -n "$KUBE_NAMESPACE" create secret generic weather-bot-secret \
                          --from-literal=BOT_NAME="$BOT_NAME" \
                          --from-literal=BOT_TOKEN="$BOT_TOKEN" \
                          --from-literal=WEATHER_API_KEY="$WEATHER_API_KEY" \
                          --from-literal=DB_PASSWORD="$DB_PASSWORD" \
                          --dry-run=client -o yaml | kubectl apply -f -

                        kubectl apply -f k8s/

                        kubectl -n "$KUBE_NAMESPACE" set image deployment/$APP_NAME $APP_NAME="$IMAGE_NAME"

                        kubectl -n "$KUBE_NAMESPACE" rollout status deployment/postgres --timeout=180s
                        kubectl -n "$KUBE_NAMESPACE" rollout status deployment/$APP_NAME --timeout=180s
                    '''
                }
            }
        }

        stage('Show Kubernetes State') {
            steps {
                sh '''
                    set -eux
                    kubectl -n "$KUBE_NAMESPACE" get pods -o wide
                    kubectl -n "$KUBE_NAMESPACE" get deploy,svc,pvc
                '''
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true, allowEmptyArchive: true
            junit testResults: '**/surefire-reports/*.xml', allowEmptyResults: true
        }
    }
}