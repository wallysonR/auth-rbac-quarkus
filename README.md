# 🔐 Auth RBAC Quarkus

A **JWT authentication and Role-Based Access Control (RBAC) API** built with **Quarkus 3**, designed as a clean, production-ready template for secure applications.

![Java](https://img.shields.io/badge/Java-21-blue)
![Quarkus](https://img.shields.io/badge/Quarkus-3.x-orange)
![PostgreSQL](https://img.shields.io/badge/Postgres-16-blue)
![License](https://img.shields.io/badge/License-MIT-green)

---

## 🚀 Features
- **User Authentication** with username + password.
- **Password hashing** with [BCrypt](https://github.com/patrickfav/bcrypt).
- **JWT-based Authorization** (with private/public key pair).
- **Role-Based Access Control (RBAC)** with `@RolesAllowed`.
- **Database Seeder**:
    - Roles `ADMIN` and `USER`
    - User `admin/admin123` → roles: `ADMIN`, `USER`
    - User `jose/jose123` → role: `USER`
- **H2 Database for tests**, PostgreSQL for development.
- Ready-to-use with **Docker Compose** for Postgres.

---

## 📂 Endpoints

### Authentication
#### `POST /auth/login`
Authenticate user and return a JWT token.

Request:
```json
{ "username": "admin", "password": "admin123" }
