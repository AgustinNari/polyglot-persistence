# Polyglot Persistence — Multi-Database E-Commerce System

An academic Java application demonstrating polyglot persistence through a console-based e-commerce system.

The project uses four different database technologies, assigning each one specific responsibilities according to its data model and access patterns.

It combines relational transactions, document storage, in-memory data structures, and session-event records within a single application.

## Architecture

| Database | Responsibilities |
| --- | --- |
| Microsoft SQL Server | Users, orders, invoices, and payments |
| MongoDB | Product catalog and product-change history |
| Redis | Shopping carts, cart state, undo, and redo operations |
| Apache Cassandra | User login and logout session records |

The application follows a layered organization with domain models, data access objects (DAOs), business services, and a console interface.

## Features

### User Management

- User registration and login.
- User roles and administrative restrictions.
- User-category information.
- Session activity records.

### Product Catalog

- Product creation and listing.
- Product updates and deletion.
- Product-change history in MongoDB.
- Administrative management operations.

### Shopping Cart

- Cart initialization and item management.
- Product additions and removals.
- Quantity updates.
- Undo and redo of cart changes through Redis.

### Orders and Billing

- Order creation from shopping carts.
- Order listing and cancellation.
- Invoice generation.
- Payment registration.
- Order, invoice, and payment queries.

The application is controlled through a command-line menu, not a graphical frontend or REST API.

## Tech Stack

- Java 17
- Apache Maven
- Microsoft SQL Server
- MongoDB
- Redis
- Apache Cassandra
- Docker Compose
- JDBC
- HikariCP
- MongoDB Java Driver
- Jedis
- DataStax Java Driver
- Gson

## Project Structure

- `src/main/java/com/tpo/app/` — Console application and menu operations.
- `src/main/java/com/tpo/config/` — Database connections and configuration.
- `src/main/java/com/tpo/dao/` — Data access interfaces and database-specific implementations.
- `src/main/java/com/tpo/modelo/` — Domain entities and data models.
- `src/main/java/com/tpo/servicio/` — Business services.
- `src/main/java/com/tpo/util/` — Demo-data loading and database maintenance utilities.
- `src/main/resources/` — Application configuration.
- `infra/` — Docker Compose configuration and initialization scripts.
- `pom.xml` — Maven dependencies and project configuration.

## Getting Started

### Requirements

- JDK 17.
- Apache Maven.
- Docker Engine with Docker Compose.
- Sufficient system resources to run SQL Server, MongoDB, Redis, and Cassandra.

### Database Environment

The Docker Compose configuration is located in:

`infra/docker-compose.yml`

The environment contains:

- Microsoft SQL Server on port `1433`.
- MongoDB on port `27017`.
- Redis on port `6379`.
- Cassandra on port `9042`.

Use local development credentials configured outside version control.

After creating the local environment file, start the services from the repository root:

```bash
docker compose --env-file infra/.env -f infra/docker-compose.yml up -d
```

Check their status:

```bash
docker compose --env-file infra/.env -f infra/docker-compose.yml ps
```

SQL Server and Cassandra may take additional time to initialize.

Database initialization must complete before starting the Java application.

### Build

From the repository root:

```bash
mvn compile
```

### Run

After the database services are ready, start the console application:

```bash
mvn exec:java -Dexec.mainClass=com.tpo.app.App
```

The application provides interactive menus for authentication, product management, shopping carts, orders, invoicing, and payments.

### Stop the Environment

```bash
docker compose --env-file infra/.env -f infra/docker-compose.yml down
```

Persistent database volumes are retained unless they are explicitly removed.

## Demo Data and Maintenance

The repository includes:

- `com.tpo.util.CargarDatos` — Utility for preparing demonstration data.
- `com.tpo.util.BorrarDatos` — Utility for deleting application data.

**Warning:** The deletion utility clears data from the configured database systems. Use it only against an isolated, disposable development environment.

## Technical Scope

This project demonstrates how multiple persistence technologies can coexist within one application.

It is an educational implementation rather than a production-ready distributed commerce platform.

The project does not implement a web frontend, HTTP API, real payment processing, or production-grade distributed transaction coordination.

Database configuration, initialization, and service readiness should be validated for the local environment before execution.
