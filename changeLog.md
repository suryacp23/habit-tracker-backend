# Habit Module

### Version 0.2.0
### Date: 30-11-2025

## Overview

Initial habit management module providing CRUD-style APIs for creating, fetching, and updating user habits, including support for day-of-week based scheduling and today’s habits retrieval.

---

## Added

### 1. Create Habit API

**`POST /api/habits`**

- Creates a new habit for the authenticated user.
- Accepts a `HabitCreate` DTO with:
    - `name`, `description`, `category`, `frequency` (`DAILY` / `WEEKLY`)
    - `days`: list of active days of week (`MONDAY`..`SUNDAY`)
- Resolves category from DB and associates it with the habit.
- Initializes:
    - `Habit` entity
    - `HabitStreak` entity with `currentStreak = 0`, `longestStreak = 0`
    - Optional `HabitHistory` for today if the habit is active today
- Returns `HabitOut` wrapped in `ApiResponse` with:
    - `status = CREATED`
    - `message`: `"Habit added successfully"`

---

### 2. Get All Habits API

**`GET /api/habits`**

- Fetches all habits for the authenticated user.
- Uses `HabitService.getAllHabits(user)` to retrieve user-specific habits.
- Returns a list of `HabitOut` wrapped in `ApiResponse` with:
    - `status = OK`
    - `message`: `"Fetched user habits successfully"`

---

### 3. Update Habit Status API

**`PUT /api/habits`**

- Updates the current day’s status for a specific habit.
- Accepts:
    - `Habit` reference (to identify habit)
    - `status` (string mapped to `HabitStatus`, e.g. `COMPLETED`, `SKIPPED`, `PENDING`)
- Uses `HabitService.updateTodayHabitStatus(user, status, habit)` to:
    - update / create today’s `HabitHistory`
    - adjust streak information (`HabitStreak`) accordingly
- Returns updated `HabitOut` wrapped in `ApiResponse` with:
    - `status = OK`
    - `message`: `"Habit updated successfully"`

---

### 4. Get Today’s Habits API

**`GET /api/habits/today`**

- Returns habits that are **active for the current day** for the authenticated user.
- Uses `HabitService.getTodayHabits(user)` which:
    - filters habits by `activeDays` (using `DayOfWeek`)
    - returns only habits whose schedule includes today
- Response:
    - List of `HabitOut` for today’s habits
    - Wrapped in `ApiResponse` with:
        - `status = OK`
        - `message`: `"Fetched today habits successfully"`

---

### 5. Error Handling

All habit APIs:

- Extract authenticated `User` from `Authentication.getPrincipal()`.
- On failures (e.g., user not found, invalid input, service errors):
    - Return `ApiResponse` with:
        - `status = BAD_REQUEST`
        - `message` set to the exception message.
- For `GET /api/habits` and `GET /api/habits/today`, response is returned with **400 Bad Request** via `ResponseEntity.badRequest()`.

# Authentication

### Version 0.1.0

### Date: 28-11-2025

## Overview

Initial authentication module setup containing Google OAuth2 integration, session handling, and user identity retrieval.

---

## Added

### 1. **Google OAuth2 Authentication API**

* **`GET /oauth2/authorization/google`**

    * Initiates Google Login / Registration flow.
    * Redirects the user to Google's OAuth2 consent screen.
    * On successful login, the backend creates or uses existing the user and issues an authentication token.

### 2. **Logout API**

* **`POST /logout`**

    * Logs out the currently authenticated user.
    * Deletes authentication cookies (e.g., `token`).
    * Returns a success response.

### 3. **Current User API**

* **`GET /api/auth/me`**

    * Returns the currently authenticated user's details.
    * Reads authentication token (HttpOnly cookie) to identify the user.

---

## Notes

* This is the first stable version of the authentication module.
* Supports jwt token-based authentication using HttpOnly cookies.
* Google OAuth2 is now fully enabled for login and automatic registration.




    