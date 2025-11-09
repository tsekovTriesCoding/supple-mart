import { Link, useLocation } from 'react-router-dom';

import CategoryNavigation from './CategoryNavigation';

const navLinks = [
  { path: '/', label: 'Home' },
  { path: '/products', label: 'Products' },
  { path: '/about', label: 'About' },
  { path: '/contact', label: 'Contact' },
];

export const DesktopNav = () => {
  const location = useLocation();
  const isActive = (path: string) => location.pathname === path;

  return (
    <nav className="hidden md:flex items-center space-x-8">
      {navLinks.map((link) => (
        <Link
          key={link.path}
          to={link.path}
          className={`${isActive(link.path) ? 'nav-link-active' : 'nav-link'} font-medium`}
        >
          {link.label}
        </Link>
      ))}
      <CategoryNavigation />
    </nav>
  );
};
