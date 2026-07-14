import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Navbar from '../components/Navbar'
import Sidebar from '../components/Sidebar'
import { cartService } from '../services/api'
import { toast } from 'react-toastify'

const CartPage = () => {
  const navigate = useNavigate()
  const [cart, setCart] = useState(null)
  const [loading, setLoading] = useState(true)

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

  const updateQuantity = async (cartItemId, quantity) => {
    if (quantity < 1) return
    try {
      const response = await cartService.updateCartItem(cartItemId, { menuItemId: 0, quantity })
      setCart(response.data)
      toast.success('Cart updated')
    } catch (error) {
      toast.error('Failed to update cart')
    }
  }

  const removeItem = async (cartItemId) => {
    try {
      const response = await cartService.removeFromCart(cartItemId)
      setCart(response.data)
      toast.success('Item removed')
    } catch (error) {
      toast.error('Failed to remove item')
    }
  }

  const proceedToCheckout = () => {
    if (cart && cart.items.length > 0) {
      navigate('/checkout')
    } else {
      toast.warning('Cart is empty')
    }
  }

  return (
    <>
      <Navbar title="My Cart" />
      <div className="dashboard">
        <Sidebar links={sidebarLinks} />
        <div className="main-content">
          <h1>My Cart</h1>

          {loading ? (
            <div className="loading">Loading...</div>
          ) : cart && cart.items.length > 0 ? (
            <>
              <div className="card">
                {cart.items.map((item) => (
                  <div key={item.id} style={{ 
                    display: 'flex', 
                    justifyContent: 'space-between', 
                    alignItems: 'center',
                    borderBottom: '1px solid #eee',
                    padding: '1rem 0'
                  }}>
                    <div style={{ flex: 1 }}>
                      <h3>{item.menuItem.name}</h3>
                      <p>₹{item.menuItem.price} x {item.quantity}</p>
                    </div>
                    <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
                      <div>
                        <button 
                          className="btn btn-secondary" 
                          onClick={() => updateQuantity(item.id, item.quantity - 1)}
                          style={{ padding: '0.5rem 1rem' }}
                        >
                          -
                        </button>
                        <span style={{ margin: '0 1rem' }}>{item.quantity}</span>
                        <button 
                          className="btn btn-secondary" 
                          onClick={() => updateQuantity(item.id, item.quantity + 1)}
                          style={{ padding: '0.5rem 1rem' }}
                        >
                          +
                        </button>
                      </div>
                      <p style={{ fontWeight: 'bold', minWidth: '100px', textAlign: 'right' }}>
                        ₹{item.subtotal}
                      </p>
                      <button className="btn btn-danger" onClick={() => removeItem(item.id)}>
                        Remove
                      </button>
                    </div>
                  </div>
                ))}
              </div>

              <div className="card">
                <h2>Cart Summary</h2>
                <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: 'var(--primary-color)' }}>
                  Total: ₹{cart.totalPrice}
                </p>
                <button className="btn btn-primary" onClick={proceedToCheckout}>
                  Proceed to Checkout
                </button>
              </div>
            </>
          ) : (
            <div className="card">
              <p>Your cart is empty. Browse restaurants to add items!</p>
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

export default CartPage
