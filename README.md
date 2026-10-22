# Task Manager REST API

A beginner-friendly REST API for managing tasks, built with Java 21 and Spring Boot.

## Tech Stack

- Java 21
- Spring Boot 3.3.5 (Web, Data JPA, Validation)
- H2 in-memory database
- Maven
- JUnit 5 and Mockito

## Prerequisites

- JDK 21 installed (`java -version` should show 21)
- Maven 3.9+ installed (`mvn -version`)

## Run the Application

```bash
mvn spring-boot:run
```

The API starts at `http://localhost:8080`.

## Run the Tests

```bash
mvn test
```

## H2 Database Console

Open `http://localhost:8080/h2-console` and use:

| Field    | Value                |
|----------|----------------------|
| JDBC URL | `jdbc:h2:mem:taskdb` |
| User     | `sa`                 |
| Password | (leave empty)        |

The database lives in memory, so all data is erased when the app stops.

## Project Structure

| Package      | Purpose                                         |
|--------------|-------------------------------------------------|
| `entity`     | JPA entity (`Task`) and the `TaskStatus` enum   |
| `repository` | Database access via Spring Data JPA             |
| `dto`        | Request and response objects                    |
| `service`    | Business logic                                  |
| `controller` | REST endpoints                                  |
| `exception`  | Custom exceptions and global error handling     |

## Task Fields

| Field         | Type     | Notes                                         |
|---------------|----------|-----------------------------------------------|
| `id`          | Long     | Auto-generated                                |
| `title`       | String   | Required, max 100 characters                  |
| `description` | String   | Optional, max 500 characters                  |
| `status`      | Enum     | `PENDING`, `IN_PROGRESS`, `COMPLETED`         |
| `createdAt`   | DateTime | Set automatically when the task is created    |

## API Endpoints

| Method | URL               | Description        | Success Code   |
|--------|-------------------|--------------------|----------------|
| POST   | `/api/tasks`      | Create a task      | 201 Created    |
| GET    | `/api/tasks`      | List all tasks     | 200 OK         |
| GET    | `/api/tasks/{id}` | Get a task by ID   | 200 OK         |
| PUT    | `/api/tasks/{id}` | Update a task      | 200 OK         |
| DELETE | `/api/tasks/{id}` | Delete a task      | 204 No Content |

### Create a task

`POST /api/tasks`

```json
{
  "title": "Write project report",
  "description": "Quarterly summary for the team",
  "status": "PENDING"
}
```

`status` is optional and defaults to `PENDING`.

Response (`201 Created`):

```json
{
  "id": 1,
  "title": "Write project report",
  "description": "Quarterly summary for the team",
  "status": "PENDING",
  "createdAt": "2026-10-22T10:15:30.123456"
}
```

### Get all tasks

`GET /api/tasks`

```json
[
  {
    "id": 1,
    "title": "Write project report",
    "description": "Quarterly summary for the team",
    "status": "PENDING",
    "createdAt": "2026-10-22T10:15:30.123456"
  }
]
```

### Get a task by ID

`GET /api/tasks/1` returns a single task object (same shape as above).

### Update a task

`PUT /api/tasks/1`

```json
{
  "title": "Write project report (final)",
  "description": "Quarterly summary, reviewed",
  "status": "COMPLETED"
}
```

If `status` is omitted, the existing status is kept.

### Delete a task

`DELETE /api/tasks/1` returns `204 No Content` with an empty body.

## Error Responses

All errors use the same JSON shape.

**404 Not Found** (task does not exist):

```json
{
  "timestamp": "2026-10-22T10:20:00.123",
  "status": 404,
  "error": "Not Found",
  "message": "Task not found with id: 99"
}
```

**400 Bad Request** (validation failed):

```json
{
  "timestamp": "2026-10-22T10:21:00.123",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "validationErrors": {
    "title": "Title is required"
  }
}
```

**400 Bad Request** (invalid status value such as `"DONE"`):

```json
{
  "timestamp": "2026-10-22T10:22:00.123",
  "status": 400,
  "error": "Bad Request",
  "message": "Malformed JSON or invalid value. Status must be one of: PENDING, IN_PROGRESS, COMPLETED"
}
```

## Try It with curl

```bash
# Create
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Learn Spring Boot","description":"Build a REST API","status":"IN_PROGRESS"}'

# List all
curl http://localhost:8080/api/tasks

# Get one
curl http://localhost:8080/api/tasks/1

# Update
curl -X PUT http://localhost:8080/api/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{"title":"Learn Spring Boot","description":"Done!","status":"COMPLETED"}'

# Delete
curl -X DELETE http://localhost:8080/api/tasks/1

# Validation error (blank title)
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"","description":"No title"}'
```
