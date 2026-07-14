import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Navbar from '../components/Navbar'
import Sidebar from '../components/Sidebar'
import { orderService, menuService } from '../services/api'
import { toast } from 'react-toastify'

const StarRating = ({ value, onChange }) => (
  <span style={{ fontSize: '1.4rem', cursor: onChange ? 'pointer' : 'default' }}>
    {[1, 2, 3, 4, 5].map((star) => (
      <span
        key={star}
        style={{ color: star <= value ? '#f5a623' : '#ccc' }}
        onClick={() => onChange && onChange(star)}
      >
        ★
      </span>
    ))}
  </span>
)

const OrderHistory = () => {
  const navigate = useNavigate()
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [reordering, setReordering] = useState(null)

  const [searchRestaurant, setSearchRestaurant] = useState('')
  const [filterStatus, setFilterStatus] = useState('')

  // Rating state: orderId -> { menuItemId -> selectedRating }
  const [expandedRating, setExpandedRating] = useState(null)
  const [pendingRatings, setPendingRatings] = useState({})
  const [submittingRating, setSubmittingRating] = useState(false)

  const sidebarLinks = [
    { path: '/customer', label: 'Dashboard' },
    { path: '/restaurants', label: 'Browse Restaurants' },
    { path: '/cart', label: 'My Cart' },
    { path: '/orders/history', label: 'Order History' },
    { path: '/preferences', label: 'Preferences' },
  ]

  useEffect(() => {
    fetchOrders()
  }, [searchRestaurant, filterStatus])

  const fetchOrders = async () => {
    setLoading(true)
    try {
      const response = await orderService.getHistory(searchRestaurant, filterStatus)
      setOrders(response.data)
    } catch (error) {
      toast.error('Failed to load order history')
    } finally {
      setLoading(false)
    }
  }

  const handleReorder = async (orderId) => {
    setReordering(orderId)
    try {
      await orderService.reorder(orderId)
      toast.success('Items added to cart! Redirecting...')
      navigate('/cart')
    } catch (error) {
      toast.error('Failed to reorder. Some items may be unavailable.')
    } finally {
      setReordering(null)
    }
  }

  const toggleRatingPanel = (orderId) => {
    setExpandedRating((prev) => (prev === orderId ? null : orderId))
    setPendingRatings({})
  }

  const handleStarClick = (menuItemId, star) => {
    setPendingRatings((prev) => ({ ...prev, [menuItemId]: star }))
  }

  const markOrderItemsAsRated = (orderId, entries) => {
    const ratedByItemId = Object.fromEntries(entries.map(([menuItemId, rating]) => [Number(menuItemId), rating]))
    setOrders((prevOrders) => prevOrders.map((order) => {
      if (order.id !== orderId) {
        return order
      }

      return {
        ...order,
        items: order.items?.map((item) => (
          ratedByItemId[item.menuItemId]
            ? { ...item, customerRating: ratedByItemId[item.menuItemId] }
            : item
        )),
      }
    }))
  }

  const handleSubmitRatings = async (orderId) => {
    const order = orders.find((o) => o.id === orderId)
    const entries = Object.entries(pendingRatings).filter(([menuItemId]) => {
      const item = order?.items?.find((orderItem) => orderItem.menuItemId === Number(menuItemId))
      return item && !item.customerRating
    })

    if (isOrderFullyRated(order)) {
      toast.info('This order is already rated and cannot be rated again.')
      return
    }

    if (entries.length === 0) {
      toast.info('Please select at least one star rating before submitting.')
      return
    }
    setSubmittingRating(true)
    try {
      await Promise.all(entries.map(([menuItemId, rating]) => menuService.rateItem(Number(menuItemId), orderId, rating)))
      toast.success('Ratings submitted! Restaurant rating updated.')
      markOrderItemsAsRated(orderId, entries)
      setExpandedRating(null)
      setPendingRatings({})
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to submit ratings.')
    } finally {
      setSubmittingRating(false)
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

  const hasRatedItems = (order) => order.items?.some((item) => item.customerRating)
  const isOrderFullyRated = (order) => order?.items?.length > 0 && order.items.every((item) => item.customerRating)

  return (
    <>
      <Navbar title="Order History" />
      <div className="dashboard">
        <Sidebar links={sidebarLinks} />
        <div className="main-content">
          <h1>Order History</h1>

          {/* Search & Filter Bar */}
          <div style={{ display: 'flex', gap: '1rem', marginBottom: '1.5rem', flexWrap: 'wrap' }}>
            <input
              type="text"
              placeholder="Search by restaurant..."
              value={searchRestaurant}
              onChange={(e) => setSearchRestaurant(e.target.value)}
              style={{
                padding: '0.5rem 1rem',
                borderRadius: '6px',
                border: '1px solid #ccc',
                minWidth: '220px',
                fontSize: '0.95rem'
              }}
            />
            <select
              value={filterStatus}
              onChange={(e) => setFilterStatus(e.target.value)}
              style={{
                padding: '0.5rem 1rem',
                borderRadius: '6px',
                border: '1px solid #ccc',
                fontSize: '0.95rem'
              }}
            >
              <option value="">All Statuses</option>
              <option value="PENDING">Pending</option>
              <option value="PREPARING">Preparing</option>
              <option value="READY">Ready</option>
              <option value="COMPLETED">Completed</option>
            </select>
            {(searchRestaurant || filterStatus) && (
              <button
                className="btn btn-secondary"
                onClick={() => { setSearchRestaurant(''); setFilterStatus('') }}
              >
                Clear Filters
              </button>
            )}
          </div>

          {loading ? (
            <div className="loading">Loading...</div>
          ) : orders.length > 0 ? (
            <div>
              {orders.map((order) => (
                <div key={order.id} className="card" style={{ marginBottom: '1rem' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '0.5rem' }}>
                    <div>
                      <strong>#{order.id}</strong> &nbsp;|&nbsp; {order.restaurantName} &nbsp;|&nbsp;
                      {new Date(order.orderDate).toLocaleDateString()} &nbsp;|&nbsp;
                      ₹{order.totalPrice} &nbsp;
                      <span className={`badge ${getStatusBadge(order.status)}`}>{order.status}</span>
                    </div>
                    <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                      <button className="btn btn-primary" onClick={() => navigate(`/orders/${order.id}/tracking`)}>
                        View Details
                      </button>
                      <button
                        className="btn btn-secondary"
                        disabled={reordering === order.id}
                        onClick={() => handleReorder(order.id)}
                      >
                        {reordering === order.id ? 'Adding...' : 'Reorder'}
                      </button>
                      {order.status === 'COMPLETED' && (
                        <button
                          className="btn btn-secondary"
                          disabled={isOrderFullyRated(order)}
                          onClick={() => toggleRatingPanel(order.id)}
                          style={{ background: expandedRating === order.id ? '#f5a623' : undefined, color: expandedRating === order.id ? '#fff' : undefined }}
                        >
                          {isOrderFullyRated(order)
                            ? 'Rated'
                            : hasRatedItems(order)
                              ? '★ Rate Remaining Items'
                              : '★ Rate Items'}
                        </button>
                      )}
                    </div>
                  </div>

                  {/* Rating Panel */}
                  {expandedRating === order.id && (
                    <div style={{ marginTop: '1rem', borderTop: '1px solid #eee', paddingTop: '1rem' }}>
                      <p style={{ fontWeight: 600, marginBottom: '0.5rem' }}>Rate the dishes from this order:</p>
                      {order.items && order.items.map((item) => (
                        <div key={item.menuItemId} style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '0.4rem', flexWrap: 'wrap' }}>
                          <span style={{ minWidth: '160px' }}>{item.menuItemName}</span>
                          <StarRating
                            value={item.customerRating || pendingRatings[item.menuItemId] || 0}
                            onChange={item.customerRating ? undefined : (star) => handleStarClick(item.menuItemId, star)}
                          />
                          {item.customerRating && (
                            <span style={{ color: '#666', fontSize: '0.95rem' }}>
                              Previously rated: {item.customerRating}/5
                            </span>
                          )}
                        </div>
                      ))}
                      <button
                        className="btn btn-primary"
                        style={{ marginTop: '0.8rem' }}
                        disabled={submittingRating}
                        onClick={() => handleSubmitRatings(order.id)}
                      >
                        {submittingRating ? 'Submitting...' : 'Submit Ratings'}
                      </button>
                    </div>
                  )}
                </div>
              ))}
            </div>
          ) : (
            <div className="card">
              <p>{searchRestaurant || filterStatus ? 'No orders match your search.' : 'No orders yet. Start ordering!'}</p>
              {!searchRestaurant && !filterStatus && (
                <button className="btn btn-primary" onClick={() => navigate('/restaurants')}>
                  Browse Restaurants
                </button>
              )}
            </div>
          )}
        </div>
      </div>
    </>
  )
}

export default OrderHistory

