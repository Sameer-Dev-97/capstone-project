import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Navbar from '../components/Navbar'
import Sidebar from '../components/Sidebar'
import RestaurantImage from '../components/RestaurantImage'
import { restaurantService } from '../services/api'
import { toast } from 'react-toastify'

const RestaurantManagement = () => {
  const navigate = useNavigate()
  const [restaurants, setRestaurants] = useState([])
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [editingId, setEditingId] = useState(null)
  const [formData, setFormData] = useState({
    name: '',
    location: '',
    cuisine: '',
    imageUrl: '',
  })

  const sidebarLinks = [
    { path: '/owner', label: 'Dashboard' },
    { path: '/owner/restaurants', label: 'My Restaurants' },
    { path: '/owner/orders', label: 'Orders' },
  ]

  useEffect(() => {
    fetchRestaurants()
  }, [])

  const fetchRestaurants = async () => {
    setLoading(true)
    try {
      const response = await restaurantService.getMyRestaurants()
      setRestaurants(response.data)
    } catch (error) {
      toast.error('Failed to load restaurants')
    } finally {
      setLoading(false)
    }
  }

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      if (editingId) {
        await restaurantService.update(editingId, formData)
        toast.success('Restaurant updated successfully!')
      } else {
        await restaurantService.create(formData)
        toast.success('Restaurant created successfully!')
      }
      setShowForm(false)
      setEditingId(null)
      setFormData({ name: '', location: '', cuisine: '', imageUrl: '' })
      fetchRestaurants()
    } catch (error) {
      toast.error('Failed to save restaurant')
    }
  }

  const handleEdit = (restaurant) => {
    setFormData({
      name: restaurant.name,
      location: restaurant.location,
      cuisine: restaurant.cuisine,
      imageUrl: restaurant.imageUrl || '',
    })
    setEditingId(restaurant.id)
    setShowForm(true)
  }

  return (
    <>
      <Navbar title="My Restaurants" />
      <div className="dashboard">
        <Sidebar links={sidebarLinks} />
        <div className="main-content">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <h1>My Restaurants</h1>
            <button 
              className="btn btn-primary" 
              onClick={() => {
                setShowForm(!showForm)
                setEditingId(null)
                setFormData({ name: '', location: '', cuisine: '', imageUrl: '' })
              }}
            >
              {showForm ? 'Cancel' : 'Add Restaurant'}
            </button>
          </div>

          {showForm && (
            <div className="card">
              <h2>{editingId ? 'Edit Restaurant' : 'Add New Restaurant'}</h2>
              <form onSubmit={handleSubmit}>
                <div className="form-group">
                  <label className="form-label">Restaurant Name</label>
                  <input
                    type="text"
                    name="name"
                    className="form-control"
                    value={formData.name}
                    onChange={handleChange}
                    required
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">Location</label>
                  <input
                    type="text"
                    name="location"
                    className="form-control"
                    value={formData.location}
                    onChange={handleChange}
                    required
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">Cuisine</label>
                  <input
                    type="text"
                    name="cuisine"
                    className="form-control"
                    value={formData.cuisine}
                    onChange={handleChange}
                    required
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">Image URL (optional)</label>
                  <input
                    type="url"
                    name="imageUrl"
                    className="form-control"
                    value={formData.imageUrl}
                    onChange={handleChange}
                    placeholder="https://example.com/restaurant.jpg"
                  />
                </div>
                <button type="submit" className="btn btn-primary">
                  {editingId ? 'Update' : 'Create'}
                </button>
              </form>
            </div>
          )}

          {loading ? (
            <div className="loading">Loading...</div>
          ) : restaurants.length > 0 ? (
            <div className="grid grid-2">
              {restaurants.map((restaurant) => (
                <div key={restaurant.id} className="card">
                  <RestaurantImage imageUrl={restaurant.imageUrl} alt={restaurant.name} />
                  <h3>{restaurant.name}</h3>
                  <p><strong>Cuisine:</strong> {restaurant.cuisine}</p>
                  <p><strong>Location:</strong> {restaurant.location}</p>
                  <p><strong>Rating:</strong> ⭐ {restaurant.rating}/5</p>
                  <div style={{ display: 'flex', gap: '0.5rem', marginTop: '1rem' }}>
                    <button 
                      className="btn btn-secondary" 
                      onClick={() => handleEdit(restaurant)}
                    >
                      Edit
                    </button>
                    <button 
                      className="btn btn-primary" 
                      onClick={() => navigate(`/owner/menu/${restaurant.id}`)}
                    >
                      Manage Menu
                    </button>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="card">
              <p>No restaurants yet. Add your first restaurant!</p>
            </div>
          )}
        </div>
      </div>
    </>
  )
}

export default RestaurantManagement
