☁️ Cloud Storage
A Spring Boot backend application for secure cloud file storage with folder management, user authentication, and MinIO integration.

🚀 Features

🔐 User authentication and registration

📁 Create and manage folders

📂 Upload/download/delete files

🗂️ View folder structure and metadata

💾 MinIO integration for S3-compatible storage

🧠 Redis-backed session management

🧪 Unit and integration testing


🧰 Tech Stack
Java 17+

Spring Boot 3

Spring Security

Spring Data JPA

MinIO (S3-compatible)

PostgreSQL (or H2 for testing)

Redis (for session store)

Docker & Docker Compose


🛠️ Setup & Run
Prerequisites
Java 17+

Docker & Docker Compose

Run with Docker
docker-compose up --build

Run Locally (Without Docker)
Create application.yml in src/main/resources/ with DB, MinIO, and Vault configs

Start PostgreSQL, MinIO, Redis, and Vault manually or with Docker

Run the app:

./mvnw spring-boot:run

🧪 Testing
Run all tests:

./mvnw test

Uses H2 in-memory DB for tests

Includes both unit and integration tests

📚 API Documentation
Once running, access Swagger UI at:

http://localhost:8080/swagger-ui/index.html

