import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import Navbar from '../components/Navbar'
import Sidebar from '../components/Sidebar'
import RestaurantImage from '../components/RestaurantImage'
import { preferenceService } from '../services/api'
import { toast } from 'react-toastify'

const CustomerDashboard = () => {
  const [recommendations, setRecommendations] = useState([])
  const [loading, setLoading] = useState(true)

  const sidebarLinks = [
    { path: '/customer', label: 'Dashboard' },
    { path: '/restaurants', label: 'Browse Restaurants' },
    { path: '/cart', label: 'My Cart' },
    { path: '/orders/history', label: 'Order History' },
    { path: '/preferences', label: 'Preferences' },
  ]

  useEffect(() => {
    fetchRecommendations()
  }, [])

  const fetchRecommendations = async () => {
    try {
      const response = await preferenceService.getRecommendations()
      setRecommendations(response.data)
    } catch (error) {
      toast.error('Failed to load recommendations')
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <Navbar title="JustEat - Customer Dashboard" />
      <div className="dashboard">
        <Sidebar links={sidebarLinks} />
        <div className="main-content">
          <h1>Welcome to JustEat!</h1>
          <p>Your favorite food, delivered fast.</p>

          <div className="card">
            <h2>Quick Actions</h2>
            <div style={{ display: 'flex', gap: '1rem', marginTop: '1rem' }}>
              <Link to="/restaurants" className="btn btn-primary">
                Browse Restaurants
              </Link>
              <Link to="/cart" className="btn btn-secondary">
                View Cart
              </Link>
            </div>
          </div>

          <div className="card">
            <h2>Recommended for You</h2>
            {loading ? (
              <p>Loading...</p>
            ) : recommendations.length > 0 ? (
              <div className="grid grid-3">
                {recommendations.map((restaurant) => (
                  <div key={restaurant.id} className="card">
                    <RestaurantImage imageUrl={restaurant.imageUrl} alt={restaurant.name} />
                    <h3>{restaurant.name}</h3>
                    <p><strong>Cuisine:</strong> {restaurant.cuisine}</p>
                    <p><strong>Location:</strong> {restaurant.location}</p>
                    <p><strong>Rating:</strong> ⭐ {restaurant.rating}/5</p>
                    <Link to={`/restaurants/${restaurant.id}/menu`} className="btn btn-primary">
                      View Menu
                    </Link>
                  </div>
                ))}
              </div>
            ) : (
              <p>No recommendations available. Update your preferences!</p>
            )}
          </div>
        </div>
      </div>
    </>
  )
}

export default CustomerDashboard
