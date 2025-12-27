import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '../../test/test-utils';
import { FormSelect } from './FormSelect';

const mockOptions = [
  { value: 'us', label: 'United States' },
  { value: 'uk', label: 'United Kingdom' },
  { value: 'ca', label: 'Canada' },
];

describe('FormSelect', () => {
  it('renders a select element', () => {
    render(<FormSelect name="country" options={mockOptions} />);
    expect(screen.getByRole('combobox')).toBeInTheDocument();
  });

  it('renders with a label', () => {
    render(<FormSelect name="country" label="Country" options={mockOptions} />);
    expect(screen.getByLabelText('Country')).toBeInTheDocument();
  });

  it('renders all options', () => {
    render(<FormSelect name="country" options={mockOptions} />);
    expect(screen.getByRole('option', { name: 'United States' })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'United Kingdom' })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'Canada' })).toBeInTheDocument();
  });

  it('renders placeholder option when provided', () => {
    render(
      <FormSelect
        name="country"
        options={mockOptions}
        placeholder="Select a country"
      />
    );
    expect(screen.getByRole('option', { name: 'Select a country' })).toBeInTheDocument();
  });

  it('shows required indicator when required', () => {
    render(
      <FormSelect name="country" label="Country" options={mockOptions} required />
    );
    expect(screen.getByText('*')).toBeInTheDocument();
  });

  it('displays error message when provided', () => {
    render(
      <FormSelect
        name="country"
        options={mockOptions}
        error="Please select a country"
      />
    );
    expect(screen.getByText('Please select a country')).toBeInTheDocument();
  });

  it('displays hint when provided and no error', () => {
    render(
      <FormSelect
        name="country"
        options={mockOptions}
        hint="Select your country of residence"
      />
    );
    expect(screen.getByText('Select your country of residence')).toBeInTheDocument();
  });

  it('hides hint when error is displayed', () => {
    render(
      <FormSelect
        name="country"
        options={mockOptions}
        hint="Select your country"
        error="Country is required"
      />
    );
    expect(screen.queryByText('Select your country')).not.toBeInTheDocument();
    expect(screen.getByText('Country is required')).toBeInTheDocument();
  });

  it('applies error styling when error is provided', () => {
    render(
      <FormSelect name="country" options={mockOptions} error="Invalid" />
    );
    const select = screen.getByRole('combobox');
    expect(select).toHaveClass('border-red-500');
  });

  it('handles selection change', async () => {
    const { user } = render(
      <FormSelect name="country" options={mockOptions} />
    );
    const select = screen.getByRole('combobox');
    await user.selectOptions(select, 'uk');
    expect(select).toHaveValue('uk');
  });

  it('forwards ref to select element', () => {
    const ref = vi.fn();
    render(<FormSelect name="country" options={mockOptions} ref={ref} />);
    expect(ref).toHaveBeenCalled();
  });

  it('can be disabled', () => {
    render(<FormSelect name="country" options={mockOptions} disabled />);
    expect(screen.getByRole('combobox')).toBeDisabled();
  });
});
