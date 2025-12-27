import { describe, it, expect } from 'vitest';
import { render, screen } from '../test/test-utils';

import { LoadingSpinner } from './LoadingSpinner';

describe('LoadingSpinner', () => {
  it('renders spinner without message', () => {
    render(<LoadingSpinner />);
    const spinner = document.querySelector('.animate-spin');
    expect(spinner).toBeInTheDocument();
  });

  it('renders with a message', () => {
    render(<LoadingSpinner message="Loading data..." />);
    expect(screen.getByText('Loading data...')).toBeInTheDocument();
  });

  it('renders fullscreen version', () => {
    render(<LoadingSpinner fullScreen />);
    const container = document.querySelector('.min-h-screen');
    expect(container).toBeInTheDocument();
  });

  it('applies different size classes', () => {
    const { rerender } = render(<LoadingSpinner size="sm" />);
    expect(document.querySelector('.h-6')).toBeInTheDocument();

    rerender(<LoadingSpinner size="lg" />);
    expect(document.querySelector('.h-16')).toBeInTheDocument();

    rerender(<LoadingSpinner size="xl" />);
    expect(document.querySelector('.h-20')).toBeInTheDocument();
  });

  it('applies custom className', () => {
    render(<LoadingSpinner className="custom-class" />);
    expect(document.querySelector('.custom-class')).toBeInTheDocument();
  });
});
