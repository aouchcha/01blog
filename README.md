# 01Blog

## 📌 Overview

**01Blog** is a social blogging platform designed for students to document and share their learning journey. Users can publish posts with media, follow other students, interact through likes and comments, and engage in meaningful discussions. The platform also includes moderation and administration tools to ensure a safe and respectful environment.

This project is a **fullstack application** built with **Java Spring Boot** on the backend and **Angular** on the frontend, following RESTful architecture and modern web development best practices.

---

## 🎯 Project Goals

* Provide a secure and user-friendly blogging platform for students
* Allow users to share progress, discoveries, and experiences
* Enable social interactions (follow, like, comment)
* Give administrators powerful moderation and management tools
* Apply real-world fullstack development concepts

---

## 🧠 Learning Objectives

* Master **Spring Boot** (REST APIs, services, security, authentication)
* Build scalable **Angular** applications (components, routing, services)
* Understand fullstack architecture and API integration
* Handle user-generated content and media uploads
* Design relational databases for social features
* Implement role-based access control (User / Admin)
* Practice collaboration using Git and agile workflows

---

## 🏗️ Architecture

```
Frontend (Angular)
    │
    │ HTTP / JSON (REST API)
    ▼
Backend (Spring Boot)
    │
    ▼
Relational Database (PostgreSQL / MySQL)
```

---

## ⚙️ Technologies Used

### Backend

* Java 17+
* Spring Boot
* Spring Security (JWT authentication)
* Spring Data JPA / Hibernate
* PostgreSQL or MySQL
* Maven

### Frontend

* Angular
* Angular Material or Bootstrap
* TypeScript
* HTML / CSS

### Other Tools

* Git & GitHub / Gitea
* RESTful APIs
* JWT for authentication

---

## ✨ Features

### 👤 Authentication & Authorization

* User registration and login
* Secure password handling
* Role-based access (USER / ADMIN)

### 🧱 User Block (Profile)

* Public profile displaying user posts
* Subscribe / unsubscribe to other users
* Notifications for new posts from subscribed profiles

### 📝 Posts

* Create, edit, and delete posts
* Upload images or videos with preview
* Like and comment on posts
* Display timestamps and engagement metrics

### 🚩 Reports

* Report users for inappropriate content
* Reports include reason and timestamp
* Reports visible only to administrators

### 🛡️ Admin Panel

* View and manage users
* Ban or delete users
* Remove or hide posts
* Handle user reports
* All admin routes protected by access control

---

## 🖥️ Frontend Pages

* Home feed (posts from subscribed users)
* User block (profile & post management)
* View other users’ blocks
* Notifications center
* Admin dashboard (users, posts, reports)

---

## 🚀 How to Run the Project

### 🔧 Backend Setup (Spring Boot)

1. Clone the repository:

   ```bash
   git clone <repository-url>
   cd backend
   ```

2. Configure the database in `application.properties` or `application.yml`:


   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/01blog
   spring.datasource.username=YOUR_USERNAME
   spring.datasource.password=YOUR_PASSWORD
   ```

3. Install postgress and connect with DataBase.

4. Run the application:

   ```bash
   ./mvnw spring-boot:run
   ```

5. Backend will be available at:

   ```
   http://localhost:8080
   ```

---

### 🎨 Frontend Setup (Angular)

1. Navigate to the frontend directory:

   ```bash
   cd frontend
   ```

2. Install dependencies:

   ```bash
   npm install
   ```

3. Run the Angular development server:

   ```bash
   ng serve
   ```

4. Open your browser at:

   ```
   http://localhost:4200
   ```

---

## 🔐 Security

* JWT-based authentication
* Protected routes based on user roles
* Secure password hashing
* Admin-only access for moderation endpoints

---

## 📊 Bonus Features (Optional)

* Real-time notifications using Server Sent Event (SSE)
* Infinite scrolling for feeds
* Admin analytics dashboard

---

👤 Author

Developed for educational purposes by Achraf OUCHCHATE.

🔗 Connect with me:

🔗 **Connect with me:**
- 🐙 GitHub: [aouchcha](https://github.com/aouchcha)
- 💼 LinkedIn: [Achraf Ouchchate](https://www.linkedin.com/in/aouchcha/)

---

## 📄 License

This project is developed for educational purposes .
