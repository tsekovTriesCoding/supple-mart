import { useState } from 'react';
import { useStripe, useElements, PaymentElement } from '@stripe/react-stripe-js';
import { CreditCard, Lock } from 'lucide-react';
import { useMutation } from '@tanstack/react-query';

interface PaymentFormProps {
  onPaymentSuccess: () => Promise<void>;
}

export const PaymentForm = ({ onPaymentSuccess }: PaymentFormProps) => {
  const stripe = useStripe();
  const elements = useElements();
  const [validationError, setValidationError] = useState<string | null>(null);

  const confirmPaymentMutation = useMutation({
    mutationFn: async () => {
      if (!stripe || !elements) {
        throw new Error('Stripe has not loaded yet. Please wait.');
      }

      const { error: stripeError, paymentIntent } = await stripe.confirmPayment({
        elements,
        confirmParams: {
          return_url: `${window.location.origin}/checkout`,
        },
        redirect: 'if_required',
      });

      if (stripeError) {
        throw new Error(stripeError.message || 'Payment failed');
      }

      if (paymentIntent && paymentIntent.status === 'succeeded') {
        return paymentIntent;
      } else {
        throw new Error('Payment was not completed successfully.');
      }
    },
    onSuccess: async () => {
      await onPaymentSuccess();
    }
  });

  const processing = confirmPaymentMutation.isPending;
  const mutationError = confirmPaymentMutation.error as Error | null;
  const error = validationError || (mutationError ? mutationError.message : null);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    if (!stripe || !elements) {
      setValidationError('Stripe has not loaded yet. Please wait.');
      return;
    }

    setValidationError(null);
    confirmPaymentMutation.mutate();
  };

  const isDisabled = !stripe || !elements || processing;

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      <div className="bg-gray-800/50 backdrop-blur-sm rounded-xl p-6 border border-gray-700">
        <div className="flex items-center gap-3 mb-4">
          <div className="w-10 h-10 rounded-lg bg-blue-500/20 flex items-center justify-center">
            <CreditCard className="w-5 h-5 text-blue-400" />
          </div>
          <div>
            <h3 className="text-lg font-semibold text-white">Payment Details</h3>
            <p className="text-sm text-gray-400">Enter your payment information</p>
          </div>
        </div>

        <div className="mt-4">
          <PaymentElement
            options={{
              layout: 'tabs',
            }}
          />
        </div>
      </div>

      {error && (
        <div className="bg-red-500/10 border border-red-500/50 rounded-lg p-4">
          <p className="text-red-400 text-sm">{error}</p>
        </div>
      )}

      <button
        type="submit"
        disabled={isDisabled}
        className="w-full btn-primary flex items-center justify-center gap-2 py-4 text-lg disabled:opacity-50 disabled:cursor-not-allowed"
      >
        {processing ? (
          <>
            <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
            <span>Processing Payment...</span>
          </>
        ) : (
          <>
            <Lock className="w-5 h-5" />
            <span>Pay Securely</span>
          </>
        )}
      </button>

      <div className="flex items-center justify-center gap-4 text-xs text-gray-500">
        <div className="flex items-center gap-1">
          <Lock className="w-3 h-3" />
          <span>Secured by Stripe</span>
        </div>
        <span>•</span>
        <span>PCI DSS Compliant</span>
        <span>•</span>
        <span>256-bit SSL Encryption</span>
      </div>
    </form>
  );
};

export default PaymentForm;
