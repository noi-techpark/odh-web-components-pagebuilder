# Open Data Hub - Web Components Pagebuilder

`TODO`: Description of the project.

## Table of contents

- [Getting started](#getting-started)
- [Testing](#testing)
- [Deployment](#deployment)
- [Docker environment](#docker-environment)
- [Information](#information)

## Getting started

These instructions will get you a copy of the project up and running
on your local machine for development and testing purposes.

### Prerequisites

To build the project, the following prerequisites must be met:

- Java JDK 1.8 or higher (e.g. [OpenJDK](https://openjdk.java.net/))
- [Maven](https://maven.apache.org/) 3.x
- PostgreSQL
- Suitable Browser/WebDriver
- AWS Account

For a ready to use Docker environment with all prerequisites already installed and prepared, you can check out the [Docker environment](#docker-environment) section.

### Setup Browser/WebDriver

The application uses automated versions of browsers to render the pages and show a preview screenshot to users. Currently, it is possible to use Chrome/Chromium or Firefox for this task.

If not configured differently, the application expects to find Chrome/Chromium and ChromeDriver installed on the default system paths.

Here is a list of resources on how to install Chrome/Chromium and ChromeDriver on different platforms:

* Ubuntu: [https://gist.github.com/ziadoz/3e8ab7e944d02fe872c3454d17af31a5](https://gist.github.com/ziadoz/3e8ab7e944d02fe872c3454d17af31a5)
* Mac: [https://www.kenst.com/2015/03/installing-chromedriver-on-mac-osx/](https://www.kenst.com/2015/03/installing-chromedriver-on-mac-osx/)

Alternatively, you can also install and use Firefox and GeckoDriver:

* Ubuntu: [https://medium.com/@sonaldwivedi/downloading-and-setting-up-geckodriver-87873e25207c](https://medium.com/@sonaldwivedi/downloading-and-setting-up-geckodriver-87873e25207c)
* Mac: [https://medium.com/@deepankosingha/how-to-install-geckodriver-on-ubuntu-94b2075b5ad3](https://medium.com/@deepankosingha/how-to-install-geckodriver-on-ubuntu-94b2075b5ad3)

If you have installed the executables in different locations or you want to use Firefox/Gecko, you'll need to provide a different `@Bean` definition in the source code (see the [Configuration](#configuration) section).

### Setup AWS

The application will use the resources provided by AWS to deploy the composed pages. It is highly advisable to create a separate user (instead of the primary credentials of your AWS account) with at least the following policies:

* AmazonS3FullAccess

See the [Configuration](#configuration) section on how to specify the user's access key and secret.

### Source code

Get a copy of the repository:

```bash
git clone https://github.com/noi-techpark/odh-web-components-pagebuilder.git
```

Change directory:

```bash
cd odh-web-components-pagebuilder/
```

### Configuration

The application expects a configuration file named `application.properties` at the location `src/main/resources` in order to run correctly. Please make a copy of the `application.properties.example` file situated in the same folder and specify the following properties:

* application.database.url (JDBC url to PostgreSQL database)
* application.database.username (database username)
* application.database.password (database password)
* application.aws.region (AWS region identifier)
* application.aws.access-key (AWS user access key)
* application.aws.access-secret (AWS user access secret)

**Important note**: Please make sure to keep the `# SYSTEM SETTINGS` section of this configuration file synced with the provided example file when fetching updates in the future.

Moreover, it is also possible and not mandatory to override and alter the default set of Spring `@Bean` components by creating a dedicated Java class with the `@Configuration` annotation.

In the following example we have installed Chrome/ChromeDriver in custom locations and we run the application under a different domain/host name.

```java
@Configuration
public class CustomConfiguration {
    
    @Bean
    @Primary
    public ScreenshotRenderer customScreenshotRenderer() {
        return new ChromeWebDriverScreenshotRenderer("/opt/bin/chromedriver", "/opt/bin/chrome");
    }
    
    @Bean
    @Primary
    public ApplicationDeployment customDeployment() {
        return new ApplicationDeployment("http://alternative-pagebuilder.testingmachine.eu");
    }

}
```

### Running

Build and run the project [locally](http://0.0.0.0:8080) (http://0.0.0.0:8080):

```bash
mvn spring-boot:run
```

### Packaging

Build and package the application as a runnable JAR file (located in the target folder) for production use:

```bash
mvn clean package -Pproduction-jar
```

If you wish to deploy the application as a WAR file on top of an existing servlet container, use the following command to generate a suitable file:

```bash
mvn clean package -Pproduction-war
```

## Testing

The unit tests can be executed with the following command:

```bash
mvn clean test
```

## Deployment

The application can be deployed by running the packaged runnable JAR file on the specified port (or 8080 in case of missing parameter), like so:

```bash
java -jar -Dserver.port=80 path/to/odh-web-components-pagebuilder-[VERSION].jar
```

If you have chosen to build the WAR file instead, deploy the generated `odh-web-components-pagebuilder-[VERSION].war` file in the servlet container (preferably Apache Tomcat) using the `/` (root) context path.

## Docker environment

For the project a Docker environment is already prepared and ready to use with all necessary prerequisites.

These Docker containers are the same as used by the continuous integration servers.

### Installation

Install [Docker](https://docs.docker.com/install/) (with Docker Compose) locally on your machine.

### Start and stop the containers

Before start working you have to start the Docker containers:

```
docker-compose up --build --detach
```

After finished working you can stop the Docker containers:

```
docker-compose stop
```

### Running commands inside the container

When the containers are running, you can execute any command inside the environment. Just replace the dots `...` in the following example with the command you wish to execute:

```bash
docker-compose exec java /bin/bash -c "..."
```

Some examples are:

```bash
docker-compose exec java /bin/bash -c "mvn clean install"

# or

docker-compose exec java /bin/bash -c "mvn clean test"
```

## Information

### Support

For support, please contact [info@opendatahub.bz.it](mailto:info@opendatahub.bz.it).

### Contributing

If you'd like to contribute, please follow the following instructions:

- Fork the repository.

- Checkout a topic branch from the `development` branch.

- Make sure the tests are passing.

- Create a pull request against the `development` branch.

### Documentation

More documentation can be found at [https://opendatahub.readthedocs.io/en/latest/index.html](https://opendatahub.readthedocs.io/en/latest/index.html).

### License

The code in this project is licensed under the GNU AFFERO GENERAL PUBLIC LICENSE Version 3 license.
See the LICENSE.md file for more information.
