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

For a ready to use Docker environment with all prerequisites already installed and prepared, you can check out the [Docker environment](#docker-environment) section.

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

The application needs a couple of environment/deployment specific configurations and components in order to run correctly. These can be configured in form of Spring beans and can be defined in the project using a separate annotation-based configuration (in a source file that will be detected by the application context), like the following

```java
@Configuration
public class LocalConfiguration {

    @Bean(destroyMethod = "close")
    public DataSource dataSource(Environment env) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.postgresql.Driver");
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/pagebuilder");
        config.setUsername("pagebuilder");
        config.setPassword("s3cret");
        return new HikariDataSource(config);
    }
    
    @Bean
    public DomainsProvider domains() {
        DefaultDomainsProvider provider = new DefaultDomainsProvider();
        
        // TODO define available domains (hostname + allow subdomains)
        provider.add("opendatahub.bz.it", false);
        
        return provider;
    }
    
    @Bean
    public PageComponentsProvider components() {
        PageComponentsDefaultProvider provider = new PageComponentsDefaultProvider();

        // TODO define available components
        provider.add(
            "Example Component",
            "This is just an example component.",
            "https://example.com/webcomps/dist/example.min.js",
            "example-webcomp",
            "<example-webcomp title="..." background="..."></example-webcomp>"
        );
        
        return provider;
    }
    
    @Bean
    public ScreenshotRenderer screenshotRenderer() {
        return new ChromeWebDriverScreenshotRenderer("/path/to/driver", "/path/to/browser");
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

If you have chosen to build the WAR file instead, deploy the generated `ROOT.war` file in the servlet container (preferably Apache Tomcat).

In order to render the page screenshot/preview images correctly, you have to ensure that the configured `ScreenshotRenderer` has access to all required binaries.

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
