import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import Navbar from '../components/Navbar'
import Sidebar from '../components/Sidebar'
import RestaurantImage from '../components/RestaurantImage'
import { restaurantService } from '../services/api'
import { toast } from 'react-toastify'

const RestaurantList = () => {
  const [restaurants, setRestaurants] = useState([])
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(true)

  const sidebarLinks = [
    { path: '/customer', label: 'Dashboard' },
    { path: '/restaurants', label: 'Browse Restaurants' },
    { path: '/cart', label: 'My Cart' },
    { path: '/orders/history', label: 'Order History' },
    { path: '/preferences', label: 'Preferences' },
  ]

  useEffect(() => {
    fetchRestaurants()
  }, [])

  const fetchRestaurants = async (searchTerm = '') => {
    setLoading(true)
    try {
      const response = await restaurantService.getAll(searchTerm)
      setRestaurants(response.data)
    } catch (error) {
      toast.error('Failed to load restaurants')
    } finally {
      setLoading(false)
    }
  }

  const handleSearch = (e) => {
    e.preventDefault()
    fetchRestaurants(search)
  }

  return (
    <>
      <Navbar title="Browse Restaurants" />
      <div className="dashboard">
        <Sidebar links={sidebarLinks} />
        <div className="main-content">
          <h1>Browse Restaurants</h1>

          <div className="card">
            <form onSubmit={handleSearch}>
              <div className="form-group">
                <input
                  type="text"
                  className="form-control"
                  placeholder="Search by name, location, or cuisine..."
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                />
              </div>
              <button type="submit" className="btn btn-primary">Search</button>
            </form>
          </div>

          {loading ? (
            <div className="loading">Loading...</div>
          ) : (
            <div className="grid grid-3">
              {restaurants.map((restaurant) => (
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
          )}
        </div>
      </div>
    </>
  )
}

export default RestaurantList
