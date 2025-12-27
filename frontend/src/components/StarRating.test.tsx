import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '../test/test-utils';

import { StarRating } from './StarRating';

describe('StarRating', () => {
  it('renders 5 stars', () => {
    render(<StarRating rating={3} />);
    const buttons = screen.getAllByRole('button');
    expect(buttons).toHaveLength(5);
  })

  it('displays the correct rating value when showValue is true', () => {
    render(<StarRating rating={4.5} showValue />);
    expect(screen.getByText('4.5')).toBeInTheDocument();
  });

  it('displays total reviews count when provided', () => {
    render(<StarRating rating={4} showValue totalReviews={120} />);
    expect(screen.getByText(/120/)).toBeInTheDocument();
    expect(screen.getByText(/reviews/)).toBeInTheDocument();
  });

  it('calls onChange when interactive and star is clicked', async () => {
    const handleChange = vi.fn();
    const { user } = render(
      <StarRating rating={3} interactive onChange={handleChange} />
    );

    const buttons = screen.getAllByRole('button');
    await user.click(buttons[4]); // Click 5th star

    expect(handleChange).toHaveBeenCalledWith(5);
  });

  it('does not call onChange when not interactive', async () => {
    const handleChange = vi.fn();
    const { user } = render(
      <StarRating rating={3} onChange={handleChange} />
    );

    const buttons = screen.getAllByRole('button');
    await user.click(buttons[4]);

    expect(handleChange).not.toHaveBeenCalled()
  });

  it('supports keyboard navigation when interactive', async () => {
    const handleChange = vi.fn();
    const { user } = render(
      <StarRating rating={3} interactive onChange={handleChange} />
    );

    const buttons = screen.getAllByRole('button');
    buttons[2].focus();
    await user.keyboard('{Enter}');

    expect(handleChange).toHaveBeenCalledWith(3)
  });

  it('applies custom className', () => {
    render(<StarRating rating={3} className="custom-rating" />);
    expect(document.querySelector('.custom-rating')).toBeInTheDocument();
  });

  it('renders with correct aria-labels when interactive', () => {
    render(<StarRating rating={3} interactive />);
    expect(screen.getByLabelText('Rate 1 star')).toBeInTheDocument();
    expect(screen.getByLabelText('Rate 2 stars')).toBeInTheDocument();
    expect(screen.getByLabelText('Rate 5 stars')).toBeInTheDocument();
  });
});
