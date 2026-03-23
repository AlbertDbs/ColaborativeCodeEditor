import React from 'react';

type Props = {
  onClick: () => void;
  loading?: boolean;
  label?: string;
};

const GoogleSignInButton: React.FC<Props> = ({ onClick, loading, label }) => {
  return (
    <button type="button" className="google-btn" onClick={onClick} disabled={loading}>
      <span className="google-icon" aria-hidden>
        <svg viewBox="0 0 48 48" width="18" height="18">
          <path
            fill="#EA4335"
            d="M24 9.5c3.15 0 5.97 1.08 8.2 3.2l6.1-6.1C34.97 3.16 29.8 1 24 1 14.64 1 6.51 6.7 3.05 15l7.45 5.79C12 14.35 17.43 9.5 24 9.5z"
          />
          <path
            fill="#4285F4"
            d="M46.5 24.5c0-1.6-.14-3.12-.41-4.59H24v9.08h12.7c-.55 2.94-2.2 5.43-4.69 7.1l7.26 5.63C43.93 38.14 46.5 31.8 46.5 24.5z"
          />
          <path
            fill="#FBBC05"
            d="M10.5 28.21c-.5-1.49-.79-3.07-.79-4.71 0-1.64.29-3.22.8-4.71l-7.46-5.79C1.6 16.49 1 19.16 1 22c0 2.84.6 5.51 1.75 7.99l7.75-5.78z"
          />
          <path
            fill="#34A853"
            d="M24 47c6.48 0 11.9-2.13 15.86-5.81l-7.26-5.63c-2 1.35-4.57 2.14-8.6 2.14-6.56 0-12.08-4.43-14.05-10.49l-7.75 5.78C6.5 41.3 14.64 47 24 47z"
          />
          <path fill="none" d="M1 1h46v46H1z" />
        </svg>
      </span>
      {loading ? 'Connecting…' : label || 'Continue with Google'}
    </button>
  );
};

export default GoogleSignInButton;
