import axios from 'axios'
import { toast } from 'react-toastify'

const API_BASE_URL = `${import.meta.env.VITE_API_URL}/api`;

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    const isAuthRoute = config.url?.startsWith('/auth/')
    if (token && !isAuthRoute) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
      toast.error('Session expired. Please login again.')
    }
    return Promise.reject(error)
  }
)

export const authService = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data),
  requestPasswordReset: (data) => api.post('/auth/reset-password', data),
  confirmPasswordReset: (data) => api.post('/auth/reset-password/confirm', data),
}

export const restaurantService = {
  getAll: (search = '') => api.get(`/restaurants${search ? `?search=${search}` : ''}`),
  getById: (id) => api.get(`/restaurants/${id}`),
  create: (data) => api.post('/restaurants', data),
  update: (id, data) => api.put(`/restaurants/${id}`, data),
  getMyRestaurants: () => api.get('/restaurants/my-restaurants'),
}

export const menuService = {
  getMenu: (restaurantId) => api.get(`/restaurants/${restaurantId}/menu`),
  createItem: (restaurantId, data) => api.post(`/restaurants/${restaurantId}/menu`, data),
  updateItem: (id, data) => api.put(`/menu/${id}`, data),
  deleteItem: (id) => api.delete(`/menu/${id}`),
  rateItem: (id, orderId, rating) => api.post(`/menu/${id}/rate`, { orderId, rating }),
  getPopularItems: (restaurantId) => api.get(`/restaurants/${restaurantId}/popular-items`),
  refreshPopularity: (restaurantId) => api.post(`/admin/restaurants/${restaurantId}/refresh-popularity`),
}

export const cartService = {
  getCart: () => api.get('/cart'),
  addToCart: (data) => api.post('/cart', data),
  updateCartItem: (id, data) => api.put(`/cart/${id}`, data),
  removeFromCart: (id) => api.delete(`/cart/${id}`),
  clearCart: () => api.delete('/cart'),
}

export const orderService = {
  placeOrder: (data) => api.post('/orders', data),
  getHistory: (restaurantName = '', status = '') => {
    const params = new URLSearchParams()
    if (restaurantName) params.append('restaurantName', restaurantName)
    if (status) params.append('status', status)
    const query = params.toString()
    return api.get(`/orders/history${query ? `?${query}` : ''}`)
  },
  getStatus: (id) => api.get(`/orders/${id}/status`),
  getRestaurantOrders: () => api.get('/orders/restaurant'),
  updateStatus: (id, data) => api.put(`/orders/${id}/status`, data),
  reorder: (id) => api.post(`/orders/${id}/reorder`),  // +++
}

export const preferenceService = {
  getPreferences: () => api.get('/preferences'),
  updatePreferences: (data) => api.put('/preferences', data),
  getRecommendations: () => api.get('/preferences/recommendations'),
}

export default api
