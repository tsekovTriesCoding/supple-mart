import { Link } from 'react-router-dom';

export const Logo = () => {
  return (
    <Link to="/" className="flex items-center space-x-2">
      <svg 
        className="w-8 h-8" 
        viewBox="0 0 256 256" 
        role="img" 
        aria-label="SuppleMart logo"
      >
        <defs>
          <linearGradient id="g1" x1="0" x2="1">
            <stop offset="0%" stopColor="#34d399" />
            <stop offset="100%" stopColor="#60a5fa" />
          </linearGradient>
          <filter id="shadow" x="-20%" y="-20%" width="140%" height="140%">
            <feDropShadow dx="0" dy="6" stdDeviation="8" floodColor="#000" floodOpacity="0.4" />
          </filter>
        </defs>

        <rect width="100%" height="100%" rx="36" ry="36" fill="#0b1220" />

        <g transform="translate(40,44)" opacity="0.12">
          <rect x="0" y="30" width="176" height="128" rx="16" fill="#fff" />
          <path d="M44 30 C44 12 132 12 132 30" fill="none" stroke="#fff" strokeWidth="6" strokeLinecap="round" />
        </g>

        <g transform="translate(36,40)" filter="url(#shadow)">
          <rect x="12" y="36" width="196" height="84" rx="42" fill="url(#g1)" />
          <path d="M110 36 v84" stroke="#ffffff" strokeOpacity="0.14" strokeWidth="6" strokeLinecap="round" />
          <path d="M34 48 C60 24 96 22 110 42" fill="none" stroke="#ffffff" strokeOpacity="0.18" strokeWidth="6" strokeLinecap="round" />
        </g>

        <path d="M168 96 C190 72 214 74 206 100 C200 120 170 136 148 140 C150 120 156 108 168 96 Z" fill="#10b981" transform="translate(0,4)" opacity="0.95" />

        <path d="M96 112 C104 100 132 100 140 92 C148 84 124 76 116 86 C108 96 124 110 140 106" fill="none" stroke="#07203f" strokeWidth="6" strokeLinecap="round" strokeLinejoin="round" transform="translate(0,0)" opacity="0.95" />

        <circle cx="196" cy="60" r="6" fill="#fef3c7" opacity="0.9" />
      </svg>
      <span className="text-xl font-bold text-white">SuppleMart</span>
    </Link>
  );
};
