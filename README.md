## BBUniversity – Institution Management App

BBUniversity is a **management application for any kind of institution** (schools, universities, training centers, etc.).  
It centralizes **accounts, classes, timetables, grades, and absences** for three roles: **Student**, **Professor**, and **Admin**.

---

## Table of Contents

- **Overview**
- **Core Features**
- **User Roles**
- **Backend Architecture & Technologies**
  - Authentication & Security (Firebase)
  - Data Layer (MongoDB Atlas)
  - API Layer
  - Email Notifications (SMTP)
  - Scalability (Firebase + MongoDB)
- **High‑Level Backend Flow**
- **Tech Stack**
- **Running the Project (Backend)**
- **Environment Configuration**
- **Future Improvements**

---

## Overview

BBUniversity is designed to help institutions **manage students, professors, and academic data** from a single platform.  
The focus is on:

- **Secure authentication** for all users.
- **Clear separation of responsibilities** between student, professor, and admin roles.
- **Scalable backend** that can handle a growing number of institutions and users.

---

## Core Features

- **Account Management**
  - Create, update **student** and **professor** accounts.
- **Class & Timetable Management**
  - Manage **classes**, **groups**, and **timetables** for each academic level.
- **Absence Management**
  - Track, validate, and review **student absences**.
- **Grades Management**
  - Professors assign and update **grades**; students can view them in the app.
- **Notifications**
  - Email notifications for important events (account creation, timetable changes, grades, etc.).

---

## User Roles

| Role      | Main Permissions                                                                 |
|----------|-----------------------------------------------------------------------------------|
| **Admin**    | Manage users (students/professors), classes, timetables, and student absences. |
| **Professor**| Assign/update grades, mark and manage absences for students in their classes.  |
| **Student**  | View grades, absence history, timetable, and class information.                |

Role‑based access is enforced on the backend using **Firebase Authentication** and data stored in **MongoDB**.

---

## Backend Architecture & Technologies

The backend of BBUniversity is designed with **security**, **horizontal scalability**, and **maintainability** in mind.

### Authentication & Security (Firebase)

- **Authentication provider**: **Firebase Authentication**
- **Why Firebase?**
  - Strong, battle‑tested **security model**.
  - Built‑in support for secure **email/password** and other identity providers.
  - Managed infrastructure that reduces the risk of implementing authentication incorrectly.
- **How it’s used**
  - Clients (mobile / web app) authenticate via Firebase and obtain a **Firebase ID token**.
  - Each backend request includes this token in the headers.
  - The backend verifies the token with Firebase to ensure the user is **authenticated** and **authorized**.
  - User roles (student / professor / admin) are associated with Firebase users and/or stored in MongoDB and enforced at the API level.

### Data Layer (MongoDB Atlas)

- **Database**: **MongoDB Atlas** (managed cloud MongoDB)
- **Why MongoDB Atlas?**
  - Fully managed, cloud‑hosted MongoDB service.
  - Easy **horizontal scalability** (sharding, replicas) when the number of users grows.
  - Flexible **document model** suitable for:
    - Users & profiles (students, professors, admins)
    - Classes, groups, and timetables
    - Grades and absences
- **Usage in the project**
  - All domain data (students, professors, classes, grades, absences, notifications, etc.) is stored in MongoDB Atlas.
  - Backend exposes **REST APIs** that read/write these collections:
    - Student / Professor / Admin management
    - Timetable and class management
    - Grades & absences CRUD operations

### API Layer

- **Purpose**
  - Acts as the bridge between the **frontend app** and **MongoDB Atlas** / **Firebase**.
  - Encapsulates business logic and enforces permissions.
- **Key responsibilities**
  - Verify **Firebase ID tokens** and extract user identity and role.
  - Apply **role‑based access control** (RBAC) so only authorized users can:
    - **Admin**: manage accounts, timetables, classes, absences.
    - **Professor**: manage grades and student absences for their classes.
    - **Student**: read‑only access to their own grades, absences, and timetable.
  - Communicate with MongoDB Atlas to perform data operations.
  - Trigger email notifications via SMTP when required (e.g., account events, alerts).

### Email Notifications (SMTP)

