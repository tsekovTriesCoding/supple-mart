import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Mail, Phone, MapPin, Clock, Send, CheckCircle } from 'lucide-react'

interface ContactForm {
  name: string
  email: string
  subject: string
  message: string
}

const Contact = () => {
  const [isSubmitted, setIsSubmitted] = useState(false)
  const [isLoading, setIsLoading] = useState(false)

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm<ContactForm>()

  const contactInfo = [
    {
      icon: <Mail className="w-6 h-6 text-blue-400" />,
      title: 'Email Us',
      content: 'support@supplemart.com',
      description: 'Get in touch via email'
    },
    {
      icon: <Phone className="w-6 h-6 text-blue-400" />,
      title: 'Call Us',
      content: '+1 (555) 123-4567',
      description: 'Mon-Fri 9AM-6PM EST'
    },
    {
      icon: <MapPin className="w-6 h-6 text-blue-400" />,
      title: 'Visit Us',
      content: '123 Wellness Street',
      description: 'New York, NY 10001'
    },
    {
      icon: <Clock className="w-6 h-6 text-blue-400" />,
      title: 'Business Hours',
      content: 'Mon-Fri: 9AM-6PM',
      description: 'Weekend: 10AM-4PM'
    }
  ]

  const onSubmit = async (data: ContactForm) => {
    setIsLoading(true)

    try {
      await new Promise(resolve => setTimeout(resolve, 1000))

      console.log('Contact form submitted:', data)
      setIsSubmitted(true)
      reset()

      setTimeout(() => setIsSubmitted(false), 5000)
    } catch (error) {
      console.error('Failed to submit contact form:', error)
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="animate-fade-in">
      <section className="text-center mb-16">
        <h1 className="text-4xl md:text-5xl font-bold mb-4 bg-linear-to-r from-blue-400 to-purple-400 bg-clip-text text-transparent">
          Get In Touch
        </h1>
        <p className="text-xl text-gray-300 max-w-2xl mx-auto">
          Have questions about our products or need personalized recommendations?
          We're here to help you on your wellness journey.
        </p>
      </section>

      <section className="mb-16">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {contactInfo.map((info, index) => (
            <div
              key={index}
              className="card-hover p-6 text-center animate-slide-in"
              style={{ animationDelay: `${index * 0.1}s` }}
            >
              <div className="flex justify-center mb-4">
                {info.icon}
              </div>
              <h3 className="text-lg font-semibold text-white mb-2">{info.title}</h3>
              <p className="text-blue-400 font-medium mb-1">{info.content}</p>
              <p className="text-gray-400 text-sm">{info.description}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="grid grid-cols-1 lg:grid-cols-2 gap-12 mb-16">
        <div className="animate-slide-in" style={{ animationDelay: '0.2s' }}>
          <div className="card p-8">
            <h2 className="text-2xl font-bold text-white mb-6">Send us a Message</h2>

            {isSubmitted && (
              <div className="mb-6 p-4 bg-green-900/20 border border-green-700 rounded-lg animate-fade-in">
                <div className="flex items-center space-x-2">
                  <CheckCircle className="w-5 h-5 text-green-400" />
                  <p className="text-green-400 font-medium">Message sent successfully!</p>
                </div>
                <p className="text-green-300 text-sm mt-1">We'll get back to you within 24 hours.</p>
              </div>
            )}

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
              <div>
                <label htmlFor="name" className="block text-sm font-medium text-white mb-2">
                  Full Name
                </label>
                <input
                  {...register('name', {
                    required: 'Name is required',
                    minLength: {
                      value: 2,
                      message: 'Name must be at least 2 characters'
                    }
                  })}
                  type="text"
                  className="input w-full"
                  placeholder="Enter your full name"
                />
                {errors.name && (
                  <p className="mt-1 text-sm text-red-400">{errors.name.message}</p>
                )}
              </div>

              <div>
                <label htmlFor="email" className="block text-sm font-medium text-white mb-2">
                  Email Address
                </label>
                <input
                  {...register('email', {
                    required: 'Email is required',
                    pattern: {
                      value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                      message: 'Invalid email address'
                    }
                  })}
                  type="email"
                  className="input w-full"
                  placeholder="Enter your email address"
                />
                {errors.email && (
                  <p className="mt-1 text-sm text-red-400">{errors.email.message}</p>
                )}
              </div>

              <div>
                <label htmlFor="subject" className="block text-sm font-medium text-white mb-2">
                  Subject
                </label>
                <select
                  {...register('subject', { required: 'Please select a subject' })}
                  className="input w-full"
                >
                  <option value="">Select a subject</option>
                  <option value="product-inquiry">Product Inquiry</option>
                  <option value="order-support">Order Support</option>
                  <option value="general-question">General Question</option>
                  <option value="partnership">Partnership</option>
                  <option value="feedback">Feedback</option>
                  <option value="other">Other</option>
                </select>
                {errors.subject && (
                  <p className="mt-1 text-sm text-red-400">{errors.subject.message}</p>
                )}
              </div>

              <div>
                <label htmlFor="message" className="block text-sm font-medium text-white mb-2">
                  Message
                </label>
                <textarea
                  {...register('message', {
                    required: 'Message is required',
                    minLength: {
                      value: 10,
                      message: 'Message must be at least 10 characters'
                    }
                  })}
                  rows={5}
                  className="input w-full resize-none"
                  placeholder="Tell us how we can help you..."
                />
                {errors.message && (
                  <p className="mt-1 text-sm text-red-400">{errors.message.message}</p>
                )}
              </div>

              <button
                type="submit"
                disabled={isLoading || isSubmitted}
                className="btn-primary w-full inline-flex items-center justify-center space-x-2 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {isLoading ? (
                  <>
                    <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                    <span>Sending...</span>
                  </>
                ) : (
                  <>
                    <Send className="w-4 h-4" />
                    <span>Send Message</span>
                  </>
                )}
              </button>
            </form>
          </div>
        </div>

        <div className="animate-slide-in" style={{ animationDelay: '0.4s' }}>
          <div className="space-y-8">
            <div className="card p-8">
              <h2 className="text-2xl font-bold text-white mb-6">Frequently Asked Questions</h2>
              <div className="space-y-4">
                <div>
                  <h3 className="text-lg font-semibold text-white mb-2">How long does shipping take?</h3>
                  <p className="text-gray-400">Standard shipping takes 3-5 business days. Express shipping is available for next-day delivery.</p>
                </div>
                <div>
                  <h3 className="text-lg font-semibold text-white mb-2">Do you offer product samples?</h3>
                  <p className="text-gray-400">Yes! We offer sample packs for many of our products so you can try before committing to a full-size purchase.</p>
                </div>
                <div>
                  <h3 className="text-lg font-semibold text-white mb-2">What's your return policy?</h3>
                  <p className="text-gray-400">We offer a 30-day money-back guarantee on all products. If you're not satisfied, return for a full refund.</p>
                </div>
              </div>
            </div>

            <div className="card p-8">
              <h2 className="text-2xl font-bold text-white mb-4">Response Times</h2>
              <div className="space-y-3">
                <div className="flex justify-between items-center">
                  <span className="text-gray-300">Email Support</span>
                  <span className="text-blue-400 font-medium">Within 24 hours</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-gray-300">Phone Support</span>
                  <span className="text-blue-400 font-medium">Immediate</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-gray-300">Live Chat</span>
                  <span className="text-blue-400 font-medium">Mon-Fri 9AM-6PM</span>
                </div>
              </div>
            </div>

            <div className="card p-8 border-l-4 border-blue-400">
              <h3 className="text-lg font-bold text-white mb-2">Need Immediate Help?</h3>
              <p className="text-gray-400 mb-4">
                For urgent matters related to product safety or adverse reactions, please contact us immediately.
              </p>
              <p className="text-blue-400 font-medium">Emergency Line: +1 (555) 999-0000</p>
              <p className="text-gray-400 text-sm mt-1">Available 24/7</p>
            </div>
          </div>
        </div>
      </section>
    </div>
  )
}

export default Contact;
