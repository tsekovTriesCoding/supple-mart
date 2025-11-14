import { formatCategoryForDisplay } from '../../utils/categoryUtils';

interface ProductFiltersProps {
  categories: string[];
  selectedCategory: string;
  priceRange: { min: string; max: string };
  onCategoryChange: (category: string) => void;
  onPriceRangeChange: (range: { min: string; max: string }) => void;
}

export const ProductFilters = ({
  categories,
  selectedCategory,
  priceRange,
  onCategoryChange,
  onPriceRangeChange,
}: ProductFiltersProps) => {
  return (
    <div className="mt-6 pt-6 border-t border-gray-600 animate-slide-in">
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div>
          <h3 className="text-lg font-semibold text-white mb-3">Categories</h3>
          <div className="space-y-2">
            {categories.map((category: string) => (
              <button
                key={category}
                onClick={() => onCategoryChange(category)}
                className={`block w-full text-left px-3 py-2 rounded-lg transition-colors ${
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

        <div>
          <h3 className="text-lg font-semibold text-white mb-3">Price Range</h3>
          <div className="space-y-3">
            <div className="flex space-x-2">
              <input
                type="number"
                placeholder="Min"
                value={priceRange.min}
                onChange={(e) =>
                  onPriceRangeChange({ ...priceRange, min: e.target.value })
                }
                className="input flex-1"
              />
              <input
                type="number"
                placeholder="Max"
                value={priceRange.max}
                onChange={(e) =>
                  onPriceRangeChange({ ...priceRange, max: e.target.value })
                }
                className="input flex-1"
              />
            </div>
            <button className="btn-secondary w-full cursor-pointer">Apply</button>
          </div>
        </div>

        <div>
          <h3 className="text-lg font-semibold text-white mb-3">Quick Filters</h3>
          <div className="space-y-2">
            <label className="flex items-center space-x-2 text-gray-300 cursor-pointer">
              <input type="checkbox" className="rounded border-gray-600" />
              <span>In Stock Only</span>
            </label>
            <label className="flex items-center space-x-2 text-gray-300 cursor-pointer">
              <input type="checkbox" className="rounded border-gray-600" />
              <span>On Sale</span>
            </label>
            <label className="flex items-center space-x-2 text-gray-300 cursor-pointer">
              <input type="checkbox" className="rounded border-gray-600" />
              <span>Free Shipping</span>
            </label>
            <label className="flex items-center space-x-2 text-gray-300 cursor-pointer">
              <input type="checkbox" className="rounded border-gray-600" />
              <span>Top Rated (4.5+)</span>
            </label>
          </div>
        </div>
      </div>
    </div>
  );
};
