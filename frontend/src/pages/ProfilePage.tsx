import AppHeader from '../components/AppHeader';

const ProfilePage = () => {
  return (
    <>
      <AppHeader />
      <div className="page-shell">
        <div className="page-header">
          <div>
            <p className="eyebrow">Account</p>
            <h1>Your profile</h1>
            <p className="muted">Profile details are available here while the account area is still taking shape.</p>
          </div>
        </div>

        <section className="panel">
          <div className="panel-header">
            <div>
              <h3>Account overview</h3>
              <p className="muted">This is a placeholder screen for basic account information.</p>
            </div>
          </div>
          <div className="stack">
            <div className="list-item">
              <div>
                <strong>Display name</strong>
                <div className="muted">To be connected to the backend.</div>
              </div>
            </div>
            <div className="list-item">
              <div>
                <strong>Email settings</strong>
                <div className="muted">Reserved for future account controls.</div>
              </div>
            </div>
          </div>
        </section>
      </div>
    </>
  );
};

export default ProfilePage;
