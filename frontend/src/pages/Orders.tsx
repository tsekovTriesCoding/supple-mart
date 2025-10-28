import { useEffect, useState } from 'react';
import { Package, Clock, CheckCircle, XCircle, Eye } from 'lucide-react';

interface Order {
  id: string;
  orderNumber: string;
  date: string;
  status: 'pending' | 'processing' | 'shipped' | 'delivered' | 'cancelled';
  total: number;
  items: {
    id: string;
    name: string;
    quantity: number;
    price: number;
    image: string;
  }[];
};

const Orders = () => {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      setLoading(false);
      return;
    }

    setTimeout(() => {
      const mockOrders: Order[] = [
        {
          id: '1',
          orderNumber: 'ORD-2024-001',
          date: '2024-10-25',
          status: 'delivered',
          total: 299.99,
          items: [
            {
              id: '1',
              name: 'Wireless Headphones',
              quantity: 1,
              price: 199.99,
              image: '/placeholder-product.jpg'
            },
            {
              id: '2',
              name: 'Phone Case',
              quantity: 2,
              price: 50.00,
              image: '/placeholder-product.jpg'
            }
          ]
        },
        {
          id: '2',
          orderNumber: 'ORD-2024-002',
          date: '2024-10-28',
          status: 'processing',
          total: 149.99,
          items: [
            {
              id: '3',
              name: 'Bluetooth Speaker',
              quantity: 1,
              price: 149.99,
              image: '/placeholder-product.jpg'
            }
          ]
        }
      ];
      setOrders(mockOrders);
      setLoading(false);
    }, 1000);
  }, []);

  const getStatusIcon = (status: Order['status']) => {
    switch (status) {
      case 'pending':
        return <Clock className="w-5 h-5 text-yellow-500" />;
      case 'processing':
        return <Package className="w-5 h-5 text-blue-500" />;
      case 'shipped':
        return <Package className="w-5 h-5 text-purple-500" />;
      case 'delivered':
        return <CheckCircle className="w-5 h-5 text-green-500" />;
      case 'cancelled':
        return <XCircle className="w-5 h-5 text-red-500" />;
      default:
        return <Clock className="w-5 h-5 text-gray-500" />;
    }
  };

  const getStatusColor = (status: Order['status']) => {
    switch (status) {
      case 'pending':
        return 'text-yellow-500';
      case 'processing':
        return 'text-blue-500';
      case 'shipped':
        return 'text-purple-500';
      case 'delivered':
        return 'text-green-500';
      case 'cancelled':
        return 'text-red-500';
      default:
        return 'text-gray-500';
    }
  };

  if (!localStorage.getItem('token')) {
    return (
      <div className="min-h-screen flex items-center justify-center" style={{ backgroundColor: '#0a0a0a' }}>
        <div className="text-center">
          <h1 className="text-2xl font-bold text-white mb-4">Please Sign In</h1>
          <p className="text-gray-400">You need to be logged in to view your orders.</p>
        </div>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center" style={{ backgroundColor: '#0a0a0a' }}>
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto mb-4"></div>
          <p className="text-gray-400">Loading your orders...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen" style={{ backgroundColor: '#0a0a0a' }}>
      <div className="container mx-auto px-4 py-8">
        <div className="max-w-4xl mx-auto">
          <h1 className="text-3xl font-bold text-white mb-8">My Orders</h1>
          
          {orders.length === 0 ? (
            <div className="card p-8 text-center">
              <Package className="w-16 h-16 text-gray-400 mx-auto mb-4" />
              <h2 className="text-xl font-semibold text-white mb-2">No Orders Yet</h2>
              <p className="text-gray-400 mb-6">You haven't placed any orders yet. Start shopping to see your orders here!</p>
              <button className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
                Start Shopping
              </button>
            </div>
          ) : (
            <div className="space-y-6">
              {orders.map((order) => (
                <div key={order.id} className="card p-6">
                  <div className="flex flex-col md:flex-row md:items-center md:justify-between mb-4">
                    <div className="flex items-center space-x-3 mb-4 md:mb-0">
                      {getStatusIcon(order.status)}
                      <div>
                        <h3 className="text-lg font-semibold text-white">{order.orderNumber}</h3>
                        <p className="text-gray-400 text-sm">Placed on {new Date(order.date).toLocaleDateString()}</p>
                      </div>
                    </div>
                    
                    <div className="flex items-center space-x-4">
                      <div className="text-right">
                        <p className={`font-medium capitalize ${getStatusColor(order.status)}`}>
                          {order.status}
                        </p>
                        <p className="text-white font-semibold">
                          ${order.total.toFixed(2)}
                        </p>
                      </div>
                      
                      <button className="p-2 text-gray-400 hover:text-white transition-colors">
                        <Eye className="w-5 h-5" />
                      </button>
                    </div>
                  </div>
                  
                  <div className="space-y-3">
                    {order.items.map((item) => (
                      <div key={item.id} className="flex items-center space-x-3 py-2 border-t border-gray-700">
                        <div className="w-12 h-12 bg-gray-700 rounded-lg flex items-center justify-center">
                          <Package className="w-6 h-6 text-gray-400" />
                        </div>
                        
                        <div className="flex-1">
                          <h4 className="text-white font-medium">{item.name}</h4>
                          <p className="text-gray-400 text-sm">Quantity: {item.quantity}</p>
                        </div>
                        
                        <div className="text-right">
                          <p className="text-white font-semibold">${item.price.toFixed(2)}</p>
                        </div>
                      </div>
                    ))}
                  </div>
                  
                  <div className="mt-4 pt-4 border-t border-gray-700 flex justify-between items-center">
                    <button className="text-blue-400 hover:text-blue-300 transition-colors">
                      View Details
                    </button>
                    
                    {order.status === 'delivered' && (
                      <button className="px-4 py-2 border border-gray-600 text-gray-300 rounded-lg hover:border-gray-500 transition-colors">
                        Reorder
                      </button>
                    )}
                    
                    {order.status === 'pending' && (
                      <button className="px-4 py-2 border border-red-600 text-red-400 rounded-lg hover:border-red-500 transition-colors">
                        Cancel Order
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Orders;
