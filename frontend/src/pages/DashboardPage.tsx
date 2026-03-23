import { Link } from 'react-router-dom';
import AppHeader from '../components/AppHeader';

const DashboardPage = () => {
  return (
    <>
      <AppHeader />
      <div className="layout">
        <div className="card wide">
          <h1>Welcome back!</h1>
          <p>Use the cards below for quick navigation.</p>
          <div className="grid">
            <div className="list-item">
              <div>
                <strong>Workspaces</strong>
                <div className="muted">Create and organize your spaces.</div>
              </div>
              <Link className="button-link" to="/workspaces">Open</Link>
            </div>
            <div className="list-item">
              <div>
                <strong>Invitations</strong>
                <div className="muted">Send or accept invitations.</div>
              </div>
              <Link className="button-link" to="/invitations">Open</Link>
            </div>
            <div className="list-item">
              <div>
                <strong>Documents</strong>
                <div className="muted">View files inside your workspaces.</div>
              </div>
              <Link className="button-link" to="/documents">Open</Link>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default DashboardPage;
