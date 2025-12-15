# Battleship – Real-Time Multiplayer Game

## 📌 Overview

This project is a **real-time multiplayer Battleship game** developed as a **study and portfolio project**.
The main goal is to practice backend development with **Java and Spring Boot**, real-time communication using **WebSocket**, and frontend integration with **JavaScript**.

The game follows a **client–server architecture**, where a Java backend manages game state and rules, and a web-based frontend handles user interaction.

---

## 🧱 Project Structure

```
battleship/
├── battleship_api/   # Backend (Java + Spring Boot)
├── battleship_app/   # Frontend (HTML, CSS, JavaScript)
└── README.md
```

---

## ⚙️ Technologies Used

### 🔹 Backend

* **Java 17+**
* **Spring Boot**
* **Spring Web** – REST support and server configuration
* **Spring WebSocket** – Real-time bidirectional communication
* **Maven** – Dependency management and build tool
* **Jackson** – JSON serialization/deserialization (via Spring Boot)
* **JUnit / Spring Boot Test** – Unit and integration testing

### 🔹 Frontend

* **HTML5**
* **CSS3**
* **JavaScript (ES6+)**
* **WebSocket API (Browser-native)**

*(No frontend framework is required initially; the project starts with vanilla JavaScript.)*

---

## 🔌 Communication Model

* Clients connect to the backend using **WebSocket**
* Messages are exchanged in **JSON format**
* The backend is the **single source of truth** for:

    * Game state
    * Turn control
    * Valid moves

This prevents cheating and keeps game logic centralized.

---

## 🎮 Game Features (Planned)

* Create and join game rooms
* Two-player matches
* Ship placement phase
* Turn-based attacks
* Hit / miss validation
* Win condition detection
* Real-time updates for both players

---

## 🔐 Security (Planned)

The initial version does not require authentication.
However, the project is designed to evolve with:

* Player session identification
* WebSocket session validation
* Optional integration with **Spring Security** for:

    * Player identity
    * Room access control
    * Future ranking system

---

## 🧪 Testing Strategy

* Unit tests for game rules and services
* Integration tests for controllers and WebSocket endpoints
* Focus on backend reliability and consistency

---

## 🚀 How to Run the Project

### Backend

```bash
cd battleship_api
mvn spring-boot:run
```

The backend will start on:

```
http://localhost:8080
```

### Frontend

Open the `index.html` file inside `battleship_app` in your browser.

*(In later stages, the frontend may be served by a development server.)*

---

## 🗺 Roadmap

* [x] Project setup and repository structure
* [x] Spring Boot configuration
* [ ] WebSocket configuration
* [ ] Game domain modeling
* [ ] Turn-based game logic
* [ ] Frontend UI
* [ ] Multiplayer room system
* [ ] Optional authentication

---

## 📚 Learning Goals

This project focuses on:

* Clean project structure
* Backend-driven game logic
* Real-time communication
* Separation of concerns
* Practical use of WebSocket in Java

---

## 📄 License

This project is intended for **educational purposes** and personal learning.
