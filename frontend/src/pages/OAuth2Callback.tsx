import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Loader2, CheckCircle, XCircle } from 'lucide-react';
import toast from 'react-hot-toast';

import { authAPI } from '../lib/api';

/**
 * OAuth2 Callback Page
 * Handles the redirect from OAuth2 providers after authentication.
 * Extracts tokens from URL params and stores them in localStorage.
 */
const OAuth2Callback = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading');
  const [message, setMessage] = useState('Processing authentication...');

  useEffect(() => {
    const handleCallback = () => {
      const token = searchParams.get('token');
      const refreshToken = searchParams.get('refreshToken');
      const userParam = searchParams.get('user');
      const error = searchParams.get('error');

      if (error) {
        setStatus('error');
        setMessage(decodeURIComponent(error));
        toast.error(`Authentication failed: ${decodeURIComponent(error)}`);
        
        setTimeout(() => {
          navigate('/', { replace: true });
        }, 3000);
        return;
      }

      if (token && refreshToken && userParam) {
        try {
          authAPI.handleOAuth2Callback(token, refreshToken);

          const user = JSON.parse(decodeURIComponent(userParam));
          localStorage.setItem('user', JSON.stringify(user));

          setStatus('success');
          setMessage('Authentication successful! Redirecting...');
          toast.success(`Welcome, ${user.firstName}!`);

          setTimeout(() => {
            window.location.href = '/';
          }, 1500);
        } catch (err) {
          console.error('Error processing OAuth2 callback:', err);
          setStatus('error');
          setMessage('Failed to complete authentication. Please try again.');
          
          localStorage.removeItem('token');
          localStorage.removeItem('refreshToken');
          
          setTimeout(() => {
            navigate('/', { replace: true });
          }, 3000);
        }
      } else {
        setStatus('error');
        setMessage('Invalid callback. Missing authentication data.');
        
        setTimeout(() => {
          navigate('/', { replace: true });
        }, 3000);
      }
    };

    handleCallback();
  }, [searchParams, navigate]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-950">
      <div className="bg-gray-900 rounded-2xl p-8 w-full max-w-md border border-gray-700 text-center">
        {status === 'loading' && (
          <>
            <Loader2 className="w-16 h-16 text-blue-500 animate-spin mx-auto mb-4" />
            <h2 className="text-xl font-semibold text-white mb-2">Authenticating</h2>
            <p className="text-gray-400">{message}</p>
          </>
        )}

        {status === 'success' && (
          <>
            <CheckCircle className="w-16 h-16 text-green-500 mx-auto mb-4" />
            <h2 className="text-xl font-semibold text-white mb-2">Success!</h2>
            <p className="text-gray-400">{message}</p>
          </>
        )}

        {status === 'error' && (
          <>
            <XCircle className="w-16 h-16 text-red-500 mx-auto mb-4" />
            <h2 className="text-xl font-semibold text-white mb-2">Authentication Failed</h2>
            <p className="text-gray-400 mb-4">{message}</p>
            <button
              onClick={() => navigate('/', { replace: true })}
              className="btn-primary"
            >
              Return to Home
            </button>
          </>
        )}
      </div>
    </div>
  );
};

export default OAuth2Callback;
