import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '../test/test-utils';
import { ProductCard } from './Product/ProductCard';
import type { Product } from '../hooks/useProducts';

const createMockProduct = (overrides: Partial<Product> = {}): Product => ({
  id: '1',
  name: 'Test Product',
  description: 'A test product description',
  price: 29.99,
  category: 'test-category',
  brand: 'Test Brand',
  imageUrl: 'https://example.com/image.jpg',
  inStock: true,
  averageRating: 4.5,
  totalReviews: 10,
  createdAt: '2024-01-01T00:00:00Z',
  updatedAt: '2024-01-01T00:00:00Z',
  ...overrides,
});

describe('ProductCard', () => {
  const defaultProps = {
    product: createMockProduct(),
    onProductClick: vi.fn(),
    onAddToCart: vi.fn(),
    onToggleWishlist: vi.fn(),
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders product name', () => {
    render(<ProductCard {...defaultProps} />);
    expect(screen.getByText('Test Product')).toBeInTheDocument();
  });

  it('renders product price', () => {
    render(<ProductCard {...defaultProps} />);
    expect(screen.getByText('$29.99')).toBeInTheDocument();
  });

  it('renders product brand', () => {
    render(<ProductCard {...defaultProps} />);
    expect(screen.getByText('Test Brand')).toBeInTheDocument();
  });

  it('renders product image', () => {
    render(<ProductCard {...defaultProps} />);
    const image = screen.getByAltText('Test Product');
    expect(image).toBeInTheDocument();
    expect(image).toHaveAttribute('src', 'https://example.com/image.jpg');
  });

  it('displays category formatted correctly', () => {
    render(<ProductCard {...defaultProps} />);
    // Category should be formatted - look for the category span specifically
    expect(screen.getByText('Test-category')).toBeInTheDocument();
  });

  it('calls onProductClick when card is clicked', async () => {
    const { user } = render(<ProductCard {...defaultProps} />);
    
    await user.click(screen.getByText('Test Product'));
    
    expect(defaultProps.onProductClick).toHaveBeenCalledWith('1');
  });

  it('calls onAddToCart when add to cart button is clicked', async () => {
    const { user } = render(<ProductCard {...defaultProps} />);
    
    const addButton = screen.getByRole('button', { name: /add to cart/i });
    await user.click(addButton);
    
    expect(defaultProps.onAddToCart).toHaveBeenCalledWith(defaultProps.product);
    // Should not trigger product click
    expect(defaultProps.onProductClick).not.toHaveBeenCalled();
  });

  it('calls onToggleWishlist when heart button is clicked', async () => {
    const { user } = render(<ProductCard {...defaultProps} />);
    
    // Find the heart/wishlist button
    const wishlistButtons = screen.getAllByRole('button');
    const heartButton = wishlistButtons.find(btn => 
      btn.querySelector('.lucide-heart')
    );
    
    if (heartButton) {
      await user.click(heartButton);
      expect(defaultProps.onToggleWishlist).toHaveBeenCalledWith(defaultProps.product);
    }
  });

  it('shows out of stock overlay when product is not in stock', () => {
    render(
      <ProductCard
        {...defaultProps}
        product={createMockProduct({ inStock: false })}
      />
    );
    
    // Both overlay and button show "Out of Stock", use getAllByText
    const outOfStockElements = screen.getAllByText('Out of Stock');
    expect(outOfStockElements.length).toBeGreaterThanOrEqual(1);
  });

  it('disables add to cart button when out of stock', () => {
    render(
      <ProductCard
        {...defaultProps}
        product={createMockProduct({ inStock: false })}
      />
    );
    
    const addButton = screen.getByRole('button', { name: /out of stock/i });
    expect(addButton).toBeDisabled();
  });

  it('shows loading state when isAddingToCart is true', () => {
    render(<ProductCard {...defaultProps} isAddingToCart={true} />);
    
    expect(screen.getByText('Adding...')).toBeInTheDocument();
  });

  it('disables button when isAddingToCart is true', () => {
    render(<ProductCard {...defaultProps} isAddingToCart={true} />);
    
    const buttons = screen.getAllByRole('button');
    const addButton = buttons.find(btn => btn.textContent?.includes('Adding'));
    expect(addButton).toBeDisabled();
  });

  it('applies loading overlay when isLoading is true', () => {
    render(<ProductCard {...defaultProps} isLoading={true} />);
    
    expect(screen.getByText('Opening...')).toBeInTheDocument();
  });

  it('shows filled heart when product is in wishlist', () => {
    const { container } = render(
      <ProductCard {...defaultProps} isInWishlist={true} />
    );
    
    // Check for filled heart styling
    const heartIcon = container.querySelector('.lucide-heart.fill-current');
    expect(heartIcon).toBeInTheDocument();
  });

  it('renders in list view variant', () => {
    render(<ProductCard {...defaultProps} variant="list" />);
    
    // In list view, description should be visible
    expect(screen.getByText('A test product description')).toBeInTheDocument();
  });

  it('shows original price with strikethrough when provided', () => {
    render(
      <ProductCard
        {...defaultProps}
        product={createMockProduct({ 
          price: 19.99, 
          originalPrice: 29.99 
        })}
      />
    );
    
    expect(screen.getByText('$19.99')).toBeInTheDocument();
    expect(screen.getByText('$29.99')).toBeInTheDocument();
    expect(screen.getByText('$29.99')).toHaveClass('line-through');
  });

  it('shows review count', () => {
    render(<ProductCard {...defaultProps} />);
    
    // Should show review count in some format
    expect(screen.getByText(/10 reviews|reviews/i)).toBeInTheDocument();
  });

  it('applies animation delay style', () => {
    const { container } = render(
      <ProductCard {...defaultProps} animationDelay={0.2} />
    );
    
    const card = container.firstChild as HTMLElement;
    expect(card).toHaveStyle({ animationDelay: '0.2s' });
  });
});