- **Technology**: **SMTP** (Simple Mail Transfer Protocol)
- **Usage**
  - Send **email notifications** to users (students, professors, or admins).
  - Typical use cases include:
    - Account creation or activation emails.
    - Notification of timetable changes.
    - Grade publication alerts.
    - Absence alerts or warnings.
- **Backend integration**
  - Backend uses an SMTP server (or provider) to send transactional emails.
  - Email sending is triggered by backend events (for example, when an admin creates a new account or when important academic information changes).

### Firebase + MongoDB Integration & Scalability

- **Integrated design**
  - **Firebase Authentication** handles **identity and security**.
  - **MongoDB Atlas** stores the **application data** linked to each authenticated user.
- **Horizontal scalability**
  - Authentication scales through Firebase’s managed infrastructure.
  - MongoDB Atlas allows scaling horizontally via:
    - **Sharding** across multiple nodes.
    - **Replica sets** for high availability.
  - This combined architecture enables the system to handle **sudden growth in users** without major changes:
    - Authentication load is handled by Firebase.
    - Data load is distributed across MongoDB Atlas clusters.
- **Benefits**
  - Clear separation between **auth** (Firebase) and **data storage** (MongoDB).
  - Easier scaling and maintenance as the number of institutions, students, and professors increases.

---

## High‑Level Backend Flow

1. **User signs in** using Firebase (e.g., email/password).
2. **Client receives a Firebase ID token** after successful authentication.
3. **Client calls backend APIs**, attaching the Firebase token in the request header.
4. **Backend verifies the token** with Firebase and determines the user’s role (student, professor, admin).
5. Backend **executes business logic** and interacts with MongoDB Atlas (for grades, absences, timetables, accounts, etc.).
6. If applicable, backend **sends notification emails** via SMTP.
7. Backend returns the **API response** to the client (app), which updates the UI.

---

## Tech Stack

- **Backend**
  - **Authentication**: Firebase Authentication
  - **Database**: MongoDB Atlas
  - **Email**: SMTP (transactional email notifications)
  - **API Style**: REST APIs (JSON)
- **Frontend / Client**
  - Mobile app (e.g., Android) that consumes the backend APIs and Firebase Authentication.

> **Note:** You can adapt this section to specify your exact backend framework (for example: Node.js/Express, Spring Boot, Laravel, etc.).

---

## Running the Project (Backend)

Because implementations can vary, these are **generic steps** you can adapt to your actual stack:

1. **Clone the repository**
   - `git clone <your-repo-url>`
2. **Configure environment variables**
   - See the **Environment Configuration** section below.
3. **Install dependencies**
   - For example (Node.js): `npm install` or `yarn install` in the backend folder.
4. **Run the backend server**
   - For example (Node.js): `npm run dev` or `npm start`.
5. **Connect the mobile app**
   - Configure the app to point to the backend base URL and connect to the same Firebase project.

Update these steps with exact commands once your backend stack is fixed (Node, Java, etc.).

---

## Environment Configuration

Create an environment file (for example: `.env`) and define variables such as:

- **Firebase**
  - `FIREBASE_PROJECT_ID`
  - `FIREBASE_CLIENT_EMAIL`
  - `FIREBASE_PRIVATE_KEY` (or credentials file path)
- **MongoDB**
  - `MONGODB_URI` (MongoDB Atlas connection string)
  - `MONGODB_DB_NAME`
- **SMTP / Email**
  - `SMTP_HOST`
  - `SMTP_PORT`
  - `SMTP_USER`
  - `SMTP_PASSWORD`
  - `EMAIL_FROM` (default sender address)
- **App**
  - `APP_BASE_URL`
  - Any other configuration specific to your backend framework.

Never commit the real `.env` file to version control. Use a sample file like `.env.example` instead.

---

## Future Improvements

- **More detailed API documentation**
  - Add a full list of endpoints (for example in `docs/` or using Swagger / OpenAPI).
- **Logging & Monitoring**
  - Centralized logs and metrics for requests, errors, and performance.
- **Role / Permission Management UI**
  - Admin interface to manage roles and permissions more dynamically.
- **Multi‑institution support**
  - Better isolation and configuration per institution (branding, domains, etc.).

---

## Project Status

This README describes the **backend architecture and main features** of the BBUniversity management app.  
It is ready to be used as documentation for:

- **Presentations / reports** about the project.
- **New developers** joining the project.
- **Future extensions** of the backend or frontend.
