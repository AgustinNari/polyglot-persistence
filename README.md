# Polyglot Persistence — Multi-Database E-Commerce System

A Java application demonstrating polyglot persistence through a console-based e-commerce system.

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
- Docker Engine with Docker Compose v2 (Linux containers).
- Sufficient system resources to run SQL Server, MongoDB, Redis, and Cassandra.

### Database Environment

The Docker Compose configuration is located in:

`infra/docker-compose.yml`

The environment contains:

- Microsoft SQL Server on port `1433`.
- MongoDB on port `27017`.
- Redis on port `6379`.
- Cassandra on port `9042`.

All four ports are bound to `127.0.0.1`. MongoDB, Redis, and Cassandra have no authentication configured: use this environment only for local, isolated development.

Compose uses the project identity `polyglot-persistence`, its own network, and project-scoped SQL Server, MongoDB, and Cassandra volumes. It does not attach external volumes or use fixed container names. Redis retains the original ephemeral storage behavior; stopping/removing its container can lose carts.

From the repository root, create the local environment file **once**, without overwriting an existing file:

```powershell
if (-not (Test-Path -LiteralPath infra/.env)) { Copy-Item infra/.env.example infra/.env }
```

On Bash, use `cp -n infra/.env.example infra/.env` instead. Edit `infra/.env` locally and fill in `MSSQL_SA_PASSWORD` with a **new**, unique development password. Keep the single-quoted, single-line format from the example, with no embedded single quotes. SQL Server requires 8–128 characters and characters from at least three categories: uppercase letters, lowercase letters, digits, and symbols. Empty placeholders intentionally block startup. `DEMO_USER_PASSWORD` can remain empty unless you explicitly run the demo-data loader.

`infra/.env` is ignored by Git. Never commit it, reuse the formerly versioned credential, or paste credentials into logs. Changing this file does **not** rotate the SQL login in an existing database volume; use new dedicated volumes for a new demonstration environment, and inspect existing volumes before reuse.

Load the same file into the terminal that will run both Compose and Java. On Windows PowerShell:

```powershell
./infra/load-env.ps1
```

The helper imports only the two supported variables without executing the file or printing their values. On Bash:

```bash
set -a
. ./infra/.env
set +a
```

Java requires `MSSQL_SA_PASSWORD` in its process environment; it does not read `.env` itself. Run Maven in this same terminal, or configure the identical environment variable in your IDE run configuration. Reload the file whenever you change it. A shell environment variable takes precedence over Compose's `--env-file`, so do not run Compose with a stale value.

Before startup, confirm the Docker engine is available and inspect existing resources:

```bash
docker info
docker ps -a --filter label=com.docker.compose.project=polyglot-persistence
docker volume ls --filter name=polyglot-persistence_
```

On Windows, also check the published ports:

```powershell
Get-NetTCPConnection -State Listen -ErrorAction SilentlyContinue |
    Where-Object LocalPort -in 1433,27017,6379,9042 |
    Select-Object LocalAddress,LocalPort,OwningProcess
```

If the engine is unavailable, any port is occupied, or existing containers/volumes cannot be confirmed as belonging to this environment, stop and resolve the conflict before continuing. Do not stop other services or point Java at databases from another project. Keep the explicit project name in all commands below.

Validate the configuration without printing interpolated secrets, then start the services:

```bash
docker compose -p polyglot-persistence --env-file infra/.env -f infra/docker-compose.yml config --quiet
docker compose -p polyglot-persistence --env-file infra/.env -f infra/docker-compose.yml up -d
```

Check their status:

```bash
docker compose -p polyglot-persistence --env-file infra/.env -f infra/docker-compose.yml ps -a
```

SQL Server and Cassandra may take several minutes to initialize. Their initializers depend on successful query healthchecks rather than fixed startup sleeps. MongoDB initialization also waits for primary election and creates collections/indexes on the primary. SQL/CQL schema creation and MongoDB initialization can be repeated without deleting existing data; unexpected replica-set configurations fail instead of being forcibly rewritten.

**Before starting Java**, all four database services must be `healthy` and all three initializers (`sqlserver-init`, `mongo-init`, `cassandra-init`) must have exited with code `0`. A successful `up -d` alone does not establish readiness. If initialization fails, inspect the affected initializer's logs locally and resolve the error before running the application.

The single-node MongoDB URI uses `replicaSet=rs0&directConnection=true` so Java on Windows connects through the published endpoint rather than trying to resolve the internal Docker hostname. Cassandra advertises `127.0.0.1` for host-side clients; this configuration is for this single-node local setup.

### Build

From the repository root:

```bash
mvn compile
```

### Run

After the database services are ready, start the console application:

```bash
mvn exec:java "-Dexec.mainClass=com.tpo.app.App"
```

The POM explicitly declares `exec-maven-plugin` version `3.1.0` and defaults its main class to `com.tpo.app.App`; `mvn exec:java` is equivalent. The `exec:java` goal requires the preceding compile step. Exit the application's main menu with `0`.

The application provides interactive menus for authentication, product management, shopping carts, orders, invoicing, and payments.

### Stop the Environment

```bash
docker compose -p polyglot-persistence --env-file infra/.env -f infra/docker-compose.yml down
```

SQL Server, MongoDB, and Cassandra volumes are retained by this command. Redis carts are not persisted by this setup. **Do not add `-v` / `--volumes`**: removing volumes destroys stored data. Do not use volume/container pruning as a shutdown procedure.

## Demo Data and Maintenance

The repository includes:

- `com.tpo.util.CargarDatos` — Utility for preparing demonstration data.
- `com.tpo.util.BorrarDatos` — Utility for deleting application data.

**Warning:** `CargarDatos` is not idempotent; reruns can duplicate data or fail on existing records. It requires a disposable `DEMO_USER_PASSWORD` in the Java process environment, imported from `infra/.env` by the helper above. Use a different value from the SQL credential and never use real accounts or passwords.

`BorrarDatos` deletes information from all four configured databases. Its Redis `FLUSHDB` clears the entire selected Redis database, including unrelated keys on a shared server. Run maintenance utilities only by explicit choice against an isolated, disposable environment. Neither utility is required for startup or validation.

## Technical Scope

This project demonstrates how multiple persistence technologies can coexist within one application.

This is a local development prototype rather than a production-ready distributed commerce platform.

Application user passwords are stored without hashing. This is a known security limitation; do not use real credentials. SQL Server's `sa` account and the local JDBC encryption settings are also intended only for this development environment.

The project does not implement a web frontend, HTTP API, real payment processing, or production-grade distributed transaction coordination.

Database configuration, initialization, and service readiness should be validated for the local environment before execution.
