import { Routes, Route } from 'react-router-dom';

import Header from './components/Header';
import Home from './pages/Home';
import Products from './pages/Products';
import Cart from './pages/Cart';
import Checkout from './pages/Checkout';
import Login from './pages/Login';
import Register from './pages/Register';
import About from './pages/About';
import Contact from './pages/Contact';
import Account from './pages/Account';
import Orders from './pages/Orders';
import Reviews from './pages/Reviews';
import { CartProvider } from './hooks';

function App() {
  return (
    <CartProvider>
      <div className="min-h-screen" style={{ backgroundColor: '#0a0a0a' }}>
        <Header />
        <main className="container mx-auto px-4 py-8">
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/products" element={<Products />} />
            <Route path="/cart" element={<Cart />} />
            <Route path="/checkout" element={<Checkout />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/about" element={<About />} />
            <Route path="/contact" element={<Contact />} />
            <Route path="/account" element={<Account />} />
            <Route path="/orders" element={<Orders />} />
            <Route path="/reviews" element={<Reviews />} />
          </Routes>
        </main>
      </div>
    </CartProvider>
  );
}

export default App;
