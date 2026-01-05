export type ShippingMethod = 'standard' | 'express' | 'overnight';

export interface ShippingOption {
  id: ShippingMethod;
  name: string;
  description: string;
  price: number;
  estimatedDays: string;
  isFree?: boolean;
}

export const FREE_SHIPPING_THRESHOLD = 50;

const SHIPPING_PRICES = {
  standard: 5.99,
  express: 12.99,
  overnight: 24.99,
} as const;

/**
 * Get all available shipping options based on cart subtotal
 */
export const getShippingOptions = (subtotal: number): ShippingOption[] => {
  const standardIsFree = subtotal >= FREE_SHIPPING_THRESHOLD;

  return [
    {
      id: 'standard',
      name: 'Standard Shipping',
      description: standardIsFree 
        ? 'Free shipping on orders over $50!' 
        : `Free on orders over $${FREE_SHIPPING_THRESHOLD}`,
      price: standardIsFree ? 0 : SHIPPING_PRICES.standard,
      estimatedDays: '5-7 business days',
      isFree: standardIsFree,
    },
    {
      id: 'express',
      name: 'Express Shipping',
      description: 'Faster delivery for your order',
      price: SHIPPING_PRICES.express,
      estimatedDays: '2-3 business days',
    },
    {
      id: 'overnight',
      name: 'Overnight Shipping',
      description: 'Get it by tomorrow',
      price: SHIPPING_PRICES.overnight,
      estimatedDays: '1 business day',
    },
  ];
};

/**
 * Get shipping cost for a specific method
 */
export const getShippingCost = (method: ShippingMethod, subtotal: number): number => {
  if (method === 'standard' && subtotal >= FREE_SHIPPING_THRESHOLD) {
    return 0;
  }
  return SHIPPING_PRICES[method];
};

export const getDefaultShippingMethod = (): ShippingMethod => 'standard';

export const getEstimatedDeliveryDate = (method: ShippingMethod): string => {
  const today = new Date();
  let daysToAdd: number;

  switch (method) {
    case 'overnight':
      daysToAdd = 1;
      break;
    case 'express':
      daysToAdd = 3;
      break;
    case 'standard':
    default:
      daysToAdd = 7;
      break;
  }

  let businessDays = 0;
  const deliveryDate = new Date(today);
  
  while (businessDays < daysToAdd) {
    deliveryDate.setDate(deliveryDate.getDate() + 1);
    const dayOfWeek = deliveryDate.getDay();
    if (dayOfWeek !== 0 && dayOfWeek !== 6) {
      businessDays++;
    }
  }

  return deliveryDate.toLocaleDateString('en-US', {
    weekday: 'long',
    month: 'short',
    day: 'numeric',
  });
};

export const formatShippingPrice = (price: number): string => {
  if (price === 0) {
    return 'FREE';
  }
  return `$${price.toFixed(2)}`;
};

export const getAmountForFreeShipping = (subtotal: number): number => {
  const remaining = FREE_SHIPPING_THRESHOLD - subtotal;
  return remaining > 0 ? remaining : 0;
};
