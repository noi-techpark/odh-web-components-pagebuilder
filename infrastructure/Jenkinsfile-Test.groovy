pipeline {
    agent any

    environment {
        DOCKER_PROJECT_NAME = "odh-web-components-pagebuilder"
        DOCKER_IMAGE = '755952719952.dkr.ecr.eu-west-1.amazonaws.com/pagebuilder'
        DOCKER_TAG = "test-$BUILD_NUMBER"

		SERVER_PORT = "1009"

        POSTGRES_URL = "jdbc:postgresql://test-pg-bdp.co90ybcr8iim.eu-west-1.rds.amazonaws.com:5432/webcompbuilder"
        POSTGRES_USERNAME = credentials('pagebuilder-test-postgres-username')
        POSTGRES_PASSWORD = credentials('pagebuilder-test-postgres-password')

        AWS_REGION = "eu-west-1"
        AWS_ACCESS_KEY = credentials('pagebuilder-test-s3-access-key')
        AWS_SECRET_KEY = credentials('pagebuilder-test-s3-secret-key')

        USERS_FILE = '/var/data/pagebuilder/application.users-file'

        BASE_URL = "https://pagebuilder2.opendatahub.testingmachine.eu"
        PAGES_DOMAIN_NAME = "pagebuildersites.opendatahub.testingmachine.eu"
        PAGES_ALLOW_SUBDOMAINS = "true"

        APPLICATION_NAME = "pagebuilder-test"
    }

    stages {
        stage('Configure') {
            steps {
                sh """
                    rm -f .env
                    cp .env.example .env
                    echo 'COMPOSE_PROJECT_NAME=${DOCKER_PROJECT_NAME}' >> .env
                    echo 'DOCKER_IMAGE=${DOCKER_IMAGE}' >> .env
                    echo 'DOCKER_TAG=${DOCKER_TAG}' >> .env

					echo 'SERVER_PORT=${SERVER_PORT}' >> .env

                    echo 'POSTGRES_URL=${POSTGRES_URL}' >> .env
                    echo 'POSTGRES_USERNAME=${POSTGRES_USERNAME}' >> .env
                    echo 'POSTGRES_PASSWORD=${POSTGRES_PASSWORD}' >> .env
                    echo 'AWS_REGION=${AWS_REGION}' >> .env
                    echo 'AWS_ACCESS_KEY=${AWS_ACCESS_KEY}' >> .env
                    echo 'AWS_SECRET_KEY=${AWS_SECRET_KEY}' >> .env
                    echo 'USERS_FILE=${USERS_FILE}' >> .env
                    echo 'BASE_URL=${BASE_URL}' >> .env
                    echo 'PAGES_DOMAIN_NAME=${PAGES_DOMAIN_NAME}' >> .env
                    echo 'PAGES_ALLOW_SUBDOMAINS=${PAGES_ALLOW_SUBDOMAINS}' >> .env
                    echo 'APPLICATION_NAME=${APPLICATION_NAME}' >> .env
                """
            }
        }
        stage('Test') {
            steps {
                sh '''
                    docker-compose --no-ansi build --pull --build-arg JENKINS_USER_ID=$(id -u jenkins) --build-arg JENKINS_GROUP_ID=$(id -g jenkins)
                    docker-compose --no-ansi run --rm --no-deps -u $(id -u jenkins):$(id -g jenkins) app "mvn -B -U clean test"
                '''
            }
        }
        stage('Build') {
            steps {
                sh '''
                    aws ecr get-login --region eu-west-1 --no-include-email | bash
                    docker-compose --no-ansi -f infrastructure/docker-compose.build.yml build --pull
                    docker-compose --no-ansi -f infrastructure/docker-compose.build.yml push
                '''
            }
        }
        stage('Deploy') {
            steps {
               sshagent(['jenkins-ssh-key']) {
                    sh """
                        (cd infrastructure/ansible && ansible-galaxy install -f -r requirements.yml)
                        (cd infrastructure/ansible && ansible-playbook --limit=test deploy.yml --extra-vars "release_name=${BUILD_NUMBER}")
                    """
                }
            }
        }
    }
}
