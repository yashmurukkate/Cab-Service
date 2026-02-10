import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || '/api';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json'
    }
});

// Add auth token to requests
api.interceptors.request.use(config => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// Handle auth errors
api.interceptors.response.use(
    response => response,
    error => {
        if (error.response?.status === 401) {
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

// Auth APIs
export const authApi = {
    login: (credentials) => api.post('/users/auth/login', credentials),
    register: (data) => api.post('/users/auth/register', data),
    logout: () => api.post('/users/auth/logout'),
    getProfile: () => api.get('/users/profile')
};

// Ride APIs
export const rideApi = {
    bookRide: (data) => api.post('/rides', data),
    getRides: (page = 0, size = 10) => api.get(`/rides?page=${page}&size=${size}`),
    getRideById: (id) => api.get(`/rides/${id}`),
    cancelRide: (id) => api.post(`/rides/${id}/cancel`),
    rateRide: (id, rating) => api.post(`/rides/${id}/rate`, { rating })
};

// Cab APIs
export const cabApi = {
    getNearbyCabs: (lat, lng, radius = 5) =>
        api.get(`/cabs/nearby?latitude=${lat}&longitude=${lng}&radiusKm=${radius}`),
    updateLocation: (lat, lng) => api.put('/cabs/location', { latitude: lat, longitude: lng }),
    updateStatus: (status) => api.put('/cabs/status', { status })
};

// Billing APIs
export const billingApi = {
    calculateFare: (data) => api.post('/billing/calculate-fare', data),
    getInvoice: (rideId) => api.get(`/billing/invoices/ride/${rideId}`),
    processPayment: (data) => api.post('/billing/payments', data)
};

// Routing APIs
export const routingApi = {
    getRoute: (data) => api.post('/routing/route', data),
    getDistance: (startLat, startLon, endLat, endLon) =>
        api.get(`/routing/distance?startLat=${startLat}&startLon=${startLon}&endLat=${endLat}&endLon=${endLon}`),
    getEta: (startLat, startLon, endLat, endLon) =>
        api.get(`/routing/eta?startLat=${startLat}&startLon=${startLon}&endLat=${endLat}&endLon=${endLon}`)
};

export default api;
