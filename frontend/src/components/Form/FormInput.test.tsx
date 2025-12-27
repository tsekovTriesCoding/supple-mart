import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '../../test/test-utils';
import { FormInput } from './FormInput';

describe('FormInput', () => {
  it('renders an input element', () => {
    render(<FormInput name="test" />);
    expect(screen.getByRole('textbox')).toBeInTheDocument();
  });

  it('renders with a label', () => {
    render(<FormInput name="email" label="Email Address" />);
    expect(screen.getByLabelText('Email Address')).toBeInTheDocument();
  });

  it('shows required indicator when required', () => {
    render(<FormInput name="email" label="Email" required />);
    expect(screen.getByText('*')).toBeInTheDocument();
  });

  it('displays error message when provided', () => {
    render(<FormInput name="email" error="Email is required" />);
    expect(screen.getByText('Email is required')).toBeInTheDocument();
  });

  it('displays hint when provided and no error', () => {
    render(<FormInput name="email" hint="Enter your email address" />);
    expect(screen.getByText('Enter your email address')).toBeInTheDocument();
  });

  it('hides hint when error is displayed', () => {
    render(
      <FormInput
        name="email"
        hint="Enter your email"
        error="Email is invalid"
      />
    );
    expect(screen.queryByText('Enter your email')).not.toBeInTheDocument();
    expect(screen.getByText('Email is invalid')).toBeInTheDocument();
  });

  it('applies error styling when error is provided', () => {
    render(<FormInput name="email" error="Invalid email" />);
    const input = screen.getByRole('textbox');
    expect(input).toHaveClass('border-red-500');
  });

  it('forwards ref to input element', () => {
    const ref = vi.fn();
    render(<FormInput name="test" ref={ref} />);
    expect(ref).toHaveBeenCalled();
  });

  it('passes through HTML input attributes', async () => {
    render(
      <FormInput
        name="email"
        type="email"
        placeholder="Enter email"
        disabled
      />
    );
    const input = screen.getByRole('textbox');
    expect(input).toHaveAttribute('type', 'email');
    expect(input).toHaveAttribute('placeholder', 'Enter email');
    expect(input).toBeDisabled();
  });

  it('handles user input', async () => {
    const { user } = render(<FormInput name="email" />);
    const input = screen.getByRole('textbox');
    await user.type(input, 'test@example.com');
    expect(input).toHaveValue('test@example.com');
  });

  it('uses name as id when id is not provided', () => {
    render(<FormInput name="email" label="Email" />);
    const input = screen.getByRole('textbox');
    expect(input).toHaveAttribute('id', 'email');
  });

  it('uses provided id over name', () => {
    render(<FormInput name="email" id="custom-email" label="Email" />);
    const input = screen.getByRole('textbox');
    expect(input).toHaveAttribute('id', 'custom-email');
  });
});
