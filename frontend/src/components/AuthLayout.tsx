import React from 'react';

type AuthLayoutProps = {
  title: string;
  subtitle?: string;
  children: React.ReactNode;
  footer?: React.ReactNode;
  hero?: React.ReactNode;
};

const AuthLayout: React.FC<AuthLayoutProps> = ({ title, subtitle, children, footer, hero }) => {
  return (
    <div className="auth-shell">
      <div className="auth-grid">
        {hero && <div className="auth-hero">{hero}</div>}
        <div className="auth-card">
          <div className="auth-card-header">
            <div className="logo-mark small">CE</div>
            <div>
              <p className="eyebrow">Collaborative Editor</p>
              <h1>{title}</h1>
              {subtitle && <p className="muted">{subtitle}</p>}
            </div>
          </div>
          <div className="auth-card-body">{children}</div>
          {footer && <div className="auth-card-footer">{footer}</div>}
        </div>
      </div>
    </div>
  );
};

export default AuthLayout;
