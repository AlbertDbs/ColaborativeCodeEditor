import { FormEvent, useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../state/AuthProvider';
import { acceptInvitation, createInvitation, fetchInvitations, Invitation, refuseInvitation } from '../api/invitations';
import { fetchWorkspaces, Workspace } from '../api/workspaces';
import AppHeader from '../components/AppHeader';

const InvitationsPage = () => {
  const { token } = useAuth();
  const [sentItems, setSentItems] = useState<Invitation[]>([]);
  const [receivedItems, setReceivedItems] = useState<Invitation[]>([]);
  const [workspaceId, setWorkspaceId] = useState('');
  const [workspaces, setWorkspaces] = useState<Workspace[]>([]);
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const load = async () => {
      if (!token) return;
      try {
        setError(null);
        setLoading(true);
        const [sentData, recvData, wsData] = await Promise.all([
          fetchInvitations(token, 'sent'),
          fetchInvitations(token, 'received'),
          fetchWorkspaces(token)
        ]);
        setSentItems(sentData);
        setReceivedItems(recvData);
        setWorkspaces(wsData);
        if (!workspaceId && wsData.length > 0) setWorkspaceId(wsData[0].id);
      } catch (e: any) {
        setError(e.message || 'Could not load invitations');
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
      const inv = await createInvitation(token, workspaceId, email);
      setSentItems((prev) => [inv, ...prev]);
      setWorkspaceId('');
      setEmail('');
    } catch (e: any) {
      setError(e.message || 'Could not create invitation');
    } finally {
      setSaving(false);
    }
  };

  const updateStatus = async (id: string, action: 'accept' | 'refuse') => {
    if (!token) return;
    try {
      const inv = action === 'accept' ? await acceptInvitation(token, id) : await refuseInvitation(token, id);
      setReceivedItems((list) => list.map((i) => (i.id === id ? inv : i)));
    } catch (e: any) {
      setError(e.message || 'Could not update invitation');
    }
  };

  const workspaceName = useMemo(() => new Map(workspaces.map((w) => [w.id, w.name])), [workspaces]);
  const workspaceLabel = (id: string) => workspaceName.get(id) || id;
  const statusBadge = (status: Invitation['status']) => (
    <span className={`badge status-${status.toLowerCase()}`}>{status}</span>
  );

  return (
    <>
      <AppHeader />
      <div className="page-shell">
        <div className="page-header">
          <div>
            <p className="eyebrow">Invitations</p>
            <h1>Manage invitations</h1>
            <p className="muted">Send, accept, or refuse invitations in your workspaces.</p>
          </div>
          <Link className="button-link" to="/">⟵ Back</Link>
        </div>

        <section className="panel wide-panel invite-form-card">
          <div>
            <h3>Send an invitation</h3>
            <p className="muted">Choose the workspace and add your teammate’s email.</p>
          </div>
          <form className="invite-form" onSubmit={onSubmit}>
            <select
              aria-label="workspace"
              value={workspaceId}
              onChange={(e) => setWorkspaceId(e.target.value)}
              required
            >
              <option value="">Select workspace</option>
              {workspaces.map((w) => (
                <option key={w.id} value={w.id}>{w.name}</option>
              ))}
            </select>
            <input
              aria-label="email"
              type="email"
              placeholder="email@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
            <button className="primary-btn" type="submit" disabled={!workspaceId || !email || saving}>
              {saving ? 'Sending...' : 'Send invitation'}
            </button>
          </form>
          {error && <p className="error" style={{ marginTop: '0.5rem' }}>{error}</p>}
        </section>

        {loading ? (
          <p className="muted">Loading...</p>
        ) : (
          <div className="invite-columns">
            <section className="panel">
              <div className="section-header">
                <div>
                  <h3>Received invitations</h3>
                  <p className="muted">Accept or refuse the invitations you got.</p>
                </div>
              </div>
              {receivedItems.length === 0 ? (
                <p className="muted empty-state">No received invitations.</p>
              ) : (
                <div className="invite-list">
                  {receivedItems.map((inv) => (
                    <div key={inv.id} className="invite-card">
                      <div className="invite-main">
                        <div className="invite-email">{inv.inviteeEmail}</div>
                        <div className="invite-workspace">Workspace: {workspaceLabel(inv.workspaceId)}</div>
                      </div>
                      <div className="invite-actions">
                        {statusBadge(inv.status)}
                        {inv.status === 'PENDING' && (
                          <>
                            <button className="btn" onClick={() => updateStatus(inv.id, 'accept')}>Accept</button>
                            <button className="danger-btn" onClick={() => updateStatus(inv.id, 'refuse')}>Refuse</button>
                          </>
                        )}
                        {inv.status === 'ACCEPTED' && (
                          <Link className="button-link" to={`/documents?workspaceId=${inv.workspaceId}`}>Open workspace</Link>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </section>

            <section className="panel">
              <div className="section-header">
                <div>
                  <h3>Sent invitations</h3>
                  <p className="muted">Track the status of invites you sent.</p>
                </div>
              </div>
              {sentItems.length === 0 ? (
                <p className="muted empty-state">No sent invitations.</p>
              ) : (
                <div className="invite-list">
                  {sentItems.map((inv) => (
                    <div key={inv.id} className="invite-card">
                      <div className="invite-main">
                        <div className="invite-email">{inv.inviteeEmail}</div>
                        <div className="invite-workspace">Workspace: {workspaceLabel(inv.workspaceId)}</div>
                      </div>
                      <div className="invite-actions">
                        {statusBadge(inv.status)}
                        {inv.status === 'ACCEPTED' && (
                          <Link className="button-link" to={`/documents?workspaceId=${inv.workspaceId}`}>Open workspace</Link>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </section>
          </div>
        )}
      </div>
    </>
  );
};

export default InvitationsPage;
