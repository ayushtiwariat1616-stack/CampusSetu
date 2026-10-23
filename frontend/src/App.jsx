import { useState, useEffect, useCallback } from 'react';
import Navbar from './components/Navbar';
import LandingPage from './components/LandingPage';
import HowItWorksPage from './components/HowItWorksPage';
import FAQPage from './components/FAQPage';
import PrivacyPage from './components/PrivacyPage';
import LoginPage from './components/LoginPage';
import StudentDashboard from './components/StudentDashboard';
import OperatorDashboard from './components/OperatorDashboard';
import AdminDashboard from './components/AdminDashboard';
import OpeningSplash from './components/OpeningSplash';
import { api } from './api';

export default function App() {
  const [currentUser, setCurrentUser] = useState(null);
  const [activeTab, setActiveTab] = useState('landing');
  const [loading, setLoading] = useState(true);
  const [showSplash, setShowSplash] = useState(true);

  const checkCurrentUser = useCallback(async () => {
    try {
      const res = await api.getMe();
      if (res?.data) {
        setCurrentUser(res.data);
        if (res.data.role === 'STUDENT') {
          setActiveTab('student-dashboard');
        } else if (res.data.role === 'OPERATOR') {
          if (res.data.operatorKeyVerified) {
            setActiveTab('operator-dashboard');
          } else {
            setActiveTab('login');
          }
        } else if (res.data.role === 'SYSTEM_ADMIN') {
          setActiveTab(res.data.operatorKeyVerified ? 'admin-dashboard' : 'login');
        }
      } else {
        setCurrentUser(null);
      }
    } catch {
      setCurrentUser(null);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    checkCurrentUser();
  }, [checkCurrentUser]);

  useEffect(() => {
    // Dynamic SEO titles based on current page
    const titles = {
      landing: 'CampusSetu | KIET College Complaint & Academic Platform',
      'how-it-works': 'How It Works | Deterministic Routing Architecture | CampusSetu',
      faq: 'Frequently Asked Questions & Support | CampusSetu',
      privacy: 'Security & Privacy Safeguards | CampusSetu',
      login: 'Sign In | Google Workspace & Operator Desk | CampusSetu',
      'student-dashboard': 'Student Portal | CampusSetu',
      'operator-dashboard': 'Operator Workspace | CampusSetu',
      'admin-dashboard': 'System Administration Console | CampusSetu',
      'shared-incidents': 'Campus Community Outages & Maintenance | CampusSetu',
      academics: 'Academic Notes & Course Assistant | CampusSetu',
    };
    document.title = titles[activeTab] || 'CampusSetu | KIET Service Platform';
  }, [activeTab]);

  const handleLoginSuccess = (userData) => {
    setCurrentUser(userData);
    if (userData.role === 'STUDENT') {
      setActiveTab('student-dashboard');
    } else if (userData.role === 'OPERATOR') {
      if (userData.operatorKeyVerified) {
        setActiveTab('operator-dashboard');
      } else {
        setActiveTab('login');
      }
    } else if (userData.role === 'SYSTEM_ADMIN') {
      setActiveTab(userData.operatorKeyVerified ? 'admin-dashboard' : 'login');
    }
  };

  const handleLogout = async () => {
    try {
      await api.logout();
    } catch {
      // Ignored
    }
    setCurrentUser(null);
    setActiveTab('landing');
  };

  if (loading) {
    return (
      <div style={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: 'var(--bg-main)'
      }}>
        <div style={{ textAlign: 'center' }}>
          <div style={{ fontSize: '1.25rem', fontWeight: '800', color: 'var(--primary-navy)', marginBottom: '8px' }}>
            CampusSetu
          </div>
          <div style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
            Loading portal session...
          </div>
        </div>
      </div>
    );
  }

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      {/* Opening Intro Animation */}
      {showSplash && (
        <OpeningSplash onFinish={() => setShowSplash(false)} />
      )}

      {/* Main Navbar */}
      <Navbar
        currentUser={currentUser}
        activeTab={activeTab}
        onSelectTab={setActiveTab}
        onLogout={handleLogout}
      />

      {/* Main Page Content */}
      <main style={{ flex: 1 }}>
        {activeTab === 'landing' && (
          <LandingPage
            onSelectTab={setActiveTab}
          />
        )}

        {activeTab === 'how-it-works' && (
          <HowItWorksPage onSelectTab={setActiveTab} />
        )}

        {activeTab === 'faq' && <FAQPage />}

        {activeTab === 'privacy' && <PrivacyPage />}

        {activeTab === 'login' && (
          <LoginPage
            currentUser={currentUser}
            onLoginSuccess={handleLoginSuccess}
            onLogout={handleLogout}
          />
        )}

        {activeTab === 'student-dashboard' && (
          <StudentDashboard
            currentUser={currentUser}
          />
        )}

        {activeTab === 'operator-dashboard' && (
          <OperatorDashboard currentUser={currentUser} />
        )}

        {activeTab === 'admin-dashboard' && (
          <AdminDashboard currentUser={currentUser} />
        )}

        {activeTab === 'shared-incidents' && (
          <div className="container" style={{ padding: '36px 0 60px', maxWidth: '860px' }}>
            <div style={{ textAlign: 'center', marginBottom: '28px' }}>
              <span className="badge badge-acknowledged" style={{ marginBottom: '8px' }}>Community Outage Noticeboard</span>
              <h1 style={{ fontSize: '1.75rem', fontWeight: '800', color: 'var(--primary-navy)' }}>
                Active Campus Shared Incidents
              </h1>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.9375rem', marginTop: '4px' }}>
                Track public restoration updates without disclosing student identities or room numbers.
              </p>
            </div>
            {/* If logged in as student, StudentDashboard handles it; if guest, prompt sign in */}
            {currentUser?.role === 'STUDENT' ? (
              <StudentDashboard currentUser={currentUser} />
            ) : (
              <div className="card" style={{ textAlign: 'center', padding: '36px' }}>
                <p style={{ color: 'var(--text-secondary)', marginBottom: '16px' }}>
                  Sign in with your verified college Google account to subscribe to community outage notices and indicate affected locations.
                </p>
                <button onClick={() => setActiveTab('login')} className="btn btn-primary">
                  Sign In to Subscribe
                </button>
              </div>
            )}
          </div>
        )}

        {activeTab === 'academics' && (
          <div className="container" style={{ padding: '36px 0 60px', maxWidth: '880px' }}>
            {currentUser ? (
              <StudentDashboard currentUser={currentUser} />
            ) : (
              <div className="card" style={{ textAlign: 'center', padding: '36px' }}>
                <h2 style={{ fontSize: '1.25rem', fontWeight: '800', color: 'var(--primary-navy)', marginBottom: '8px' }}>
                  Academic Notes & Syllabus Retrieval Hub
                </h2>
                <p style={{ color: 'var(--text-secondary)', marginBottom: '16px' }}>
                  Please sign in with your college account to view department-scoped course notes and ask questions with exact page citations.
                </p>
                <button onClick={() => setActiveTab('login')} className="btn btn-primary">
                  Sign In to Browse Notes
                </button>
              </div>
            )}
          </div>
        )}
      </main>

      {/* College Service Portal Footer */}
      <footer style={{
        backgroundColor: '#091827',
        color: '#94a3b8',
        borderTop: '1px solid #1e293b',
        padding: '36px 0 24px',
        fontSize: '0.8125rem'
      }}>
        <div className="container">
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '28px', marginBottom: '28px' }}>
            <div>
              <div style={{ fontSize: '1rem', fontWeight: '800', color: '#ffffff', marginBottom: '6px' }}>
                CampusSetu
              </div>
              <p style={{ lineHeight: 1.5, color: '#64748b' }}>
                Unified campus grievance redressal, deterministic facilities routing, and grounded academic assistance for KIET Group of Institutions.
              </p>
              <div style={{ marginTop: '10px', fontSize: '0.75rem', color: '#94a3b8', fontWeight: '600' }}>
                KIET Group of Institutions • Enterprise Campus Portal
              </div>
            </div>

            <div>
              <div style={{ fontWeight: '700', color: '#e2e8f0', marginBottom: '8px' }}>Platform Navigation</div>
              <ul style={{ listStyle: 'none', display: 'flex', flexDirection: 'column', gap: '6px' }}>
                <li><a href="#" onClick={(e) => { e.preventDefault(); setActiveTab('landing'); }} style={{ color: '#94a3b8' }}>Portal Overview</a></li>
                <li><a href="#" onClick={(e) => { e.preventDefault(); setActiveTab('how-it-works'); }} style={{ color: '#94a3b8' }}>How Routing Works</a></li>
                <li><a href="#" onClick={(e) => { e.preventDefault(); setActiveTab('shared-incidents'); }} style={{ color: '#94a3b8' }}>Shared Outages</a></li>
                <li><a href="#" onClick={(e) => { e.preventDefault(); setActiveTab('academics'); }} style={{ color: '#94a3b8' }}>Academic Notes</a></li>
              </ul>
            </div>

            <div>
              <div style={{ fontWeight: '700', color: '#e2e8f0', marginBottom: '8px' }}>Accountability & Privacy</div>
              <ul style={{ listStyle: 'none', display: 'flex', flexDirection: 'column', gap: '6px' }}>
                <li><a href="#" onClick={(e) => { e.preventDefault(); setActiveTab('privacy'); }} style={{ color: '#94a3b8' }}>Privacy Safeguards</a></li>
                <li><a href="#" onClick={(e) => { e.preventDefault(); setActiveTab('faq'); }} style={{ color: '#94a3b8' }}>Student Closure Confirmation</a></li>
                <li><a href="https://www.kiet.edu" target="_blank" rel="noopener noreferrer" style={{ color: '#94a3b8' }}>Official KIET Website ↗</a></li>
              </ul>
            </div>
          </div>

          <div style={{
            borderTop: '1px solid #1e293b',
            paddingTop: '16px',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            flexWrap: 'wrap',
            gap: '8px',
            color: '#64748b',
            fontSize: '0.75rem'
          }}>
            <div>
              © 2026 CampusSetu. KIET Group of Institutions. All rights reserved.
            </div>
            <div>
              Enterprise Facilities & Academic Services Network
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
}
