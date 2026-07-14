# JustEat - Food Ordering Platform

## 🍔 Project Overview

JustEat is a full-stack food ordering platform similar to Zomato/Swiggy, built with Spring Boot 3 and React.js. The application supports two user roles: **Customers** who can browse restaurants, place orders, and track deliveries, and **Restaurant Owners** who can manage their restaurants, menus, and incoming orders.

## 🏗️ Architecture

### Backend

- **Framework:** Spring Boot 3.2.5
- **Java Version:** 17
- **Security:** JWT Authentication
- **Database:** PostgreSQL
- **ORM:** Spring Data JPA / Hibernate
- **Build Tool:** Maven
- **API Documentation:** Swagger/OpenAPI
- **Testing:** JUnit 5, Mockito

### Frontend

- **Framework:** React 18
- **Build Tool:** Vite
- **Routing:** React Router v6
- **HTTP Client:** Axios
- **Notifications:** React Toastify
- **Styling:** Custom CSS

### Architecture Pattern

- **Clean Layered Architecture:**
  - Controller Layer (REST APIs)
  - Service Layer (Business Logic)
  - Repository Layer (Data Access)
  - Entity Layer (Database Models)
  - DTO Layer (Data Transfer Objects)

## ✨ Features

### Customer Features

1. **Authentication**
   - Register with role selection
   - Login with JWT token
   - Password reset functionality
   - Password complexity validation

2. **Restaurant Browsing**
   - Search restaurants by name, location, or cuisine
   - View restaurant ratings
   - Browse restaurant menus

3. **Shopping Cart**
   - Add items to cart
   - Update quantities
   - Remove items
   - Real-time total calculation

4. **Order Management**
   - Place orders
   - Real-time order tracking (PENDING → PREPARING → READY → COMPLETED)
   - Order history
   - Order confirmation page

5. **Preferences**
   - Save favorite cuisines
   - Set dietary preferences
   - Get personalized restaurant recommendations

### Restaurant Owner Features

1. **Restaurant Management**
   - Create and update restaurants
   - Manage restaurant details (name, location, cuisine, rating)

2. **Menu Management**
   - CRUD operations for menu items
   - Mark items as "Today's Special" or "Deal of Day"
   - Automatic "Most Ordered" flagging based on order frequency
   - Set availability status

3. **Order Management**
   - View incoming orders
   - Update order status
   - Filter orders by status
   - View customer details

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Node.js 18+
- PostgreSQL 14+
- Maven 3.8+
- Docker & Docker Compose (optional)

### Local Setup

#### 1. Database Setup

```sql
CREATE DATABASE justeat_database;
```

#### 2. Backend Setup

```bash
# Navigate to backend directory
cd backend

# Verify database credentials in src/main/resources/application.properties
# spring.datasource.url=jdbc:postgresql://localhost:5432/justeat_database
# spring.datasource.username=postgres
# spring.datasource.password=root

# Build and run
mvn clean install
mvn spring-boot:run
```

Backend will start on: `http://localhost:8082`

Swagger UI: `http://localhost:8082/swagger-ui/index.html`

OpenAPI JSON: `http://localhost:8082/api-docs`

Azure full application url: `https://blue-pebble-0a4711200-preview.eastasia.7.azurestaticapps.net/`

#### 3. Frontend Setup

```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev
```

Frontend will start on: `http://localhost:5173`

### Docker Setup

```bash
# Build and start all services
docker-compose up --build

# Access the application
# Frontend: http://localhost
# Backend: http://localhost:8082
# PostgreSQL: localhost:5432
```

## 📚 Documentation

- Project guide: [README.md](README.md)
- AI usage report: [AI_USAGE.md](AI_USAGE.md)
- Swagger UI (live): `http://localhost:8082/swagger-ui/index.html`
- OpenAPI spec (live): `http://localhost:8082/api-docs`

## 📋 API Documentation

### Authentication APIs

- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user
- `POST /api/auth/reset-password` - Initiate password reset
- `POST /api/auth/reset-password/confirm` - Confirm password reset

### Customer APIs

- `GET /api/restaurants` - Get all restaurants (supports search)
- `GET /api/restaurants/{id}` - Get restaurant by ID
- `GET /api/restaurants/{id}/menu` - Get restaurant menu
- `GET /api/cart` - Get user cart
- `POST /api/cart` - Add item to cart
- `PUT /api/cart/{id}` - Update cart item
- `DELETE /api/cart/{id}` - Remove from cart
- `POST /api/orders` - Place order
- `GET /api/orders/history` - Get order history
- `GET /api/orders/{id}/status` - Get order status
- `GET /api/preferences` - Get customer preferences
- `PUT /api/preferences` - Update preferences
- `GET /api/preferences/recommendations` - Get recommendations

### Restaurant Owner APIs

- `POST /api/restaurants` - Create restaurant
- `PUT /api/restaurants/{id}` - Update restaurant
- `GET /api/restaurants/my-restaurants` - Get owned restaurants
- `POST /api/restaurants/{id}/menu` - Create menu item
- `PUT /api/menu/{id}` - Update menu item
- `DELETE /api/menu/{id}` - Delete menu item
- `GET /api/orders/restaurant` - Get restaurant orders
- `PUT /api/orders/{id}/status` - Update order status

## 🧪 Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AuthServiceTest

