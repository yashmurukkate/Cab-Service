import { useState, useEffect } from 'react';
import { cabApi } from '../services/api';
import './DriverDashboard.css';

function DriverDashboard() {
    const [status, setStatus] = useState('OFFLINE');
    const [stats, setStats] = useState({ todayRides: 0, todayEarnings: 0, rating: 0 });
    const [rideRequests, setRideRequests] = useState([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        // Start location updates when online
        let locationInterval;
        if (status === 'AVAILABLE') {
            updateLocation();
            locationInterval = setInterval(updateLocation, 30000); // Update every 30 seconds
        }
        return () => clearInterval(locationInterval);
    }, [status]);

    const updateLocation = () => {
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(
                async (position) => {
                    try {
                        await cabApi.updateLocation(position.coords.latitude, position.coords.longitude);
                    } catch (error) {
                        console.error('Failed to update location:', error);
                    }
                },
                (error) => console.error('Location error:', error)
            );
        }
    };

    const toggleStatus = async () => {
        const newStatus = status === 'OFFLINE' ? 'AVAILABLE' : 'OFFLINE';
        setLoading(true);
        try {
            await cabApi.updateStatus(newStatus);
            setStatus(newStatus);
        } catch (error) {
            console.error('Failed to update status:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleAcceptRide = (rideId) => {
        // Handle ride acceptance
        setRideRequests(prev => prev.filter(r => r.id !== rideId));
        setStatus('BUSY');
    };

    const handleRejectRide = (rideId) => {
        setRideRequests(prev => prev.filter(r => r.id !== rideId));
    };

    return (
        <div className="driver-dashboard">
            <div className="container">
                <div className="dashboard-header">
                    <h1>Driver Dashboard</h1>
                    <button
                        className={`status-toggle ${status === 'AVAILABLE' ? 'online' : 'offline'}`}
                        onClick={toggleStatus}
                        disabled={loading}
                    >
                        <span className="status-indicator"></span>
                        {status === 'AVAILABLE' ? 'Online' : 'Offline'}
                    </button>
                </div>

                <div className="stats-grid">
                    <div className="stat-card glass">
                        <span className="stat-icon">🚗</span>
                        <div className="stat-content">
                            <span className="stat-value">{stats.todayRides}</span>
                            <span className="stat-label">Today's Rides</span>
                        </div>
                    </div>
                    <div className="stat-card glass">
                        <span className="stat-icon">💰</span>
                        <div className="stat-content">
                            <span className="stat-value">₹{stats.todayEarnings}</span>
                            <span className="stat-label">Today's Earnings</span>
                        </div>
                    </div>
                    <div className="stat-card glass">
                        <span className="stat-icon">⭐</span>
                        <div className="stat-content">
                            <span className="stat-value">{stats.rating.toFixed(1)}</span>
                            <span className="stat-label">Rating</span>
                        </div>
                    </div>
                </div>

                {status === 'AVAILABLE' && (
                    <div className="ride-requests-section">
                        <h2>Incoming Ride Requests</h2>

                        {rideRequests.length === 0 ? (
                            <div className="no-requests glass">
                                <div className="pulse-animation"></div>
                                <p>Waiting for ride requests...</p>
                            </div>
                        ) : (
                            <div className="requests-list">
                                {rideRequests.map((request) => (
                                    <div key={request.id} className="request-card card">
                                        <div className="request-header">
                                            <span className="request-distance">{request.distanceToPickup} km away</span>
                                            <span className="request-fare">₹{request.estimatedFare}</span>
                                        </div>

                                        <div className="request-route">
                                            <div className="route-point">
                                                <span className="icon">📍</span>
                                                <span>{request.pickupAddress}</span>
                                            </div>
                                            <div className="route-point">
                                                <span className="icon">📌</span>
                                                <span>{request.dropoffAddress}</span>
                                            </div>
                                        </div>

                                        <div className="request-actions">
                                            <button
                                                className="btn btn-secondary"
                                                onClick={() => handleRejectRide(request.id)}
                                            >
                                                Decline
                                            </button>
                                            <button
                                                className="btn btn-primary"
                                                onClick={() => handleAcceptRide(request.id)}
                                            >
                                                Accept Ride
                                            </button>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                )}

                {status === 'OFFLINE' && (
                    <div className="offline-message glass">
                        <h2>You're Offline</h2>
                        <p>Go online to start receiving ride requests</p>
                    </div>
                )}
            </div>
        </div>
    );
}

export default DriverDashboard;
