import { Search } from 'lucide-react';

interface SearchInputProps {
  className?: string;
}

export const SearchInput = ({ className = 'w-64' }: SearchInputProps) => {
  return (
    <div className="relative">
      <div className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none">
        <Search className="w-4 h-4" style={{ color: '#9ca3af' }} />
      </div>
      <input
        type="text"
        placeholder="Search products..."
        className={`input ${className}`}
        style={{ paddingLeft: '2.75rem', paddingRight: '1rem' }}
      />
    </div>
  );
};
