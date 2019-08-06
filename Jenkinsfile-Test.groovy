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

        LOCAL_CONFIGURATION = """
package it.bz.opendatahub.webcomponentspagebuilder;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import it.bz.opendatahub.webcomponentspagebuilder.data.DefaultDomainsProvider;
import it.bz.opendatahub.webcomponentspagebuilder.data.DomainsProvider;
import it.bz.opendatahub.webcomponentspagebuilder.data.PageComponentsDefaultProvider;
import it.bz.opendatahub.webcomponentspagebuilder.data.PageComponentsProvider;

/**
 * Local configuration of available components
 */
@Configuration
public class LocalConfiguration {

	@Bean(destroyMethod = "close")
	public DataSource dataSource(Environment env) {
		HikariConfig config = new HikariConfig();
		config.setDriverClassName("org.h2.Driver");
		config.setJdbcUrl("jdbc:h2:/path/to/pagebuilder-database-file");
		return new HikariDataSource(config);
	}

	@Bean
	public DomainsProvider domains() {
		DefaultDomainsProvider provider = new DefaultDomainsProvider();
		provider.add("suedtirol.info", true);
		provider.add("opendatahub.bz.it", false);
		return provider;
	}

	@Bean
	public PageComponentsProvider components() {
		PageComponentsDefaultProvider provider = new PageComponentsDefaultProvider();

		provider.add("Header", "Introduction element with image/title.",
				"https://danielrampanelli.com/webcomps/odh-samples-header.js", "odh-samples-header",
				"<odh-samples-header></odh-samples-header>");

		provider.add("Texts", "Some text paragraphs.", "https://danielrampanelli.com/webcomps/odh-samples-text.js",
				"odh-samples-text", "<odh-samples-text></odh-samples-text>");

		provider.add("Weather", "Show the current weather for the region.",
				"https://danielrampanelli.com/webcomps/odh-samples-weather.js", "odh-samples-weather",
				"<odh-samples-weather></odh-samples-weather>");

		provider.add("Beacons Map/Table", "Show all the beacons in South Tyrol as a map or table.",
				"https://danielrampanelli.com/webcomps/beacons-map-table.min.js", "beacons-map-table",
				"<beacons-map-table view=\"all\" search></beacons-map-table>");

		return provider;
	}

}
        """
    }

    stages {
        stage('Configure') {
            steps {
                sh 'sed -i -e "s/<\\/settings>$//g\" ~/.m2/settings.xml'
                sh 'echo "    <servers>" >> ~/.m2/settings.xml'
                sh 'echo "        ${TESTSERVER_TOMCAT_CREDENTIALS}" >> ~/.m2/settings.xml'
                sh 'echo "    </servers>" >> ~/.m2/settings.xml'
                sh 'echo "</settings>" >> ~/.m2/settings.xml'

                sh 'echo "${LOCAL_CONFIGURATION}" > src/main/java/it/bz/opendatahub/webcomponentspagebuilder/LocalConfiguration.java'
            }
        }
        stage('Test') {
            steps {
                sh 'mvn -B -U clean test verify -Pintegration-tests'
            }
        }
        stage('Build') {
            steps {
                sh 'mvn -B -U clean install package -Pproduction-war'
            }
        }
        stage('Deploy') {
            steps{
                sh 'mvn -B -U -Pproduction-war tomcat:redeploy -Dmaven.tomcat.url=${TESTSERVER_TOMCAT_ENDPOINT} -Dmaven.tomcat.server=testServer'
            }
        }
    }
}
