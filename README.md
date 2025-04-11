# Hotel Booking and Management System

This is a Spring Boot application for hotel booking and management. It provides a RESTful API for managing users, rooms, and bookings.

## Features

- User management (registration, authentication, profile management)
- Room management (add, update, delete rooms)
- Booking management (create, update, cancel bookings)
- JWT-based authentication
- Role-based authorization
- File upload for room photos

## Technologies Used

- Java 17
- Spring Boot 3.x
- Spring Security with JWT
- Spring Data JPA
- MySQL Database
- Maven

## Getting Started

### Prerequisites

- Java 17 or higher
- MySQL 8.0 or higher
- Maven

### Database Setup

1. Create a MySQL database named `hotel_db`
2. Update the database configuration in `application.properties` if needed

### Running the Application

```bash
# Clone the repository (if you haven't already)
# Navigate to the project directory
cd hotel

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on port 4040 by default.

## API Endpoints

### Authentication

- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Login and get JWT token

### Users

- `GET /api/users` - Get all users (admin only)
- `GET /api/users/{id}` - Get user by ID
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

### Rooms

- `GET /api/rooms` - Get all rooms
- `GET /api/rooms/{id}` - Get room by ID
- `GET /api/rooms/available` - Get all available rooms
- `GET /api/rooms/type/{roomType}` - Get rooms by type
- `POST /api/rooms` - Add a new room (admin only)
- `PUT /api/rooms/{id}` - Update room (admin only)
- `DELETE /api/rooms/{id}` - Delete room (admin only)

### Bookings

- `GET /api/bookings` - Get all bookings (admin only)
- `GET /api/bookings/{id}` - Get booking by ID
- `GET /api/bookings/user/{userId}` - Get bookings by user ID
- `GET /api/bookings/room/{roomId}` - Get bookings by room ID
- `GET /api/bookings/status/{status}` - Get bookings by status
- `GET /api/bookings/reference/{reference}` - Get booking by reference
- `POST /api/bookings` - Create a new booking
- `PUT /api/bookings/{id}` - Update booking
- `PATCH /api/bookings/{id}/status` - Update booking status
- `DELETE /api/bookings/{id}` - Delete booking

### File Upload

- `POST /api/uploads/room-photo` - Upload a room photo
- `DELETE /api/uploads/room-photo/{fileName}` - Delete a room photo

## Security

The application uses JWT (JSON Web Token) for authentication. To access protected endpoints, include the JWT token in the Authorization header:

```
Authorization: Bearer <your_jwt_token>
```

## License

This project is licensed under the MIT License.
