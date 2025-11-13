import { useState, useCallback } from 'react';
import { paymentsAPI } from '../lib/api/payments';
import type { PaymentIntentRequest } from '../types/payment';

export const usePaymentIntent = () => {
  const [isCreatingIntent, setIsCreatingIntent] = useState(false);
  const [clientSecret, setClientSecret] = useState<string | null>(null);
  const [paymentIntentId, setPaymentIntentId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const createPaymentIntent = useCallback(async (request: PaymentIntentRequest) => {
    try {
      setIsCreatingIntent(true);
      setError(null);

      const response = await paymentsAPI.createPaymentIntent(request);

      setClientSecret(response.clientSecret);
      setPaymentIntentId(response.paymentIntentId);

      return response;
    } catch (err) {
      const error = err as { response?: { data?: { message?: string } } };
      const errorMessage = error.response?.data?.message || 'Failed to initialize payment';
      setError(errorMessage);
      throw new Error(errorMessage);
    } finally {
      setIsCreatingIntent(false);
    }
  }, []);

  const reset = useCallback(() => {
    setClientSecret(null);
    setPaymentIntentId(null);
    setError(null);
    setIsCreatingIntent(false);
  }, []);

  return {
    clientSecret,
    paymentIntentId,
    isCreatingIntent,
    error,
    createPaymentIntent,
    reset,
  };
};

export default usePaymentIntent;
