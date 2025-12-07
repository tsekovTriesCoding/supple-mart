import { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { useMutation, useQuery } from '@tanstack/react-query';
import { X, Eye, EyeOff, Mail, Lock, User } from 'lucide-react';
import toast from 'react-hot-toast';

import { authAPI } from '../lib/api';
import type { LoginForm, RegisterForm, OAuth2Provider } from '../types/auth';

const GoogleIcon = () => (
  <svg className="w-5 h-5" viewBox="0 0 24 24">
    <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
    <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
    <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
    <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
  </svg>
);

const GitHubIcon = () => (
  <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
    <path fillRule="evenodd" clipRule="evenodd" d="M12 2C6.477 2 2 6.477 2 12c0 4.42 2.865 8.17 6.839 9.49.5.092.682-.217.682-.482 0-.237-.008-.866-.013-1.7-2.782.604-3.369-1.34-3.369-1.34-.454-1.156-1.11-1.464-1.11-1.464-.908-.62.069-.608.069-.608 1.003.07 1.531 1.03 1.531 1.03.892 1.529 2.341 1.087 2.91.831.092-.646.35-1.086.636-1.336-2.22-.253-4.555-1.11-4.555-4.943 0-1.091.39-1.984 1.029-2.683-.103-.253-.446-1.27.098-2.647 0 0 .84-.269 2.75 1.025A9.578 9.578 0 0112 6.836c.85.004 1.705.114 2.504.336 1.909-1.294 2.747-1.025 2.747-1.025.546 1.377.203 2.394.1 2.647.64.699 1.028 1.592 1.028 2.683 0 3.842-2.339 4.687-4.566 4.935.359.309.678.919.678 1.852 0 1.336-.012 2.415-.012 2.743 0 .267.18.578.688.48C19.138 20.167 22 16.418 22 12c0-5.523-4.477-10-10-10z"/>
  </svg>
);

interface AuthModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess?: () => void;
}

type AuthMode = 'login' | 'register';

