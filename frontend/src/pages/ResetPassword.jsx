import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { toast } from 'react-toastify'
import { authService } from '../services/api'
import './Auth.css'

const PASSWORD_RULES = [
  { label: 'At least 8 characters', test: (p) => p.length >= 8 },
  { label: 'One uppercase letter (A-Z)', test: (p) => /[A-Z]/.test(p) },
  { label: 'One lowercase letter (a-z)', test: (p) => /[a-z]/.test(p) },
  { label: 'One digit (0-9)', test: (p) => /[0-9]/.test(p) },
  { label: 'One special character (@#$%^&+=)', test: (p) => /[@#$%^&+=]/.test(p) },
]

const ResetPassword = () => {
  const navigate = useNavigate()
  const [formData, setFormData] = useState({ token: '', newPassword: '', confirmPassword: '' })
  const [loading, setLoading] = useState(false)
  const [showRules, setShowRules] = useState(false)

  const handleChange = (e) => setFormData({ ...formData, [e.target.name]: e.target.value })

  const allRulesMet = PASSWORD_RULES.every((r) => r.test(formData.newPassword))

  const handleSubmit = async (e) => {
    e.preventDefault()

    if (!allRulesMet) {
      toast.error('Password does not meet complexity requirements.')
      return
    }

    if (formData.newPassword !== formData.confirmPassword) {
      toast.error('Passwords do not match.')
      return
    }

    setLoading(true)
    try {
      const response = await authService.confirmPasswordReset({
        token: formData.token,
        newPassword: formData.newPassword,
      })
      toast.success(response.data || 'Password reset successful! Please log in.')
      navigate('/login')
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to reset password. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-container">
      <div className="auth-card">
        <h1>JustEat</h1>
        <h2>Reset Password</h2>
        <p style={{ color: '#666', marginBottom: '1.5rem', fontSize: '0.95rem' }}>
          Enter the 6-digit OTP sent to your email, then choose a new password.
        </p>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">OTP Code</label>
            <input
              type="text"
              name="token"
              className="form-control"
              value={formData.token}
              onChange={handleChange}
              placeholder="Enter 6-digit OTP"
              maxLength={6}
              required
              style={{ letterSpacing: '4px', fontSize: '1.2rem', textAlign: 'center' }}
            />
          </div>

          <div className="form-group">
            <label className="form-label">New Password</label>
            <input
              type="password"
              name="newPassword"
              className="form-control"
              value={formData.newPassword}
              onChange={handleChange}
              onFocus={() => setShowRules(true)}
              placeholder="New password"
              required
            />
            {showRules && (
              <ul style={{ marginTop: '0.5rem', paddingLeft: '1.2rem', fontSize: '0.82rem' }}>
                {PASSWORD_RULES.map((rule) => (
                  <li
                    key={rule.label}
                    style={{ color: rule.test(formData.newPassword) ? '#2e7d32' : '#c62828' }}
                  >
                    {rule.test(formData.newPassword) ? '✔' : '✘'} {rule.label}
                  </li>
                ))}
              </ul>
            )}
          </div>

          <div className="form-group">
            <label className="form-label">Confirm New Password</label>
            <input
              type="password"
              name="confirmPassword"
              className="form-control"
              value={formData.confirmPassword}
              onChange={handleChange}
              placeholder="Repeat new password"
              required
            />
            {formData.confirmPassword && (
              <p style={{ fontSize: '0.82rem', marginTop: '0.3rem', color: formData.newPassword === formData.confirmPassword ? '#2e7d32' : '#c62828' }}>
                {formData.newPassword === formData.confirmPassword ? '✔ Passwords match' : '✘ Passwords do not match'}
              </p>
            )}
          </div>

          <button
            type="submit"
            className="btn btn-primary btn-block"
            disabled={loading || !allRulesMet}
          >
            {loading ? 'Resetting...' : 'Reset Password'}
          </button>
        </form>
        <p className="auth-footer">
          <Link to="/forgot-password">Resend OTP</Link> &nbsp;|&nbsp; <Link to="/login">Back to Login</Link>
        </p>
      </div>
    </div>
  )
}

export default ResetPassword
