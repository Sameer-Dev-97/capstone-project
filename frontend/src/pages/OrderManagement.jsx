import { useEffect, useState } from 'react'
import Navbar from '../components/Navbar'
import Sidebar from '../components/Sidebar'
import { orderService } from '../services/api'
import { toast } from 'react-toastify'

const OrderManagement = () => {
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [filter, setFilter] = useState('ALL')

  const sidebarLinks = [
    { path: '/owner', label: 'Dashboard' },
    { path: '/owner/restaurants', label: 'My Restaurants' },
    { path: '/owner/orders', label: 'Orders' },
  ]

  useEffect(() => {
    fetchOrders()
    const interval = setInterval(fetchOrders, 30000)
    return () => clearInterval(interval)
  }, [])

  const fetchOrders = async () => {
    setLoading(true)
    try {
      const response = await orderService.getRestaurantOrders()
      setOrders(response.data)
    } catch (error) {
      toast.error('Failed to load orders')
    } finally {
      setLoading(false)
    }
  }

  const updateStatus = async (orderId, status) => {
    try {
      await orderService.updateStatus(orderId, { status })
      toast.success('Order status updated!')
      fetchOrders()
    } catch (error) {
      toast.error('Failed to update order status')
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

  const filteredOrders = filter === 'ALL' 
    ? orders 
    : orders.filter((o) => o.status === filter)

  return (
    <>
      <Navbar title="Order Management" />
      <div className="dashboard">
        <Sidebar links={sidebarLinks} />
        <div className="main-content">
          <h1>Order Management</h1>

          <div className="card">
            <h2>Filter Orders</h2>
            <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
              {['ALL', 'PENDING', 'PREPARING', 'READY', 'COMPLETED'].map((status) => (
                <button
                  key={status}
                  className={`btn ${filter === status ? 'btn-primary' : 'btn-secondary'}`}
                  onClick={() => setFilter(status)}
                >
                  {status}
                </button>
              ))}
            </div>
          </div>

          {loading ? (
            <div className="loading">Loading...</div>
          ) : filteredOrders.length > 0 ? (
            <div className="grid grid-2">
              {filteredOrders.map((order) => (
                <div key={order.id} className="card">
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <h3>Order #{order.id}</h3>
                    <span className={`badge ${getStatusBadge(order.status)}`}>
                      {order.status}
                    </span>
                  </div>
                  <p><strong>Customer:</strong> {order.customerName}</p>
                  <p><strong>Restaurant:</strong> {order.restaurantName}</p>
                  <p><strong>Date:</strong> {new Date(order.orderDate).toLocaleString()}</p>
                  <p><strong>Total:</strong> ₹{order.totalPrice}</p>
                  
                  <div style={{ marginTop: '1rem', borderTop: '1px solid #eee', paddingTop: '1rem' }}>
                    <strong>Items:</strong>
                    <ul style={{ marginTop: '0.5rem', paddingLeft: '1.5rem' }}>
                      {order.items.map((item, index) => (
                        <li key={index}>
                          {item.menuItemName} x {item.quantity} - ₹{item.price * item.quantity}
                        </li>
                      ))}
                    </ul>
                  </div>

                  <div style={{ marginTop: '1rem', display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                    {order.status === 'PENDING' && (
                      <button 
                        className="btn btn-primary" 
                        onClick={() => updateStatus(order.id, 'PREPARING')}
                      >
                        Start Preparing
                      </button>
                    )}
                    {order.status === 'PREPARING' && (
                      <button 
                        className="btn btn-primary" 
                        onClick={() => updateStatus(order.id, 'READY')}
                      >
                        Mark as Ready
                      </button>
                    )}
                    {order.status === 'READY' && (
                      <button 
                        className="btn btn-success" 
                        onClick={() => updateStatus(order.id, 'COMPLETED')}
                      >
                        Complete Order
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="card">
              <p>No {filter === 'ALL' ? '' : filter.toLowerCase()} orders found.</p>
            </div>
          )}
        </div>
      </div>
    </>
  )
}

export default OrderManagement
