import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen } from '../test/test-utils';

import { ProtectedRoute } from './ProtectedRoute';

vi.mock('../hooks', () => ({
  useIsAuthenticated: vi.fn(),
}));

import { useIsAuthenticated } from '../hooks';

const mockedUseIsAuthenticated = vi.mocked(useIsAuthenticated);

describe('ProtectedRoute', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
  });

  it('redirects to home when not authenticated', () => {
    mockedUseIsAuthenticated.mockReturnValue(false);

    render(
      <ProtectedRoute>
        <div>Protected Content</div>
      </ProtectedRoute>
    );

    expect(screen.queryByText('Protected Content')).not.toBeInTheDocument();
  });

  it('redirects when authenticated but no user in localStorage', () => {
    mockedUseIsAuthenticated.mockReturnValue(true);

    render(
      <ProtectedRoute>
        <div>Protected Content</div>
      </ProtectedRoute>
    );

    expect(screen.queryByText('Protected Content')).not.toBeInTheDocument();
  });

  it('renders children when authenticated with user data', () => {
    mockedUseIsAuthenticated.mockReturnValue(true);
    localStorage.setItem('user', JSON.stringify({ id: '1', role: 'USER' }));

    render(
      <ProtectedRoute>
        <div>Protected Content</div>
      </ProtectedRoute>
    );

    expect(screen.getByText('Protected Content')).toBeInTheDocument();
  });

  it('redirects when user role does not match required role', () => {
    mockedUseIsAuthenticated.mockReturnValue(true);
    localStorage.setItem('user', JSON.stringify({ id: '1', role: 'USER' }));

    render(
      <ProtectedRoute requiredRole="ADMIN">
        <div>Admin Content</div>
      </ProtectedRoute>
    );

    expect(screen.queryByText('Admin Content')).not.toBeInTheDocument();
  });

  it('renders children when user role matches required role', () => {
    mockedUseIsAuthenticated.mockReturnValue(true);
    localStorage.setItem('user', JSON.stringify({ id: '1', role: 'ADMIN' }));

    render(
      <ProtectedRoute requiredRole="ADMIN">
        <div>Admin Content</div>
      </ProtectedRoute>
    );

    expect(screen.getByText('Admin Content')).toBeInTheDocument();
  });

  it('redirects when user data is invalid JSON', () => {
    mockedUseIsAuthenticated.mockReturnValue(true);
    localStorage.setItem('user', 'invalid-json');

    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

    render(
      <ProtectedRoute requiredRole="ADMIN">
        <div>Admin Content</div>
      </ProtectedRoute>
    );

    expect(screen.queryByText('Admin Content')).not.toBeInTheDocument();
    expect(consoleSpy).toHaveBeenCalled();

    consoleSpy.mockRestore();
  });

  it('allows access without role check when no requiredRole specified', () => {
    mockedUseIsAuthenticated.mockReturnValue(true);
    localStorage.setItem('user', JSON.stringify({ id: '1', role: 'USER' }));

    render(
      <ProtectedRoute>
        <div>User Content</div>
      </ProtectedRoute>
    );

    expect(screen.getByText('User Content')).toBeInTheDocument();
  });
});
