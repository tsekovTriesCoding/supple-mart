import { useState, useEffect } from 'react';

import { formatCategoryForDisplay } from '../../utils/categoryUtils';

interface ProductFiltersProps {
  categories: string[];
  selectedCategory: string;
  priceRange: { min: string; max: string };
  onCategoryChange: (category: string) => void;
  onPriceRangeChange: (range: { min: string; max: string }) => void;
  onClose?: () => void;
}

export const ProductFilters = ({
  categories,
  selectedCategory,
  priceRange,
  onCategoryChange,
  onPriceRangeChange,
  onClose,
}: ProductFiltersProps) => {
  const [tempPriceRange, setTempPriceRange] = useState(priceRange);

  useEffect(() => {
    setTempPriceRange(priceRange);
  }, [priceRange]);

  const handleApplyPriceFilter = () => {
    onPriceRangeChange(tempPriceRange);
    onClose?.();
  };

  const handleCategoryChange = (category: string) => {
    onCategoryChange(category);
    onClose?.();
  };

  return (
    <div className="mt-6 pt-6 border-t border-gray-600 animate-slide-in overflow-hidden">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="min-w-0">
          <h3 className="text-lg font-semibold text-white mb-3">Categories</h3>
          <div className="space-y-2 max-h-48 md:max-h-none overflow-y-auto pr-2">
            {categories.map((category: string) => (
              <button
                key={category}
                onClick={() => handleCategoryChange(category)}
                className={`block w-full text-left px-3 py-2 rounded-lg transition-colors cursor-pointer ${
                  (category === 'all' && !selectedCategory) || category === selectedCategory
                    ? 'bg-blue-600 text-white'
                    : 'text-gray-400 hover:text-white hover:bg-gray-700'
                }`}
              >
                {formatCategoryForDisplay(category)}
              </button>
            ))}
          </div>
        </div>

        <div className="min-w-0">
          <h3 className="text-lg font-semibold text-white mb-3">Price Range</h3>
          <div className="space-y-3">
            <div className="flex gap-2">
              <input
                type="number"
                placeholder="Min"
                value={tempPriceRange.min}
                onChange={(e) =>
                  setTempPriceRange({ ...tempPriceRange, min: e.target.value })
                }
                className="input w-full min-w-0"
                min="0"
              />
              <input
                type="number"
                placeholder="Max"
                value={tempPriceRange.max}
                onChange={(e) =>
                  setTempPriceRange({ ...tempPriceRange, max: e.target.value })
                }
                className="input w-full min-w-0"
                min="0"
              />
            </div>
            <button 
              onClick={handleApplyPriceFilter}
              className="btn-secondary w-full cursor-pointer"
            >
              Apply
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
