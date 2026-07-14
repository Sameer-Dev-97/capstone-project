import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import Navbar from '../components/Navbar'
import Sidebar from '../components/Sidebar'
import { orderService } from '../services/api'
import { toast } from 'react-toastify'

const OrderTracking = () => {
  const { id } = useParams()
  const navigate = useNavigate()
  const [order, setOrder] = useState(null)
  const [loading, setLoading] = useState(true)

  const sidebarLinks = [
    { path: '/customer', label: 'Dashboard' },
    { path: '/restaurants', label: 'Browse Restaurants' },
    { path: '/cart', label: 'My Cart' },
    { path: '/orders/history', label: 'Order History' },
    { path: '/preferences', label: 'Preferences' },
  ]

  useEffect(() => {
    fetchOrder()
    const interval = setInterval(fetchOrder, 10000)
    return () => clearInterval(interval)
  }, [id])

  const fetchOrder = async () => {
    try {
      const response = await orderService.getStatus(id)
      setOrder(response.data)
      setLoading(false)
    } catch (error) {
      toast.error('Failed to load order')
      setLoading(false)
    }
  }

  const getStatusBadge = (status) => {
    const badges = {
      PENDING: 'badge-pending',
      PREPARING: 'badge-preparing',
      READY: 'badge-ready',
      COMPLETED: 'badge-completed'
    }
    return badges[status] || 'badge-pending'
  }

  return (
    <>
      <Navbar title="Order Tracking" />
      <div className="dashboard">
        <Sidebar links={sidebarLinks} />
        <div className="main-content">
          <h1>Order Tracking</h1>

          {loading ? (
            <div className="loading">Loading...</div>
          ) : order ? (
            <>
              <div className="card">
                <h2>Order #{order.id}</h2>
                <p><strong>Restaurant:</strong> {order.restaurantName}</p>
                <p><strong>Status:</strong> <span className={`badge ${getStatusBadge(order.status)}`}>{order.status}</span></p>
                <p><strong>Total:</strong> ₹{order.totalPrice}</p>
                <p><strong>Order Date:</strong> {new Date(order.orderDate).toLocaleString()}</p>
              </div>

              <div className="card">
                <h2>Items</h2>
                {order.items.map((item, index) => (
                  <div key={index} style={{ 
                    display: 'flex', 
                    justifyContent: 'space-between',
                    padding: '0.5rem 0'
                  }}>
                    <span>{item.menuItemName} x {item.quantity}</span>
                    <span>₹{item.price * item.quantity}</span>
                  </div>
                ))}
              </div>

              <button className="btn btn-secondary" onClick={() => navigate('/orders/history')}>
                View Order History
              </button>
            </>
          ) : (
            <div className="card">
              <p>Order not found.</p>
            </div>
          )}
        </div>
      </div>
    </>
  )
}

export default OrderTracking
