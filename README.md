# WorkBridge

workbridge is an app designed to connect servic ebased entrepreneurs and service seekers in a seemless way. 

---

## Overview

---

### Purpose
This app provides a digital sollution for connecting service seekers and providers, and allow them to conduct ther buissness in a simple way, allowing for secure payments through the app.

### Core functionalities

- List your services as a service provider
- Book services as a regular user
- cancel booked services as a regular user
- atribute reviews to service providers
- chat with the service provider for more details
- enable and disable accounts as an admin
- see activity history of both service providers and seekers
- have a detailed brakdown of revenue from the app.


### Functional requirements

---

## Setup instructions

---

1. **Create `.env` file**:
   At the root of the project, create a `.env` file with the following content:
   ```env
   DB_URL=jdbc:postgresql://workbridge-db:5432/workbridge_db
   DB_USERNAME=your_username
   DB_PASSWORD=your_password
   ```
2. **Build Docker containers**:
   From the root directory, build the Docker containers:
   ```bash
   docker compose build
   ```

3. **Start the app in detached mode**:
   ```bash
   docker compose up -d
   ```
---

## Diagrams

### Class Diagram