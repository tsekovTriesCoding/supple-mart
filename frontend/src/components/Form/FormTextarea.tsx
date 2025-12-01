import { forwardRef } from 'react';
import type { TextareaHTMLAttributes } from 'react';

interface FormTextareaProps extends TextareaHTMLAttributes<HTMLTextAreaElement> {
  label?: string;
  error?: string;
  hint?: string;
  required?: boolean;
  containerClassName?: string;
}

export const FormTextarea = forwardRef<HTMLTextAreaElement, FormTextareaProps>(
  ({ label, error, hint, required, containerClassName = '', className = '', id, ...props }, ref) => {
    const textareaId = id || props.name;

    return (
      <div className={containerClassName}>
        {label && (
          <label 
            htmlFor={textareaId} 
            className="block text-sm font-medium text-gray-400 mb-2"
          >
            {label}
            {required && <span className="text-red-400 ml-1">*</span>}
          </label>
        )}
        <textarea
          ref={ref}
          id={textareaId}
          className={`input w-full resize-none ${error ? 'border-red-500' : ''} ${className}`}
          {...props}
        />
        {error && (
          <p className="mt-1 text-sm text-red-400">{error}</p>
        )}
        {hint && !error && (
          <p className="mt-1 text-xs text-gray-500">{hint}</p>
        )}
      </div>
    );
  }
);

FormTextarea.displayName = 'FormTextarea';
