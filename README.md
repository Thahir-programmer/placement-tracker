# Placement Prep Tracker

A full-stack web app where students sign up, log in, and track the companies they have applied to (status + notes), with dashboard counts per stage.

**Live demo:** https://placement-tracker-qxzl.onrender.com
(Hosted on Render's free plan. The first load may take ~30–60 seconds while it wakes up.)

## Tech Stack

- **Backend:** Java 17, Spring Boot 4.1.2, Spring Web, Spring Data JPA (Hibernate), Spring Security + JWT (HS256), Bean Validation, Lombok
- **Database:** PostgreSQL (Neon)
- **Frontend:** plain HTML, CSS, JavaScript (served from `src/main/resources/static`)
- **Build / Deploy:** Maven, Docker (multi-stage), Render

## Features

- Register and log in (JWT, 1-hour expiry)
- Add, view, edit and delete job applications
- Statuses: `APPLIED`, `ONLINE_TEST`, `INTERVIEW`, `OFFER`, `REJECTED`
- Filter applications by status
- Dashboard with total and per-status counts

## Security Highlights

- Passwords hashed with BCrypt; 72-byte limit enforced so long passwords never cause a 500
- Same error message for unknown email and wrong password, plus a dummy BCrypt check (prevents user enumeration and timing attacks)
- JWT subject is the user id; tampered tokens and `alg=none` are rejected
- IDOR prevention: every query is scoped by the user id from the token (`findByIdAndUserId`); other users' records return 404
- DTOs only; entities and password hashes are never exposed
- Frontend uses `textContent` (XSS-safe); only http/https job links are clickable
- Secrets live in environment variables, never in git

## Run Locally

Requirements: JDK 17+, and a PostgreSQL database (e.g. a free Neon project).

1. Clone the repo:
   ```bash
   git clone https://github.com/Thahir-programmer/placement-tracker.git
   cd placement-tracker
   ```
2. Create a `.env` file in the project root (it is git-ignored):
   ```properties
   DB_HOST=your-db-host
   DB_NAME=your-db-name
   DB_USER=your-db-user
   DB_PASSWORD=your-db-password
   JWT_SECRET=a-long-random-string-at-least-32-characters
   ```
3. Start the app:
   ```bash
   ./mvnw spring-boot:run
   ```
   On Windows PowerShell: `.\mvnw.cmd spring-boot:run`
4. Open http://localhost:8080

## API

| Method | Endpoint | Auth | Notes |
|---|---|---|---|
| POST | `/api/auth/register` | Public | 201; 409 duplicate email; 400 validation errors |
| POST | `/api/auth/login` | Public | Returns `{token}`; 401 on bad credentials |
| GET | `/api/auth/me` | JWT | Current user (id, name, email) |
| POST | `/api/applications` | JWT | 201; status defaults to `APPLIED` |
| GET | `/api/applications?status=` | JWT | Newest applied date first |
| GET | `/api/applications/{id}` | JWT | 404 if missing or not yours |
| PUT | `/api/applications/{id}` | JWT | Update |
| DELETE | `/api/applications/{id}` | JWT | 204 |
| GET | `/api/applications/stats` | JWT | `{total, byStatus}` |

Send the token as `Authorization: Bearer <token>`.

## Project Structure

```
src/main/java/com/thahirudeen/placementtracker/
  entity/  repository/  dto/  service/  controller/  config/  exception/
src/main/resources/static/   (index.html, dashboard.html, css/, js/)
```
