# Co-operative B Test Task - Customer Transaction API

A RESTful API built with **Spring Boot** for managing customer accounts and performing fund transfers. This application demonstrates clean architecture, proper validation, transactional integrity, and comprehensive error handling.

---

## 🚀 Tech Stack

| Technology | Purpose |
|-----------|---------|
| Java 17 | Programming Language |
| Spring Boot 3.4.1 | Application Framework |
| Spring Data JPA | Database ORM |
| PostgreSQL | Relational Database |
| Gradle | Build Tool |
| Swagger / OpenAPI 3 | API Documentation |
| Lombok | Boilerplate Reduction |
| Spring Dotenv | Environment Variable Management |

---

## 📁 Project Structure

```
src/main/java/com/cop/test/
├── TestApplication.java            # Main entry point
├── config/
│   └── SwaggerConfig.java          # OpenAPI/Swagger configuration
├── controller/
│   ├── CustomerController.java     # Customer endpoints
│   └── TransactionController.java  # Transfer endpoints
├── dto/
│   ├── request/
│   │   ├── CustomerRequest.java    # Create customer payload
│   │   └── TransferRequest.java    # Fund transfer payload
│   └── response/
│       ├── ApiResponse.java        # Generic API response wrapper
│       ├── BalanceResponse.java    # Balance enquiry response
│       ├── CustomerResponse.java   # Customer creation response
│       └── TransferResponse.java   # Transfer result response
├── exception/
│   ├── DuplicateResourceException.java
│   ├── GlobalExceptionHandler.java # Centralized error handling
│   ├── InsufficientFundsException.java
│   ├── InvalidTransactionException.java
│   └── ResourceNotFoundException.java
├── model/
│   ├── Customer.java               # Customer entity
│   ├── Transaction.java            # Transaction entity
│   ├── TransactionStatus.java      # PENDING, SUCCESSFUL, FAILED
│   └── TransactionType.java        # DEPOSIT, WITHDRAWAL, TRANSFER
├── repository/
│   ├── CustomerRepository.java     # Customer data access
│   └── TransactionRepository.java  # Transaction data access
└── service/
    ├── CustomerService.java        # Customer service interface
    ├── TransactionService.java     # Transaction service interface
    └── impl/
        ├── CustomerServiceImpl.java    # Customer business logic
        └── TransactionServiceImpl.java # Transfer business logic
```

---

## ⚙️ Setup & Installation

### Prerequisites

- **Java 17** or higher
- **PostgreSQL** installed and running
- **Gradle** (or use the included Gradle wrapper)

### 1. Clone the Repository

```bash
git clone https://github.com/titusmbole/coop-test
cd coop-test
```

### 2. Create the PostgreSQL Database

```bash
createdb corp_test_db
```

Or via psql:

```sql
CREATE DATABASE corp_test_db;
```

### 3. Configure Environment Variables

Copy the example environment file and update with your credentials:

```bash
cp .env.example .env
```

Edit `.env`:

```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=corp_test_db
DB_USERNAME=postgres
DB_PASSWORD=your_password
SERVER_PORT=8080
```

### 4. Build the Project

```bash
./gradlew build
```

### 5. Run the Application

```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`.

---

## 📖 Swagger / API Documentation

Once the application is running, access the interactive API documentation:

