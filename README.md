# ☁️ Cloud Storage

A Spring Boot backend application for secure cloud file storage with folder management, user authentication, and MinIO integration.

## 🚀 Features

- 🔐 User authentication and registration
- 📁 Create and manage folders  
- 📂 Upload/download/delete files  
- 🗂️ View folder structure and metadata  
- 💾 MinIO integration for S3-compatible storage  
- 🧠 Redis-backed session management
- 🧪 Unit and integration testing  

## 🧰 Tech Stack

- Java 17+  
- Spring Boot 3  
- Spring Security  
- Spring Data JPA  
- MinIO (S3-compatible)  
- PostgreSQL (or H2 for testing)  
- Redis (for session store)  
- HashiCorp Vault (for secrets management)  
- Docker & Docker Compose  

## 🛠️ Setup & Run

### Prerequisites

- Java 17+  
- Docker & Docker Compose  

### Run with Docker

```bash
docker-compose up --build
```

### Run Locally (Without Docker)

1. Create `application.yml` in `src/main/resources/` with DB, MinIO, and Vault configs  
2. Start PostgreSQL, MinIO, Redis, and Vault manually or with Docker  
3. Run the app:

```bash
./mvnw spring-boot:run
```

## 🧪 Testing

Run all tests:

```bash
./mvnw test
```

- Uses H2 in-memory DB for tests  
- Includes both unit and integration tests  

## 📚 API Documentation

Once running, access Swagger UI at:  
[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)



