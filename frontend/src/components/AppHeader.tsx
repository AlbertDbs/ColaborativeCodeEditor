import { NavLink, useNavigate } from 'react-router-dom';
import { useState } from 'react';
import { useAuth } from '../state/AuthProvider';

const AppHeader = () => {
  const { logout } = useAuth();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };

  const navLinks = [
    { to: '/workspaces', label: 'Workspaces' },
    { to: '/invitations', label: 'Invitations' },
    { to: '/documents', label: 'Editor' },
    { to: '/profile', label: 'Profile' }
  ];

  const navClass = ({ isActive }: { isActive: boolean }) =>
    `header-link ${isActive ? 'active' : ''}`;

  return (
    <header className="app-header">
      <div className="header-left" onClick={() => navigate('/')} role="button" tabIndex={0}>
        <div className="logo-mark">CE</div>
        <div className="logo-text">
          <span>Collaborative</span>
          <strong>Editor</strong>
        </div>
      </div>

      <nav className={`header-nav ${open ? 'open' : ''}`}>
        {navLinks.map((link) => (
          <NavLink
            key={link.to}
            to={link.to}
            className={navClass}
            onClick={() => setOpen(false)}
          >
            {link.label}
          </NavLink>
        ))}
      </nav>

      <div className="header-actions">
        <button className="ghost-btn desktop-only" onClick={() => navigate('/profile')}>
          Profile
        </button>
        <button className="primary-btn desktop-only" onClick={handleLogout}>Logout</button>
        <button
          className="icon-btn mobile-only"
          aria-label="Toggle navigation"
          onClick={() => setOpen((v) => !v)}
        >
          ☰
        </button>
      </div>
    </header>
  );
};

export default AppHeader;
