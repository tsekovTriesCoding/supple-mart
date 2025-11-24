import { api } from './index';
import type { ContactFormData, ContactResponse } from '../../types/contact';

class ContactAPI {
  async submitContactForm(formData: ContactFormData): Promise<ContactResponse> {
    const { data } = await api.post('contact', formData);
    return data;
  }
}

export const contactAPI = new ContactAPI();
