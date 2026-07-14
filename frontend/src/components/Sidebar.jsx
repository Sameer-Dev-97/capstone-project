import { Link, useLocation } from 'react-router-dom'
import './Sidebar.css'

const Sidebar = ({ links }) => {
  const location = useLocation()

  return (
    <div className="sidebar">
      <div className="sidebar-header">
        <h2>Dashboard</h2>
      </div>
      <div className="sidebar-links">
        {links.map((link) => (
          <Link
            key={link.path}
            to={link.path}
            className={`sidebar-link ${location.pathname === link.path ? 'active' : ''}`}
          >
            {link.label}
          </Link>
        ))}
      </div>
    </div>
  )
}

export default Sidebar
