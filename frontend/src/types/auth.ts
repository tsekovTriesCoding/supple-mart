export type UserRole = 'CUSTOMER' | 'ADMIN';
export interface User {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  role: UserRole;
  name?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface UserData {
  id?: string;
  name?: string;
  firstName?: string;
  lastName?: string;
  email?: string;
}

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

export interface AuthResponse {
  token: string;
  user: User;
  expiresIn?: number;
}

export interface RefreshTokenResponse {
  token: string;
  expiresIn?: number;
}
