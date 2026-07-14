import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import Navbar from '../components/Navbar'
import Sidebar from '../components/Sidebar'
import { menuService, cartService } from '../services/api'
import { toast } from 'react-toastify'

const MenuPage = () => {
  const { id } = useParams()
  const navigate = useNavigate()
  const [menuItems, setMenuItems] = useState([])
  const [loading, setLoading] = useState(true)

  const sidebarLinks = [
    { path: '/customer', label: 'Dashboard' },
    { path: '/restaurants', label: 'Browse Restaurants' },
    { path: '/cart', label: 'My Cart' },
    { path: '/orders/history', label: 'Order History' },
    { path: '/preferences', label: 'Preferences' },
  ]

  useEffect(() => {
    fetchMenu()
  }, [id])

  const fetchMenu = async () => {
    setLoading(true)
    try {
      const response = await menuService.getMenu(id)
      setMenuItems(response.data)
    } catch (error) {
      toast.error('Failed to load menu')
    } finally {
      setLoading(false)
    }
  }

  const addToCart = async (menuItemId) => {
    try {
      await cartService.addToCart({ menuItemId, quantity: 1 })
      toast.success('Added to cart!')
    } catch (error) {
      toast.error('Failed to add to cart')
    }
  }

  return (
    <>
      <Navbar title="Restaurant Menu" />
      <div className="dashboard">
        <Sidebar links={sidebarLinks} />
        <div className="main-content">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <h1>Menu</h1>
            <button className="btn btn-secondary" onClick={() => navigate('/cart')}>
              View Cart
            </button>
          </div>

          {loading ? (
            <div className="loading">Loading...</div>
          ) : (
            <div className="grid grid-2">
              {menuItems.map((item) => (
                <div key={item.id} className="card">
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <h3>{item.name}</h3>
                    <div>
                      {item.todaysSpecial && <span className="badge badge-ready">Today's Special</span>}
                      {item.dealOfDay && <span className="badge badge-preparing" style={{marginLeft: '5px'}}>Deal of Day</span>}
                      {item.mostOrdered && <span className="badge badge-pending" style={{marginLeft: '5px'}}>Most Ordered</span>}
                    </div>
                  </div>
                  <p>{item.description}</p>
                  <p><strong>Category:</strong> {item.category}</p>
                  <p style={{ fontSize: '1.25rem', color: 'var(--primary-color)', fontWeight: 'bold' }}>
                    ₹{item.price}
                  </p>
                  <button
                    className="btn btn-primary"
                    onClick={() => addToCart(item.id)}
                    disabled={!item.available}
                  >
                    {item.available ? 'Add to Cart' : 'Not Available'}
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </>
  )
}

export default MenuPage
