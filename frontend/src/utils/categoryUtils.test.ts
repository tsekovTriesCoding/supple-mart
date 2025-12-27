import { describe, it, expect } from 'vitest';

import {
  Category,
  formatCategoryForDisplay,
  formatCategoryForBackend,
  formatCategoryForUrl,
  formatCategoryFromUrl,
  backendCategoryToUrl,
  urlCategoryToBackend,
  getAllCategories,
  getAllCategoriesForDisplay,
  getAllCategoriesForUrl,
} from './categoryUtils';

describe('categoryUtils', () => {
  describe('formatCategoryForDisplay', () => {
    it('returns "All" for empty string', () => {
      expect(formatCategoryForDisplay('')).toBe('All');
    });

    it('returns "All" for "all"', () => {
      expect(formatCategoryForDisplay('all')).toBe('All');
    });

    it('formats single word category', () => {
      expect(formatCategoryForDisplay('PROTEIN')).toBe('Protein');
      expect(formatCategoryForDisplay('VITAMINS')).toBe('Vitamins');
    });

    it('formats multi-word categories with spaces', () => {
      expect(formatCategoryForDisplay('AMINO_ACIDS')).toBe('Amino Acids');
      expect(formatCategoryForDisplay('MASS_GAINER')).toBe('Mass Gainer');
    });

    it('formats workout categories with dashes', () => {
      expect(formatCategoryForDisplay('PRE_WORKOUT')).toBe('Pre-Workout');
      expect(formatCategoryForDisplay('POST_WORKOUT')).toBe('Post-Workout');
      expect(formatCategoryForDisplay('WEIGHT_LOSS')).toBe('Weight-Loss');
    });

    it('handles lowercase input', () => {
      expect(formatCategoryForDisplay('pre_workout')).toBe('Pre-Workout');
      expect(formatCategoryForDisplay('protein')).toBe('Protein');
    });
  });

  describe('formatCategoryForBackend', () => {
    it('returns empty string for empty input', () => {
      expect(formatCategoryForBackend('')).toBe('');
    });

    it('returns empty string for "all"', () => {
      expect(formatCategoryForBackend('all')).toBe('');
      expect(formatCategoryForBackend('All')).toBe('');
    });

    it('converts display format to backend format', () => {
      expect(formatCategoryForBackend('Amino Acids')).toBe('AMINO_ACIDS');
      expect(formatCategoryForBackend('Pre-Workout')).toBe('PRE-WORKOUT');
    });

    it('converts lowercase to uppercase with underscores', () => {
      expect(formatCategoryForBackend('protein')).toBe('PROTEIN');
      expect(formatCategoryForBackend('mass gainer')).toBe('MASS_GAINER');
    });

    it('trims whitespace', () => {
      expect(formatCategoryForBackend('  protein  ')).toBe('PROTEIN');
    });
  });

  describe('formatCategoryForUrl', () => {
    it('returns empty string for empty input', () => {
      expect(formatCategoryForUrl('')).toBe('');
    });

    it('returns empty string for "all"', () => {
      expect(formatCategoryForUrl('all')).toBe('');
      expect(formatCategoryForUrl('All')).toBe('');
    });

    it('converts to lowercase with dashes', () => {
      expect(formatCategoryForUrl('AMINO_ACIDS')).toBe('amino-acids');
      expect(formatCategoryForUrl('PRE_WORKOUT')).toBe('pre-workout');
    });

    it('handles spaces', () => {
      expect(formatCategoryForUrl('Mass Gainer')).toBe('mass-gainer');
    });

    it('trims whitespace', () => {
      expect(formatCategoryForUrl('  protein  ')).toBe('protein');
    });
  });

  describe('formatCategoryFromUrl', () => {
    it('returns "All" for empty string', () => {
      expect(formatCategoryFromUrl('')).toBe('All');
    });

    it('returns "All" for "all"', () => {
      expect(formatCategoryFromUrl('all')).toBe('All');
    });

    it('converts URL format to display format', () => {
      expect(formatCategoryFromUrl('amino-acids')).toBe('Amino Acids');
      expect(formatCategoryFromUrl('pre-workout')).toBe('Pre Workout');
    });

    it('handles single word', () => {
      expect(formatCategoryFromUrl('protein')).toBe('Protein');
    });
  });

  describe('backendCategoryToUrl', () => {
    it('returns empty string for empty input', () => {
      expect(backendCategoryToUrl('')).toBe('');
    });

    it('converts backend format to URL format', () => {
      expect(backendCategoryToUrl('AMINO_ACIDS')).toBe('amino-acids');
      expect(backendCategoryToUrl('PRE_WORKOUT')).toBe('pre-workout');
      expect(backendCategoryToUrl('PROTEIN')).toBe('protein');
    });
  });

  describe('urlCategoryToBackend', () => {
    it('returns empty string for empty input', () => {
      expect(urlCategoryToBackend('')).toBe('');
    });

    it('returns empty string for "all"', () => {
      expect(urlCategoryToBackend('all')).toBe('');
    });

    it('converts URL format to backend format', () => {
      expect(urlCategoryToBackend('amino-acids')).toBe('AMINO_ACIDS');
      expect(urlCategoryToBackend('pre-workout')).toBe('PRE_WORKOUT');
      expect(urlCategoryToBackend('protein')).toBe('PROTEIN');
    });
  });

  describe('getAllCategories', () => {
    it('returns all category values', () => {
      const categories = getAllCategories();
      expect(categories).toContain('PROTEIN');
      expect(categories).toContain('VITAMINS');
      expect(categories).toContain('PRE_WORKOUT');
      expect(categories).toHaveLength(Object.keys(Category).length);
    });
  });

  describe('getAllCategoriesForDisplay', () => {
    it('returns formatted category names', () => {
      const categories = getAllCategoriesForDisplay();
      expect(categories).toContain('Protein');
      expect(categories).toContain('Vitamins');
      expect(categories).toContain('Pre-Workout');
      expect(categories).toHaveLength(Object.keys(Category).length);
    });
  });

  describe('getAllCategoriesForUrl', () => {
    it('returns URL-formatted category names', () => {
      const categories = getAllCategoriesForUrl();
      expect(categories).toContain('protein');
      expect(categories).toContain('vitamins');
      expect(categories).toContain('pre-workout');
      expect(categories).toHaveLength(Object.keys(Category).length);
    });
  });
});
