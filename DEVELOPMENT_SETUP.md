# Development Setup

This guide explains what another developer needs to install and run the Attendance System locally.

## Required Software

- Java JDK 21
- Maven 3.9 or newer
- Node.js 20 or newer
- npm 10 or newer
- MySQL Server 8.x
- Git

## Project Structure

- `backend/` - Spring Boot API
- `frontend/` - React + Vite web app
- MySQL database name used by default: `attendance_db`

## Database Setup

Create an empty MySQL database before starting the backend:

```sql
CREATE DATABASE attendance_db;
```

The backend uses Flyway migrations, so tables are created automatically when the Spring Boot app starts.

## Backend Configuration

The backend reads most local settings from environment variables. If an environment variable is not set, `application.yml` provides local defaults.

Recommended environment variables:

```bash
DB_HOST=localhost
DB_PORT=3306
DB_NAME=attendance_db
DB_USERNAME=root
DB_PASSWORD=your_mysql_password
JWT_SECRET=replace_with_a_long_random_secret_key
JWT_EXPIRATION_MS=86400000
SERVER_PORT=8080
BOOTSTRAP_ADMIN_ENABLED=true
BOOTSTRAP_ADMIN_USERNAME=admin
BOOTSTRAP_ADMIN_PASSWORD=admin@123
BOOTSTRAP_ADMIN_FULLNAME=System Administrator
CORS_ALLOWED_ORIGINS=http://localhost:5173
```

Start the backend:

```bash
cd backend
mvn spring-boot:run
```

Run backend checks:

```bash
cd backend
mvn test
```

## Frontend Configuration

Install dependencies:

```bash
cd frontend
npm install
```

Start the frontend:

```bash
cd frontend
npm run dev
```

By default, Vite runs on:

```text
http://localhost:5173
```

The Vite dev server proxies API requests to:

```text
http://localhost:8080
```

If you do not use the Vite proxy, set:

```bash
VITE_API_BASE_URL=http://localhost:8080
```

Build frontend:

```bash
cd frontend
npm run build
```

## Default Login

If `BOOTSTRAP_ADMIN_ENABLED=true`, the backend creates the configured admin account on startup.

Default local credentials:

```text
Username: admin
Password: admin@123
```

## Git Notes

Do not commit generated folders:

- `backend/target/`
- `frontend/dist/`
- `frontend/node_modules/`

These are ignored by `.gitignore` and should be regenerated locally.

## Common Fixes

If VS Code Java problems show stale generated files or missing classes:

1. Run `mvn clean compile` inside `backend/`.
2. In VS Code, run `Java: Clean Java Language Server Workspace`.
3. Reload VS Code.

If the backend cannot connect to MySQL:

1. Confirm MySQL is running.
2. Confirm `attendance_db` exists.
3. Confirm `DB_USERNAME` and `DB_PASSWORD` match your local MySQL user.
