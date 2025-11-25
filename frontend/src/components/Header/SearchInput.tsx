import { useState, useEffect, useRef } from 'react';
import { Search, X } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

import { useProducts } from '../../hooks/useProducts';
import type { Product } from '../../types/product';

interface SearchInputProps {
  className?: string;
}

export const SearchInput = ({ className = 'w-64' }: SearchInputProps) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [isOpen, setIsOpen] = useState(false);
  const navigate = useNavigate();
  const dropdownRef = useRef<HTMLDivElement>(null);
  
  const { data: productsData } = useProducts({
    search: searchTerm,
    page: 1,
    limit: 10, // Show top 10 results in dropdown
  });

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  useEffect(() => {
    if (searchTerm.length > 0 && productsData?.products && productsData.products.length > 0) {
      setIsOpen(true);
    } else {
      setIsOpen(false);
    }
  }, [searchTerm, productsData]);

  const handleProductClick = (productId: string) => {
    navigate(`/products/${productId}`);
    setSearchTerm('');
    setIsOpen(false);
  };

  const handleClearSearch = () => {
    setSearchTerm('');
    setIsOpen(false);
  };

  return (
    <div className="relative" ref={dropdownRef}>
      <div className="relative">
        <div className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none">
          <Search className="w-4 h-4" style={{ color: '#9ca3af' }} />
        </div>
        <input
          type="text"
          placeholder="Search products..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className={`input ${className}`}
          style={{ paddingLeft: '2.75rem', paddingRight: searchTerm ? '2.5rem' : '1rem' }}
        />
        {searchTerm && (
          <button
            type="button"
            onClick={handleClearSearch}
            className="absolute inset-y-0 right-0 flex items-center pr-3 text-gray-400 hover:text-white transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        )}
      </div>

      {isOpen && productsData?.products && productsData.products.length > 0 && (
        <div className="absolute top-full left-0 right-0 mt-2 bg-gray-800 border border-gray-700 rounded-lg shadow-xl z-50 max-h-96 overflow-y-auto">
          <div className="p-2">
            {productsData.products.map((product: Product) => (
              <button
                key={product.id}
                onClick={() => handleProductClick(product.id)}
                className="w-full flex items-center gap-3 p-2 rounded-lg hover:bg-gray-700 transition-colors text-left"
              >
                <img
                  src={product.imageUrl}
                  alt={product.name}
                  className="w-12 h-12 object-cover rounded"
                />
                <div className="flex-1 min-w-0">
                  <p className="text-white font-medium truncate">{product.name}</p>
                  <p className="text-sm text-gray-400">${product.price.toFixed(2)}</p>
                </div>
              </button>
            ))}
          </div>
        </div>
      )}

      {isOpen && searchTerm.length > 0 && productsData?.products?.length === 0 && (
        <div className="absolute top-full left-0 right-0 mt-2 bg-gray-800 border border-gray-700 rounded-lg shadow-xl z-50 p-4">
          <p className="text-gray-400 text-center text-sm">No products found</p>
        </div>
      )}
    </div>
  );
};
