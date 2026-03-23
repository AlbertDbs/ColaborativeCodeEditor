import { FormEvent, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../state/AuthProvider';
import AuthLayout from '../components/AuthLayout';
import AuthDivider from '../components/AuthDivider';
import GoogleSignInButton from '../components/GoogleSignInButton';
import { getGoogleIdToken } from '../utils/googleAuth';

const LoginPage = () => {
  const { login, loginWithGoogle } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [googleLoading, setGoogleLoading] = useState(false);

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await login(email, password);
      const from = (location.state as any)?.from?.pathname || '/';
      navigate(from, { replace: true });
    } catch (err: any) {
      setError(err.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  const onGoogle = async () => {
    setError(null);
    setGoogleLoading(true);
    try {
      const credential = await getGoogleIdToken();
      await loginWithGoogle(credential);
      const from = (location.state as any)?.from?.pathname || '/';
      navigate(from, { replace: true });
    } catch (err: any) {
      setError(err.message || 'Google sign-in failed (configure Google auth)');
    } finally {
      setGoogleLoading(false);
    }
  };

  return (
    <AuthLayout
      title="Welcome back"
      subtitle="Sign in to collaborate on code, documents, and workspaces."
      hero={
        <div className="auth-hero-pane">
          <h2>Ship together</h2>
          <p className="muted">Real-time editing, shared workspaces, and lightning-fast navigation.</p>
          <ul className="feature-list">
            <li>Live cursors & presence</li>
            <li>Secure workspace access</li>
            <li>Versioned documents</li>
          </ul>
        </div>
      }
    >
      <form className="auth-form" onSubmit={onSubmit}>
        <label className="field">
          <span>Email</span>
          <input
            aria-label="email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </label>
        <label className="field">
          <span>Password</span>
          <input
            aria-label="password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </label>
        <div className="auth-inline">
          <Link className="muted" to="#" onClick={(e) => e.preventDefault()}>Forgot password?</Link>
        </div>
        <button className="btn" type="submit" disabled={loading}>
          {loading ? 'Signing in...' : 'Sign in'}
        </button>
      </form>

      <AuthDivider />

      <GoogleSignInButton onClick={onGoogle} loading={googleLoading} />

      {error && <p className="error" style={{ marginTop: '0.75rem' }}>{error}</p>}

      <div className="auth-footnote">
        <p className="muted">
          Don’t have an account? <Link to="/register">Sign up</Link>
        </p>
      </div>
    </AuthLayout>
  );
};

export default LoginPage;
