# Cab Booking Application

A production-ready cab booking application built with microservices architecture using Spring Boot, Spring Cloud, and React.

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        React Frontend                            │
│                      (Port 3000)                                 │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                       API Gateway                                │
│                      (Port 8080)                                 │
│   JWT Auth │ Rate Limiting │ Circuit Breaker │ Load Balancing   │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                     Eureka Server                                │
│                      (Port 8761)                                 │
│                   Service Discovery                              │
└─────────────────────────────────────────────────────────────────┘
                                │
        ┌───────────────────────┼───────────────────────┐
        ▼                       ▼                       ▼
┌───────────────┐     ┌───────────────┐     ┌───────────────┐
│ User Service  │     │  Cab Service  │     │ Ride Service  │
│  (Port 8081)  │     │  (Port 8082)  │     │  (Port 8083)  │
└───────────────┘     └───────────────┘     └───────────────┘
        │                       │                       │
        ▼                       ▼                       ▼
┌───────────────┐     ┌───────────────┐     ┌───────────────┐
│Billing Service│     │Notif. Service │     │Routing Service│
│  (Port 8084)  │     │  (Port 8085)  │     │  (Port 8086)  │
└───────────────┘     └───────────────┘     └───────────────┘
```

## Technology Stack

### Backend
- **Java 17** with **Spring Boot 3.2**
- **Spring Cloud 2023.0** (Gateway, Eureka, OpenFeign)
- **Spring Security** with JWT authentication
- **Spring Data JPA** with MySQL
- **Apache Kafka** for event-driven communication
- **Resilience4j** for circuit breaker patterns

### Frontend
- **React 18** with Vite
- **React Router** for navigation
- **Axios** for API calls
- **Leaflet.js** for maps
- Modern CSS with glassmorphism design

### Infrastructure
- **Docker** & **Docker Compose**
- **MySQL 8.0** for persistence
- **Redis** for rate limiting
- **Kafka** for messaging

## Services

| Service | Port | Description |
|---------|------|-------------|
| Eureka Server | 8761 | Service discovery |
| API Gateway | 8080 | Routing, auth, rate limiting |
| User Service | 8081 | User management, authentication |
| Cab Service | 8082 | Driver/vehicle management, location tracking |
| Ride Service | 8083 | Ride booking, lifecycle management |
| Billing Service | 8084 | Fare calculation, payments |
| Notification Service | 8085 | Email, SMS, push notifications |
| Routing Service | 8086 | Distance, ETA, route calculation |

## Quick Start

### Prerequisites
- Docker & Docker Compose
- Node.js 18+ (for frontend development)
- Java 17 (for local development)

### Run with Docker
```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

### Run Locally

1. **Start Infrastructure**
```bash
# Start MySQL, Kafka, and Redis
docker-compose up mysql zookeeper kafka redis -d
```

2. **Start Services** (in order)
```bash
cd eureka-server && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
## How to Run (Hybrid Mode)

Due to local Docker network restrictions, this project uses a **Hybrid Run** strategy:

1. **Infrastructure (Docker)**:
   MySql, Kafka, Zookeeper, and Redis run in Docker.
   ```bash
   docker-compose up -d mysql zookeeper kafka redis
   ```
   *(These are already running if you followed the agent's setup)*

2. **Backend Microservices (Local)**:
   We use a script to launch all 8 services in separate terminal windows.
   - Run: `d:\Cab-Service\start-app.bat`
   - This script sets up `JAVA_HOME` and `MAVEN_HOME` automatically.

3. **Frontend (Local)**:
   - Run: `cd frontend && npm run dev`
   - Access: http://localhost:3000

## API Documentation
- **Eureka Dashboard**: http://localhost:8761
- **API Gateway**: http://localhost:8080
- **Swagger UI**: Available on each service port (e.g., http://localhost:8081/swagger-ui.html)
3/swagger-ui.html
- Billing Service: http://localhost:8084/swagger-ui.html
- Notification Service: http://localhost:8085/swagger-ui.html
- Routing Service: http://localhost:8086/swagger-ui.html

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| MYSQL_HOST | localhost | MySQL host |
| MYSQL_PORT | 3306 | MySQL port |
| MYSQL_USER | root | Database user |
| MYSQL_PASSWORD | root | Database password |
| KAFKA_BOOTSTRAP_SERVERS | localhost:9092 | Kafka brokers |
| EUREKA_URI | http://localhost:8761/eureka | Eureka server |
| JWT_SECRET | (generated) | JWT signing key |

## Features

### For Customers
- User registration and authentication
- Book rides with real-time driver matching
- Track ride in real-time on map
- View ride history and invoices
- Rate drivers after ride completion

### For Drivers
- Driver registration and vehicle management
- Toggle availability status
- Accept/reject ride requests
- Real-time location updates
- Earnings tracking

### For Admins
- Dashboard with system metrics
- User and driver management
- Ride monitoring
- Billing reports

## License

MIT License
