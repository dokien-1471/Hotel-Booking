# Hotel Management Frontend

This is the frontend application for the Hotel Management System built with React, TypeScript, and Vite.

## Project Structure

```
frontend/
├── public/              # Static files
├── src/
│   ├── assets/         # Images, fonts, and other static assets
│   │   ├── common/    # Common UI components
│   │   ├── layout/    # Layout components
│   │   └── features/  # Feature-specific components
│   ├── config/        # Configuration files
│   ├── hooks/         # Custom React hooks
│   ├── lib/           # Utility functions and constants
│   ├── pages/         # Page components
│   ├── services/      # API services
│   │   ├── api/      # API configuration and instances
│   │   └── features/ # Feature-specific API services
│   ├── store/         # State management
│   ├── styles/        # Global styles
│   ├── types/         # TypeScript type definitions
│   └── App.tsx        # Root component
```

## Features

- 🔐 Authentication (Login/Register)
- 🏨 Room Management
- 📅 Booking System
- 👥 User Management
- 📊 Admin Dashboard

## Tech Stack

- React 18
- TypeScript
- Vite
- TanStack Query (React Query)
- Axios
- Tailwind CSS
- Shadcn UI
- React Hook Form
- Zod (Form Validation)

## Getting Started

1. Install dependencies:
```bash
npm install
```

2. Create a `.env` file in the root directory:
```env
VITE_API_URL=http://localhost:8081
```

3. Start the development server:
```bash
npm run dev
```

## Development Guidelines

1. **Components**
   - Use functional components with TypeScript
   - Follow the feature-first organization
   - Keep components small and focused

2. **API Integration**
   - Use services for API calls
   - Handle errors consistently
   - Use React Query for data fetching

3. **State Management**
   - Use React Query for server state
   - Use Context for global UI state
   - Use local state for component-specific state

4. **Styling**
   - Use Tailwind CSS for styling
   - Follow the design system
   - Keep styles modular

5. **Type Safety**
   - Define types for all data structures
   - Use interfaces for API responses
   - Avoid using 'any' type

## Production Build

```bash
npm run build
```

## Contributing

1. Follow the established project structure
2. Write clean, maintainable code
3. Add appropriate documentation
4. Test your changes thoroughly
