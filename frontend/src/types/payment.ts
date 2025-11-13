export interface PaymentIntentRequest {
  orderId: string;
  currency?: string;
}

export interface PaymentIntentResponse {
  clientSecret: string;
  paymentIntentId: string;
  amount: number;
  currency: string;
  status: string;
}

export interface PaymentMethodData {
  type: 'card';
  card?: {
    number: string;
    expMonth: number;
    expYear: number;
    cvc: string;
  };
}

export interface PaymentState {
  isProcessing: boolean;
  error: string | null;
  clientSecret: string | null;
  paymentIntentId: string | null;
  succeeded: boolean;
}

export type PaymentStatus = 
  | 'idle'
  | 'creating-intent'
  | 'ready-for-payment'
  | 'processing-payment'
  | 'succeeded'
  | 'failed';
