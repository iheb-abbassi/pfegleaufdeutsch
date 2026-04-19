# 📄 PROJECT_TECH_STACK.md

## 🧱 Architecture

- Type: Modular Monolith
- Pattern: REST API

Flow:

React PWA → Spring Boot API → PostgreSQL

## 🎨 Frontend

- Framework: React 18 + TypeScript
- Build Tool: Vite
- Routing: React Router
- State / Data: TanStack Query
- Styling: Tailwind CSS
- Forms: React Hook Form + Zod
- PWA: vite-plugin-pwa

## ⚙️ Backend

- Language: Java 17
- Framework: Spring Boot

Modules:

- Spring Web
- Spring Security
- Spring Data JPA
- Bean Validation
- OpenAPI (Swagger)

## 🗄️ Database

- DBMS: PostgreSQL
- ORM: Hibernate (via JPA)

Core Tables

- users
- domains
- questions
- question_options
- user_question_progress
- exam_attempts
- exam_attempt_questions

## 🔐 Authentication

- Method: JWT (Access + Refresh)

Login:

- Email + Password
- Google OAuth2

- Roles: USER, ADMIN

## 📥 Data Import

- CSV: Apache Commons CSV
- Excel: Apache POI
w