import { Link } from 'react-router-dom'
import { Star, Award, Users, Truck, Shield, Heart, Target, Zap, Globe } from 'lucide-react'

const About = () => {
  const stats = [
    { icon: <Users className="w-8 h-8 text-blue-400" />, number: '50,000+', label: 'Happy Customers' },
    { icon: <Award className="w-8 h-8 text-blue-400" />, number: '1,000+', label: 'Premium Products' },
    { icon: <Star className="w-8 h-8 text-blue-400" />, number: '4.9', label: 'Average Rating' },
    { icon: <Globe className="w-8 h-8 text-blue-400" />, number: '15', label: 'Countries Served' }
  ]

  const values = [
    {
      icon: <Heart className="w-12 h-12 text-blue-400" />,
      title: 'Health First',
      description: 'We prioritize your health and wellness above all else, offering only the highest quality supplements that meet rigorous safety standards.'
    },
    {
      icon: <Shield className="w-12 h-12 text-blue-400" />,
      title: 'Quality Assurance',
      description: 'Every product undergoes extensive testing and quality control measures to ensure you receive authentic, effective supplements.'
    },
    {
      icon: <Target className="w-12 h-12 text-blue-400" />,
      title: 'Personalized Approach',
      description: 'We understand that every wellness journey is unique. Our experts help you find the right products for your specific needs.'
    },
    {
      icon: <Zap className="w-12 h-12 text-blue-400" />,
      title: 'Innovation',
      description: 'We stay at the forefront of nutritional science, continuously updating our product line with the latest breakthroughs.'
    }
  ]

  const teamMembers = [
    {
      name: 'Dr. Sarah Johnson',
      role: 'Chief Nutritionist',
      image: 'https://images.unsplash.com/photo-1559839734-2b71ea197ec2?w=300&h=300&fit=crop&crop=center',
      bio: '15+ years in nutritional science with a PhD from Harvard Medical School.'
    },
    {
      name: 'Michael Chen',
      role: 'Product Quality Manager',
      image: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=300&h=300&fit=crop&crop=center',
      bio: 'Former FDA inspector with expertise in supplement manufacturing standards.'
    },
    {
      name: 'Emily Rodriguez',
      role: 'Customer Wellness Advisor',
      image: 'https://images.unsplash.com/photo-1494790108755-2616b332c371?w=300&h=300&fit=crop&crop=center',
      bio: 'Certified nutritionist helping customers achieve their health goals.'
    }
  ]

  const milestones = [
    { year: '2018', title: 'Company Founded', description: 'Started with a mission to make premium supplements accessible to everyone.' },
    { year: '2019', title: 'First 1,000 Customers', description: 'Reached our first milestone and established our reputation for quality.' },
    { year: '2021', title: 'International Expansion', description: 'Expanded to serve customers across 15 countries worldwide.' },
    { year: '2023', title: 'Premium Certification', description: 'Achieved the highest industry certifications for quality and safety.' },
    { year: '2024', title: '50,000+ Happy Customers', description: 'Celebrating a community of health-conscious individuals.' }
  ]

  return (
    <div className="animate-fade-in">
      <section className="relative py-20 mb-16">
        <div className="absolute inset-0 bg-linear-to-r from-blue-900/20 to-purple-900/20 rounded-2xl"></div>
        <div className="relative text-center">
          <h1 className="text-5xl md:text-6xl font-bold mb-6 bg-linear-to-r from-blue-400 to-purple-400 bg-clip-text text-transparent">
            About SuppleMart
          </h1>
          <p className="text-xl md:text-2xl text-gray-300 mb-8 max-w-4xl mx-auto">
            Your trusted partner in health and wellness, dedicated to providing premium supplements
            that empower you to live your best life.
          </p>
        </div>
      </section>

      <section className="mb-16">
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-8">
          {stats.map((stat, index) => (
            <div key={index} className="card p-6 text-center animate-slide-in" style={{ animationDelay: `${index * 0.1}s` }}>
              <div className="flex justify-center mb-4">
                {stat.icon}
              </div>
              <h3 className="text-3xl font-bold text-white mb-2">{stat.number}</h3>
              <p className="text-gray-400">{stat.label}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="mb-16">
        <div className="card p-12">
          <div className="max-w-4xl mx-auto text-center">
            <h2 className="text-3xl md:text-4xl font-bold text-white mb-6">Our Mission</h2>
            <p className="text-lg text-gray-300 mb-8 leading-relaxed">
              At SuppleMart, we believe that everyone deserves access to high-quality, science-backed supplements
              that support their unique health goals. Our mission is to bridge the gap between cutting-edge
              nutritional science and everyday wellness, making premium supplements accessible, affordable,
              and trustworthy for health-conscious individuals worldwide.
            </p>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mt-12">
              <div className="text-center">
                <Truck className="w-12 h-12 text-blue-400 mx-auto mb-4" />
                <h3 className="text-xl font-semibold text-white mb-2">Fast & Reliable</h3>
                <p className="text-gray-400">Quick delivery with careful packaging to preserve product quality.</p>
              </div>
              <div className="text-center">
                <Shield className="w-12 h-12 text-blue-400 mx-auto mb-4" />
                <h3 className="text-xl font-semibold text-white mb-2">Tested & Verified</h3>
                <p className="text-gray-400">All products undergo rigorous third-party testing for purity and potency.</p>
              </div>
              <div className="text-center">
                <Heart className="w-12 h-12 text-blue-400 mx-auto mb-4" />
                <h3 className="text-xl font-semibold text-white mb-2">Customer Care</h3>
                <p className="text-gray-400">Dedicated support team to help you on your wellness journey.</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section className="mb-16">
        <div className="text-center mb-12">
          <h2 className="text-3xl md:text-4xl font-bold text-white mb-4">Our Values</h2>
          <p className="text-gray-400 text-lg">The principles that guide everything we do</p>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          {values.map((value, index) => (
            <div key={index} className="card-hover p-8 animate-slide-in" style={{ animationDelay: `${index * 0.1}s` }}>
              <div className="flex items-start space-x-6">
                <div className="shrink-0">
                  {value.icon}
                </div>
                <div>
                  <h3 className="text-xl font-semibold text-white mb-3">{value.title}</h3>
                  <p className="text-gray-400 leading-relaxed">{value.description}</p>
                </div>
              </div>
            </div>
          ))}
        </div>
      </section>

      <section className="mb-16">
        <div className="text-center mb-12">
          <h2 className="text-3xl md:text-4xl font-bold text-white mb-4">Meet Our Team</h2>
          <p className="text-gray-400 text-lg">Experts dedicated to your health and wellness</p>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {teamMembers.map((member, index) => (
            <div key={index} className="card-hover p-6 text-center animate-slide-in" style={{ animationDelay: `${index * 0.1}s` }}>
              <div className="aspect-square bg-gray-800 rounded-full mb-6 overflow-hidden mx-auto w-32 h-32">
                <img
                  src={member.image}
                  alt={member.name}
                  className="w-full h-full object-cover"
                />
              </div>
              <h3 className="text-xl font-semibold text-white mb-2">{member.name}</h3>
              <p className="text-blue-400 font-medium mb-3">{member.role}</p>
              <p className="text-gray-400 text-sm leading-relaxed">{member.bio}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="mb-16">
        <div className="text-center mb-12">
          <h2 className="text-3xl md:text-4xl font-bold text-white mb-4">Our Journey</h2>
          <p className="text-gray-400 text-lg">Key milestones in our mission to serve you better</p>
        </div>
        <div className="relative">
          <div className="absolute left-1/2 transform -translate-x-1/2 w-1 h-full bg-blue-400/20"></div>
          <div className="space-y-8">
            {milestones.map((milestone, index) => (
              <div key={index} className={`flex items-center animate-slide-in ${index % 2 === 0 ? 'justify-start' : 'justify-end'}`} style={{ animationDelay: `${index * 0.2}s` }}>
                <div className={`w-5/12 ${index % 2 === 0 ? 'pr-8 text-right' : 'pl-8'}`}>
                  <div className="card p-6">
                    <div className="text-2xl font-bold text-blue-400 mb-2">{milestone.year}</div>
                    <h3 className="text-xl font-semibold text-white mb-2">{milestone.title}</h3>
                    <p className="text-gray-400">{milestone.description}</p>
                  </div>
                </div>
                <div className="absolute left-1/2 transform -translate-x-1/2 w-4 h-4 bg-blue-400 rounded-full border-4 border-gray-900"></div>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="text-center py-16">
        <div className="card p-12">
          <h2 className="text-3xl md:text-4xl font-bold text-white mb-4">
            Ready to Start Your Wellness Journey?
          </h2>
          <p className="text-gray-400 text-lg mb-8 max-w-2xl mx-auto">
            Join thousands of satisfied customers who trust SuppleMart for their health and wellness needs.
            Experience the difference quality makes.
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Link to="/products" className="btn-primary inline-flex items-center space-x-2">
              <span>Shop Now</span>
            </Link>
            <Link to="/contact" className="btn-outline inline-flex items-center space-x-2">
              <span>Contact Us</span>
            </Link>
          </div>
        </div>
      </section>
    </div>
  )
}

export default About;
