import { useState, useEffect, useRef } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap, Polyline } from 'react-leaflet';
import { billingApi, cabApi, rideApi, routingApi } from '../services/api';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import './BookRide.css';

// Fix Leaflet default icons
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
    iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png',
    iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png',
    shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
});

const vehicleTypes = [
    { id: 'MINI', name: 'Mini', icon: '🚗', description: 'Budget-friendly' },
    { id: 'SEDAN', name: 'Sedan', icon: '🚙', description: 'Comfortable' },
    { id: 'SUV', name: 'SUV', icon: '🚐', description: 'Spacious' },
    { id: 'PREMIUM', name: 'Premium', icon: '✨', description: 'Luxury' },
];

function MapController({ center }) {
    const map = useMap();
    useEffect(() => {
        if (center) {
            map.setView(center, 14);
        }
    }, [center, map]);
    return null;
}

function BookRide() {
    const [pickup, setPickup] = useState(null);
    const [dropoff, setDropoff] = useState(null);
    const [pickupAddress, setPickupAddress] = useState('');
    const [dropoffAddress, setDropoffAddress] = useState('');
    const [selectedVehicle, setSelectedVehicle] = useState('SEDAN');
    const [fareEstimate, setFareEstimate] = useState(null);
    const [route, setRoute] = useState(null);
    const [nearbyCabs, setNearbyCabs] = useState([]);
    const [loading, setLoading] = useState(false);
    const [bookingStatus, setBookingStatus] = useState(null);
    const mapRef = useRef(null);

    // Default center (Mumbai)
    const defaultCenter = [19.0760, 72.8777];

    useEffect(() => {
        // Get user's location
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(
                (position) => {
                    const { latitude, longitude } = position.coords;
                    setPickup([latitude, longitude]);
                    fetchNearbyCabs(latitude, longitude);
                },
                () => console.log('Location access denied')
            );
        }
    }, []);

    const fetchNearbyCabs = async (lat, lng) => {
        try {
            const response = await cabApi.getNearbyCabs(lat, lng, 5);
            setNearbyCabs(response.data || []);
        } catch (error) {
            console.log('Error fetching cabs:', error);
        }
    };

    const handleMapClick = (e) => {
        const { lat, lng } = e.latlng;
        if (!pickup) {
            setPickup([lat, lng]);
            setPickupAddress(`${lat.toFixed(4)}, ${lng.toFixed(4)}`);
        } else if (!dropoff) {
            setDropoff([lat, lng]);
            setDropoffAddress(`${lat.toFixed(4)}, ${lng.toFixed(4)}`);
        }
    };

    const calculateFare = async () => {
        if (!pickup || !dropoff) return;

        setLoading(true);
        try {
            const [fareRes, routeRes] = await Promise.all([
                billingApi.calculateFare({
                    pickupLatitude: pickup[0],
                    pickupLongitude: pickup[1],
                    dropoffLatitude: dropoff[0],
                    dropoffLongitude: dropoff[1],
                    vehicleType: selectedVehicle
                }),
                routingApi.getRoute({
                    startLatitude: pickup[0],
                    startLongitude: pickup[1],
                    endLatitude: dropoff[0],
                    endLongitude: dropoff[1],
                    vehicleType: selectedVehicle
                })
            ]);
            setFareEstimate(fareRes.data);
            setRoute(routeRes.data);
        } catch (error) {
            console.error('Error calculating fare:', error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (pickup && dropoff) {
            calculateFare();
        }
    }, [pickup, dropoff, selectedVehicle]);

    const handleBookRide = async () => {
        if (!pickup || !dropoff || !fareEstimate) return;

        setLoading(true);
        setBookingStatus('searching');

        try {
            const response = await rideApi.bookRide({
                pickupLatitude: pickup[0],
                pickupLongitude: pickup[1],
                dropoffLatitude: dropoff[0],
                dropoffLongitude: dropoff[1],
                pickupAddress,
                dropoffAddress,
                vehicleType: selectedVehicle
            });
            setBookingStatus('confirmed');
        } catch (error) {
            setBookingStatus('failed');
            console.error('Booking failed:', error);
        } finally {
            setLoading(false);
        }
    };

    const resetBooking = () => {
        setPickup(null);
        setDropoff(null);
        setPickupAddress('');
        setDropoffAddress('');
        setFareEstimate(null);
        setRoute(null);
        setBookingStatus(null);
    };

    return (
        <div className="book-ride">
            <div className="booking-sidebar glass">
                <h2>Book Your Ride</h2>

                <div className="location-inputs">
                    <div className="input-group">
                        <label>📍 Pickup Location</label>
                        <input
                            type="text"
                            placeholder="Click on map or enter address"
                            value={pickupAddress}
                            onChange={(e) => setPickupAddress(e.target.value)}
                        />
                    </div>

                    <div className="input-group">
                        <label>📌 Drop-off Location</label>
                        <input
                            type="text"
                            placeholder="Click on map or enter address"
                            value={dropoffAddress}
                            onChange={(e) => setDropoffAddress(e.target.value)}
                        />
                    </div>
                </div>

                <div className="vehicle-selection">
                    <label>Select Vehicle Type</label>
                    <div className="vehicle-options">
                        {vehicleTypes.map((vehicle) => (
                            <div
                                key={vehicle.id}
                                className={`vehicle-option ${selectedVehicle === vehicle.id ? 'selected' : ''}`}
                                onClick={() => setSelectedVehicle(vehicle.id)}
                            >
                                <span className="vehicle-icon">{vehicle.icon}</span>
                                <span className="vehicle-name">{vehicle.name}</span>
                                <span className="vehicle-desc">{vehicle.description}</span>
                            </div>
                        ))}
                    </div>
                </div>

                {fareEstimate && (
                    <div className="fare-estimate">
                        <h3>Fare Estimate</h3>
                        <div className="fare-breakdown">
                            <div className="fare-row">
                                <span>Base Fare</span>
                                <span>₹{fareEstimate.baseFare}</span>
                            </div>
                            <div className="fare-row">
                                <span>Distance ({fareEstimate.estimatedDistanceKm} km)</span>
                                <span>₹{fareEstimate.distanceCharge}</span>
                            </div>
                            <div className="fare-row">
                                <span>Time ({fareEstimate.estimatedDurationMinutes} min)</span>
                                <span>₹{fareEstimate.timeCharge}</span>
                            </div>
                            {fareEstimate.surgeCharge > 0 && (
                                <div className="fare-row surge">
                                    <span>Surge ({fareEstimate.surgeMultiplier}x)</span>
                                    <span>₹{fareEstimate.surgeCharge}</span>
                                </div>
                            )}
                            <div className="fare-row total">
                                <span>Total</span>
                                <span className="total-amount">₹{fareEstimate.totalAmount}</span>
                            </div>
                        </div>
                    </div>
                )}

                {bookingStatus === 'searching' && (
                    <div className="booking-status searching">
                        <div className="loader"></div>
                        <p>Finding nearby drivers...</p>
                    </div>
                )}

                {bookingStatus === 'confirmed' && (
                    <div className="booking-status confirmed">
                        <span className="status-icon">✓</span>
                        <h3>Ride Confirmed!</h3>
                        <p>Your driver is on the way</p>
                        <button className="btn btn-secondary" onClick={resetBooking}>
                            Book Another Ride
                        </button>
                    </div>
                )}

                {!bookingStatus && (
                    <button
                        className="btn btn-primary btn-block"
                        onClick={handleBookRide}
                        disabled={loading || !pickup || !dropoff}
                    >
                        {loading ? <span className="loader-sm"></span> : 'Book Ride'}
                    </button>
                )}

                {pickup && !bookingStatus && (
                    <button className="btn btn-secondary btn-block" onClick={resetBooking}>
                        Reset
                    </button>
                )}
            </div>

            <div className="map-container">
                <MapContainer
                    center={pickup || defaultCenter}
                    zoom={13}
                    className="ride-map"
                    ref={mapRef}
                    onClick={handleMapClick}
                >
                    <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
                    <MapController center={pickup || defaultCenter} />

                    {pickup && (
                        <Marker position={pickup}>
                            <Popup>Pickup Location</Popup>
                        </Marker>
                    )}

                    {dropoff && (
                        <Marker position={dropoff}>
                            <Popup>Drop-off Location</Popup>
                        </Marker>
                    )}

                    {route && route.polyline && (
                        <Polyline
                            positions={route.polyline.map(p => [p.latitude, p.longitude])}
                            color="#6366f1"
                            weight={4}
                        />
                    )}

                    {nearbyCabs.map((cab, index) => (
                        <Marker
                            key={index}
                            position={[cab.latitude, cab.longitude]}
                            icon={L.divIcon({
                                className: 'cab-marker',
                                html: '🚕',
                                iconSize: [30, 30]
                            })}
                        >
                            <Popup>{cab.driverName || 'Available Cab'}</Popup>
                        </Marker>
                    ))}
                </MapContainer>
            </div>
        </div>
    );
}

export default BookRide;
