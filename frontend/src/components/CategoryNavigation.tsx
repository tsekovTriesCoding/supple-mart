import { Link } from 'react-router-dom';
import { ChevronDown } from 'lucide-react';
import { useState } from 'react';

import { useProductCategories } from '../hooks/useProducts';
import { formatCategoryForDisplay, formatCategoryForUrl } from '../utils/categoryUtils';

const CategoryNavigation = () => {
  const { data: categoriesData, isLoading } = useProductCategories();
  const [isOpen, setIsOpen] = useState(false);

  if (isLoading) {
    return (
      <div className="relative">
        <button className="nav-link flex items-center space-x-1 font-medium">
          <span>Categories</span>
          <ChevronDown className="w-4 h-4" />
        </button>
      </div>
    );
  }

  return (
    <div className="relative">
      <button
        onClick={() => setIsOpen(!isOpen)}
        className={`nav-link flex items-center space-x-1 font-medium ${isOpen ? 'text-blue-400' : ''}`}
      >
        <span>Categories</span>
        <ChevronDown className={`w-4 h-4 transition-transform ${isOpen ? 'rotate-180' : ''}`} />
      </button>

      {isOpen && (
        <div className="absolute top-full left-0 mt-2 w-64 bg-gray-800 border border-gray-700 rounded-lg shadow-xl z-50">
          <div className="p-2">
            <Link
              to="/products"
              className="block px-3 py-2 text-gray-300 hover:text-white hover:bg-gray-700 rounded-md transition-colors"
              onClick={() => setIsOpen(false)}
            >
              All Products
            </Link>
            {categoriesData?.map((category: string) => (
              <Link
                key={category}
                to={`/products?category=${formatCategoryForUrl(category)}`}
                className="block px-3 py-2 text-gray-300 hover:text-white hover:bg-gray-700 rounded-md transition-colors"
                onClick={() => setIsOpen(false)}
              >
                {formatCategoryForDisplay(category)}
              </Link>
            ))}
          </div>
        </div>
      )}

      {isOpen && (
        <div
          className="fixed inset-0 z-40"
          onClick={() => setIsOpen(false)}
        />
      )}
    </div>
  );
};

export default CategoryNavigation;