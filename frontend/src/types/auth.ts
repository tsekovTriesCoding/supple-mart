export type UserRole = 'CUSTOMER' | 'ADMIN';
export type AuthProvider = 'LOCAL' | 'GOOGLE' | 'GITHUB';

export interface User {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  role: UserRole;
  authProvider?: AuthProvider;
  imageUrl?: string;
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

export interface OAuth2Provider {
  name: string;
  displayName: string;
  authorizationUrl: string;
}

export interface OAuth2ProvidersResponse {
  providers: OAuth2Provider[];
  enabled: boolean;
}
