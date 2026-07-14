import { useAuth } from '../context/AuthContext'
import './Navbar.css'

const Navbar = ({ title }) => {
  const { user, logout } = useAuth()

  return (
    <nav className="navbar">
      <div className="navbar-brand">{title || 'JustEat'}</div>
      <div className="navbar-menu">
        <span>Welcome, {user?.username}</span>
        <button className="btn btn-secondary" onClick={logout}>
          Logout
        </button>
      </div>
    </nav>
  )
}

export default Navbar
