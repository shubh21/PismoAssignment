# Mini Account & Transaction Service

A clean, hexagonal-architecture-based Spring Boot service supporting account creation and financial transactions with proper domain validations and MySQL persistence.

It supports:
* Creating accounts
* Retrieving accounts
* Creating transactions with business rules
* Persisting accounts and transactions
* Enforcing sign rules based on OperationType
* Returning structured JSON responses
* Proper exception handling using Spring ProblemDetail

Designed with Hexagonal Architecture, focusing on:
* Domain-centric models
* Clear separation between web, use case, and persistence layers
* Adapters inside infrastructure.adapter.in/out
* Ports inside core.contract.input/output
* Rich domain validations where appropriate

# Business Rules (Transaction Behavior)
## Operation Types:

| ID | Type                 | Sign Rule         |
| -- | -------------------- | ----------------- |
| 1  | CASH_PURCHASE        | Amount → negative |
| 2  | INSTALLMENT_PURCHASE | Amount → negative |
| 3  | WITHDRAWAL           | Amount → negative |
| 4  | PAYMENT              | Amount → positive |



# REST Endpoints
## 1. Create Account 
## POST /accounts
## Request
```json
  {
   "document_number": 12345678900
   }
```
  ## Response
````json
    {
        "account_id": 1,
        "document_number": 12345678900
    }
````

## 2. Retrieve Account
 ## GET /accounts/{accountId}
 ## Response
```json
{
  "account_id": 1,
  "document_number": 12345678900
}
```

## 3. Create Transaction
   POST /transactions
 ## Request
````json
{
  "account_id": 1,
  "operation_type_id": 2,
  "amount": 100.00
}
````
  ## Response
```json
{
  "transaction_id": 10,
  "account_id": 1,
  "operationType_Id": 2,
  "amount": -100.00,
  "event_date": "2025-01-01T12:00:00"
}
```

# Testing Strategy
This project contains:
* Unit Tests
* Integration Tests 
  * Using @JdbcTest + Testcontainers or embedded MySQL

Covers:
* Persistence adapters (Account & Transaction)


# Docker Setup
Start the full environment (App + MySQL):
```
docker-compose up --build
```

Stops & removes containers:
```
docker-compose down
```
