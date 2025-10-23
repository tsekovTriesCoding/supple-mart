import { Routes, Route } from 'react-router-dom';

import Header from './components/Header';
import Home from './pages/Home';
import Products from './pages/Products';
import Login from './pages/Login';

function App() {
  return (
    <div className="min-h-screen" style={{ backgroundColor: '#0a0a0a' }}>
      <Header />
      <main className="container mx-auto px-4 py-8">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/products" element={<Products />} />
          <Route path="/login" element={<Login />} />
        </Routes>
      </main>
    </div>
  )
}

export default App;
