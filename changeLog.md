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




