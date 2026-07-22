# Robinhood Clone

This is a full-stack application that replicates core functionalities of Robinhood, including user authentication, data management, and a responsive frontend. The project is divided into two main parts: the frontend (React-based) and the backend (Java-based).

---

## Project Structure

- frontend/ — React-based client-side code built with Vite.
- user-service/ — Java-based backend service built with Spring Boot.
- docker-compose.yml — container setup for local development.
- ROADMAP.md — planned services and milestones.

---

## What is implemented so far

- User registration and login
- JWT-based authentication
- Protected routes for authenticated users
- Dashboard and watchlist-related frontend experience
- Backend APIs for user management and watchlist support

---

## Tech Stack

### Frontend
- React 19
- Vite
- React Router
- Axios
- ESLint

### Backend
- Java 21
- Spring Boot 3.2.5
- Spring Security
- Spring Data JPA
- Hibernate
- JWT

---

## How to run locally

### 1. Start infrastructure

`ash
docker-compose up -d
`

### 2. Run the backend

`ash
cd user-service
mvn spring-boot:run

ash
cd market-data-service
mvn spring-boot:run`

### 3. Run the frontend

`ash
cd frontend
npm install
npm run dev
`

The frontend will typically run on http://localhost:3000 and the backend on http://localhost:8081.
