import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '../../test/test-utils';
import { FormTextarea } from './FormTextarea';

describe('FormTextarea', () => {
  it('renders a textarea element', () => {
    render(<FormTextarea name="message" />);
    expect(screen.getByRole('textbox')).toBeInTheDocument();
  });

  it('renders with a label', () => {
    render(<FormTextarea name="message" label="Message" />);
    expect(screen.getByLabelText('Message')).toBeInTheDocument();
  });

  it('shows required indicator when required', () => {
    render(<FormTextarea name="message" label="Message" required />);
    expect(screen.getByText('*')).toBeInTheDocument();
  });

  it('displays error message when provided', () => {
    render(<FormTextarea name="message" error="Message is required" />);
    expect(screen.getByText('Message is required')).toBeInTheDocument();
  });

  it('displays hint when provided and no error', () => {
    render(<FormTextarea name="message" hint="Enter your message here" />);
    expect(screen.getByText('Enter your message here')).toBeInTheDocument();
  });

  it('hides hint when error is displayed', () => {
    render(
      <FormTextarea
        name="message"
        hint="Write a message"
        error="Message too short"
      />
    );
    expect(screen.queryByText('Write a message')).not.toBeInTheDocument();
    expect(screen.getByText('Message too short')).toBeInTheDocument();
  });

  it('applies error styling when error is provided', () => {
    render(<FormTextarea name="message" error="Invalid" />);
    const textarea = screen.getByRole('textbox');
    expect(textarea).toHaveClass('border-red-500');
  });

  it('handles user input', async () => {
    const { user } = render(<FormTextarea name="message" />);
    const textarea = screen.getByRole('textbox');
    await user.type(textarea, 'Hello World');
    expect(textarea).toHaveValue('Hello World');
  });

  it('forwards ref to textarea element', () => {
    const ref = vi.fn();
    render(<FormTextarea name="message" ref={ref} />);
    expect(ref).toHaveBeenCalled();
  });

  it('passes through HTML textarea attributes', () => {
    render(
      <FormTextarea
        name="message"
        placeholder="Enter message"
        rows={5}
        maxLength={500}
        disabled
      />
    );
    const textarea = screen.getByRole('textbox');
    expect(textarea).toHaveAttribute('placeholder', 'Enter message');
    expect(textarea).toHaveAttribute('rows', '5');
    expect(textarea).toHaveAttribute('maxLength', '500');
    expect(textarea).toBeDisabled();
  });

  it('has resize-none class by default', () => {
    render(<FormTextarea name="message" />);
    const textarea = screen.getByRole('textbox');
    expect(textarea).toHaveClass('resize-none');
  });

  it('uses name as id when id is not provided', () => {
    render(<FormTextarea name="message" label="Message" />);
    const textarea = screen.getByRole('textbox');
    expect(textarea).toHaveAttribute('id', 'message');
  });

  it('uses provided id over name', () => {
    render(<FormTextarea name="message" id="custom-message" label="Message" />);
    const textarea = screen.getByRole('textbox');
    expect(textarea).toHaveAttribute('id', 'custom-message');
  });
});
