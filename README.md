# Personal Finance REST API

A comprehensive personal finance management REST API built with Spring Boot 3.x featuring JWT authentication with refresh tokens, Redis caching, and comprehensive Swagger documentation.

## Features

- **User Authentication & Authorization**
  - User registration with email validation
  - JWT-based authentication
  - Access token (4-hour expiration) and refresh token (7-day expiration)
  - Secure logout with immediate token invalidation

- **Account Management**
  - Create, read, update, and delete financial accounts
  - Support for multiple account types (savings, checking, credit, etc.)
  - Real-time balance tracking
  - Total balance calculation across all accounts

- **Category Management**
  - Create custom categories for income and expense tracking
  - Filter categories by type (INCOME/EXPENSE)
  - Customizable icons and colors

- **Transaction Management**
  - Record income and expense transactions
  - Pagination and sorting support
  - Date range filtering
  - Transaction summaries with totals

- **API Documentation**
  - Interactive Swagger UI documentation at `/swagger-ui.html`
  - OpenAPI 3.0 specification

## Tech Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA** (MySQL)
- **Spring Security** (JWT Authentication with jjwt 0.12.3)
- **Spring Data Redis** (Refresh token storage)
- **Spring Actuator** (Monitoring)
- **springdoc-openapi** (Swagger/OpenAPI)
- **Lombok** (Boilerplate reduction)
- **Maven** (Build Tool)

## Prerequisites

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 7.0+ (optional, for refresh tokens)

## Installation & Setup

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/personal-finance.git
cd personal-finance
```

### 2. Configure Database

Update `src/main/resources/application.yml` with your MySQL credentials:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/personal_finance
    username: root
    password: your_password
```

### 3. Create Database

```sql
CREATE DATABASE personal_finance;
```

### 4. Start Redis (Optional)

```bash
# macOS
brew services start redis

# Ubuntu/Debian
sudo systemctl start redis

# Windows (using Docker)
docker run -d -p 6379:6379 redis:alpine
```

### 5. Build the Application

```bash
mvn clean install
```

### 6. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Documentation

Once the application is running, access the Swagger UI at:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## API Endpoints

### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login and get tokens |
| POST | `/api/auth/refresh` | Refresh access token |
| POST | `/api/auth/logout` | Logout and invalidate token |

### Accounts

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/accounts` | List all accounts |
| GET | `/api/accounts/{id}` | Get account by ID |
| POST | `/api/accounts` | Create a new account |
| PUT | `/api/accounts/{id}` | Update account |
| DELETE | `/api/accounts/{id}` | Delete account |
| GET | `/api/accounts/total-balance` | Get total balance |

### Categories

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/categories` | List all categories |
| GET | `/api/categories?type=EXPENSE` | Filter by type |
| GET | `/api/categories/{id}` | Get category by ID |
| POST | `/api/categories` | Create a new category |
| PUT | `/api/categories/{id}` | Update category |
| DELETE | `/api/categories/{id}` | Delete category |

### Transactions

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/transactions` | List all transactions (paginated) |
| GET | `/api/transactions/{id}` | Get transaction by ID |
| POST | `/api/transactions` | Create a new transaction |
| PUT | `/api/transactions/{id}` | Update transaction |
| DELETE | `/api/transactions/{id}` | Delete transaction |
| GET | `/api/transactions/by-date-range` | Filter by date range |
| GET | `/api/transactions/summary` | Get transaction summary |

## API Usage Examples

### Register a User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "password123"
  }'
```

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "password123"
  }'
```

Response:
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "userId": 1,
    "username": "johndoe",
    "email": "john@example.com"
  }
}
```

### Create an Account (Authenticated)

```bash
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "name": "My Savings",
    "type": "SAVINGS",
    "balance": 1000.00,
    "currency": "USD"
  }'
```

### Create a Category

```bash
curl -X POST http://localhost:8080/api/categories \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "name": "Food & Dining",
    "type": "EXPENSE",
    "icon": "restaurant",
    "color": "#FF5722"
  }'
```

### Create a Transaction

```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "amount": 25.50,
    "type": "EXPENSE",
    "transactionDate": "2024-01-15",
    "description": "Lunch at restaurant",
    "accountId": 1,
    "categoryId": 1
  }'
```

## JWT Authentication Flow

1. **Register/Login**: User registers or logs in to receive access and refresh tokens
2. **Access Token**: Used in `Authorization: Bearer <token>` header (expires in 4 hours)
3. **Refresh Token**: Stored in Redis, used to get new access token (valid for 7 days)
4. **Token Refresh**: Call `/api/auth/refresh` with refresh token and user ID before expiration
5. **Logout**: Invalidates refresh token immediately in Redis

## Project Structure

```
src/main/java/com/example/finance/
├── config/              # Configuration classes
│   ├── OpenApiConfig.java    # Swagger/OpenAPI configuration
│   └── SecurityConfig.java  # Spring Security configuration
├── controller/          # REST controllers
│   ├── AuthController.java
│   ├── AccountController.java
│   ├── CategoryController.java
│   └── TransactionController.java
├── dto/                 # Data Transfer Objects
│   ├── request/
│   └── response/
├── entity/              # JPA entities
│   ├── User.java
│   ├── Account.java
│   ├── Category.java
│   └── Transaction.java
├── exception/           # Custom exceptions
├── repository/          # JPA repositories
├── security/            # JWT security components
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   ├── RefreshTokenService.java
│   └── UserPrincipal.java
└── service/             # Business logic
```

## Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AccountServiceTest
```

## Code Quality

### Format Code with Spotless

```bash
# Apply code formatting
mvn spotless:apply

# Check code formatting
mvn spotless:check
```

## Monitoring

Access actuator endpoints at:

- `GET /actuator/health` - Health check
- `GET /actuator/info` - Application info
- `GET /actuator/metrics` - Application metrics

## License

This project is licensed under the MIT License.
