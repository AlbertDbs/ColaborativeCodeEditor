import { FormEvent, useEffect, useState } from 'react';
import { useAuth } from '../state/AuthProvider';
import { createWorkspace, fetchWorkspaces, Workspace } from '../api/workspaces';
import { Link } from 'react-router-dom';
import AppHeader from '../components/AppHeader';

const WorkspacesPage = () => {
  const { token, logout } = useAuth();
  const [workspaces, setWorkspaces] = useState<Workspace[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [name, setName] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    const load = async () => {
      if (!token) return;
      try {
        setError(null);
        setLoading(true);
        const data = await fetchWorkspaces(token);
        setWorkspaces(data);
      } catch (e: any) {
        setError(e.message || 'Could not load workspaces');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [token]);

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!token) return;
    setSaving(true);
    setError(null);
    try {
      const ws = await createWorkspace(token, name);
      setWorkspaces((w) => [ws, ...w]);
      setName('');
    } catch (e: any) {
      setError(e.message || 'Could not create workspace');
    } finally {
      setSaving(false);
    }
  };

  return (
    <>
      <AppHeader />
      <div className="page-shell">
        <div className="page-header">
          <div>
            <p className="eyebrow">Workspaces</p>
            <h1>Workspaces</h1>
            <p className="muted">Create and manage your spaces in one place.</p>
          </div>
          <Link className="button-link" to="/">⟵ Back</Link>
        </div>

        <section className="panel wide-panel workspace-create">
          <div>
            <h3>Create a workspace</h3>
            <p className="muted">Give it a clear name so teammates recognize it.</p>
          </div>
          <form onSubmit={onSubmit} className="workspace-form">
            <input
              aria-label="workspace name"
              placeholder="Workspace name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />
            <button className="primary-btn" type="submit" disabled={!name || saving}>
              {saving ? 'Creating...' : 'Create workspace'}
            </button>
          </form>
          {error && <p className="error" style={{ marginTop: '0.5rem' }}>{error}</p>}
        </section>

        <section className="panel">
          <div className="section-header">
            <div>
              <h3>Your workspaces</h3>
              <p className="muted">Select a workspace to open its files.</p>
            </div>
          </div>

          {loading ? (
            <p className="muted">Loading...</p>
          ) : workspaces.length === 0 ? (
            <p className="muted empty-state">You don’t have any workspaces yet.</p>
          ) : (
            <div className="workspace-list">
              {workspaces.map((ws) => (
                <div key={ws.id} className="workspace-card" role="button" tabIndex={0}>
                  <div className="workspace-main">
                    <div className="workspace-name">{ws.name}</div>
                    <div className="workspace-meta">
                      <span>Created: {new Date(ws.createdAt).toLocaleDateString()}</span>
                      <span>•</span>
                      <span>ID: {ws.id.slice(0, 8)}</span>
                    </div>
                  </div>
                  <Link className="button-link" to={`/documents?workspaceId=${ws.id}`}>Open</Link>
                </div>
              ))}
            </div>
          )}
        </section>
      </div>
    </>
  );
};

export default WorkspacesPage;
