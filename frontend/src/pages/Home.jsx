import { Link } from 'react-router-dom';
import './Home.css';

function Home() {
    return (
        <div className="home">
            {/* Hero Section */}
            <section className="hero">
                <div className="hero-background">
                    <div className="gradient-orb orb-1"></div>
                    <div className="gradient-orb orb-2"></div>
                    <div className="grid-overlay"></div>
                </div>

                <div className="container hero-content animate-slide-up">
                    <h1 className="hero-title">
                        Your Ride,<br /><span className="gradient-text">Your Way</span>
                    </h1>
                    <p className="hero-subtitle">
                        Fast, reliable, and affordable rides at your fingertips.
                        Book your next journey in seconds.
                    </p>
                    <div className="hero-cta">
                        <Link to="/book" className="btn btn-primary btn-lg">
                            <span>🚕</span> Book a Ride
                        </Link>
                        <Link to="/register" className="btn btn-secondary btn-lg">
                            Get Started
                        </Link>
                    </div>

                    <div className="hero-stats">
                        <div className="stat">
                            <span className="stat-value">50K+</span>
                            <span className="stat-label">Happy Riders</span>
                        </div>
                        <div className="stat">
                            <span className="stat-value">10K+</span>
                            <span className="stat-label">Verified Drivers</span>
                        </div>
                        <div className="stat">
                            <span className="stat-value">100+</span>
                            <span className="stat-label">Cities</span>
                        </div>
                    </div>
                </div>
            </section>

            {/* Features Section */}
            <section className="features">
                <div className="container">
                    <h2 className="section-title text-center">Why Choose CabService?</h2>
                    <div className="features-grid">
                        <div className="feature-card glass">
                            <div className="feature-icon">📍</div>
                            <h3>Real-time Tracking</h3>
                            <p>Track your ride in real-time on the map. Know exactly when your driver will arrive.</p>
                        </div>
                        <div className="feature-card glass">
                            <div className="feature-icon">💳</div>
                            <h3>Easy Payments</h3>
                            <p>Pay with card, UPI, or cash. Multiple payment options for your convenience.</p>
                        </div>
                        <div className="feature-card glass">
                            <div className="feature-icon">🔒</div>
                            <h3>Safe Rides</h3>
                            <p>All drivers are verified and trained. Your safety is our top priority.</p>
                        </div>
                        <div className="feature-card glass">
                            <div className="feature-icon">💰</div>
                            <h3>Best Prices</h3>
                            <p>Transparent pricing with no hidden fees. Get fare estimates before booking.</p>
                        </div>
                    </div>
                </div>
            </section>

            {/* Vehicle Types Section */}
            <section className="vehicles">
                <div className="container">
                    <h2 className="section-title text-center">Choose Your Ride</h2>
                    <div className="vehicles-grid">
                        <div className="vehicle-card card">
                            <div className="vehicle-image">🚗</div>
                            <h3>Mini</h3>
                            <p>Budget-friendly rides</p>
                            <span className="price">From ₹40</span>
                        </div>
                        <div className="vehicle-card card featured">
                            <div className="vehicle-badge">Popular</div>
                            <div className="vehicle-image">🚙</div>
                            <h3>Sedan</h3>
                            <p>Comfortable rides</p>
                            <span className="price">From ₹60</span>
                        </div>
                        <div className="vehicle-card card">
                            <div className="vehicle-image">🚐</div>
                            <h3>SUV</h3>
                            <p>Spacious rides</p>
                            <span className="price">From ₹80</span>
                        </div>
                        <div className="vehicle-card card">
                            <div className="vehicle-image">✨</div>
                            <h3>Premium</h3>
                            <p>Luxury rides</p>
                            <span className="price">From ₹120</span>
                        </div>
                    </div>
                </div>
            </section>

            {/* CTA Section */}
            <section className="cta-section">
                <div className="container">
                    <div className="cta-card glass">
                        <h2>Ready to Ride?</h2>
                        <p>Download the app or book online. Your journey starts here.</p>
                        <Link to="/book" className="btn btn-primary btn-lg">
                            Book Now
                        </Link>
                    </div>
                </div>
            </section>

            {/* Footer */}
            <footer className="footer">
                <div className="container">
                    <div className="footer-content">
                        <div className="footer-brand">
                            <span className="logo-icon">🚕</span>
                            <span className="gradient-text">CabService</span>
                        </div>
                        <p className="footer-text">
                            &copy; 2024 CabService. All rights reserved.
                        </p>
                    </div>
                </div>
            </footer>
        </div>
    );
}

export default Home;
