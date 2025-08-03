# ☁️ Cloud Storage

A Spring Boot backend application for secure cloud file storage with folder management, user authentication, and MinIO integration.

## 🚀 Features

- 🔐 User authentication and registration (JWT-based)
- 📁 Create and manage folders
- 📂 Upload/download/delete files
- 🗂️ View folder structure and metadata
- 💾 MinIO integration for S3-compatible storage
- 🧠 Redis-backed session management (optional)
- 🧪 Unit and integration testing
- 🔒 Vault integration for secret management (optional)

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

## 📦 Project Structure

cloud_storage/
├── src/
│ ├── main/
│ │ ├── java/org/ek/cloud_storage/
│ │ │ ├── auth/
│ │ │ ├── bucket/
│ │ │ ├── config/
│ │ │ ├── exceptions/
│ │ │ └── ...
│ │ └── resources/
│ │ ├── application.yml
│ │ └── ...
│ └── test/
│ └── ...
├── Dockerfile
├── docker-compose.yml
├── README.md
└── ...

shell
Copy
Edit

## 🛠️ Setup & Run

### Prerequisites

- Java 17+
- Docker & Docker Compose

### Run with Docker

```bash
docker-compose up --build


Run Locally (Without Docker)
Create application.yml in src/main/resources/ with DB, MinIO, and Vault configs.

Start PostgreSQL, MinIO, Redis, and Vault manually or with Docker.

Run the app:

bash
Copy
Edit
./mvnw spring-boot:run


## 🧪 Testing
Run all tests:

bash
Copy
Edit
./mvnw test
Uses H2 in-memory DB for tests.

Includes both unit and integration tests.

## 📚 API Documentation
Once running, access Swagger UI at:

bash
Copy
Edit
http://localhost:8080/swagger-ui/index.html

## ⚙️ Environment Variables
Variable	Description
SPRING_DATASOURCE_URL	JDBC URL for PostgreSQL
SPRING_DATASOURCE_USERNAME	DB username
SPRING_DATASOURCE_PASSWORD	DB password
MINIO_URL	MinIO service endpoint
MINIO_ACCESS_KEY	MinIO access key
MINIO_SECRET_KEY	MinIO secret key
VAULT_TOKEN	Vault root token (if used)
REDIS_HOST	Redis hostname (if used)

## 🤝 Contributing
Pull requests are welcome. For major changes, open an issue first to discuss what you'd like to change.
