pipeline {
    agent {
        dockerfile {
            filename 'docker/dockerfile-java'
            additionalBuildArgs '--build-arg JENKINS_USER_ID=`id -u jenkins` --build-arg JENKINS_GROUP_ID=`id -g jenkins`'
        }
    }

    environment {
        TESTSERVER_TOMCAT_ENDPOINT = "http://pagebuilder.tomcat02.testingmachine.eu:8080/manager/text"
        TESTSERVER_TOMCAT_CREDENTIALS = credentials('testserver-tomcat8-credentials')

        POSTGRES_URL = "jdbc:postgresql://test-pg-bdp.co90ybcr8iim.eu-west-1.rds.amazonaws.com:5432/webcompbuilder"
        POSTGRES_USERNAME = credentials('pagebuilder-test-postgres-username')
        POSTGRES_PASSWORD = credentials('pagebuilder-test-postgres-password')

        AWS_REGION = 'eu-west-1'
        AWS_ACCESS_KEY = credentials('pagebuilder-test-s3-access-key')
        AWS_SECRET_KEY = credentials('pagebuilder-test-s3-secret-key')

        USERS_FILE = credentials('pagebuilder-test-users-file')
    }

    stages {
        stage('Configure') {
            steps {
                sh 'sed -i -e "s/<\\/settings>$//g\" ~/.m2/settings.xml'
                sh 'echo "    <servers>" >> ~/.m2/settings.xml'
                sh 'echo "        ${TESTSERVER_TOMCAT_CREDENTIALS}" >> ~/.m2/settings.xml'
                sh 'echo "    </servers>" >> ~/.m2/settings.xml'
                sh 'echo "</settings>" >> ~/.m2/settings.xml'

                sh 'cp src/main/resources/application.properties.example src/main/resources/application.properties'
                sh 'cp ${USERS_FILE} src/main/resources/application.users-file'
                sh 'chmod 644 src/main/resources/application.users-file'
                
                sh 'sed -i -e "s%\\(application.database.url\\s*=\\).*\\$%\\1${POSTGRES_URL}%" src/main/resources/application.properties'
                sh 'sed -i -e "s%\\(application.database.username\\s*=\\).*\\$%\\1${POSTGRES_USERNAME}%" src/main/resources/application.properties'
                sh 'sed -i -e "s%\\(application.database.password\\s*=\\).*\\$%\\1${POSTGRES_PASSWORD}%" src/main/resources/application.properties'
                
                sh 'sed -i -e "s%\\(application.aws.region\\s*=\\).*\\$%\\1${AWS_REGION}%" src/main/resources/application.properties'
                sh 'sed -i -e "s%\\(application.aws.access-key\\s*=\\).*\\$%\\1${AWS_ACCESS_KEY}%" src/main/resources/application.properties'
                sh 'sed -i -e "s%\\(application.aws.secret-key\\s*=\\).*\\$%\\1${AWS_SECRET_KEY}%" src/main/resources/application.properties'
                
                sh 'sed -i -e "s%\\(application.users-file\\s*=\\).*\\$%\\1application.users-file%" src/main/resources/application.properties'
            }
        }
        stage('Test') {
            steps {
                sh 'mvn -B -U clean test verify'
            }
        }
        stage('Deploy') {
            steps{
                sh 'mvn -B -U -Pproduction-war tomcat:redeploy -Dmaven.tomcat.url=${TESTSERVER_TOMCAT_ENDPOINT} -Dmaven.tomcat.server=testServer -Dmaven.tomcat.path=/'
            }
        }
    }
}
