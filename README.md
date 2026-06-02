# Distributed Student Clearance System

A Java-based **Distributed Student Clearance Management System** that automates and manages student clearance requests across multiple university departments using a distributed architecture.

## Features

### Student

* Login securely
* Submit clearance requests
* Track request status
* View department decisions
* Receive real-time notifications
* View officer comments and rejection reasons

### Department Officer

* View pending clearance requests
* Approve requests
* Reject requests
* Add comments and decision reasons
* Send notifications to students
* View department-specific requests

### Administrator

* View all clearance requests
* Monitor system activity
* Manage users
* View clearance statistics

### Distributed System Features

* Socket-based communication
* Multiple department servers
* Real-time notifications
* Centralized database
* Multi-user support

---

## Technology Stack

| Component    | Technology                         |
| ------------ | ---------------------------------- |
| Language     | Java 21                            |
| GUI          | JavaFX                             |
| Database     | MySQL                              |
| Build Tool   | Maven                              |
| Networking   | Java Sockets                       |
| Architecture | Client-Server / Distributed System |

---

## System Architecture

```text
                          +----------------+
                          |    Student     |
                          +--------+-------+
                                   |
                                   v
                          +----------------+
                          |   Main Server  |
                          +--------+-------+
                                   |
     ---------------------------------------------------------
     |                  |                 |                  |
     v                  v                 v                  v

+------------+   +------------+   +-------------+   +-------------+
|  Finance   |   |  Library   |   | Registrar   |   | Dormitory   |
|   Server   |   |   Server   |   |   Server    |   |   Server    |
+------------+   +------------+   +-------------+   +-------------+
     |                  |                 |                  |
     ---------------------------------------------------------
                                   |
                                   v
                          +----------------+
                          |    Database    |
                          +----------------+
                                   |
                                   v
                          +----------------+
                          | Notification   |
                          |    Server      |
                          +----------------+
```

---

## Project Structure

```text
src
└── main
    ├── java
    │   └── com.distributedclearance
    │       ├── database
    │       ├── gui
    │       ├── models
    │       ├── server
    │       ├── services
    │       └── utils
    └── resources
```

---

## Database Tables

### users

Stores:

* Students
* Officers
* Administrators

### clearance_requests

Stores:

* Request information
* Submission date
* Current status

### approvals

Stores:

* Department approvals
* Comments
* Approval status

### notifications

Stores:

* Notification messages
* Student updates

---

## Running the Project

### Clone Repository

```bash
git clone https://github.com/Distributed-Student-Clearance-System/AP_project.git
cd AP_project
```

### 2. Configure MySQL

Update your database settings inside:

```java
Constants.java
```

Configure:

* Database URL
* Username
* Password

---

### 3. Build Project

```bash
mvn clean install
```

---

### 4. Start Notification Server

```bash
mvn exec:java "-Dexec.mainClass=com.distributedclearance.server.networking.NotificationServer"
```

---

### 5. Start Main Server

```bash
mvn exec:java "-Dexec.mainClass=com.distributedclearance.server.networking.MainServer"
```

---

### 6. Start Department Servers

```bash
mvn exec:java "-Dexec.mainClass=com.distributedclearance.server.networking.DepartmentLauncher"
```

---

### 7. Launch Application

```bash
mvn javafx:run
```

---

## Typical Workflow

1. Student logs in.
2. Student submits a clearance request.
3. Request is forwarded to departments.
4. Officers review pending requests.
5. Officers approve or reject requests.
6. Officers add comments when necessary.
7. Notifications are sent to the student.
8. Student dashboard updates with the latest status.

---

## Key Concepts Demonstrated

* Distributed Systems
* Client-Server Architecture
* Socket Programming
* Multi-threading
* DAO Pattern
* JavaFX GUI Development
* MySQL Integration
* Real-Time Notifications
* Role-Based Access Control

---

## Screenshots

### Login Screen

<img src="screenshots/login.png" alt="Description" width="400">

### Student Dashboard

*Add screenshot here*

### Officer Dashboard

*Add screenshot here*

### Admin Dashboard

*Add screenshot here*
