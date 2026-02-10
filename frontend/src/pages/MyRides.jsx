import { useState, useEffect } from 'react';
import { rideApi } from '../services/api';
import './MyRides.css';

function MyRides() {
    const [rides, setRides] = useState([]);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(0);
    const [hasMore, setHasMore] = useState(true);

    useEffect(() => {
        fetchRides();
    }, [page]);

    const fetchRides = async () => {
        try {
            const response = await rideApi.getRides(page, 10);
            const data = response.data;
            setRides(prev => page === 0 ? data.content : [...prev, ...data.content]);
            setHasMore(!data.last);
        } catch (error) {
            console.error('Error fetching rides:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleCancelRide = async (rideId) => {
        try {
            await rideApi.cancelRide(rideId);
            setRides(rides.map(r => r.id === rideId ? { ...r, status: 'CANCELLED' } : r));
        } catch (error) {
            console.error('Cancel failed:', error);
        }
    };

    const handleRateRide = async (rideId, rating) => {
        try {
            await rideApi.rateRide(rideId, rating);
            setRides(rides.map(r => r.id === rideId ? { ...r, rating } : r));
        } catch (error) {
            console.error('Rating failed:', error);
        }
    };

    const getStatusBadge = (status) => {
        const statusMap = {
            REQUESTED: { class: 'pending', label: 'Looking for driver' },
            ACCEPTED: { class: 'active', label: 'Driver assigned' },
            ARRIVED: { class: 'active', label: 'Driver arrived' },
            IN_PROGRESS: { class: 'active', label: 'In progress' },
            COMPLETED: { class: 'success', label: 'Completed' },
            CANCELLED: { class: 'cancelled', label: 'Cancelled' }
        };
        const s = statusMap[status] || { class: '', label: status };
        return <span className={`status-badge ${s.class}`}>{s.label}</span>;
    };

    if (loading && rides.length === 0) {
        return (
            <div className="my-rides loading">
                <div className="loader"></div>
            </div>
        );
    }

    return (
        <div className="my-rides">
            <div className="container">
                <h1>My Rides</h1>

                {rides.length === 0 ? (
                    <div className="no-rides glass">
                        <span className="empty-icon">🚕</span>
                        <h2>No rides yet</h2>
                        <p>Your ride history will appear here after your first trip.</p>
                    </div>
                ) : (
                    <div className="rides-list">
                        {rides.map((ride) => (
                            <div key={ride.id} className="ride-card card">
                                <div className="ride-header">
                                    <div className="ride-date">
                                        {new Date(ride.createdAt).toLocaleDateString('en-IN', {
                                            day: 'numeric',
                                            month: 'short',
                                            year: 'numeric',
                                            hour: '2-digit',
                                            minute: '2-digit'
                                        })}
                                    </div>
                                    {getStatusBadge(ride.status)}
                                </div>

                                <div className="ride-route">
                                    <div className="route-point pickup">
                                        <span className="route-icon">📍</span>
                                        <span className="route-address">{ride.pickupAddress || 'Pickup Location'}</span>
                                    </div>
                                    <div className="route-line"></div>
                                    <div className="route-point dropoff">
                                        <span className="route-icon">📌</span>
                                        <span className="route-address">{ride.dropoffAddress || 'Dropoff Location'}</span>
                                    </div>
                                </div>

                                <div className="ride-details">
                                    <div className="detail">
                                        <span className="detail-label">Vehicle</span>
                                        <span className="detail-value">{ride.vehicleType}</span>
                                    </div>
                                    <div className="detail">
                                        <span className="detail-label">Distance</span>
                                        <span className="detail-value">{ride.distanceKm || 'N/A'} km</span>
                                    </div>
                                    <div className="detail">
                                        <span className="detail-label">Fare</span>
                                        <span className="detail-value fare">₹{ride.estimatedFare || ride.actualFare || 'N/A'}</span>
                                    </div>
                                </div>

                                {ride.driverInfo && (
                                    <div className="driver-info">
                                        <div className="driver-avatar">
                                            {ride.driverInfo.firstName?.[0] || 'D'}
                                        </div>
                                        <div className="driver-details">
                                            <span className="driver-name">{ride.driverInfo.firstName} {ride.driverInfo.lastName}</span>
                                            <span className="driver-vehicle">{ride.driverInfo.vehicleNumber}</span>
                                        </div>
                                        {ride.driverInfo.rating && (
                                            <div className="driver-rating">⭐ {ride.driverInfo.rating.toFixed(1)}</div>
                                        )}
                                    </div>
                                )}

                                <div className="ride-actions">
                                    {ride.status === 'COMPLETED' && !ride.rating && (
                                        <div className="rating-section">
                                            <span>Rate this ride:</span>
                                            <div className="rating-stars">
                                                {[1, 2, 3, 4, 5].map((star) => (
                                                    <button
                                                        key={star}
                                                        className="star-btn"
                                                        onClick={() => handleRateRide(ride.id, star)}
                                                    >
                                                        ⭐
                                                    </button>
                                                ))}
                                            </div>
                                        </div>
                                    )}
                                    {ride.rating && (
                                        <div className="your-rating">Your rating: {'⭐'.repeat(ride.rating)}</div>
                                    )}
                                    {['REQUESTED', 'ACCEPTED'].includes(ride.status) && (
                                        <button
                                            className="btn btn-secondary"
                                            onClick={() => handleCancelRide(ride.id)}
                                        >
                                            Cancel Ride
                                        </button>
                                    )}
                                </div>
                            </div>
                        ))}
                    </div>
                )}

                {hasMore && (
                    <button
                        className="btn btn-secondary load-more"
                        onClick={() => setPage(p => p + 1)}
                        disabled={loading}
                    >
                        {loading ? <span className="loader-sm"></span> : 'Load More'}
                    </button>
                )}
            </div>
        </div>
    );
}

export default MyRides;
