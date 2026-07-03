# Robinhood Clone

This is a full-stack application that replicates core functionalities of Robinhood, including user authentication, data management, and a responsive frontend. The project is divided into two main parts: the **frontend** (React-based) and the **backend** (Java-based).

---

## Project Structure

- **`frontend/`**: Contains the React-based client-side code.
  - Built with Vite for fast development and build processes.
  - Includes reusable components, API modules, and global state management using React Context.
- **`user-service/`**: Contains the Java-based backend service.
  - Built with Maven for dependency management.
  - Implements REST APIs for user authentication and data handling.
- **`docker-compose.yml`**: Configures Docker containers for running the application locally.

---

## Dependencies

### Frontend
- **React**: For building the user interface.
- **Vite**: For development and build tooling.
- **Axios**: For making HTTP requests to the backend.
- **ESLint**: For code linting and maintaining code quality.

### Backend
- **Java 17**: The programming language used for the backend.
- **Spring Boot**: For building the REST API and managing application configuration.
- **Spring Security**: For implementing JWT-based authentication.
- **Hibernate**: For ORM (Object-Relational Mapping) with the database.
- **JUnit**: For writing and running unit tests.

---

## How to Run Locally

### Prerequisites
- **Node.js** (v16 or higher) and **npm** for the frontend.
- **Java 17** and **Maven** for the backend.
- **Docker** and **Docker Compose** for containerized deployment.

### Steps

1. **Clone the Repository**
   ```sh
   git clone <repository-url>
   cd <repository-folder>
