# 📘 Project Changelog

## API Quick Table

### 🟪 Habit History APIs (Version 0.3.0)

| Endpoint                                     | Method  | Description                         | Params / Body  | Sample Response                      |
| -------------------------------------------- | ------- | ----------------------------------- | -------------- | ------------------------------------ |
| `/api/habits/history/completed?year=current` | **GET** | Last 365 days completed habit count | `year=current` | `{ "2024-12-1": 1, "2024-12-2": 0 }` |
| `/api/habits/history/completed?year=YYYY`    | **GET** | Completed count for full year       | `year=2025`    | `{ "2025-01-01": 2 }`                |

---

### 🟦 Habit APIs (Version 0.2.1)

| Endpoint                  | Method    | Description                 | Params / Body                                        | Sample Response                                                  |
| ------------------------- | --------- | --------------------------- | ---------------------------------------------------- | ---------------------------------------------------------------- |
| `/api/habits`             | **POST**  | Create a habit              | `{ name, description, category, frequency, days[] }` | `{ "status": "CREATED", "message": "Habit added successfully" }` |
| `/api/habits`             | **GET**   | Get all habits of the user  | None                                                 | `{ "status": "OK", "data": [...] }`                              |
| `/api/habits/{habitId}`   | **PUT**   | Update today's habit status | `{ status }`                                         | `{ "status": "OK", "message": "Habit updated successfully" }`    |
| `/api/habits/today`       | **GET**   | Get today’s active habits   | None                                                 | `{ "status": "OK", "data": [...] }`                              |
| `/api/habits/{habitId}`   | **DELETE**| Disable/Delete a habit      | None                                                 | `{ "status": "OK", "message": "Habit deleted successfully" }`    |

---

### 🟩 Authentication APIs (Version 0.1.0)

| Endpoint                       | Method   | Description                       | Params          | Sample Response                              |
| ------------------------------ | -------- | --------------------------------- | --------------- | -------------------------------------------- |
| `/oauth2/authorization/google` | **GET**  | Start Google login                | None            | Redirect                                     |
| `/logout`                      | **POST** | Logout user & delete token cookie | None            | `{ "status": "success" }`                    |
| `/api/auth/me`                 | **GET**  | Fetch current logged-in user      | HttpOnly cookie | `{ "id": 1, "email": "...", "name": "..." }` |

---

---

# 📘 Habit History Module

### Version 0.3.0

### Date: 01-12-2025

## Overview

The HabitHistory module adds analytics APIs for heatmap-style habit progress tracking.
APIs return a `Map<String, Long>` where keys are dates and values represent the count of completed habits.

---

## Added

### 1. Get Completed Habit Count — Last 365 Days

**`GET /api/habits/history/completed?year=current`**

* Fetches completion count from *today → 365 days back*.
* Returns daily completion counts in a date→count map.

**Example Response**

```json
{
  "status": "success",
  "message": "Completed history fetched successfully",
  "data": {
    "2024-12-1": 1,
    "2024-12-2": 0,
    "2024-12-3": 1
  }
}
```

---

### 2. Get Completed Habit Count — Specific Year

**`GET /api/habits/history/completed?year=2025`**

* Fetches daily `COMPLETED` counts for full year.
* Useful for yearly analytics dashboards.

**Example Response**

```json
{
  "status": "success",
  "message": "Completed history fetched successfully",
  "data": {
    "2025-01-01": 2,
    "2025-01-02": 1
  }
}
```

---

### 3. Error Handling

**If no history exists:**

```json
{
  "status": "error",
  "message": "No habit history found for the given year",
  "data": null
}
```

---

---

# 📗 Habit Module

### Version 0.2.0

### Date: 30-11-2025

## Overview

Provides CRUD-style habit management with support for scheduling, streaks, and retrieving today's habits.

---

## Added

### 1. Create Habit API

**`POST /api/habits`**

* Accepts a `HabitCreate` DTO:

    * `name`, `description`, `category`, `frequency`, `days[]`
* Creates:

    * `Habit`
    * `HabitStreak`
    * Optional `HabitHistory` (if active today)

**Response**

```json
{
  "status": "CREATED",
  "message": "Habit added successfully"
}
```

---

### 2. Get All Habits API

**`GET /api/habits`**

* Fetches all authenticated user's habits.
* Returns list of `HabitOut`.

```json
{
  "status": "OK",
  "message": "Fetched user habits successfully",
  "data": [
    {
      "id": 1,
      "name": "Morning Walk",
      "description": "30 min walk",
      "frequency": "DAILY",
      "createdAt": "2025-11-20",
      "habitStatus": "PENDING",
      "lastCompletedAt": "2025-11-29",
      "currentStreak": 4,
      "category": "HEALTH"
    }
  ]
}
```
---

### 3. Update Habit Status API

**`PUT /api/habits`**

* Updates today's habit status:

    * `COMPLETED`
    * `SKIPPED`
    * `PENDING`
* Adjusts streaks and history accordingly.

```json
{
  "status": "OK",
  "message": "Habit updated successfully",
  "data": {
    "id": 1,
    "name": "Morning Walk",
    "description": "30 min walk",
    "frequency": "DAILY",
    "createdAt": "2025-11-20",
    "habitStatus": "COMPLETED",
    "lastCompletedAt": "2025-12-02",
    "currentStreak": 5,
    "category": "HEALTH"
  }
}

```
---

### 4. Get Today’s Habits API

**`GET /api/habits/today`**

* Filters habits by current weekday.
* Returns active habits for today.
```json
{
  "status": "OK",
  "message": "Fetched today habits successfully",
  "data": [
    {
      "id": 2,
      "name": "Drink Water",
      "description": "Track hydration",
      "frequency": "DAILY",
      "createdAt": "2025-10-11",
      "habitStatus": "PENDING",
      "lastCompletedAt": "2025-12-01",
      "currentStreak": 3,
      "category": "WELLNESS"
    }
  ]
}

```
---

### 5. Delete Habit API 

**`DELETE /api/habits/{habitId}`**

* Soft-deletes or disables a habit.
* Removes it from active/today filtering.
* Retains history for analytics.

**Response**
```json
{
  "status": "OK",
  "message": "Habit deleted successfully"
}
```

### 6. Error Handling 

All errors are handled globally using a `@RestControllerAdvice`-based **Global Exception Handler**.

Every error response follows the `ApiResponse` structure:

```json
{
  "status": "<HTTP_STATUS>",
  "message": "<error description>"
}
```

---

# 📙 Authentication Module

### Version 0.1.0

### Date: 28-11-2025

## Overview

Initial security layer integrating Google OAuth2 and JWT authentication stored in HttpOnly cookies.

---

## Added

### 1. Google OAuth2 Authentication

**`GET /oauth2/authorization/google`**

* Starts Google login flow.
* Creates new user or returns existing one.
* Issues authentication token (JWT).

---

### 2. Logout API

**`POST /logout`**

* Logs out the authenticated user.
* Deletes `token` HttpOnly cookie.

---

### 3. Current User API

**`GET /api/auth/me`**

* Returns authenticated user details.
* Identifies user via JWT stored in HttpOnly cookie.

---

## Notes

* First stable version of authentication module.
* Google login fully enabled.
* JWT-based authentication (no session storage).
* Secure HttpOnly cookie used for token.
