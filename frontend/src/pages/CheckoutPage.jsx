import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Navbar from '../components/Navbar'
import Sidebar from '../components/Sidebar'
import { cartService, orderService } from '../services/api'
import { toast } from 'react-toastify'

const CheckoutPage = () => {
  const navigate = useNavigate()
  const [cart, setCart] = useState(null)
  const [loading, setLoading] = useState(true)
  const [placing, setPlacing] = useState(false)
  const [deliveryAddress, setDeliveryAddress] = useState('')  // +++

  const sidebarLinks = [
    { path: '/customer', label: 'Dashboard' },
    { path: '/restaurants', label: 'Browse Restaurants' },
    { path: '/cart', label: 'My Cart' },
    { path: '/orders/history', label: 'Order History' },
    { path: '/preferences', label: 'Preferences' },
  ]

  useEffect(() => {
    fetchCart()
  }, [])

  const fetchCart = async () => {
    setLoading(true)
    try {
      const response = await cartService.getCart()
      setCart(response.data)
    } catch (error) {
      toast.error('Failed to load cart')
    } finally {
      setLoading(false)
    }
  }

  const placeOrder = async () => {
    if (!cart || cart.items.length === 0) {
      toast.warning('Cart is empty')
      return
    }

    // +++ Validate delivery address +++
    if (!deliveryAddress.trim()) {
      toast.warning('Please enter a delivery address')
      return
    }

    const restaurantId = cart.items[0].menuItem.restaurantId
    setPlacing(true)

    try {
      const response = await orderService.placeOrder({ restaurantId, deliveryAddress: deliveryAddress.trim() })
      toast.success('Order placed successfully!')
      navigate(`/orders/${response.data.id}/tracking`)
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to place order')
    } finally {
      setPlacing(false)
    }
  }

  return (
    <>
      <Navbar title="Checkout" />
      <div className="dashboard">
        <Sidebar links={sidebarLinks} />
        <div className="main-content">
          <h1>Checkout</h1>

          {loading ? (
            <div className="loading">Loading...</div>
          ) : cart && cart.items.length > 0 ? (
            <>
              <div className="card">
                <h2>Order Summary</h2>
                {cart.items.map((item) => (
                  <div key={item.id} style={{ 
                    display: 'flex', 
                    justifyContent: 'space-between',
                    padding: '0.5rem 0'
                  }}>
                    <span>{item.menuItem.name} x {item.quantity}</span>
                    <span>₹{item.subtotal}</span>
                  </div>
                ))}
                <hr />
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '1.25rem', fontWeight: 'bold' }}>
                  <span>Total</span>
                  <span style={{ color: 'var(--primary-color)' }}>₹{cart.totalPrice}</span>
                </div>
              </div>

              {/* +++ Delivery Address Input +++ */}
              <div className="card">
                <h2>Delivery Address</h2>
                <p style={{ marginBottom: '0.75rem', color: '#555' }}>
                  Enter the address where you'd like your order delivered.
                </p>
                <textarea
                  rows={3}
                  placeholder="e.g. Flat 4B, Green Apartments, MG Road, Bangalore - 560001"
                  value={deliveryAddress}
                  onChange={(e) => setDeliveryAddress(e.target.value)}
                  style={{
                    width: '100%',
                    padding: '0.65rem 0.75rem',
                    borderRadius: '6px',
                    border: deliveryAddress.trim() ? '1px solid #ccc' : '1px solid #e74c3c',
                    fontSize: '0.95rem',
                    resize: 'vertical',
                    boxSizing: 'border-box',
                  }}
                />
                {!deliveryAddress.trim() && (
                  <p style={{ color: '#e74c3c', fontSize: '0.85rem', marginTop: '0.35rem' }}>
                    Delivery address is required
                  </p>
                )}
              </div>

              <div className="card">
                <h2>Confirm Order</h2>
                <p>Review your order above, then click Place Order.</p>
                <button
                  className="btn btn-primary"
                  onClick={placeOrder}
                  disabled={placing || !deliveryAddress.trim()}
                >
                  {placing ? 'Placing Order...' : 'Place Order'}
                </button>
              </div>
            </>
          ) : (
            <div className="card">
              <p>Your cart is empty.</p>
              <button className="btn btn-primary" onClick={() => navigate('/restaurants')}>
                Browse Restaurants
              </button>
            </div>
          )}
        </div>
      </div>
    </>
  )
}

export default CheckoutPage
