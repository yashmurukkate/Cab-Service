import { useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthContext } from '../App';
import './Navbar.css';

function Navbar() {
    const { user, logout } = useContext(AuthContext);
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/');
    };

    return (
        <nav className="navbar glass">
            <div className="navbar-container">
                <Link to="/" className="navbar-logo">
                    <span className="logo-icon">🚕</span>
                    <span className="gradient-text">CabService</span>
                </Link>

                <div className="navbar-links">
                    <Link to="/" className="nav-link">Home</Link>
                    {user ? (
                        <>
                            <Link to="/book" className="nav-link">Book Ride</Link>
                            <Link to="/rides" className="nav-link">My Rides</Link>
                            {user.role === 'DRIVER' && (
                                <Link to="/driver" className="nav-link">Driver Dashboard</Link>
                            )}
                            <div className="navbar-user">
                                <div className="user-avatar">
                                    {user.firstName?.[0]?.toUpperCase() || 'U'}
                                </div>
                                <span className="user-name">{user.firstName}</span>
                                <button onClick={handleLogout} className="btn-logout">
                                    Logout
                                </button>
                            </div>
                        </>
                    ) : (
                        <div className="navbar-auth">
                            <Link to="/login" className="btn btn-secondary">Login</Link>
                            <Link to="/register" className="btn btn-primary">Sign Up</Link>
                        </div>
                    )}
                </div>
            </div>
        </nav>
    );
}

export default Navbar;
