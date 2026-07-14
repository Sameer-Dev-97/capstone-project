import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import Navbar from '../components/Navbar'
import Sidebar from '../components/Sidebar'
import { restaurantService, orderService, menuService } from '../services/api'
import { toast } from 'react-toastify'

const OwnerDashboard = () => {
  const [restaurants, setRestaurants] = useState([])
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [popularItems, setPopularItems] = useState([])
  const [popularLoading, setPopularLoading] = useState(false)
  const [refreshingId, setRefreshingId] = useState(null)

  const sidebarLinks = [
    { path: '/owner', label: 'Dashboard' },
    { path: '/owner/restaurants', label: 'My Restaurants' },
    { path: '/owner/orders', label: 'Orders' },
  ]

  useEffect(() => {
    fetchData()
  }, [])

  const fetchData = async () => {
    setLoading(true)
    try {
      const [restaurantsRes, ordersRes] = await Promise.all([
        restaurantService.getMyRestaurants(),
        orderService.getRestaurantOrders(),
      ])
      setRestaurants(restaurantsRes.data)
      setOrders(ordersRes.data)
      fetchAllPopularItems(restaurantsRes.data)
    } catch (error) {
      toast.error('Failed to load data')
    } finally {
      setLoading(false)
    }
  }

  const fetchAllPopularItems = async (restaurantList) => {
    if (!restaurantList || restaurantList.length === 0) return
    setPopularLoading(true)
    try {
      const results = await Promise.all(
        restaurantList.map((r) =>
          menuService.getPopularItems(r.id).then((res) =>
            res.data.map((item) => ({ ...item, restaurantName: r.name }))
          ).catch(() => [])
        )
      )
      setPopularItems(results.flat())
    } catch (error) {
      // silently ignore — individual errors handled per restaurant
    } finally {
      setPopularLoading(false)
    }
  }

  const handleRefreshPopularity = async (restaurantId) => {
    setRefreshingId(restaurantId)
    try {
      await menuService.refreshPopularity(restaurantId)
      toast.success('Popularity refreshed!')
      const restaurant = restaurants.find((r) => r.id === restaurantId)
      const res = await menuService.getPopularItems(restaurantId)
      const updatedItems = res.data.map((item) => ({ ...item, restaurantName: restaurant?.name }))
      setPopularItems((prev) => [
        ...prev.filter((item) => item.restaurantId !== restaurantId),
        ...updatedItems,
      ])
    } catch (error) {
      toast.error('Failed to refresh popularity')
    } finally {
      setRefreshingId(null)
    }
  }

  const pendingOrders = orders.filter((o) => o.status === 'PENDING').length

  const formatOrderItems = (items = []) => {
    if (!items.length) return 'N/A'
    return items
      .map((item) => `${item.menuItemName} x${item.quantity}`)
      .join(', ')
  }

  return (
    <>
      <Navbar title="JustEat - Owner Dashboard" />
      <div className="dashboard">
        <Sidebar links={sidebarLinks} />
        <div className="main-content">
          <h1>Restaurant Owner Dashboard</h1>

          <div className="grid grid-3">
            <div className="card" style={{ background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', color: 'white' }}>
              <h2>{restaurants.length}</h2>
              <p>Total Restaurants</p>
            </div>
            <div className="card" style={{ background: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)', color: 'white' }}>
              <h2>{orders.length}</h2>
              <p>Total Orders</p>
            </div>
            <div className="card" style={{ background: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)', color: 'white' }}>
              <h2>{pendingOrders}</h2>
              <p>Pending Orders</p>
            </div>
          </div>

          <div className="card">
            <h2>Quick Actions</h2>
            <div style={{ display: 'flex', gap: '1rem', marginTop: '1rem' }}>
              <Link to="/owner/restaurants" className="btn btn-primary">
                Manage Restaurants
              </Link>
              <Link to="/owner/orders" className="btn btn-secondary">
                View Orders
              </Link>
            </div>
          </div>

          <div className="card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h2>🔥 Mostly Ordered Items</h2>
              <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                {restaurants.map((r) => (
                  <button
                    key={r.id}
                    className="btn btn-secondary"
                    onClick={() => handleRefreshPopularity(r.id)}
                    disabled={refreshingId === r.id}
                    style={{ fontSize: '0.8rem', padding: '0.4rem 0.8rem' }}
                  >
                    {refreshingId === r.id ? 'Refreshing…' : `Refresh "${r.name}"`}
                  </button>
                ))}
              </div>
            </div>
            {popularLoading ? (
              <p>Loading popular items…</p>
            ) : popularItems.length > 0 ? (
              <div className="table" style={{ marginTop: '1rem' }}>
                <table>
                  <thead>
                    <tr>
                      <th>Item</th>
                      <th>Restaurant</th>
                      <th>Category</th>
                      <th>Price</th>
                      <th>Orders</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {popularItems.map((item) => (
                      <tr key={item.id}>
                        <td><strong>{item.name}</strong></td>
                        <td>{item.restaurantName}</td>
                        <td>{item.category}</td>
                        <td>₹{item.price}</td>
                        <td>{item.orderCount}</td>
                        <td>
                          <span className="badge" style={{ background: '#ff6b35', color: 'white', padding: '0.2rem 0.6rem', borderRadius: '12px', fontSize: '0.75rem' }}>
                            🔥 Most Ordered
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : (
              <p style={{ marginTop: '1rem', color: '#666' }}>
                No popular items yet. Orders need to accumulate before items are flagged automatically. Use "Refresh" to recalculate.
              </p>
            )}
          </div>

          <div className="card">
            <h2>Recent Orders</h2>
            {loading ? (
              <p>Loading...</p>
            ) : orders.length > 0 ? (
              <div className="table">
                <table>
                  <thead>
                    <tr>
                      <th>Order ID</th>
                      <th>Customer</th>
                      <th>Restaurant</th>
                      <th>Items</th>
                      <th>Total</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {orders.slice(0, 5).map((order) => (
                      <tr key={order.id}>
                        <td>#{order.id}</td>
                        <td>{order.customerName}</td>
                        <td>{order.restaurantName}</td>
                        <td>{formatOrderItems(order.items)}</td>
                        <td>₹{order.totalPrice}</td>
                        <td>
                          <span className={`badge badge-${order.status.toLowerCase()}`}>
                            {order.status}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : (
              <p>No orders yet.</p>
            )}
          </div>
        </div>
      </div>
    </>
  )
}

export default OwnerDashboard
