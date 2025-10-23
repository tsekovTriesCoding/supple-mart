import { Link } from 'react-router-dom';

import { ArrowRight, Star, ShoppingBag, Truck, Shield, Headphones } from 'lucide-react';

const Home = () => {
  const featuredProducts = [
    {
      id: 1,
      name: "Premium Protein Powder",
      price: "$49.99",
      rating: 4.8,
      image: "https://images.unsplash.com/photo-1593095948071-474c5cc2989d?w=300&h=300&fit=crop&crop=center",
      category: "Supplements"
    },
    {
      id: 2,
      name: "Organic Multivitamin",
      price: "$29.99",
      rating: 4.6,
      image: "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=300&h=300&fit=crop&crop=center",
      category: "Vitamins"
    },
    {
      id: 3,
      name: "Energy Boost Formula",
      price: "$39.99",
      rating: 4.7,
      image: "https://images.unsplash.com/photo-1550572017-edd951b55104?w=300&h=300&fit=crop&crop=center",
      category: "Energy"
    },
  ]

  const features = [
    {
      icon: <Truck className="w-8 h-8 text-blue-400" />,
      title: "Free Shipping",
      description: "Free delivery on orders over $50"
    },
    {
      icon: <Shield className="w-8 h-8 text-blue-400" />,
      title: "Quality Guaranteed",
      description: "100% authentic products"
    },
    {
      icon: <Headphones className="w-8 h-8 text-blue-400" />,
      title: "24/7 Support",
      description: "Expert customer service"
    }
  ]

  return (
    <div className="animate-fade-in">
      <section className="relative py-20 mb-16">
        <div className="absolute inset-0 bg-linear-to-r from-blue-900/20 to-purple-900/20 rounded-2xl"></div>
        <div className="relative text-center">
          <h1 className="text-5xl md:text-6xl font-bold mb-6 bg-linear-to-r from-blue-400 to-purple-400 bg-clip-text text-transparent">
            Premium Health Supplements
          </h1>
          <p className="text-xl md:text-2xl text-gray-300 mb-8 max-w-3xl mx-auto">
            Discover the finest collection of health supplements, vitamins, and wellness products
            to fuel your journey to optimal health.
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Link to="/products" className="btn-primary inline-flex items-center space-x-2">
              <ShoppingBag className="w-5 h-5" />
              <span>Shop Now</span>
              <ArrowRight className="w-5 h-5" />
            </Link>
            <Link to="/about" className="btn-outline inline-flex items-center space-x-2">
              <span>Learn More</span>
            </Link>
          </div>
        </div>
      </section>
      <section className="mb-16">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {features.map((feature, index) => (
            <div key={index} className="card p-6 text-center animate-slide-in" style={{ animationDelay: `${index * 0.1}s` }}>
              <div className="flex justify-center mb-4">
                {feature.icon}
              </div>
              <h3 className="text-xl font-semibold text-white mb-2">{feature.title}</h3>
              <p className="text-gray-400">{feature.description}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="mb-16">
        <div className="text-center mb-12">
          <h2 className="text-3xl md:text-4xl font-bold text-white mb-4">Featured Products</h2>
          <p className="text-gray-400 text-lg">Our most popular supplements trusted by thousands</p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          {featuredProducts.map((product, index) => (
            <div key={product.id} className="card-hover p-6 animate-slide-in" style={{ animationDelay: `${index * 0.1}s` }}>
              <div className="aspect-square bg-gray-800 rounded-lg mb-4 overflow-hidden">
                <img
                  src={product.image}
                  alt={product.name}
                  className="w-full h-full object-cover hover:scale-105 transition-transform duration-300"
                />
              </div>
              <div className="flex justify-between items-start mb-2">
                <span className="text-sm text-blue-400 font-medium">{product.category}</span>
                <div className="flex items-center space-x-1">
                  <Star className="w-4 h-4 text-yellow-400 fill-current" />
                  <span className="text-sm text-gray-300">{product.rating}</span>
                </div>
              </div>
              <h3 className="text-xl font-semibold text-white mb-2">{product.name}</h3>
              <div className="flex justify-between items-center">
                <span className="text-2xl font-bold text-blue-400">{product.price}</span>
                <button className="btn-primary">Add to Cart</button>
              </div>
            </div>
          ))}
        </div>
      </section>
      <section className="text-center py-16">
        <div className="card p-12">
          <h2 className="text-3xl md:text-4xl font-bold text-white mb-4">
            Ready to Start Your Health Journey?
          </h2>
          <p className="text-gray-400 text-lg mb-8 max-w-2xl mx-auto">
            Join thousands of satisfied customers who trust SuppleMart for their health and wellness needs.
          </p>
          <Link to="/products" className="btn-primary inline-flex items-center space-x-2">
            <span>Browse All Products</span>
            <ArrowRight className="w-5 h-5" />
          </Link>
        </div>
      </section>
    </div>
  )
}

export default Home;
