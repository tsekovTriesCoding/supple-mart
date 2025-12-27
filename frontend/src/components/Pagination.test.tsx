import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '../test/test-utils';

import { Pagination } from './Pagination';

describe('Pagination', () => {
  it('returns null when totalPages is 1', () => {
    const { container } = render(
      <Pagination currentPage={1} totalPages={1} onPageChange={() => {}} />
    );
    expect(container.firstChild).toBeNull();
  });

  it('renders page numbers for small page count', () => {
    render(
      <Pagination currentPage={1} totalPages={5} onPageChange={() => {}} />
    );
    
    expect(screen.getByText('1')).toBeInTheDocument();
    expect(screen.getByText('2')).toBeInTheDocument();
    expect(screen.getByText('3')).toBeInTheDocument();
    expect(screen.getByText('4')).toBeInTheDocument();
    expect(screen.getByText('5')).toBeInTheDocument();
  })

  it('highlights the current page', () => {
    render(
      <Pagination currentPage={3} totalPages={5} onPageChange={() => {}} />
    );
    
    const currentPageButton = screen.getByText('3');
    expect(currentPageButton).toHaveClass('bg-blue-600');
  });

  it('calls onPageChange when a page is clicked', async () => {
    const handlePageChange = vi.fn();
    const { user } = render(
      <Pagination currentPage={1} totalPages={5} onPageChange={handlePageChange} />
    );

    await user.click(screen.getByText('3'));
    expect(handlePageChange).toHaveBeenCalledWith(3);
  });

  it('disables prev button on first page', () => {
    render(
      <Pagination currentPage={1} totalPages={5} onPageChange={() => {}} />
    );
    
    const buttons = screen.getAllByRole('button');
    const prevButton = buttons[0];
    expect(prevButton).toBeDisabled();
  });

  it('disables next button on last page', () => {
    render(
      <Pagination currentPage={5} totalPages={5} onPageChange={() => {}} />
    );
    
    const buttons = screen.getAllByRole('button');
    const nextButton = buttons[buttons.length - 1];
    expect(nextButton).toBeDisabled();
  });

  it('navigates to previous page when prev button clicked', async () => {
    const handlePageChange = vi.fn()
    const { user } = render(
      <Pagination currentPage={3} totalPages={5} onPageChange={handlePageChange} />
    );

    const buttons = screen.getAllByRole('button');
    await user.click(buttons[0]); // prev button

    expect(handlePageChange).toHaveBeenCalledWith(2);
  });

  it('navigates to next page when next button clicked', async () => {
    const handlePageChange = vi.fn();
    const { user } = render(
      <Pagination currentPage={3} totalPages={5} onPageChange={handlePageChange} />
    );

    const buttons = screen.getAllByRole('button');
    await user.click(buttons[buttons.length - 1]); // next button

    expect(handlePageChange).toHaveBeenCalledWith(4);
  });

  it('shows ellipsis for large page count at the beginning', () => {
    render(
      <Pagination currentPage={2} totalPages={10} onPageChange={() => {}} />
    );
    
    expect(screen.getByText('...')).toBeInTheDocument();
  });

  it('shows ellipsis on both sides when in middle', () => {
    render(
      <Pagination currentPage={5} totalPages={10} onPageChange={() => {}} />
    );
    
    const ellipses = screen.getAllByText('...');
    expect(ellipses).toHaveLength(2);
  });
});
