import { useNavigate } from 'react-router-dom'
import './Landing.css'

const Landing = () => {
  const navigate = useNavigate()

  return (
    <div className="landing-page">
      <nav className="landing-nav">
        <div className="nav-brand">
          <h1>🍔 JustEat</h1>
        </div>
        <div className="nav-buttons">
          <button className="btn-outline" onClick={() => navigate('/login')}>
            Login
          </button>
          <button className="btn-primary" onClick={() => navigate('/register')}>
            Sign Up
          </button>
        </div>
      </nav>

      <section className="hero-section">
        <div className="hero-content">
          <h1 className="hero-title">
            Delicious Food, Delivered <span className="highlight">Fast</span>
          </h1>
          <p className="hero-subtitle">
            Order from your favorite restaurants and get food delivered to your doorstep in minutes
          </p>
          <div className="hero-buttons">
            <button className="btn-large btn-primary" onClick={() => navigate('/register')}>
              Get Started
            </button>
            <button className="btn-large btn-outline" onClick={() => navigate('/login')}>
              I have an account
            </button>
          </div>
        </div>
        <div className="hero-image">
          <div className="food-icon">🍕🍔🍜🌮🍱🥗</div>
        </div>
      </section>

      <section className="features-section">
        <h2 className="section-title">Why Choose JustEat?</h2>
        <div className="features-grid">
          <div className="feature-card">
            <div className="feature-icon">🏪</div>
            <h3>Wide Selection</h3>
            <p>Browse hundreds of restaurants and cuisines in your area</p>
          </div>
          <div className="feature-card">
            <div className="feature-icon">⚡</div>
            <h3>Fast Delivery</h3>
            <p>Get your favorite food delivered hot and fresh in no time</p>
          </div>
          <div className="feature-card">
            <div className="feature-icon">🔒</div>
            <h3>Secure Payments</h3>
            <p>Multiple payment options with bank-level security</p>
          </div>
          <div className="feature-card">
            <div className="feature-icon">⭐</div>
            <h3>Top Rated</h3>
            <p>Read reviews and ratings from real customers</p>
          </div>
          <div className="feature-card">
            <div className="feature-icon">📱</div>
            <h3>Track Orders</h3>
            <p>Real-time order tracking from kitchen to doorstep</p>
          </div>
          <div className="feature-card">
            <div className="feature-icon">💰</div>
            <h3>Best Deals</h3>
            <p>Exclusive offers and deals on your favorite meals</p>
          </div>
        </div>
      </section>

      <section className="cta-section">
        <h2>Ready to Order?</h2>
        <p>Join thousands of food lovers today!</p>
        <div className="cta-buttons">
          <button className="btn-large btn-primary" onClick={() => navigate('/register')}>
            Sign Up as Customer
          </button>
          <button className="btn-large btn-secondary" onClick={() => navigate('/register')}>
            Register Your Restaurant
          </button>
        </div>
      </section>

      <footer className="landing-footer">
        <div className="footer-content">
          <div className="footer-section">
            <h3>🍔 JustEat</h3>
            <p>Your favorite food, delivered fast</p>
          </div>
          <div className="footer-section">
            <h4>Quick Links</h4>
            <ul>
              <li><a href="#features">Features</a></li>
              <li><a href="#about">About Us</a></li>
              <li><a href="#contact">Contact</a></li>
            </ul>
          </div>
          <div className="footer-section">
            <h4>Legal</h4>
            <ul>
              <li><a href="#privacy">Privacy Policy</a></li>
              <li><a href="#terms">Terms of Service</a></li>
            </ul>
          </div>
        </div>
        <div className="footer-bottom">
          <p>&copy; 2026 JustEat. All rights reserved.</p>
        </div>
      </footer>
    </div>
  )
}

export default Landing
