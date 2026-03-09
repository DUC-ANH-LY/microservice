# Tech Context

## Stack
- **Java 17**
- **Spring Boot 3.2.5**
- **gRPC 1.63.0** with Protobuf 3.25.3
- **net.devh:grpc-spring-boot-starter 3.1.0.RELEASE** for gRPC integration
- **Maven** multi-module build
- **HdrHistogram 2.2.2** for latency measurement
- **Lombok** for boilerplate reduction

## Modules
| Module     | Purpose                                      |
|------------|----------------------------------------------|
| `proto`    | Protobuf definitions + generated Java code   |
| `server`   | Spring Boot server (REST + gRPC endpoints)  |
| `benchmark`| Benchmark client measuring both protocols   |

## Ports
- REST: `8080`
- gRPC: `9090`

## Build
```bash
mvn clean install       # Build all modules
cd server && mvn spring-boot:run   # Start server
cd benchmark && mvn spring-boot:run  # Run benchmarks
```
