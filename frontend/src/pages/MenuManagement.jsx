import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import Navbar from '../components/Navbar'
import Sidebar from '../components/Sidebar'
import { menuService } from '../services/api'
import { toast } from 'react-toastify'

const MenuManagement = () => {
  const { restaurantId } = useParams()
  const [menuItems, setMenuItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [editingId, setEditingId] = useState(null)
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    price: 0,
    category: '',
    available: true,
    todaysSpecial: false,
    dealOfDay: false,
  })

  const sidebarLinks = [
    { path: '/owner', label: 'Dashboard' },
    { path: '/owner/restaurants', label: 'My Restaurants' },
    { path: '/owner/orders', label: 'Orders' },
  ]

  useEffect(() => {
    fetchMenu()
  }, [restaurantId])

  const fetchMenu = async () => {
    setLoading(true)
    try {
      const response = await menuService.getMenu(restaurantId)
      setMenuItems(response.data)
    } catch (error) {
      toast.error('Failed to load menu')
    } finally {
      setLoading(false)
    }
  }

  const handleChange = (e) => {
    const value = e.target.type === 'checkbox' ? e.target.checked : e.target.value
    setFormData({ ...formData, [e.target.name]: value })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      if (editingId) {
        await menuService.updateItem(editingId, formData)
        toast.success('Menu item updated successfully!')
      } else {
        await menuService.createItem(restaurantId, formData)
        toast.success('Menu item created successfully!')
      }
      setShowForm(false)
      setEditingId(null)
      setFormData({
        name: '',
        description: '',
        price: 0,
        category: '',
        available: true,
        todaysSpecial: false,
        dealOfDay: false,
      })
      fetchMenu()
    } catch (error) {
      toast.error('Failed to save menu item')
    }
  }

  const handleEdit = (item) => {
    setFormData({
      name: item.name,
      description: item.description,
      price: item.price,
      category: item.category,
      available: item.available,
      todaysSpecial: item.todaysSpecial,
      dealOfDay: item.dealOfDay,
    })
    setEditingId(item.id)
    setShowForm(true)
  }

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this item?')) {
      try {
        await menuService.deleteItem(id)
        toast.success('Menu item deleted successfully!')
        fetchMenu()
      } catch (error) {
        toast.error(error.response?.data?.message || 'Failed to delete menu item')
      }
    }
  }

  return (
    <>
      <Navbar title="Menu Management" />
      <div className="dashboard">
        <Sidebar links={sidebarLinks} />
        <div className="main-content">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <h1>Menu Management</h1>
            <button 
              className="btn btn-primary" 
              onClick={() => {
                setShowForm(!showForm)
                setEditingId(null)
                setFormData({
                  name: '',
                  description: '',
                  price: 0,
                  category: '',
                  available: true,
                  todaysSpecial: false,
                  dealOfDay: false,
                })
              }}
            >
              {showForm ? 'Cancel' : 'Add Menu Item'}
            </button>
          </div>

          {showForm && (
            <div className="card">
              <h2>{editingId ? 'Edit Menu Item' : 'Add New Menu Item'}</h2>
              <form onSubmit={handleSubmit}>
                <div className="form-group">
                  <label className="form-label">Item Name</label>
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
                  <label className="form-label">Description</label>
                  <textarea
                    name="description"
                    className="form-control"
                    value={formData.description}
                    onChange={handleChange}
                    rows="3"
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">Price (₹)</label>
                  <input
                    type="number"
                    name="price"
                    className="form-control"
                    min="0"
                    step="0.01"
                    value={formData.price}
                    onChange={handleChange}
                    required
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">Category</label>
                  <input
                    type="text"
                    name="category"
                    className="form-control"
                    value={formData.category}
                    onChange={handleChange}
                    required
                  />
                </div>
                <div className="form-group">
                  <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <input
                      type="checkbox"
                      name="available"
                      checked={formData.available}
                      onChange={handleChange}
                    />
                    Available
                  </label>
                </div>
                <div className="form-group">
                  <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <input
                      type="checkbox"
                      name="todaysSpecial"
                      checked={formData.todaysSpecial}
                      onChange={handleChange}
                    />
                    Today's Special
                  </label>
                </div>
                <div className="form-group">
                  <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <input
                      type="checkbox"
                      name="dealOfDay"
                      checked={formData.dealOfDay}
                      onChange={handleChange}
                    />
                    Deal of Day
                  </label>
                </div>
                <button type="submit" className="btn btn-primary">
                  {editingId ? 'Update' : 'Create'}
                </button>
              </form>
            </div>
          )}

          {loading ? (
            <div className="loading">Loading...</div>
          ) : menuItems.length > 0 ? (
            <div className="table">
              <table>
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Category</th>
                    <th>Price</th>
                    <th>Status</th>
                    <th>Special</th>
                    <th>Orders</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {menuItems.map((item) => (
                    <tr key={item.id}>
                      <td>{item.name}</td>
                      <td>{item.category}</td>
                      <td>₹{item.price}</td>
                      <td>
                        <span className={`badge ${item.available ? 'badge-ready' : 'badge-pending'}`}>
                          {item.available ? 'Available' : 'Unavailable'}
                        </span>
                      </td>
                      <td>
                        {item.todaysSpecial && <span className="badge badge-ready">Today</span>}
                        {item.dealOfDay && <span className="badge badge-preparing" style={{marginLeft: '5px'}}>Deal</span>}
                        {item.mostOrdered && <span className="badge badge-pending" style={{marginLeft: '5px'}}>Most Ordered</span>}
                      </td>
                      <td>{item.orderCount}</td>
                      <td>
                        <div style={{ display: 'flex', gap: '0.5rem' }}>
                          <button 
                            className="btn btn-secondary" 
                            onClick={() => handleEdit(item)}
                            style={{ padding: '0.5rem 1rem' }}
                          >
                            Edit
                          </button>
                          <button 
                            className="btn btn-danger" 
                            onClick={() => handleDelete(item.id)}
                            style={{ padding: '0.5rem 1rem' }}
                          >
                            Delete
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="card">
              <p>No menu items yet. Add your first item!</p>
            </div>
          )}
        </div>
      </div>
    </>
  )
}

export default MenuManagement
