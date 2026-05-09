# Implementation & Setup Guide

This document outlines the technical stack and instructions for running the Customer Account Reconciliation service.

## 1. Tech Stack
The application is built using modern Java technologies:
*   **Language:** Java 17
*   **Framework:** Spring Boot 3.3.5
*   **Database:** PostgreSQL (for persistent storage)
*   **Migration Tool:** Liquibase (handles database schema versioning)
*   **Build Tool:** Maven
*   **Containerization:** Docker & Docker Compose
*   **Testing:** JUnit 5, Mockito, and Testcontainers (for integration tests with real database)

---

## 2. How to Run

### Option A: Using Docker

This starts the application and the PostgreSQL database in containers.

Run:
```bash
make up
```
The API will be available at `http://localhost:8080`.

To stop the services:
```bash
make down
```

### Option B: Local Development

If you prefer running the application locally:

**Start the database only**:
```bash
make db-up
```

**Run the Spring Boot application**:
```bash
make run
```

---

## 4. Running Tests

The project includes unit and integration tests. Integration tests use Testcontainers to spin up a temporary database.

```bash
make test
```
