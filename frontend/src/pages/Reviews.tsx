import { useEffect, useState } from 'react';
import { Star, Package, Edit3, Trash2 } from 'lucide-react';

interface Review {
  id: string;
  productId: string;
  productName: string;
  productImage: string;
  rating: number;
  title: string;
  comment: string;
  date: string;
  verified: boolean;
};

const Reviews = () => {
  const [reviews, setReviews] = useState<Review[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      setLoading(false);
      return;
    }
    setTimeout(() => {
      const mockReviews: Review[] = [
        {
          id: '1',
          productId: '1',
          productName: 'Wireless Headphones',
          productImage: '/placeholder-product.jpg',
          rating: 5,
          title: 'Excellent sound quality!',
          comment: 'These headphones exceeded my expectations. The sound quality is amazing and they are very comfortable to wear for long periods.',
          date: '2024-10-20',
          verified: true
        },
        {
          id: '2',
          productId: '2',
          productName: 'Bluetooth Speaker',
          productImage: '/placeholder-product.jpg',
          rating: 4,
          title: 'Great portable speaker',
          comment: 'Good sound quality for the price. Battery life is excellent. Only downside is it could be a bit louder.',
          date: '2024-10-15',
          verified: true
        },
        {
          id: '3',
          productId: '3',
          productName: 'Phone Case',
          productImage: '/placeholder-product.jpg',
          rating: 3,
          title: 'Decent protection',
          comment: 'The case provides good protection but the material feels a bit cheap. Does the job though.',
          date: '2024-10-10',
          verified: false
        }
      ];
      setReviews(mockReviews);
      setLoading(false);
    }, 1000);
  }, []);

  const renderStars = (rating: number) => {
    return (
      <div className="flex items-center space-x-1">
        {[1, 2, 3, 4, 5].map((star) => (
          <Star
            key={star}
            className={`w-4 h-4 ${star <= rating ? 'text-yellow-400 fill-current' : 'text-gray-400'}`}
          />
        ))}
      </div>
    );
  };

  if (!localStorage.getItem('token')) {
    return (
      <div className="min-h-screen flex items-center justify-center" style={{ backgroundColor: '#0a0a0a' }}>
        <div className="text-center">
          <h1 className="text-2xl font-bold text-white mb-4">Please Sign In</h1>
          <p className="text-gray-400">You need to be logged in to view your reviews.</p>
        </div>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center" style={{ backgroundColor: '#0a0a0a' }}>
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto mb-4"></div>
          <p className="text-gray-400">Loading your reviews...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen" style={{ backgroundColor: '#0a0a0a' }}>
      <div className="container mx-auto px-4 py-8">
        <div className="max-w-4xl mx-auto">
          <div className="flex justify-between items-center mb-8">
            <h1 className="text-3xl font-bold text-white">My Reviews</h1>
            <div className="text-gray-400">
              {reviews.length} review{reviews.length !== 1 ? 's' : ''}
            </div>
          </div>
          
          {reviews.length === 0 ? (
            <div className="card p-8 text-center">
              <Star className="w-16 h-16 text-gray-400 mx-auto mb-4" />
              <h2 className="text-xl font-semibold text-white mb-2">No Reviews Yet</h2>
              <p className="text-gray-400 mb-6">You haven't written any reviews yet. Purchase and review products to share your experience!</p>
              <button className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
                Start Shopping
              </button>
            </div>
          ) : (
            <div className="space-y-6">
              {reviews.map((review) => (
                <div key={review.id} className="card p-6">
                  <div className="flex flex-col md:flex-row gap-4">
                    <div className="shrink-0">
                      <div className="w-20 h-20 bg-gray-700 rounded-lg flex items-center justify-center">
                        <Package className="w-8 h-8 text-gray-400" />
                      </div>
                    </div>
                    
                    <div className="flex-1">
                      <div className="flex flex-col md:flex-row md:items-start md:justify-between mb-3">
                        <div>
                          <h3 className="text-lg font-semibold text-white mb-1">{review.productName}</h3>
                          <div className="flex items-center space-x-3 mb-2">
                            {renderStars(review.rating)}
                            {review.verified && (
                              <span className="text-xs bg-green-900 text-green-300 px-2 py-1 rounded-full">
                                Verified Purchase
                              </span>
                            )}
                          </div>
                        </div>
                        
                        <div className="flex items-center space-x-2">
                          <button className="p-2 text-gray-400 hover:text-blue-400 transition-colors">
                            <Edit3 className="w-4 h-4" />
                          </button>
                          <button className="p-2 text-gray-400 hover:text-red-400 transition-colors">
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </div>
                      </div>
                      
                      <h4 className="text-white font-medium mb-2">{review.title}</h4>
                      <p className="text-gray-300 mb-3 leading-relaxed">{review.comment}</p>
                      
                      <div className="flex justify-between items-center text-sm text-gray-400">
                        <span>Reviewed on {new Date(review.date).toLocaleDateString()}</span>
                        <button className="text-blue-400 hover:text-blue-300 transition-colors">
                          View Product
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              ))}

              <div className="card p-6 mt-8">
                <h3 className="text-lg font-semibold text-white mb-4">Review Summary</h3>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                  <div className="text-center">
                    <div className="text-2xl font-bold text-blue-400">
                      {(reviews.reduce((sum, review) => sum + review.rating, 0) / reviews.length).toFixed(1)}
                    </div>
                    <div className="text-gray-400 text-sm">Average Rating</div>
                  </div>
                  
                  <div className="text-center">
                    <div className="text-2xl font-bold text-green-400">
                      {reviews.filter(review => review.verified).length}
                    </div>
                    <div className="text-gray-400 text-sm">Verified Reviews</div>
                  </div>
                  
                  <div className="text-center">
                    <div className="text-2xl font-bold text-yellow-400">
                      {reviews.filter(review => review.rating === 5).length}
                    </div>
                    <div className="text-gray-400 text-sm">5-Star Reviews</div>
                  </div>
                  
                  <div className="text-center">
                    <div className="text-2xl font-bold text-white">
                      {reviews.length}
                    </div>
                    <div className="text-gray-400 text-sm">Total Reviews</div>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Reviews;
