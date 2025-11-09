import { Link } from 'react-router-dom';

export const Logo = () => {
  return (
    <Link to="/" className="flex items-center space-x-2">
      <div className="w-8 h-8 rounded-lg flex items-center justify-center" style={{ backgroundColor: '#2563eb' }}>
        <span className="text-white font-bold text-xl">S</span>
      </div>
      <span className="text-xl font-bold text-white">SuppleMart</span>
    </Link>
  );
};
