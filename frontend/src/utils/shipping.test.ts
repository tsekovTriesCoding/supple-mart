import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';

import {
  FREE_SHIPPING_THRESHOLD,
  getShippingOptions,
  getShippingCost,
  getDefaultShippingMethod,
  getEstimatedDeliveryDate,
  formatShippingPrice,
  getAmountForFreeShipping,
} from './shipping';
import type { ShippingMethod } from './shipping';

describe('shipping utilities', () => {
  describe('FREE_SHIPPING_THRESHOLD', () => {
    it('should be $50', () => {
      expect(FREE_SHIPPING_THRESHOLD).toBe(50);
    });
  });

  describe('getShippingOptions', () => {
    it('returns all three shipping options', () => {
      const options = getShippingOptions(0);
      expect(options).toHaveLength(3);
      expect(options.map(o => o.id)).toEqual(['standard', 'express', 'overnight']);
    });

    it('returns standard shipping with cost when subtotal is below threshold', () => {
      const options = getShippingOptions(30);
      const standard = options.find(o => o.id === 'standard');
      
      expect(standard).toBeDefined();
      expect(standard?.price).toBe(5.99);
      expect(standard?.isFree).toBe(false);
      expect(standard?.description).toContain('Free on orders over $50');
    });

    it('returns free standard shipping when subtotal meets threshold', () => {
      const options = getShippingOptions(50);
      const standard = options.find(o => o.id === 'standard');
      
      expect(standard).toBeDefined();
      expect(standard?.price).toBe(0);
      expect(standard?.isFree).toBe(true);
      expect(standard?.description).toContain('Free shipping on orders over $50!');
    });

    it('returns free standard shipping when subtotal exceeds threshold', () => {
      const options = getShippingOptions(100);
      const standard = options.find(o => o.id === 'standard');
      
      expect(standard?.price).toBe(0);
      expect(standard?.isFree).toBe(true);
    });

    it('express shipping price is always $12.99', () => {
      expect(getShippingOptions(0).find(o => o.id === 'express')?.price).toBe(12.99);
      expect(getShippingOptions(50).find(o => o.id === 'express')?.price).toBe(12.99);
      expect(getShippingOptions(100).find(o => o.id === 'express')?.price).toBe(12.99);
    });

    it('overnight shipping price is always $24.99', () => {
      expect(getShippingOptions(0).find(o => o.id === 'overnight')?.price).toBe(24.99);
      expect(getShippingOptions(50).find(o => o.id === 'overnight')?.price).toBe(24.99);
      expect(getShippingOptions(100).find(o => o.id === 'overnight')?.price).toBe(24.99);
    });

    it('includes correct estimated days for each method', () => {
      const options = getShippingOptions(0);
      
      expect(options.find(o => o.id === 'standard')?.estimatedDays).toBe('5-7 business days');
      expect(options.find(o => o.id === 'express')?.estimatedDays).toBe('2-3 business days');
      expect(options.find(o => o.id === 'overnight')?.estimatedDays).toBe('1 business day');
    });

    it('includes correct names for each method', () => {
      const options = getShippingOptions(0);
      
      expect(options.find(o => o.id === 'standard')?.name).toBe('Standard Shipping');
      expect(options.find(o => o.id === 'express')?.name).toBe('Express Shipping');
      expect(options.find(o => o.id === 'overnight')?.name).toBe('Overnight Shipping');
    });
  });

  describe('getShippingCost', () => {
    describe('standard shipping', () => {
      it('returns $5.99 when subtotal is below threshold', () => {
        expect(getShippingCost('standard', 0)).toBe(5.99);
        expect(getShippingCost('standard', 25)).toBe(5.99);
        expect(getShippingCost('standard', 49.99)).toBe(5.99);
      });

      it('returns $0 when subtotal meets threshold', () => {
        expect(getShippingCost('standard', 50)).toBe(0);
      });

      it('returns $0 when subtotal exceeds threshold', () => {
        expect(getShippingCost('standard', 75)).toBe(0);
        expect(getShippingCost('standard', 100)).toBe(0);
        expect(getShippingCost('standard', 500)).toBe(0);
      });
    });

    describe('express shipping', () => {
      it('always returns $12.99 regardless of subtotal', () => {
        expect(getShippingCost('express', 0)).toBe(12.99);
        expect(getShippingCost('express', 50)).toBe(12.99);
        expect(getShippingCost('express', 100)).toBe(12.99);
      });
    });

    describe('overnight shipping', () => {
      it('always returns $24.99 regardless of subtotal', () => {
        expect(getShippingCost('overnight', 0)).toBe(24.99);
        expect(getShippingCost('overnight', 50)).toBe(24.99);
        expect(getShippingCost('overnight', 100)).toBe(24.99);
      });
    });
  });

  describe('getDefaultShippingMethod', () => {
    it('returns standard as the default shipping method', () => {
      expect(getDefaultShippingMethod()).toBe('standard');
    });
  });

  describe('getEstimatedDeliveryDate', () => {
    beforeEach(() => {
      vi.useFakeTimers();
      vi.setSystemTime(new Date('2026-01-05T12:00:00'));
    });

    afterEach(() => {
      vi.useRealTimers();
    });

    it('returns a formatted date string', () => {
      const result = getEstimatedDeliveryDate('standard');
      expect(result).toMatch(/\w+, \w+ \d+/);
    });

    it('overnight delivery is 1 business day away', () => {
      const result = getEstimatedDeliveryDate('overnight');
      expect(result).toBe('Tuesday, Jan 6');
    });

    it('express delivery is 3 business days away', () => {
      const result = getEstimatedDeliveryDate('express');
      expect(result).toBe('Thursday, Jan 8');
    });

    it('standard delivery is 7 business days away', () => {
      const result = getEstimatedDeliveryDate('standard');
      expect(result).toBe('Wednesday, Jan 14');
    });

    it('skips weekends for business days calculation', () => {
      vi.setSystemTime(new Date('2026-01-09T12:00:00'));
      
      const overnight = getEstimatedDeliveryDate('overnight');
      expect(overnight).toBe('Monday, Jan 12');
    });
  });

  describe('formatShippingPrice', () => {
    it('returns "FREE" for $0', () => {
      expect(formatShippingPrice(0)).toBe('FREE');
    });

    it('formats non-zero prices with dollar sign and 2 decimals', () => {
      expect(formatShippingPrice(5.99)).toBe('$5.99');
      expect(formatShippingPrice(12.99)).toBe('$12.99');
      expect(formatShippingPrice(24.99)).toBe('$24.99');
    });

    it('formats whole numbers with 2 decimal places', () => {
      expect(formatShippingPrice(5)).toBe('$5.00');
      expect(formatShippingPrice(10)).toBe('$10.00');
    });

    it('rounds to 2 decimal places', () => {
      expect(formatShippingPrice(5.999)).toBe('$6.00');
      expect(formatShippingPrice(5.994)).toBe('$5.99');
    });
  });

  describe('getAmountForFreeShipping', () => {
    it('returns remaining amount when subtotal is below threshold', () => {
      expect(getAmountForFreeShipping(0)).toBe(50);
      expect(getAmountForFreeShipping(10)).toBe(40);
      expect(getAmountForFreeShipping(25)).toBe(25);
      expect(getAmountForFreeShipping(49)).toBe(1);
      expect(getAmountForFreeShipping(49.99)).toBeCloseTo(0.01);
    });

    it('returns 0 when subtotal meets threshold', () => {
      expect(getAmountForFreeShipping(50)).toBe(0);
    });

    it('returns 0 when subtotal exceeds threshold', () => {
      expect(getAmountForFreeShipping(51)).toBe(0);
      expect(getAmountForFreeShipping(75)).toBe(0);
      expect(getAmountForFreeShipping(100)).toBe(0);
      expect(getAmountForFreeShipping(500)).toBe(0);
    });
  });

  describe('ShippingMethod type', () => {
    it('accepts valid shipping methods', () => {
      const methods: ShippingMethod[] = ['standard', 'express', 'overnight'];
      expect(methods).toHaveLength(3);
    });
  });

  describe('integration scenarios', () => {
    it('cart just below free shipping threshold shows remaining amount', () => {
      const subtotal = 45;
      const remaining = getAmountForFreeShipping(subtotal);
      const shippingCost = getShippingCost('standard', subtotal);
      
      expect(remaining).toBe(5);
      expect(shippingCost).toBe(5.99);
      expect(formatShippingPrice(shippingCost)).toBe('$5.99');
    });

    it('cart at free shipping threshold shows free shipping', () => {
      const subtotal = 50;
      const remaining = getAmountForFreeShipping(subtotal);
      const shippingCost = getShippingCost('standard', subtotal);
      const options = getShippingOptions(subtotal);
      
      expect(remaining).toBe(0);
      expect(shippingCost).toBe(0);
      expect(formatShippingPrice(shippingCost)).toBe('FREE');
      expect(options.find(o => o.id === 'standard')?.isFree).toBe(true);
    });

    it('calculates correct total with shipping for order below threshold', () => {
      const subtotal = 30;
      const shippingCost = getShippingCost('standard', subtotal);
      const total = subtotal + shippingCost;
      
      expect(total).toBe(35.99);
    });

    it('calculates correct total with free shipping for order above threshold', () => {
      const subtotal = 75;
      const shippingCost = getShippingCost('standard', subtotal);
      const total = subtotal + shippingCost;
      
      expect(total).toBe(75);
    });

    it('express shipping always adds to total regardless of subtotal', () => {
      const subtotal = 100;
      const shippingCost = getShippingCost('express', subtotal);
      const total = subtotal + shippingCost;
      
      expect(shippingCost).toBe(12.99);
      expect(total).toBe(112.99);
    });
  });
});
