import { useState, useEffect, useCallback } from 'react';
import {
  Bell,
  LogOut,
  Layers,
  BookOpen,
  HelpCircle,
  Menu,
  X
} from 'lucide-react';
import { api } from '../api';

export default function Navbar({ currentUser, activeTab, onSelectTab, onLogout }) {
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [showNotifs, setShowNotifs] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const loadNotifications = useCallback(async () => {
    try {
      const res = await api.getNotifications(0, 10);
      if (res?.data) {
        setNotifications(res.data.notifications?.content || []);
        setUnreadCount(res.data.unreadCount || 0);
      }
    } catch {
      // Ignored for unauthenticated states
    }
  }, []);

  useEffect(() => {
    if (currentUser) {
      loadNotifications();
      const interval = setInterval(loadNotifications, 30000);
      return () => clearInterval(interval);
    }
  }, [currentUser, loadNotifications]);

  const handleMarkRead = async (id) => {
    try {
      await api.markNotificationRead(id);
      loadNotifications();
    } catch (e) {
      console.error(e);
    }
  };

  return (
    <header style={{
      backgroundColor: '#ffffff',
      borderBottom: '1px solid var(--border-subtle)',
      position: 'sticky',
      top: 0,
      zIndex: 100,
      boxShadow: 'var(--shadow-sm)'
    }}>
      <div className="container" style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        height: '68px'
      }}>
        {/* Brand */}
        <div
          onClick={() => onSelectTab(currentUser ? (currentUser.role === 'STUDENT' ? 'student-dashboard' : 'operator-dashboard') : 'landing')}
          style={{ display: 'flex', alignItems: 'center', gap: '12px', cursor: 'pointer' }}
        >
          <div style={{
            width: '40px',
            height: '40px',
            borderRadius: '10px',
            background: 'linear-gradient(135deg, #0f2942 0%, #0d9488 100%)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: '#ffffff',
            fontWeight: '800',
            fontSize: '1.2rem',
            boxShadow: '0 2px 4px rgba(13, 148, 136, 0.2)'
          }}>
            CS
          </div>
          <div>
            <div style={{ fontSize: '1.15rem', fontWeight: '800', color: 'var(--primary-navy)', letterSpacing: '-0.02em', lineHeight: 1.1 }}>
              CampusSetu
            </div>
            <div style={{ fontSize: '0.6875rem', fontWeight: '600', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
              KIET Group of Institutions
            </div>
          </div>
        </div>

        {/* Desktop Navigation */}
        <nav style={{ display: 'flex', alignItems: 'center', gap: '8px' }} className="desktop-nav">
          <button
            onClick={() => onSelectTab('landing')}
            className={`btn btn-sm ${activeTab === 'landing' ? 'btn-navy' : 'btn-outline'}`}
          >
            Overview
          </button>
          <button
            onClick={() => onSelectTab('how-it-works')}
            className={`btn btn-sm ${activeTab === 'how-it-works' ? 'btn-navy' : 'btn-outline'}`}
          >
            How It Works
          </button>
          <button
            onClick={() => onSelectTab('shared-incidents')}
            className={`btn btn-sm ${activeTab === 'shared-incidents' ? 'btn-navy' : 'btn-outline'}`}
          >
            <Layers size={14} />
            Shared Outages
          </button>
          <button
            onClick={() => onSelectTab('academics')}
            className={`btn btn-sm ${activeTab === 'academics' ? 'btn-navy' : 'btn-outline'}`}
          >
            <BookOpen size={14} />
            Academic Notes
          </button>
          <button
            onClick={() => onSelectTab('faq')}
            className={`btn btn-sm ${activeTab === 'faq' ? 'btn-navy' : 'btn-outline'}`}
          >
            <HelpCircle size={14} />
            Help & FAQ
          </button>
        </nav>

        {/* User / Auth Area */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          {currentUser ? (
            <>
              {/* Notification Bell */}
              <div style={{ position: 'relative' }}>
                <button
                  onClick={() => setShowNotifs(!showNotifs)}
                  style={{
                    backgroundColor: 'var(--bg-subtle)',
                    border: '1px solid var(--border-subtle)',
                    borderRadius: '8px',
                    width: '38px',
                    height: '38px',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    cursor: 'pointer',
                    position: 'relative',
                    color: 'var(--text-secondary)'
                  }}
                  title="Notifications"
                >
                  <Bell size={18} />
                  {unreadCount > 0 && (
                    <span style={{
                      position: 'absolute',
                      top: '-4px',
                      right: '-4px',
                      backgroundColor: '#ef4444',
                      color: '#ffffff',
                      borderRadius: '10px',
                      padding: '1px 5px',
                      fontSize: '0.65rem',
                      fontWeight: '700'
                    }}>
                      {unreadCount}
                    </span>
                  )}
                </button>

                {/* Notifications Popup */}
                {showNotifs && (
                  <div style={{
                    position: 'absolute',
                    right: 0,
                    top: '46px',
                    width: '340px',
                    maxHeight: '420px',
                    overflowY: 'auto',
                    backgroundColor: '#ffffff',
                    border: '1px solid var(--border-subtle)',
                    borderRadius: 'var(--radius-md)',
                    boxShadow: 'var(--shadow-lg)',
                    padding: '12px',
                    zIndex: 200
                  }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px', paddingBottom: '6px', borderBottom: '1px solid var(--border-subtle)' }}>
                      <span style={{ fontWeight: '700', fontSize: '0.875rem' }}>Notifications</span>
                      <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{unreadCount} unread</span>
                    </div>

                    {notifications.length === 0 ? (
                      <div style={{ padding: '24px 8px', textAlign: 'center', color: 'var(--text-muted)', fontSize: '0.8125rem' }}>
                        No new notifications
                      </div>
                    ) : (
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                        {notifications.map((n) => (
                          <div
                            key={n.id}
                            style={{
                              padding: '8px 10px',
                              backgroundColor: n.read ? '#ffffff' : 'var(--accent-teal-subtle)',
                              border: '1px solid var(--border-subtle)',
                              borderRadius: '6px',
                              fontSize: '0.8125rem',
                              cursor: 'pointer'
                            }}
                            onClick={() => handleMarkRead(n.id)}
                          >
                            <div style={{ fontWeight: '600', color: 'var(--primary-navy)', marginBottom: '2px' }}>{n.title}</div>
                            <div style={{ color: 'var(--text-secondary)', fontSize: '0.75rem', lineHeight: '1.3' }}>{n.message}</div>
                            <div style={{ fontSize: '0.6875rem', color: 'var(--text-muted)', marginTop: '4px' }}>
                              {new Date(n.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                            </div>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                )}
              </div>

              {/* User Profile Pill */}
              <div
                onClick={() => onSelectTab(currentUser.role === 'STUDENT' ? 'student-dashboard' : 'operator-dashboard')}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '8px',
                  padding: '4px 10px',
                  backgroundColor: 'var(--bg-subtle)',
                  borderRadius: '20px',
                  border: '1px solid var(--border-subtle)',
                  cursor: 'pointer'
                }}
              >
                <div style={{
                  width: '28px',
                  height: '28px',
                  borderRadius: '50%',
                  backgroundColor: currentUser.role === 'STUDENT' ? '#0d9488' : '#0f2942',
                  color: '#ffffff',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: '0.75rem',
                  fontWeight: '700'
                }}>
                  {currentUser.fullName ? currentUser.fullName[0] : 'U'}
                </div>
                <div style={{ textAlign: 'left', lineHeight: 1.1 }}>
                  <div style={{ fontSize: '0.8125rem', fontWeight: '700', color: 'var(--text-primary)' }}>
                    {currentUser.fullName || currentUser.email.split('@')[0]}
                  </div>
                  <div style={{ fontSize: '0.6875rem', color: 'var(--text-muted)' }}>
                    {currentUser.role === 'STUDENT' ? `Student • ${currentUser.hostelCode || 'Hostel'}` : (currentUser.role === 'SYSTEM_ADMIN' ? 'Administrator' : 'Operator')}
                  </div>
                </div>
              </div>

              {/* Logout Button */}
              <button
                onClick={onLogout}
                className="btn btn-outline btn-sm"
                title="Sign Out"
                style={{ padding: '8px' }}
              >
                <LogOut size={16} />
              </button>
            </>
          ) : (
            <button
              onClick={() => onSelectTab('login')}
              className="btn btn-primary btn-sm"
            >
              Sign In
            </button>
          )}

          {/* Mobile hamburger toggle */}
          <button
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            style={{
              display: 'none',
              background: 'none',
              border: 'none',
              cursor: 'pointer',
              color: 'var(--text-primary)'
            }}
            className="mobile-nav-toggle"
          >
            {mobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
          </button>
        </div>
      </div>

      {/* Mobile Drawer */}
      {mobileMenuOpen && (
        <div style={{
          backgroundColor: '#ffffff',
          borderBottom: '1px solid var(--border-subtle)',
          padding: '12px 16px',
          display: 'flex',
          flexDirection: 'column',
          gap: '8px'
        }}>
          <button onClick={() => { onSelectTab('landing'); setMobileMenuOpen(false); }} className="btn btn-outline" style={{ justifyContent: 'flex-start' }}>Overview</button>
          <button onClick={() => { onSelectTab('how-it-works'); setMobileMenuOpen(false); }} className="btn btn-outline" style={{ justifyContent: 'flex-start' }}>How It Works</button>
          <button onClick={() => { onSelectTab('shared-incidents'); setMobileMenuOpen(false); }} className="btn btn-outline" style={{ justifyContent: 'flex-start' }}>Shared Outages</button>
          <button onClick={() => { onSelectTab('academics'); setMobileMenuOpen(false); }} className="btn btn-outline" style={{ justifyContent: 'flex-start' }}>Academic Notes</button>
          <button onClick={() => { onSelectTab('faq'); setMobileMenuOpen(false); }} className="btn btn-outline" style={{ justifyContent: 'flex-start' }}>Help & FAQ</button>
          {currentUser && (
            <button
              onClick={() => {
                onSelectTab(currentUser.role === 'STUDENT' ? 'student-dashboard' : 'operator-dashboard');
                setMobileMenuOpen(false);
              }}
              className="btn btn-primary"
              style={{ justifyContent: 'flex-start' }}
            >
              Go to Dashboard
            </button>
          )}
        </div>
      )}

      <style>{`
        @media (max-width: 840px) {
          .desktop-nav { display: none !important; }
          .mobile-nav-toggle { display: block !important; }
        }
      `}</style>
    </header>
  );
}
