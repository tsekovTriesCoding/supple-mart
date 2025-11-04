// User role type
export type UserRole = 'CUSTOMER' | 'ADMIN';

// User types
export interface User {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  role: UserRole;
  name?: string; // Computed full name or display name
  createdAt?: string;
  updatedAt?: string;
}

// Legacy UserData interface (for backward compatibility)
export interface UserData {
  id?: string;
  name?: string;
  firstName?: string;
  lastName?: string;
  email?: string;
}

// Authentication form types
export interface LoginForm {
  email: string;
  password: string;
}

export interface RegisterForm {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  confirmPassword: string;
}

// Auth API request types
export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
}

// Auth API response types
export interface AuthResponse {
  token: string;
  user: User;
  expiresIn?: number;
}

export interface RefreshTokenResponse {
  token: string;
  expiresIn?: number;
}