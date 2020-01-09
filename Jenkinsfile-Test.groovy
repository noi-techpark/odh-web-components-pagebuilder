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

        AWS_REGION = "eu-west-1"
        AWS_ACCESS_KEY = credentials('pagebuilder-test-s3-access-key')
        AWS_SECRET_KEY = credentials('pagebuilder-test-s3-secret-key')

        USERS_FILE = '/var/data/pagebuilder/application.users-file'

        BASE_URL = "https://pagebuilder.opendatahub.testingmachine.eu"
        PAGES_DOMAIN_NAME = "pagebuildersites.opendatahub.testingmachine.eu"
        PAGES_ALLOW_SUBDOMAINS = "true"
    }

    stages {
        stage('Configure') {
            steps {
                sh '''
                    sed -i -e "s/<\\/settings>$//g\" ~/.m2/settings.xml
                    echo "    <servers>" >> ~/.m2/settings.xml
                    echo "        ${TESTSERVER_TOMCAT_CREDENTIALS}" >> ~/.m2/settings.xml
                    echo "    </servers>" >> ~/.m2/settings.xml
                    echo "</settings>" >> ~/.m2/settings.xml

                    cp src/main/resources/application.properties.example src/main/resources/application.properties
                
                    sed -i -e "s%\\(application.database.url\\s*=\\).*\\$%\\1${POSTGRES_URL}%" src/main/resources/application.properties
                    sed -i -e "s%\\(application.database.username\\s*=\\).*\\$%\\1${POSTGRES_USERNAME}%" src/main/resources/application.properties
                    sed -i -e "s%\\(application.database.password\\s*=\\).*\\$%\\1${POSTGRES_PASSWORD}%" src/main/resources/application.properties
                
                    sed -i -e "s%\\(application.aws.region\\s*=\\).*\\$%\\1${AWS_REGION}%" src/main/resources/application.properties
                    sed -i -e "s%\\(application.aws.access-key\\s*=\\).*\\$%\\1${AWS_ACCESS_KEY}%" src/main/resources/application.properties
                    sed -i -e "s%\\(application.aws.access-secret\\s*=\\).*\\$%\\1${AWS_SECRET_KEY}%" src/main/resources/application.properties
                
                    sed -i -e "s%\\(application.users-file\\s*=\\).*\\$%\\1${USERS_FILE}%" src/main/resources/application.properties
                
                    sed -i -e "s%\\(application.base-url\\s*=\\).*\\$%\\1${BASE_URL}%" src/main/resources/application.properties
                    sed -i -e "s%\\(application.pages.domain-name\\s*=\\).*\\$%\\1${PAGES_DOMAIN_NAME}%" src/main/resources/application.properties
                    sed -i -e "s%\\(application.pages.allow-subdomains\\s*=\\).*\\$%\\1${PAGES_ALLOW_SUBDOMAINS}%" src/main/resources/application.properties
                '''
            }
        }
        stage('Test') {
            steps {
                sh 'mvn -B -U clean test verify'
            }
        }
        stage('Build') {
            steps {
                sh 'mvn -B -U -Pproduction-war -Dmaven.test.skip=true clean package'
            }
        }
        stage('Deploy') {
            steps{
                sh 'mvn -B -U -Dmaven.test.skip=true tomcat:redeploy -Dmaven.tomcat.url=${TESTSERVER_TOMCAT_ENDPOINT} -Dmaven.tomcat.server=testServer -Dmaven.tomcat.path=/'
            }
        }
    }
}
