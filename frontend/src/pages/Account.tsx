import { useEffect, useState } from 'react';
import { User, Mail, MapPin, Phone } from 'lucide-react';

interface UserProfile {
  id: string;
  name: string;
  email: string;
  phone?: string;
  address?: string;
};

const Account = () => {
  const [user, setUser] = useState<UserProfile | null>(null);

  useEffect(() => {
    const userData = localStorage.getItem('user');
    if (userData) {
      setUser(JSON.parse(userData));
    }
  }, []);

  if (!user) {
    return (
      <div className="min-h-screen flex items-center justify-center" style={{ backgroundColor: '#0a0a0a' }}>
        <div className="text-center">
          <h1 className="text-2xl font-bold text-white mb-4">Please Sign In</h1>
          <p className="text-gray-400">You need to be logged in to access your account.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen" style={{ backgroundColor: '#0a0a0a' }}>
      <div className="container mx-auto px-4 py-8">
        <div className="max-w-4xl mx-auto">
          <h1 className="text-3xl font-bold text-white mb-8">My Account</h1>
          
          <div className="grid gap-6 md:grid-cols-2">
            <div className="card p-6">
              <h2 className="text-xl font-semibold text-white mb-4 flex items-center">
                <User className="w-5 h-5 mr-2 text-blue-400" />
                Profile Information
              </h2>
              
              <div className="space-y-4">
                <div className="flex items-center space-x-3">
                  <User className="w-5 h-5 text-gray-400" />
                  <div>
                    <p className="text-sm text-gray-400">Full Name</p>
                    <p className="text-white">{user.name}</p>
                  </div>
                </div>
                
                <div className="flex items-center space-x-3">
                  <Mail className="w-5 h-5 text-gray-400" />
                  <div>
                    <p className="text-sm text-gray-400">Email</p>
                    <p className="text-white">{user.email}</p>
                  </div>
                </div>
                
                <div className="flex items-center space-x-3">
                  <Phone className="w-5 h-5 text-gray-400" />
                  <div>
                    <p className="text-sm text-gray-400">Phone</p>
                    <p className="text-white">{user.phone || 'Not provided'}</p>
                  </div>
                </div>
                
                <div className="flex items-center space-x-3">
                  <MapPin className="w-5 h-5 text-gray-400" />
                  <div>
                    <p className="text-sm text-gray-400">Address</p>
                    <p className="text-white">{user.address || 'Not provided'}</p>
                  </div>
                </div>
              </div>
              
              <button className="mt-6 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
                Edit Profile
              </button>
            </div>
            
            <div className="card p-6">
              <h2 className="text-xl font-semibold text-white mb-4">Account Settings</h2>
              
              <div className="space-y-3">
                <button className="w-full text-left p-3 rounded-lg border border-gray-700 hover:border-gray-600 transition-colors">
                  <h3 className="text-white font-medium">Change Password</h3>
                  <p className="text-gray-400 text-sm">Update your account password</p>
                </button>
                
                <button className="w-full text-left p-3 rounded-lg border border-gray-700 hover:border-gray-600 transition-colors">
                  <h3 className="text-white font-medium">Notification Preferences</h3>
                  <p className="text-gray-400 text-sm">Manage email and push notifications</p>
                </button>
                
                <button className="w-full text-left p-3 rounded-lg border border-gray-700 hover:border-gray-600 transition-colors">
                  <h3 className="text-white font-medium">Privacy Settings</h3>
                  <p className="text-gray-400 text-sm">Control your privacy preferences</p>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Account;
