# Personal Finance Creator MVP (Backend Only)

Spring Boot 3 + MySQL backend focused on creator cash flow:

- Creator Income Inbox (`incomeSource`: `BRAND`, `ADS`, `AFFILIATE`, `PRODUCT`)
- Monthly income summary by source
- Monthly safe-to-spend number based on recent income volatility

## Prerequisites

- JDK 17
- Maven 3.9+
- MySQL 8

## Run with Docker Compose

From project root:

```bash
docker compose up --build
```

API base URL: `http://localhost:8080`

## Run locally

1. Ensure MySQL is running and database `personal_finance` exists (or use `createDatabaseIfNotExist` in JDBC URL).
2. Start app:

```bash
mvn spring-boot:run
```

OpenAPI:

- JSON: `http://localhost:8080/v3/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## Key environment variables


| Variable                        | Purpose                | Default (local)                            |
| ------------------------------- | ---------------------- | ------------------------------------------ |
| `SPRING_DATASOURCE_URL`         | JDBC URL               | MySQL on `localhost:3306/personal_finance` |
| `SPRING_DATASOURCE_USERNAME`    | DB user                | `root`                                     |
| `SPRING_DATASOURCE_PASSWORD`    | DB password            | `Admin@123`                                |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Schema mode            | `update`                                   |
| `JWT_SECRET`                    | JWT signing key        | value in `application.yml`                 |
| `JWT_ACCESS_EXPIRATION`         | Access token TTL (ms)  | `14400000`                                 |
| `JWT_REFRESH_EXPIRATION`        | Refresh token TTL (ms) | `604800000`                                |
| `SERVER_PORT`                   | HTTP port              | `8080`                                     |


## Creator MVP APIs

All endpoints below require authentication except `/api/auth/**`.

- `POST /api/transactions`
  - Create transaction.
  - For `type=INCOME`, `incomeSource` is required.
- `GET /api/transactions/income-source-summary?month=YYYY-MM`
  - Returns monthly grouped income totals by source.
- `GET /api/creator-insights/safe-to-spend?month=YYYY-MM`
  - Returns monthly safe-to-spend calculation.
  - If `month` is omitted, current month is used.

### Example create income transaction

```json
{
  "amount": 1200.00,
  "type": "INCOME",
  "incomeSource": "BRAND",
  "transactionDate": "2026-04-10",
  "description": "Brand deal payout",
  "accountId": 1,
  "categoryId": 1
}
```

## Creator financial analyst (AI)

`POST /api/ai/assistant/query` (authenticated) behaves as a **personal financial analyst for creators with irregular income**. It uses your transaction window (`startDate` / `endDate`), plus **creator metrics for the calendar month of `endDate`**: income by source (`BRAND`, `ADS`, `AFFILIATE`, `PRODUCT`) and the same safe-to-spend inputs as `/api/creator-insights/safe-to-spend`. Configure the LLM via `AI_*` environment variables in `application.yml`.

## Tests

```bash
mvn spotless:check
mvn test
```

