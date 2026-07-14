import { useEffect, useState } from 'react'
import Navbar from '../components/Navbar'
import Sidebar from '../components/Sidebar'
import { preferenceService } from '../services/api'
import { toast } from 'react-toastify'

const Preferences = () => {
  const [preferences, setPreferences] = useState({
    favoriteRestaurants: [],
    favoriteCuisines: [],
    dietaryPreferences: [],
  })
  const [cuisine, setCuisine] = useState('')
  const [dietary, setDietary] = useState('')
  const [loading, setLoading] = useState(true)

  const sidebarLinks = [
    { path: '/customer', label: 'Dashboard' },
    { path: '/restaurants', label: 'Browse Restaurants' },
    { path: '/cart', label: 'My Cart' },
    { path: '/orders/history', label: 'Order History' },
    { path: '/preferences', label: 'Preferences' },
  ]

  useEffect(() => {
    fetchPreferences()
  }, [])

  const fetchPreferences = async () => {
    setLoading(true)
    try {
      const response = await preferenceService.getPreferences()
      setPreferences(response.data)
    } catch (error) {
      toast.error('Failed to load preferences')
    } finally {
      setLoading(false)
    }
  }

  const addCuisine = () => {
    if (cuisine && !preferences.favoriteCuisines.includes(cuisine)) {
      setPreferences({
        ...preferences,
        favoriteCuisines: [...preferences.favoriteCuisines, cuisine],
      })
      setCuisine('')
    }
  }

  const removeCuisine = (item) => {
    setPreferences({
      ...preferences,
      favoriteCuisines: preferences.favoriteCuisines.filter((c) => c !== item),
    })
  }

  const addDietary = () => {
    if (dietary && !preferences.dietaryPreferences.includes(dietary)) {
      setPreferences({
        ...preferences,
        dietaryPreferences: [...preferences.dietaryPreferences, dietary],
      })
      setDietary('')
    }
  }

  const removeDietary = (item) => {
    setPreferences({
      ...preferences,
      dietaryPreferences: preferences.dietaryPreferences.filter((d) => d !== item),
    })
  }

  const savePreferences = async () => {
    try {
      await preferenceService.updatePreferences(preferences)
      toast.success('Preferences updated successfully!')
    } catch (error) {
      toast.error('Failed to update preferences')
    }
  }

  return (
    <>
      <Navbar title="My Preferences" />
      <div className="dashboard">
        <Sidebar links={sidebarLinks} />
        <div className="main-content">
          <h1>My Preferences</h1>

          {loading ? (
            <div className="loading">Loading...</div>
          ) : (
            <>
              <div className="card">
                <h2>Favorite Cuisines</h2>
                <div style={{ display: 'flex', gap: '1rem', marginBottom: '1rem', flexWrap: 'wrap' }}>
                  {preferences.favoriteCuisines.map((item, index) => (
                    <div key={index} style={{ 
                      padding: '0.5rem 1rem', 
                      background: 'var(--light-color)',
                      borderRadius: '20px',
                      display: 'flex',
                      gap: '0.5rem',
                      alignItems: 'center'
                    }}>
                      <span>{item}</span>
                      <button 
                        onClick={() => removeCuisine(item)}
                        style={{ background: 'none', border: 'none', cursor: 'pointer', fontWeight: 'bold' }}
                      >
                        ×
                      </button>
                    </div>
                  ))}
                </div>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  <input
                    type="text"
                    className="form-control"
                    placeholder="Add cuisine (e.g., Italian, Chinese)"
                    value={cuisine}
                    onChange={(e) => setCuisine(e.target.value)}
                    onKeyPress={(e) => e.key === 'Enter' && addCuisine()}
                  />
                  <button className="btn btn-primary" onClick={addCuisine}>Add</button>
                </div>
              </div>

              <div className="card">
                <h2>Dietary Preferences</h2>
                <div style={{ display: 'flex', gap: '1rem', marginBottom: '1rem', flexWrap: 'wrap' }}>
                  {preferences.dietaryPreferences.map((item, index) => (
                    <div key={index} style={{ 
                      padding: '0.5rem 1rem', 
                      background: 'var(--light-color)',
                      borderRadius: '20px',
                      display: 'flex',
                      gap: '0.5rem',
                      alignItems: 'center'
                    }}>
                      <span>{item}</span>
                      <button 
                        onClick={() => removeDietary(item)}
                        style={{ background: 'none', border: 'none', cursor: 'pointer', fontWeight: 'bold' }}
                      >
                        ×
                      </button>
                    </div>
                  ))}
                </div>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  <input
                    type="text"
                    className="form-control"
                    placeholder="Add preference (e.g., Vegetarian, Vegan)"
                    value={dietary}
                    onChange={(e) => setDietary(e.target.value)}
                    onKeyPress={(e) => e.key === 'Enter' && addDietary()}
                  />
                  <button className="btn btn-primary" onClick={addDietary}>Add</button>
                </div>
              </div>

              <button className="btn btn-primary" onClick={savePreferences}>
                Save Preferences
              </button>
            </>
          )}
        </div>
      </div>
    </>
  )
}

export default Preferences
