Bug Tracker — Full Stack Microservices Application

A full-stack Bug Tracking System migrated from a monolithic architecture to Spring Boot Microservices, with an Angular frontend and centralized API Gateway authentication.

Architecture
------------

Angular Frontend → API Gateway → Eureka Service Discovery → Microservices

Microservices
-------------

| Service | Responsibility | Port |
|---|---|---|
| Eureka Server | Service discovery | 8761 |
| API Gateway | Routing and JWT validation | 8080 |
| User Service | Authentication and user management | 8081 |
| Project Service | Project management | 8082 |
| Bug Service | Bug lifecycle and assignments | 8083 |
| Notification Service | User notifications | 8084 |
| Dashboard Service | Role-based dashboard aggregation | 8085 |

Tech Stack
-----------

Frontend
- Angular
- TypeScript
- Angular Material
- HTML
- CSS

Backend
- Java
- Spring Boot
- Spring Cloud
- Spring Security
- Spring Data JPA
- OpenFeign
- Resilience4j
- JWT

Database
- MySQL / H2 

Tools
- Git
- GitHub
- Postman
- Maven
- VS Code / Spring Tool Suite

Key Features
------------

- JWT-based authentication
- Centralized API Gateway
- Eureka service discovery
- Role-based access control
- Project and bug management
- Multiple developer assignments
- Bug comments and attachments
- Notification management
- Role-based dashboard statistics
- Inter-service communication using OpenFeign
- Circuit breakers and fallback handling
- Correlation ID propagation across services
- Database-per-service architecture

Dashboard
----------

The Dashboard Service aggregates statistics from User, Project, and Bug services while preserving role-specific dashboard behaviour.

Project Structure
-----------------


frontend/
backend/
  eureka-server/
  api-gateway/
  user-service/
  project-service/
  bug-service/
  notification-service/
  dashboard-service/


Running the Application
-----------------------

1. Configure database connections and environment variables.
2. Start Eureka Server.
3. Start User, Project, Bug, and Notification services.
4. Start Dashboard Service.
5. Start API Gateway.
6. Start Angular frontend.

Refer to the individual service configuration files for actual requirements.

Security
--------

Do not commit JWT secrets, passwords, API keys, or production credentials.

Author
-------

Nandhini P

GitHub: https://github.com/BoomikaNandhini/bug-tracker-microservices/