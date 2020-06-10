pipeline {
    agent any

    environment {
        AWS_ACCESS_KEY_ID = credentials('AWS_ACCESS_KEY_ID')
        AWS_SECRET_ACCESS_KEY = credentials('AWS_SECRET_ACCESS_KEY')
		DOCKER_PROJECT_NAME = "pagebuilder"
		DOCKER_SERVER_IP = "63.33.73.203" // EC2 name = test-docker-01
        DOCKER_SERVER_DIRECTORY = "/var/docker/pagebuilder"
		DOCKER_IMAGE = '755952719952.dkr.ecr.eu-west-1.amazonaws.com/pagebuilder'
		DOCKER_TAG = "test-$BUILD_NUMBER"

        POSTGRES_URL = "jdbc:postgresql://test-pg-bdp.co90ybcr8iim.eu-west-1.rds.amazonaws.com:5432/webcompbuilder"
        POSTGRES_USERNAME = credentials('pagebuilder-test-postgres-username')
        POSTGRES_PASSWORD = credentials('pagebuilder-test-postgres-password')

        AWS_REGION = "eu-west-1"
        AWS_ACCESS_KEY = credentials('pagebuilder-test-s3-access-key')
        AWS_SECRET_KEY = credentials('pagebuilder-test-s3-secret-key')

        USERS_FILE = '/var/data/pagebuilder/application.users-file'

        BASE_URL = "https://pagebuilder2.opendatahub.testingmachine.eu"
        PAGES_DOMAIN_NAME = "www.pagebuildersites.opendatahub.testingmachine.eu"
        PAGES_ZONE_NAME = "pagebuildersites.opendatahub.testingmachine.eu"
        PAGES_ALLOW_SUBDOMAINS = "true"

        APPLICATION_NAME = "pagebuilder-test"
    }

    stages {
        stage('Configure') {
            steps {
                sh """
                    cp src/main/resources/application.properties.example src/main/resources/application.properties

                    rm -f .env
					cp .env.example .env

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
        // stage('Test') {
        //     steps {
        //         sh '''
		// 			docker-compose --no-ansi build --pull --build-arg JENKINS_USER_ID=$(id -u jenkins) --build-arg JENKINS_GROUP_ID=$(id -g jenkins)
		// 			docker-compose --no-ansi run --rm --no-deps -u $(id -u jenkins):$(id -g jenkins) app "mvn -B -U clean test"
		// 		'''
        //     }
        // }
        stage('Build') {
            steps {
                sh '''
					aws ecr get-login --region eu-west-1 --no-include-email | bash
					docker-compose --no-ansi -f docker-compose.build.yml build --pull
					docker-compose --no-ansi -f docker-compose.build.yml push
				'''
            }
        }
        stage('Deploy') {
            steps {
                sshagent(['jenkins-ssh-key']) {
                    sh '''
					    ssh -o StrictHostKeyChecking=no ${DOCKER_SERVER_IP} bash -euc "'
							mkdir -p ${DOCKER_SERVER_DIRECTORY}
							ls -1t ${DOCKER_SERVER_DIRECTORY}/releases/ | tail -n +10 | grep -v \$(readlink -f ${DOCKER_SERVER_DIRECTORY}/current | xargs basename --) -- | xargs -r printf \"${DOCKER_SERVER_DIRECTORY}/releases/%s\\n\" | xargs -r rm -rf --
							mkdir -p ${DOCKER_SERVER_DIRECTORY}/releases/${BUILD_NUMBER}
						'"

						scp -o StrictHostKeyChecking=no docker-compose.run.yml ${DOCKER_SERVER_IP}:${DOCKER_SERVER_DIRECTORY}/releases/${BUILD_NUMBER}/docker-compose.yml
						scp -o StrictHostKeyChecking=no .env ${DOCKER_SERVER_IP}:${DOCKER_SERVER_DIRECTORY}/releases/${BUILD_NUMBER}/.env

						ssh -o StrictHostKeyChecking=no ${DOCKER_SERVER_IP} bash -euc "'
							AWS_ACCESS_KEY_ID="${AWS_ACCESS_KEY_ID}" AWS_SECRET_ACCESS_KEY="${AWS_SECRET_ACCESS_KEY}" aws ecr get-login --region eu-west-1 --no-include-email | bash
							export DOCKER_IMAGE="${DOCKER_IMAGE}"
							export DOCKER_TAG="${DOCKER_TAG}"
							cd ${DOCKER_SERVER_DIRECTORY}/releases/${BUILD_NUMBER}
                            docker-compose --no-ansi pull
							[ -d \"${DOCKER_SERVER_DIRECTORY}/current\" ] && (cd ${DOCKER_SERVER_DIRECTORY}/current && docker-compose --no-ansi down) || true
							ln -sfn ${DOCKER_SERVER_DIRECTORY}/releases/${BUILD_NUMBER} ${DOCKER_SERVER_DIRECTORY}/current
							cd ${DOCKER_SERVER_DIRECTORY}/current
                            docker-compose --no-ansi up --detach
						'"
					'''
                }
            }
        }
    }
}
