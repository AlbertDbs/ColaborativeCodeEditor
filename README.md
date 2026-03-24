# Collaborative Code Editor

A collaborative coding platform built as a microservices project with a React frontend and a Spring Boot backend.

At the current stage, the project already covers the full core flow: authentication, workspace management, invitations, document editing, threaded comments, and running code directly from the editor. The goal is to make collaboration feel practical and straightforward, while keeping the architecture clean enough to grow further.

## What the project does right now

- lets users register and log in with email/password
- allows users to create and manage workspaces
- supports sending, accepting, and refusing workspace invitations
- provides a code editor for project documents
- supports threaded comments on documents
- lets users run code from the editor
- auto-maps file extensions from the selected language in the editor UI

## Tech stack

### Frontend

- React 18
- TypeScript
- Vite
- React Router
- Monaco Editor

### Backend

- Java 17
- Spring Boot 3
- Spring Cloud Gateway
- Spring Security
- JWT authentication
- Spring Data JPA / Hibernate
- Flyway
- Maven

### Data and infrastructure

- PostgreSQL
- REST APIs
- Server-Sent Events for live document updates

### Languages involved in the project

- Java for the backend services
- TypeScript for the frontend
- SQL for database schema migrations

## Project structure

```text
ColaborativePlatform/
├── backend/
│   ├── pom.xml
│   └── services/
│       ├── api-gateway
│       ├── auth-service
│       ├── user-service
│       ├── workspace-service
│       ├── invitation-service
│       └── document-service
└── frontend/
    ├── package.json
    └── src/
```

### Backend services

- `api-gateway` - entry point for the frontend
- `auth-service` - registration, login, JWT, Google login
- `user-service` - user profile data
- `workspace-service` - workspaces and membership logic
- `invitation-service` - invitation flow
- `document-service` - documents, comments, live updates, and code execution

## Default local ports

- Frontend (Vite): `3000`
- API Gateway: `8080`
- Auth Service: `8081`
- User Service: `8082`
- Workspace Service: `8083`
- Invitation Service: `8084`
- Document Service: `8085`
- PostgreSQL: expected on `5433` by default

## Running the project locally

### 1. Prerequisites

Make sure you have:

- Node.js 18+ and npm
- Java 17+
- Maven 3.9+
- PostgreSQL

### 2. Create the PostgreSQL databases

By default, the services expect PostgreSQL on `localhost:5433` with:

- username: `collab`
- password: `collab`

Create the databases below:

- `collab_auth`
- `collab_user`
- `collab_workspace`
- `collab_invite`
- `collab_document`

Example SQL:

```sql
CREATE USER collab WITH PASSWORD 'collab';

CREATE DATABASE collab_auth OWNER collab;
CREATE DATABASE collab_user OWNER collab;
CREATE DATABASE collab_workspace OWNER collab;
CREATE DATABASE collab_invite OWNER collab;
CREATE DATABASE collab_document OWNER collab;
```

If your PostgreSQL instance runs on a different port or with different credentials, you can override the datasource environment variables for each service.

### 3. Start the backend

```bash
cd backend
mvn -pl services/user-service spring-boot:run
```

```bash
cd backend
mvn -pl services/auth-service spring-boot:run
```

```bash
cd backend
mvn -pl services/workspace-service spring-boot:run
```

```bash
cd backend
mvn -pl services/invitation-service spring-boot:run
```

```bash
cd backend
mvn -pl services/document-service spring-boot:run
```

```bash
cd backend
mvn -pl services/api-gateway spring-boot:run
```

Flyway migrations run automatically on startup.

### 4. Start the frontend

```bash
cd frontend
npm install
npm run dev
```

Then open:
```text
http://localhost:3000
```
