import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { ToastContainer } from 'react-toastify'
import 'react-toastify/dist/ReactToastify.css'
import { AuthProvider } from './context/AuthContext'
import Landing from './pages/Landing'
import Login from './pages/Login'
import Register from './pages/Register'
import ForgotPassword from './pages/ForgotPassword'
import ResetPassword from './pages/ResetPassword'
import CustomerDashboard from './pages/CustomerDashboard'
import OwnerDashboard from './pages/OwnerDashboard'
import RestaurantList from './pages/RestaurantList'
import MenuPage from './pages/MenuPage'
import CartPage from './pages/CartPage'
import CheckoutPage from './pages/CheckoutPage'
import OrderTracking from './pages/OrderTracking'
import OrderHistory from './pages/OrderHistory'
import Preferences from './pages/Preferences'
import RestaurantManagement from './pages/RestaurantManagement'
import MenuManagement from './pages/MenuManagement'
import OrderManagement from './pages/OrderManagement'
import ProtectedRoute from './components/ProtectedRoute'
import './App.css'

function App() {
  return (
    <Router>
      <AuthProvider>
        <div className="App">
          <Routes>
            <Route path="/" element={<Landing />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/forgot-password" element={<ForgotPassword />} />
            <Route path="/reset-password" element={<ResetPassword />} />
            
            <Route path="/customer" element={
              <ProtectedRoute role="CUSTOMER">
                <CustomerDashboard />
              </ProtectedRoute>
            } />
            
            <Route path="/restaurants" element={
              <ProtectedRoute role="CUSTOMER">
                <RestaurantList />
              </ProtectedRoute>
            } />
            
            <Route path="/restaurants/:id/menu" element={
              <ProtectedRoute role="CUSTOMER">
                <MenuPage />
              </ProtectedRoute>
            } />
            
            <Route path="/cart" element={
              <ProtectedRoute role="CUSTOMER">
                <CartPage />
              </ProtectedRoute>
            } />
            
            <Route path="/checkout" element={
              <ProtectedRoute role="CUSTOMER">
                <CheckoutPage />
              </ProtectedRoute>
            } />
            
            <Route path="/orders/:id/tracking" element={
              <ProtectedRoute role="CUSTOMER">
                <OrderTracking />
              </ProtectedRoute>
            } />
            
            <Route path="/orders/history" element={
              <ProtectedRoute role="CUSTOMER">
                <OrderHistory />
              </ProtectedRoute>
            } />
            
            <Route path="/preferences" element={
              <ProtectedRoute role="CUSTOMER">
                <Preferences />
              </ProtectedRoute>
            } />
            
            <Route path="/owner" element={
              <ProtectedRoute role="RESTAURANT_OWNER">
                <OwnerDashboard />
              </ProtectedRoute>
            } />
            
            <Route path="/owner/restaurants" element={
              <ProtectedRoute role="RESTAURANT_OWNER">
                <RestaurantManagement />
              </ProtectedRoute>
            } />
            
            <Route path="/owner/menu/:restaurantId" element={
              <ProtectedRoute role="RESTAURANT_OWNER">
                <MenuManagement />
              </ProtectedRoute>
            } />
            
            <Route path="/owner/orders" element={
              <ProtectedRoute role="RESTAURANT_OWNER">
                <OrderManagement />
              </ProtectedRoute>
            } />
          </Routes>
          <ToastContainer position="top-right" autoClose={3000} />
        </div>
      </AuthProvider>
    </Router>
  )
}

export default App
