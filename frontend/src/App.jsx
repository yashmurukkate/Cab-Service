import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useState, useEffect, createContext } from 'react';
import Navbar from './components/Navbar';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import BookRide from './pages/BookRide';
import MyRides from './pages/MyRides';
import DriverDashboard from './pages/DriverDashboard';
import './App.css';

export const AuthContext = createContext(null);

function App() {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const token = localStorage.getItem('token');
        const userData = localStorage.getItem('user');
        if (token && userData) {
            setUser(JSON.parse(userData));
        }
        setLoading(false);
    }, []);

    const login = (userData, token) => {
        localStorage.setItem('token', token);
        localStorage.setItem('user', JSON.stringify(userData));
        setUser(userData);
    };

    const logout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        setUser(null);
    };

    if (loading) {
        return (
            <div className="loading-screen">
                <div className="loader"></div>
            </div>
        );
    }

    return (
        <AuthContext.Provider value={{ user, login, logout }}>
            <BrowserRouter>
                <div className="app">
                    <Navbar />
                    <main className="main-content">
                        <Routes>
                            <Route path="/" element={<Home />} />
                            <Route path="/login" element={user ? <Navigate to="/book" /> : <Login />} />
                            <Route path="/register" element={user ? <Navigate to="/book" /> : <Register />} />
                            <Route path="/book" element={user ? <BookRide /> : <Navigate to="/login" />} />
                            <Route path="/rides" element={user ? <MyRides /> : <Navigate to="/login" />} />
                            <Route path="/driver" element={user?.role === 'DRIVER' ? <DriverDashboard /> : <Navigate to="/" />} />
                        </Routes>
                    </main>
                </div>
            </BrowserRouter>
        </AuthContext.Provider>
    );
}

export default App;