| Resource | URL |
|----------|-----|
| **Swagger UI** | [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) |
| **OpenAPI JSON** | [http://localhost:8080/api-docs](http://localhost:8080/api-docs) |

Swagger UI allows you to explore and test all endpoints directly from the browser.

---

## 📡 API Endpoints

### 1. Create Customer Account

**POST** `/api/v1/customers`

Creates a new customer with an initial deposit.

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phoneNumber": "+254712345678",
  "initialDeposit": 5000.00
}
```

**Success Response (201 Created):**
```json
{
  "success": true,
  "message": "Customer account created successfully",
  "data": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "accountNumber": "ACC7B3F2A1D9E",
    "accountBalance": 5000.00,
    "email": "john.doe@example.com",
    "phoneNumber": "+254712345678",
    "createdAt": "2026-06-09T17:45:00"
  },
  "timestamp": "2026-06-09T17:45:00"
}
```

**Validations:**
- First name & last name: required, 2–50 characters
- Email: required, valid format, **must be unique**
- Phone number: required, valid format, **must be unique**
- Initial deposit: required, must be ≥ 0

---

### 2. Transfer Funds

**POST** `/api/v1/transactions/transfer`

Transfers money from one customer account to another.

**Request Body:**
```json
{
  "senderAccountNumber": "ACC7B3F2A1D9E",
  "senderName": "John Doe",
  "receiverAccountNumber": "ACC4E8C1F6B2A",
  "receiverName": "Jane Smith",
  "amount": 1500.00,
  "currency": "KES",
  "description": "Payment for services",
  "channel": "API"
}
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Funds transferred successfully",
  "data": {
    "transactionReference": "TXN8A4F2C1B9E3D",
    "senderAccountNumber": "ACC7B3F2A1D9E",
    "senderName": "John Doe",
    "receiverAccountNumber": "ACC4E8C1F6B2A",
    "receiverName": "Jane Smith",
    "amount": 1500.00,
    "currency": "KES",
    "transactionType": "TRANSFER",
    "status": "SUCCESSFUL",
    "description": "Payment for services",
    "channel": "API",
    "senderBalanceAfter": 3500.00,
    "receiverBalanceAfter": 6500.00,
    "transactionDate": "2026-06-09T17:50:00"
  },
  "timestamp": "2026-06-09T17:50:00"
}
```

**Validations:**
- Sender & receiver account numbers: required
- Sender & receiver names: required, **must match actual account holders**
- Amount: required, must be > 0
- Currency: required
- Cannot transfer to the same account
- Sender must have sufficient balance

---

### 3. Get Account Balance

**GET** `/api/v1/customers/{accountNumber}/balance`

Retrieves the current balance for a customer account.

**Example:** `GET /api/v1/customers/ACC7B3F2A1D9E/balance`

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Account balance retrieved successfully",
  "data": {
    "accountNumber": "ACC7B3F2A1D9E",
    "customerName": "John Doe",
    "availableBalance": 3500.00,
    "currency": "KES",
    "enquiryDate": "2026-06-09T18:00:00"
  },
  "timestamp": "2026-06-09T18:00:00"
}
```

---

## ❌ Error Handling

All errors return a consistent JSON structure:

```json
{
  "success": false,
  "message": "Error description here",
  "timestamp": "2026-06-09T18:00:00"
}
```

### Error Codes

| HTTP Status | Exception | Scenario |
|-------------|-----------|----------|
| 400 | `InvalidTransactionException` | Same account transfer, name mismatch |
| 400 | `InsufficientFundsException` | Not enough balance |
| 400 | `MethodArgumentNotValidException` | Validation failures |
| 404 | `ResourceNotFoundException` | Account not found |
| 409 | `DuplicateResourceException` | Email or phone already exists |
| 500 | `Exception` | Unexpected server error |

### Validation Error Response Example:
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "email": "Email must be a valid email address",
    "phoneNumber": "Phone number must be a valid format"
  },
  "timestamp": "2026-06-09T18:00:00"
}
```

---

## 🔒 Transaction Safety

The fund transfer operation uses:

- **`@Transactional(isolation = SERIALIZABLE, rollbackFor = Exception.class)`** — Ensures atomic operations with full rollback on any failure.
- **Pessimistic Locking (`@Lock(PESSIMISTIC_WRITE)`)** — Prevents race conditions during concurrent transfers.
- **Failed Transaction Logging** — Failed attempts are recorded in a separate transaction (`Propagation.REQUIRES_NEW`) for audit purposes, even when the main transaction rolls back.

This means:
- ✅ If debiting sender succeeds but crediting receiver fails → **both roll back**
- ✅ If any error occurs mid-transfer → **balances remain unchanged**
- ✅ Concurrent transfers on the same account are serialized safely

---

## 🧪 Testing the API

### Using cURL

**Create Customer:**
```bash
curl -X POST http://localhost:8080/api/v1/customers \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "phoneNumber": "+254712345678",
    "initialDeposit": 10000.00
  }'
```

**Transfer Funds:**
```bash
curl -X POST http://localhost:8080/api/v1/transactions/transfer \
  -H "Content-Type: application/json" \
  -d '{
    "senderAccountNumber": "ACC_SENDER_HERE",
    "senderName": "John Doe",
    "receiverAccountNumber": "ACC_RECEIVER_HERE",
    "receiverName": "Jane Smith",
    "amount": 2500.00,
    "currency": "KES",
    "description": "Monthly payment"
  }'
