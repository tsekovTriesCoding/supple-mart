import { Routes, Route } from 'react-router-dom';

import Header from './components/Header/Header';
import AdminRoute from './components/AdminRoute';
import Home from './pages/Home';
import Products from './pages/Products';
import Cart from './pages/Cart';
import Checkout from './pages/Checkout';
import About from './pages/About';
import Contact from './pages/Contact';
import Account from './pages/Account';
import Orders from './pages/Orders';
import Reviews from './pages/Reviews';
import AdminDashboard from './pages/admin/AdminDashboard';
import AdminProducts from './pages/admin/AdminProducts';
import AdminOrders from './pages/admin/AdminOrders';
import AdminUsers from './pages/admin/AdminUsers';
import { CartProvider } from './hooks';

function App() {
  return (
    <CartProvider>
      <Routes>
        <Route
          path="/admin/*"
          element={
            <AdminRoute>
              <Routes>
                <Route path="/" element={<AdminDashboard />} />
                <Route path="/products" element={<AdminDashboard />}>
                  <Route index element={<AdminProducts />} />
                </Route>
                <Route path="/orders" element={<AdminDashboard />}>
                  <Route index element={<AdminOrders />} />
                </Route>
                <Route path="/users" element={<AdminDashboard />}>
                  <Route index element={<AdminUsers />} />
                </Route>
              </Routes>
            </AdminRoute>
          }
        />

        <Route
          path="/*"
          element={
            <div className="min-h-screen" style={{ backgroundColor: '#0a0a0a' }}>
              <Header />
              <main className="container mx-auto px-4 py-8">
                <Routes>
                  <Route path="/" element={<Home />} />
                  <Route path="/products" element={<Products />} />
                  <Route path="/cart" element={<Cart />} />
                  <Route path="/checkout" element={<Checkout />} />
                  <Route path="/about" element={<About />} />
                  <Route path="/contact" element={<Contact />} />
                  <Route path="/account" element={<Account />} />
                  <Route path="/orders" element={<Orders />} />
                  <Route path="/reviews" element={<Reviews />} />
                </Routes>
              </main>
            </div>
          }
        />
      </Routes>
    </CartProvider>
  );
}

export default App;
