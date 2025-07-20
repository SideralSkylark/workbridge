# **WorkBridge**

**WorkBridge** is a digital platform designed to seamlessly connect service-based entrepreneurs with clients seeking their services. It offers an intuitive, secure environment for discovering, booking, and paying for services.

---

## **Overview**

### **Purpose**

WorkBridge functions as a digital service marketplace that facilitates interactions between service seekers and providers. It simplifies the entire lifecycle of a service transaction — from discovery and communication to booking and secure in-app payments.

---

### **Core Features**

#### **For Service Providers**

* Publish and manage service listings.
* Set pricing and availability.
* Communicate with clients via in-app chat.

#### **For Service Seekers**

* Browse and book available services.
* Cancel bookings as needed.
* Communicate with providers.
* Rate and review completed services.

#### **For Administrators**

* Manage user accounts (enable/disable access).
* Monitor user activity and system interactions.
* Access financial reports and app performance metrics.

---

## **Functional Requirements**

### **1. User Management**

* Users can register, log in, and update their profiles.
* Admins have privileges to manage, disable, or remove accounts.

### **2. Service Listings**

* Providers can create and maintain listings, including titles, descriptions, pricing, and availability.

### **3. Bookings and Cancellations**

* Seekers can book services and cancel when necessary.
* Booking data is stored and managed securely.

### **4. Ratings and Reviews**

* After a service is completed, seekers can rate and review the provider.
* Reviews are visible to other users to ensure service quality transparency.

### **5. Messaging**

* Integrated chat system allows users to communicate directly before and after bookings.

### **6. Payments**

* Secure in-app payment processing for all service transactions.
* Payment records are linked to booking history.

### **7. Administrative Tools**

* Admin dashboard to manage users, review platform usage, and access revenue statistics.

---

## **Setup Instructions**

### **1. Environment Configuration**

#### **1.1 Create ********`.env`******** Files**

At the root of the project, create a `.env` file with the following variables:

```env
DB_URL=jdbc:postgresql://workbridge-db:5432/workbridge_db
DB_USERNAME=your_username
DB_PASSWORD=your_password

EMAIL_USERNAME=your_email_address
EMAIL_PASSWORD=your_email_password
EMAIL_HOST=smtp.gmail.com
EMAIL_PORT=587

JWT_SECRET=your_jwt_secret_key

MINIO_USER=your_minio_user
MINIO_PASSWORD=your_minio_password

MINIO_URL=http://workbridge-minio:9000
MINIO_ACCESS_KEY=your_minio_access_key
MINIO_SECRET_KEY=your_minio_secret_key
MINIO_BUCKET=your_minio_bucket_name
```

---

### **2. Build Docker Containers**

From the project root, run:

```bash
docker build -t workbridge-base:1.0 -f Dockerfile.base .
```

To build the base image followed by:

```bash
docker compose build
```

> You need a .jdk folder in the backend directory if you plan on using the base image, this base image was created to reduce the building time and to keep the java version consistent.

---

### **3. Start Application**

To launch the application in detached mode:

```bash
docker compose up -d
```

> ⚠️ Note: The Angular frontend container is currently not fully configured. If you're developing locally, use the following command after shutting down the frontend container:
>
> ```bash
> ng serve
> ```
>
> If you plan to use the frontend container, make sure to update the environment configuration so it calls the container name (e.g., workbridge-backend) instead of localhost.

---

### **4. Running Backend Tests**

Navigate to the `backend` directory and run:

```bash
mvn test
```

> ⚠️ *Make sure the database container is running and accepting connections before executing tests, as a temporary test database will be initialized.*

---

## **Architecture Diagrams**

### **Class Diagram**
![Class diagram](diagrams/workbridge.jpg)
---

## **Planned Enhancements**

* **Availability Entity**: Will be introduced to manage providers' availability schedules.
* **Category Entity**: Services will be categorized (e.g., Plumbing, Graphic Design) to improve searchability and user navigation.

> These additions will enhance the filtering experience and make it easier for users to find relevant services.
