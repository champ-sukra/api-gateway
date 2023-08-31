# API Gateway

The API Gateway is a dynamic routing component that directs incoming requests to various services based on predefined routes. It offers features like caching, reverse proxying, and integration with Spring Boot Actuator. This project demonstrates the creation of a robust API Gateway using Spring Boot.

## Features

- **Dynamic Routing**: Routes incoming requests to different services based on predefined configurations.
- **Reverse Proxying**: Acts as a reverse proxy to forward requests to backend services.
- **Actuator Integration**: Integrates Spring Boot Actuator for monitoring and management.

## Setup

1. Clone this repository to your local machine.
2. Open the project in your preferred Java IDE (e.g., IntelliJ IDEA, Eclipse).

## Configuration

### Dynamic Routes

Configure routes and their properties in the `api_scopes.json` file. Each route specifies a path, method, and service.

Example configuration:

```json
[
  {
    "service": "auth",
    "path": "/auth/management/v1/roles",
    "method": "GET"
  },
  {
    "path": "/composite/v1/profiles",
    "method": "GET",
    "routes": [
      [
        {
          "service": "auth",
          "path": "/auth/management/v1/roles",
          "method": "GET",
          "key": "x1"
        }
      ]
    ]
  }
]
```

### Application Configuration

You can set the default service and other service details in the `application.yml` file. For example:

```yaml
server:
  services:
    auth:
      scheme: http
      host: localhost
      port: 8082
    customer:
      scheme: http
      host: localhost
      port: 8081
```

### Caching Configuration

Caching settings can be configured in the `CacheConfig` class. By default, responses are cached for 10 minutes with a maximum size of 1000.

```java
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("responses");
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }

    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(1000);
    }
}
```

## Usage

1. Build and run the API Gateway application.
2. Make requests to the configured routes, and the API Gateway will handle dynamic routing and reverse proxying based on the route definitions.

## Response Structure

The API Gateway returns responses in the following structure:

```json
{
   "code": "success",
   "data": { ... }
}
```

## Monitoring and Management

Spring Boot Actuator endpoints are available to monitor and manage the API Gateway. Access the management console at: http://{{HOST}}:8080/actuator/

## Contribution

Contributions are welcome! If you find any issues or want to add new features, feel free to open a pull request or issue.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

This project is inspired by the need for a flexible and efficient API Gateway solution using Spring Boot.