const AuthModal = ({ isOpen, onClose, onSuccess }: AuthModalProps) => {
  const [authMode, setAuthMode] = useState<AuthMode>('login');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  // Fetch OAuth2 providers
  const { data: oauth2Data } = useQuery({
    queryKey: ['oauth2-providers'],
    queryFn: authAPI.getOAuth2Providers,
    staleTime: 5 * 60 * 1000,
    retry: false, // Don't retry if OAuth2 is not configured
  });

  const loginForm = useForm<LoginForm>({
    defaultValues: {
      email: '',
      password: '',
    },
  });

  const registerForm = useForm<RegisterForm>({
    defaultValues: {
      firstName: '',
      lastName: '',
      email: '',
      password: '',
      confirmPassword: '',
    },
  });

  const loginMutation = useMutation({
    mutationFn: (data: LoginForm) => authAPI.login(data.email, data.password),
    onSuccess: (response) => {
      localStorage.setItem('token', response.accessToken);
      localStorage.setItem('refreshToken', response.refreshToken);
      localStorage.setItem('user', JSON.stringify(response.user));
      
      toast.success(`Welcome back, ${response.user.firstName}!`);
      onSuccess?.();
      onClose();
      window.location.reload();
    },
    onError: (error) => {
      toast.error(getErrorMessage(error));
    },
  });

  const registerMutation = useMutation({
    mutationFn: (data: RegisterForm) => authAPI.register({
      firstName: data.firstName,
      lastName: data.lastName,
      email: data.email,
      password: data.password,
    }),
    onSuccess: (response) => {
      localStorage.setItem('token', response.accessToken);
      localStorage.setItem('refreshToken', response.refreshToken);
      localStorage.setItem('user', JSON.stringify(response.user));
      
      toast.success(`Welcome, ${response.user.firstName}! Account created successfully.`);
      onSuccess?.();
      onClose();
      window.location.reload();
    },
    onError: (error) => {
      toast.error(getErrorMessage(error));
    },
  });

  const isLoading = loginMutation.isPending || registerMutation.isPending;

  useEffect(() => {
    if (!isOpen) {
      setAuthMode('login');
      setShowPassword(false);
      setShowConfirmPassword(false);
      loginForm.reset();
      registerForm.reset();
    }
  }, [isOpen, loginForm, registerForm]);

  const switchMode = () => {
    setAuthMode(authMode === 'login' ? 'register' : 'login');
    setShowPassword(false);
    setShowConfirmPassword(false);
  };

  useEffect(() => {
    const handleEscape = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };

    if (isOpen) {
      document.addEventListener('keydown', handleEscape);
      document.body.style.overflow = 'hidden';
    }

    return () => {
      document.removeEventListener('keydown', handleEscape);
      document.body.style.overflow = 'unset';
    };
  }, [isOpen, onClose]);

  const handleLogin = async (data: LoginForm) => {
    loginMutation.mutate(data);
  };

  const handleRegister = async (data: RegisterForm) => {
    if (data.password !== data.confirmPassword) {
      registerForm.setError('confirmPassword', {
        type: 'manual',
        message: 'Passwords do not match',
      });
      return;
    }
    registerMutation.mutate(data);
  };

  const handleOAuth2Login = (provider: OAuth2Provider) => {
    // Redirect to OAuth2 authorization endpoint
    window.location.href = authAPI.getOAuth2AuthorizationUrl(provider.name);
  };

  const getProviderIcon = (providerName: string) => {
    switch (providerName.toLowerCase()) {
      case 'google':
        return <GoogleIcon />;
      case 'github':
        return <GitHubIcon />;
      default:
        return null;
    }
  };

  const getProviderButtonStyle = (providerName: string) => {
    switch (providerName.toLowerCase()) {
      case 'google':
        return 'bg-white hover:bg-gray-100 text-gray-800 border border-gray-300';
      case 'github':
        return 'bg-gray-800 hover:bg-gray-700 text-white border border-gray-600';
      default:
        return 'bg-gray-700 hover:bg-gray-600 text-white';
    }
  };

  const getErrorMessage = (err: unknown): string => {
    const axiosError = err as {
      response?: { 
        data?: { 
          message?: string;
          errors?: Record<string, string>;
        } 
      };
    };

    if (axiosError.response?.data?.errors) {
      const validationErrors = axiosError.response.data.errors;
      return Object.values(validationErrors).join('\n');
    } else if (axiosError.response?.data?.message) {
      return axiosError.response.data.message;
    }
    return authMode === 'login' ? 'Login failed. Please try again.' : 'Registration failed. Please try again.';
  };

  if (!isOpen) return null;

  const hasOAuth2Providers = oauth2Data?.enabled && oauth2Data.providers.length > 0;

  return (
    <>
      <div 
        className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4"
        onClick={onClose}
      >
        <div 
          className="bg-gray-900 rounded-2xl p-8 w-full max-w-md border border-gray-700"
          onClick={(e) => e.stopPropagation()}
        >
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-2xl font-bold text-white">
              {authMode === 'login' ? 'Welcome Back' : 'Create Account'}
            </h2>
            <button
              onClick={onClose}
              className="p-2 hover:bg-gray-800 rounded-lg transition-colors cursor-pointer"
            >
              <X className="w-5 h-5 text-gray-400" />
            </button>
          </div>

          {hasOAuth2Providers && (
            <>
              <div className="space-y-3 mb-6">
                {oauth2Data.providers.map((provider) => (
                  <button
                    key={provider.name}
                    onClick={() => handleOAuth2Login(provider)}
                    disabled={isLoading}
                    className={`w-full flex items-center justify-center gap-3 py-3 px-4 rounded-lg font-medium transition-colors ${getProviderButtonStyle(provider.name)} disabled:opacity-50 disabled:cursor-not-allowed`}
                  >
                    {getProviderIcon(provider.name)}
                    <span>Continue with {provider.displayName}</span>
                  </button>
                ))}
              </div>

              <div className="relative mb-6">
                <div className="absolute inset-0 flex items-center">
                  <div className="w-full border-t border-gray-700"></div>
                </div>
                <div className="relative flex justify-center text-sm">
                  <span className="px-4 bg-gray-900 text-gray-400">or continue with email</span>
                </div>
              </div>
            </>
          )}

          {authMode === 'login' && (
            <form onSubmit={loginForm.handleSubmit(handleLogin)} className="space-y-4">
              <div>
                <label className="block text-gray-300 text-sm font-medium mb-2">
                  Email Address
                </label>
                <div className="relative">
                  <Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                  <input
                    {...loginForm.register('email', { 
                      required: 'Email is required',
                      pattern: {
                        value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                        message: 'Please enter a valid email address'
                      }
                    })}
                    type="email"
                    className="input auth-input w-full"
                    style={{ paddingLeft: '3rem' }}
                    placeholder="Enter your email"
                    disabled={isLoading}
                  />
                </div>
                {loginForm.formState.errors.email && (
                  <p className="text-red-400 text-sm mt-1">
                    {loginForm.formState.errors.email.message}
                  </p>
                )}
              </div>
              <div>
                <label className="block text-gray-300 text-sm font-medium mb-2">
                  Password
                </label>
                <div className="relative">
                  <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                  <input
                    {...loginForm.register('password', { 
                      required: 'Password is required',
                      minLength: {
                        value: 6,
                        message: 'Password must be at least 6 characters'
                      }
                    })}
                    type={showPassword ? 'text' : 'password'}
                    className="input auth-input password-input w-full"
                    style={{ paddingLeft: '3rem', paddingRight: '3rem' }}
                    placeholder="Enter your password"
                    disabled={isLoading}
                    autoComplete="current-password"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-300 cursor-pointer"
                    disabled={isLoading}
                  >
                    {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                  </button>
                </div>
                {loginForm.formState.errors.password && (
                  <p className="text-red-400 text-sm mt-1">
                    {loginForm.formState.errors.password.message}
                  </p>
                )}
              </div>
              <button
                type="submit"
                disabled={isLoading}
                className="btn-primary w-full"
              >
                {isLoading ? 'Signing In...' : 'Sign In'}
              </button>
            </form>
          )}

          {authMode === 'register' && (
            <form onSubmit={registerForm.handleSubmit(handleRegister)} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-gray-300 text-sm font-medium mb-2">
                    First Name
                  </label>
                  <div className="relative">
                    <User className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                    <input
                      {...registerForm.register('firstName', { 
                        required: 'First name is required'
                      })}
                      type="text"
                      className="input auth-input w-full"
                      style={{ paddingLeft: '3rem' }}
                      placeholder="First name"
                      disabled={isLoading}
                    />
                  </div>
                  {registerForm.formState.errors.firstName && (
                    <p className="text-red-400 text-sm mt-1">
                      {registerForm.formState.errors.firstName.message}
                    </p>
                  )}
                </div>

                <div>
                  <label className="block text-gray-300 text-sm font-medium mb-2">
                    Last Name
                  </label>
                  <input
                    {...registerForm.register('lastName', { 
                      required: 'Last name is required'
                    })}
                    type="text"
                    className="input auth-input w-full"
                    placeholder="Last name"
                    disabled={isLoading}
                  />
                  {registerForm.formState.errors.lastName && (
                    <p className="text-red-400 text-sm mt-1">
                      {registerForm.formState.errors.lastName.message}
                    </p>
                  )}
                </div>
              </div>
              <div>
                <label className="block text-gray-300 text-sm font-medium mb-2">
                  Email Address
                </label>
                <div className="relative">
                  <Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                  <input
                    {...registerForm.register('email', { 
                      required: 'Email is required',
                      pattern: {
                        value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                        message: 'Please enter a valid email address'
                      }
                    })}
                    type="email"
                    className="input auth-input w-full"
                    style={{ paddingLeft: '3rem' }}
                    placeholder="Enter your email"
                    disabled={isLoading}
                  />
                </div>
                {registerForm.formState.errors.email && (
                  <p className="text-red-400 text-sm mt-1">
                    {registerForm.formState.errors.email.message}
                  </p>
                )}
              </div>
              <div>
                <label className="block text-gray-300 text-sm font-medium mb-2">
                  Password
                </label>
                <div className="relative">
                  <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                  <input
                    {...registerForm.register('password', { 
                      required: 'Password is required',
                      minLength: {
                        value: 6,
                        message: 'Password must be at least 6 characters'
                      }
                    })}
                    type={showPassword ? 'text' : 'password'}
                    className="input auth-input password-input w-full"
                    style={{ paddingLeft: '3rem', paddingRight: '3rem' }}
                    placeholder="Create a password"
                    disabled={isLoading}
                    autoComplete="new-password"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-300 cursor-pointer"
                    disabled={isLoading}
                  >
                    {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                  </button>
                </div>
                {registerForm.formState.errors.password && (
                  <p className="text-red-400 text-sm mt-1">
                    {registerForm.formState.errors.password.message}
                  </p>
                )}
              </div>
              <div>
                <label className="block text-gray-300 text-sm font-medium mb-2">
                  Confirm Password
                </label>
                <div className="relative">
                  <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
                  <input
                    {...registerForm.register('confirmPassword', { 
                      required: 'Please confirm your password'
                    })}
                    type={showConfirmPassword ? 'text' : 'password'}
                    className="input auth-input password-input w-full"
                    style={{ paddingLeft: '3rem', paddingRight: '3rem' }}
                    placeholder="Confirm your password"
                    disabled={isLoading}
                    autoComplete="new-password"
                  />
                  <button
                    type="button"
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                    className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-300 cursor-pointer"
                    disabled={isLoading}
                  >
                    {showConfirmPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                  </button>
                </div>
                {registerForm.formState.errors.confirmPassword && (
                  <p className="text-red-400 text-sm mt-1">
                    {registerForm.formState.errors.confirmPassword.message}
                  </p>
                )}
              </div>
              <button
                type="submit"
                disabled={isLoading}
                className="btn-primary w-full"
              >
                {isLoading ? 'Creating Account...' : 'Create Account'}
              </button>
            </form>
          )}

          <div className="mt-6 text-center">
            <p className="text-gray-400 text-sm">
              {authMode === 'login' ? (
                <>
                  Don't have an account?{' '}
                  <button
                    onClick={switchMode}
                    className="text-blue-400 hover:text-blue-300 font-medium cursor-pointer"
                    disabled={isLoading}
                  >
                    Sign up
                  </button>
                </>
              ) : (
                <>
                  Already have an account?{' '}
                  <button
                    onClick={switchMode}
                    className="text-blue-400 hover:text-blue-300 font-medium cursor-pointer"
                    disabled={isLoading}
                  >
                    Sign in
                  </button>
                </>
              )}
            </p>
          </div>
        </div>
      </div>
    </>
  );
};

export default AuthModal;
