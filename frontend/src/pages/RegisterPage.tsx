import { FormEvent, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../state/AuthProvider';
import AuthLayout from '../components/AuthLayout';
import AuthDivider from '../components/AuthDivider';
import GoogleSignInButton from '../components/GoogleSignInButton';
import { getGoogleIdToken } from '../utils/googleAuth';

const RegisterPage = () => {
  const { register, loginWithGoogle } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [googleLoading, setGoogleLoading] = useState(false);

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      if (password !== confirm) {
        setError('Passwords do not match');
        setLoading(false);
        return;
      }
      await register(email, password);
      navigate('/', { replace: true });
    } catch (err: any) {
      setError(err.message || 'Register failed');
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
      navigate('/', { replace: true });
    } catch (err: any) {
      setError(err.message || 'Google sign-in failed (configure Google auth)');
    } finally {
      setGoogleLoading(false);
    }
  };

  return (
    <AuthLayout
      title="Create your account"
      subtitle="Spin up workspaces, invite teammates, and start coding together."
      hero={
        <div className="auth-hero-pane">
          <h2>Collaborate securely</h2>
          <p className="muted">Granular permissions, live updates, and audit-friendly history.</p>
          <ul className="feature-list">
            <li>Invite-only workspaces</li>
            <li>Realtime presence</li>
            <li>File versioning</li>
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
        <label className="field">
          <span>Confirm password</span>
          <input
            aria-label="confirm password"
            type="password"
            value={confirm}
            onChange={(e) => setConfirm(e.target.value)}
            required
          />
        </label>
        <button className="btn" type="submit" disabled={loading}>
          {loading ? 'Creating account...' : 'Create account'}
        </button>
      </form>

      <AuthDivider />

      <GoogleSignInButton onClick={onGoogle} loading={googleLoading} />

      {error && <p className="error" style={{ marginTop: '0.75rem' }}>{error}</p>}

      <div className="auth-footnote">
        <p className="muted">
          Already have an account? <Link to="/login">Sign in</Link>
        </p>
      </div>
    </AuthLayout>
  );
};

export default RegisterPage;
