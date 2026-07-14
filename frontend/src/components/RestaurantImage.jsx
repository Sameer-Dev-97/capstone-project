import { useState } from 'react'

const RestaurantImage = ({ imageUrl, alt }) => {
  const [hasError, setHasError] = useState(false)
  const showImage = Boolean(imageUrl) && !hasError

  return (
    <div className="restaurant-image-wrapper" aria-label="Restaurant image container">
      {showImage ? (
        <img
          src={imageUrl}
          alt={alt || 'Restaurant'}
          className="restaurant-image"
          onError={() => setHasError(true)}
        />
      ) : (
        <div className="restaurant-image-fallback" aria-label="No restaurant image">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.75"
            strokeLinecap="round"
            strokeLinejoin="round"
            width="40"
            height="40"
            aria-hidden="true"
          >
            <rect x="3" y="5" width="18" height="14" rx="2" ry="2" />
            <circle cx="9" cy="10" r="1.5" />
            <path d="M21 15l-4.5-4.5a1 1 0 00-1.4 0L8 17" />
          </svg>
          <span>No image</span>
        </div>
      )}
    </div>
  )
}

export default RestaurantImage
