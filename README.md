# Corp Test — Customer Transaction API

A simple Spring Boot REST API for managing customer accounts and transferring funds between them.

---

## Getting Started

### What you need

- Java 17+
- PostgreSQL
- Gradle (or just use the included `./gradlew`)

### Setup

1. **Create the database:**

```bash
createdb corp_test_db
```

2. **Configure your environment:**

Copy `.env.example` to `.env` and fill in your DB credentials:

```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=corp_test_db
DB_USERNAME=postgres
DB_PASSWORD=your_password
SERVER_PORT=8080
```

3. **Run the app:**

```bash
./gradlew bootRun
```

4. **Open Swagger UI:**

Go to [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) to explore and test the APIs.

---

## API Overview

| Method | Endpoint | What it does |
|--------|----------|--------------|
| POST | `/api/v1/customers` | Create a new customer account |
| POST | `/api/v1/transactions/transfer` | Transfer funds between accounts |
| GET | `/api/v1/customers/{accountNumber}/balance` | Check account balance |

### Create Customer

```json
POST /api/v1/customers
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "phoneNumber": "+254712345678",
  "initialDeposit": 5000.00
}
```

### Transfer Funds

```json
POST /api/v1/transactions/transfer
{
  "senderAccountNumber": "ACC7B3F2A1D9E",
  "senderName": "John Doe",
  "receiverAccountNumber": "ACC4E8C1F6B2A",
  "receiverName": "Jane Smith",
  "amount": 1500.00,
  "currency": "KES",
  "description": "Lunch money"
}
```

> Names must match the actual account holders — this is validated.

### Check Balance

```
GET /api/v1/customers/ACC7B3F2A1D9E/balance
```

---

## Running Tests

```bash
./gradlew test
```

24 unit tests covering services and controllers — happy paths, validation errors, insufficient funds, name mismatches, etc.

---

## Project Structure

```
com.cop.test/
├── config/          → Swagger config
├── controller/      → REST endpoints
├── dto/             → Request & response objects
├── exception/       → Custom exceptions + global handler
├── model/           → JPA entities
├── repository/      → Data access layer
└── service/         → Business logic (interfaces + implementations)
```

---

## Key Highlights

- **Validation** — Email format, phone format, unique email & phone, name matching on transfers
- **Transactions** — Full rollback on failure, pessimistic locking to prevent race conditions
- **Error handling** — Consistent JSON error responses with proper HTTP status codes
- **Swagger** — Interactive API docs out of the box
- **.env** — Secrets stay out of source code

---

## Tech

Spring Boot 3.4 · Java 17 · PostgreSQL · JPA/Hibernate · Gradle · Swagger/OpenAPI · Lombok

