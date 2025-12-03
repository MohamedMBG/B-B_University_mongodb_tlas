## BBUniversity – Institution Management App

BBUniversity is a management application for any kind of institution (schools, universities, training centers, etc.).  
It supports three main roles: **Student**, **Professor**, and **Admin**, and centralizes account management, classes, timetables, grades, and student absences.

---

## Core Features

- **Roles & Permissions**
  - **Admin**
    - Manages **student** and **professor** accounts (creation, update, activation/deactivation).
    - Manages **classes**, **groups**, and **timetables**.
    - Manages and validates **student absences**.
  - **Professor**
    - Assigns and updates **student grades**.
    - Marks and manages **student absences**.
  - **Student**
    - Views **grades** for each subject.
    - Views their **absence history**.
    - Views **timetables** and class information.

---

## Backend Architecture & Technologies

The backend of BBUniversity is designed with **security**, **scalability**, and **maintainability** in mind.

### Authentication & Security (Firebase)

- **Authentication provider**: **Firebase Authentication**
- **Why Firebase?**
  - Strong, battle‑tested **security model**.
  - Built‑in support for secure **email/password** and other identity providers.
  - Managed infrastructure that reduces the risk of implementing authentication incorrectly.
- **How it’s used**
  - Clients (mobile / web app) authenticate via Firebase and obtain a **Firebase ID token**.
  - The backend verifies this token on each request to ensure the user is **authenticated** and **authorized**.
  - User roles (student / professor / admin) are associated with Firebase users and/or stored in the database, and enforced at the API level.

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
  - Verify **Firebase ID tokens** and extract user identity.
  - Apply **role‑based access control** (RBAC) so only authorized users can:
    - Admin: manage accounts, timetables, classes, absences.
    - Professor: manage grades and student absences for their classes.
    - Student: read‑only access to their own grades, absences, and timetable.
  - Communicate with MongoDB Atlas to perform data operations.
  - Trigger email notifications via SMTP when required (e.g., account events, alerts).

---

## Email Notifications (SMTP)

- **Technology**: **SMTP** (Simple Mail Transfer Protocol)
- **Usage**
  - Send **email notifications** to users (students, professors, or admins).
  - Typical use cases include:
    - Account creation or activation emails.
    - Notification of important updates (e.g., timetable changes, grade publication, absence alerts).
- **Backend integration**
  - Backend uses an SMTP server (or provider) to send transactional emails.
  - Email sending is triggered by backend events (e.g., when an admin creates a new account, or when important academic information changes).

---

## Firebase + MongoDB Integration & Scalability

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

## Project Status

This README describes the **backend architecture and main features** of the BBUniversity management app.  
You can extend it with:
- **Setup instructions** (how to run the backend locally, environment variables, etc.).
- **Deployment details** (production environment, CI/CD).
- **Example API endpoints** and request/response samples.