```

**Check Balance:**
```bash
curl http://localhost:8080/api/v1/customers/ACC_NUMBER_HERE/balance
```

### Using Swagger UI

1. Navigate to [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
2. Expand the desired endpoint
3. Click "Try it out"
4. Fill in the request body
5. Click "Execute"

---

## 📝 Key Design Decisions

1. **Interface-Implementation Pattern** — Services are split into interfaces and implementations for loose coupling and testability.
2. **DTO Layer** — Request/Response objects are separated from entities to control API exposure.
3. **Global Exception Handler** — Centralized error handling with `@RestControllerAdvice` ensures consistent error responses.
4. **Account Number Generation** — Unique account numbers are auto-generated (format: `ACC` + 10 random alphanumeric characters).
5. **Name Verification on Transfer** — Sender and receiver names must match their account records, adding a security layer.
6. **Environment Variables** — Sensitive configuration is externalized via `.env` file (not committed to git).

---

## 🧪 Unit Tests

The project includes comprehensive unit tests for both service and controller layers using **JUnit 5**, **Mockito**, and **MockMvc**.

### Running Tests

```bash
./gradlew test
```

Test reports are generated at: `build/reports/tests/test/index.html`

### Test Structure

```
src/test/java/com/cop/test/
├── CopApplicationTests.java              # Context load test
├── controller/
│   ├── CustomerControllerTest.java       # Customer API endpoint tests
│   └── TransactionControllerTest.java    # Transfer API endpoint tests
└── service/
    ├── CustomerServiceTest.java          # Customer business logic tests
    └── TransactionServiceTest.java       # Transfer business logic tests
```

### Test Coverage Summary

#### CustomerServiceTest (5 tests)
| Test | Description |
|------|-------------|
| `createCustomer_Success` | Verifies successful customer creation with all fields |
| `createCustomer_DuplicateEmail` | Ensures duplicate email throws `DuplicateResourceException` |
| `createCustomer_DuplicatePhone` | Ensures duplicate phone throws `DuplicateResourceException` |
| `getAccountBalance_Success` | Verifies balance retrieval returns correct data |
| `getAccountBalance_AccountNotFound` | Ensures unknown account throws `ResourceNotFoundException` |

#### TransactionServiceTest (8 tests)
| Test | Description |
|------|-------------|
| `transferFunds_Success` | Verifies successful fund transfer end-to-end |
| `transferFunds_SameAccount` | Rejects transfer to the same account |
| `transferFunds_SenderNotFound` | Rejects when sender account doesn't exist |
| `transferFunds_ReceiverNotFound` | Rejects when receiver account doesn't exist |
| `transferFunds_SenderNameMismatch` | Rejects when sender name doesn't match account holder |
| `transferFunds_ReceiverNameMismatch` | Rejects when receiver name doesn't match account holder |
| `transferFunds_InsufficientFunds` | Rejects when sender has insufficient balance |
| `transferFunds_CorrectBalanceUpdates` | Verifies exact debit/credit amounts |
| `transferFunds_ZeroAmount` | Rejects zero or negative transfer amounts |

#### CustomerControllerTest (4 tests)
| Test | Description |
|------|-------------|
| `createCustomer_Success` | 201 Created with valid payload |
| `createCustomer_InvalidEmail` | 400 Bad Request for invalid email format |
| `createCustomer_MissingFields` | 400 Bad Request with validation errors |
| `getBalance_Success` | 200 OK with balance data |
| `getBalance_NotFound` | 404 Not Found for unknown account |

#### TransactionControllerTest (6 tests)
| Test | Description |
|------|-------------|
| `transferFunds_Success` | 200 OK with transfer details |
| `transferFunds_ValidationError` | 400 Bad Request for missing fields |
| `transferFunds_InsufficientFunds` | 400 Bad Request for low balance |
| `transferFunds_SameAccount` | 400 Bad Request for same account |
| `transferFunds_AccountNotFound` | 404 Not Found for unknown account |
| `transferFunds_NameMismatch` | 400 Bad Request for name mismatch |

---

## 📄 License

This project is for assessment/demonstration purposes.
