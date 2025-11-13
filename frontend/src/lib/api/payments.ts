import { api } from './index';

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

export const paymentsAPI = {
  /**
   * Create a Stripe Payment Intent for an existing order
   * This generates a client secret for the frontend to complete payment
   */
  createPaymentIntent: async (request: PaymentIntentRequest): Promise<PaymentIntentResponse> => {
    const response = await api.post('payments/create-intent', {
      orderId: request.orderId,
      currency: request.currency || 'usd'
    });
    return response.data;
  }
};

export default paymentsAPI;