# Run with coverage
mvn test jacoco:report
```

Test Coverage:

- AuthService
- RestaurantService
- CartService
- OrderService
- JwtUtil

## 🔒 Security

- **Authentication:** JWT-based authentication
- **Authorization:** Role-based access control (CUSTOMER, RESTAURANT_OWNER)
- **Password Encryption:** BCrypt
- **CORS:** Configured for localhost:5173 and localhost:3000
- **API Protection:** All APIs except login/register require authentication

## 📊 Database Schema

Key Entities:

- **User** - User accounts with roles
- **Restaurant** - Restaurant information
- **MenuItem** - Menu items with special flags
- **Cart / CartItem** - Shopping cart
- **Order / OrderItem** - Orders and line items
- **CustomerPreference** - User preferences
- **PasswordResetToken** - Password reset tokens

## 🌐 Deployment to Azure

### Azure App Service Deployment

1. **Create Azure Resources**

```bash
# Create Resource Group
az group create --name justeat-rg --location eastus

# Create PostgreSQL Database
az postgres flexible-server create \
  --name justeat-postgres \
  --resource-group justeat-rg \
  --admin-user justeatadmin \
  --admin-password YourSecurePassword123! \
  --sku-name Standard_B1ms

# Create App Service Plan
az appservice plan create \
  --name justeat-plan \
  --resource-group justeat-rg \
  --sku B1 \
  --is-linux

# Create Web App for Backend
az webapp create \
  --name justeat-backend \
  --resource-group justeat-rg \
  --plan justeat-plan \
  --runtime "JAVA:17-java17"

# Create Web App for Frontend
az webapp create \
  --name justeat-frontend \
  --resource-group justeat-rg \
  --plan justeat-plan \
  --runtime "NODE:18-lts"
```

2. **Configure Environment Variables**

```bash
az webapp config appsettings set \
  --name justeat-backend \
  --resource-group justeat-rg \
  --settings \
    DATABASE_URL="jdbc:postgresql://justeat-postgres.postgres.database.azure.com:5432/justeat_database" \
    DATABASE_USERNAME="justeatadmin" \
    DATABASE_PASSWORD="YourSecurePassword123!" \
    JWT_SECRET="your-jwt-secret-key"
```

3. **Deploy Backend**

```bash
# Build the application
cd backend
mvn clean package -DskipTests

# Deploy to Azure
az webapp deploy \
  --name justeat-backend \
  --resource-group justeat-rg \
  --src-path target/*.jar \
  --type jar
```

4. **Deploy Frontend**

```bash
# Build the application
cd frontend
npm run build

# Deploy to Azure (using ZIP deployment)
cd dist
zip -r ../dist.zip .
az webapp deploy \
  --name justeat-frontend \
  --resource-group justeat-rg \
  --src-path ../dist.zip \
  --type zip
```

### Using Docker Containers

```bash
# Build and push Docker images
docker build -t justeat-backend ./backend
docker build -t justeat-frontend ./frontend

# Tag for Azure Container Registry
docker tag justeat-backend <your-acr>.azurecr.io/justeat-backend
docker tag justeat-frontend <your-acr>.azurecr.io/justeat-frontend

# Push to ACR
docker push <your-acr>.azurecr.io/justeat-backend
docker push <your-acr>.azurecr.io/justeat-frontend

# Deploy to Azure Web App
az webapp create \
  --name justeat-backend \
  --resource-group justeat-rg \
  --plan justeat-plan \
  --deployment-container-image-name <your-acr>.azurecr.io/justeat-backend
```

## 🎨 UI Screenshots

The application features:

- Modern, responsive design
- Beautiful gradient backgrounds
- Intuitive dashboards with sidebar navigation
- Real-time order status updates
- Professional cards and tables
- Toast notifications for user feedback

## 📁 Project Structure

```
capstone-project/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/justeat/
│   │   │   │   ├── controller/      # REST Controllers
│   │   │   │   ├── service/         # Business Logic
│   │   │   │   ├── repository/      # Data Access
│   │   │   │   ├── entity/          # JPA Entities
│   │   │   │   ├── dto/             # Data Transfer Objects
│   │   │   │   ├── security/        # Security Components
│   │   │   │   ├── config/          # Configuration
│   │   │   │   ├── exception/       # Exception Handling
│   │   │   │   └── util/            # Utilities
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/                    # Unit Tests
│   ├── pom.xml
│   └── Dockerfile
├── frontend/
│   ├── src/
│   │   ├── components/          # Reusable Components
│   │   ├── pages/               # Page Components
│   │   ├── services/            # API Services
│   │   ├── context/             # React Context
│   │   └── App.jsx
│   └── package.json
├── docker-compose.yml
├── AI_USAGE.md
└── README.md
```

## 🛠️ Technologies Used

### Backend

- Spring Boot 3.2.5
- Spring Security
- Spring Data JPA
- JWT (jjwt 0.11.5)
- Lombok
- PostgreSQL JDBC Driver
- Springdoc OpenAPI 2.3.0
- JUnit 5
- Mockito
- H2 (testing)

### Frontend

- React 18
- Vite 5
- React Router 6
- Axios
- React Toastify
- ESLint

### DevOps

- Docker
- Docker Compose
- Maven
- npm

## 👨‍💻 Development Best Practices

- Clean code with proper naming conventions
- Comprehensive logging using SLF4J
- Global exception handling
- Input validation
- Transaction management
- Password complexity validation
- JWT token-based authentication
- Role-based authorization
- Database indexing for performance
- Unit testing with high coverage
- RESTful API design
- Proper error responses
- CORS configuration

## 📝 License

This project is created for educational purposes as a capstone project.
