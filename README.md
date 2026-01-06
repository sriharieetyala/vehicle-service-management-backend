# Vehicle Service Management System (VSMS)

A microservices-based vehicle service center management platform designed to streamline service booking, technician assignment, inventory tracking, billing, and customer notifications.

---

## Architecture

The system follows a **microservices architecture** using the **API Gateway pattern**. All backend services are registered with **Eureka Service Registry** for dynamic service discovery and fetch centralized configuration from **Spring Cloud Config Server**. Asynchronous, event-driven communication is implemented using **RabbitMQ** for notifications and system events.

<img width="1132" height="1125" alt="image" src="https://github.com/user-attachments/assets/628b31eb-467b-41c2-9dfd-004c43341631" />


---

## Tech Stack

| Layer             | Technology              |
| ----------------- | ----------------------- |
| Backend           | Spring Boot 3, Java 17+  |
| Frontend          | Angular 19, Bootstrap 5 |
| API Gateway       | Spring Cloud Gateway    |
| Service Discovery | Netflix Eureka          |
| Configuration     | Spring Cloud Config     |
| Database          | PostgreSQL              |
| Messaging         | RabbitMQ                |
| Containerization  | Docker, Docker Compose  |
| CI/CD             | Jenkins                 |
| Code Quality      | SonarCloud, JaCoCo      |

---

## Repositories

* **Frontend (Angular)**
  [https://github.com/sriharieetyala/vehicle-service-management-frontend](https://github.com/sriharieetyala/vehicle-service-management-frontend)

* **Config Server**
  [https://github.com/sriharieetyala/vsms_config-server](https://github.com/sriharieetyala/vsms_config-server)

---

## Services

| Service                 | Port | Description                        |
| ----------------------- | ---- | ---------------------------------- |
| API Gateway             | 8080 | Routing, JWT authentication        |
| Service Registry        | 8761 | Eureka service discovery           |
| Config Server           | 8888 | Centralized configuration          |
| Auth Service            | 8081 | Authentication and user management |
| Vehicle Service         | 8082 | Vehicle and customer management    |
| Service Request Service | 8083 | Service booking, tracking, billing |
| Inventory Service       | 8084 | Spare parts and stock management   |
| Notification Service    | 8086 | Email notifications via RabbitMQ   |

---

## Databases

| Database          | Service                 |
| ----------------- | ----------------------- |
| vsms_auth_db      | Auth Service            |
| vsms_vehicle_db   | Vehicle Service         |
| vsms_service_db   | Service Request Service |
| vsms_inventory_db | Inventory Service       |

---

## Code Quality

<img width="1618" height="819" alt="image" src="https://github.com/user-attachments/assets/e59f224f-0056-4d90-848d-0a11e700ee12" />


---

## Features

* JWT-based authentication with role-based access control
* Customer vehicle registration and service booking
* Technician task assignment and duty tracking
* Inventory management with automatic stock updates
* Invoice generation and payment tracking
* Event-driven email notifications
* Admin dashboard with operational analytics

---

## Run Locally

### Prerequisites

* Java 17+
* Docker & Docker Compose
* PostgreSQL
* RabbitMQ

<img width="1117" height="1125" alt="image" src="https://github.com/user-attachments/assets/4845926b-f876-46b2-a481-e2c3e4ea9e62" />

### Quick Start

```bash
# Start all services using Docker
docker-compose up -d

# Run services individually (example)
cd auth-service
mvn spring-boot:run
```

### Access Points

* **Frontend**: [http://localhost:4200](http://localhost:4200)
* **API Gateway**: [http://localhost:8080](http://localhost:8080)
* **Eureka Dashboard**: [http://localhost:8761](http://localhost:8761)


