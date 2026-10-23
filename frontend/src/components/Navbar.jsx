import { useState, useEffect, useCallback } from 'react';
import {
  Bell,
  LogOut,
  Layers,
  HelpCircle,
  Menu,
  X,
  Shield,
  GraduationCap
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

  const isFullyAuthenticated = currentUser && (currentUser.role === 'STUDENT' || currentUser.operatorKeyVerified);

  return (
    <header style={{ position: 'sticky', top: 0, zIndex: 100, boxShadow: 'var(--shadow-md)' }}>
      {/* KIET Official Top Information Bar */}
      <div style={{
        backgroundColor: '#081a33',
        color: '#ffffff',
        fontSize: '0.75rem',
        padding: '5px 0',
        borderBottom: '2px solid #f8b81f'
      }}>
        <div className="container" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '8px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px', fontWeight: '600', letterSpacing: '0.03em' }}>
            <span style={{ color: '#f8b81f', fontWeight: '800' }}>KIET DEEMED TO BE UNIVERSITY</span>
            <span style={{ opacity: 0.5 }}>|</span>
            <span style={{ color: '#cbd5e1' }}>Delhi-NCR, Ghaziabad</span>
            <span style={{ opacity: 0.5 }}>|</span>
            <span style={{ backgroundColor: '#9b1c2e', padding: '1px 7px', borderRadius: '4px', fontSize: '0.6875rem', fontWeight: '700' }}>
              NAAC 'A+' GRADE
            </span>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '16px', color: '#cbd5e1', fontSize: '0.71875rem' }}>
            <span>Grievance Redressal & Academic Verification Cell</span>
            <a href="https://www.kiet.edu" target="_blank" rel="noopener noreferrer" style={{ color: '#f8b81f', textDecoration: 'none', fontWeight: '700' }}>
              kiet.edu ↗
            </a>
          </div>
        </div>
      </div>

      {/* Main Navigation Bar */}
      <div style={{ backgroundColor: '#ffffff', borderBottom: '1px solid var(--border-subtle)' }}>
        <div className="container" style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          height: '66px'
        }}>
          {/* Brand */}
          <div
            onClick={() => onSelectTab(currentUser ? (currentUser.role === 'STUDENT' ? 'student-dashboard' : 'operator-dashboard') : 'landing')}
            style={{ display: 'flex', alignItems: 'center', gap: '12px', cursor: 'pointer' }}
          >
            <div style={{
              width: '42px',
              height: '42px',
              borderRadius: '8px',
              background: 'linear-gradient(135deg, #0b2545 0%, #800020 100%)',
              border: '2px solid #f8b81f',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: '#ffffff',
              fontWeight: '800',
              fontSize: '1.25rem',
              boxShadow: '0 2px 6px rgba(11, 37, 69, 0.25)'
            }}>
              CS
            </div>
            <div>
              <div style={{ fontSize: '1.2rem', fontWeight: '800', color: 'var(--primary-navy)', letterSpacing: '-0.02em', lineHeight: 1.1 }}>
                CampusSetu
              </div>
              <div style={{ fontSize: '0.6875rem', fontWeight: '700', color: 'var(--kiet-crimson)', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
                KIET Group of Institutions
              </div>
            </div>
          </div>

          {/* Desktop Navigation - NOTE: Academic Notes removed from main header bar as requested */}
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
              onClick={() => onSelectTab('faq')}
              className={`btn btn-sm ${activeTab === 'faq' ? 'btn-navy' : 'btn-outline'}`}
            >
              <HelpCircle size={14} />
              Help & FAQ
            </button>
          </nav>

          {/* User / Auth Area */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>

            {isFullyAuthenticated ? (
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
                      position: 'relative'
                    }}
                    aria-label="Notifications"
                  >
                    <Bell size={18} color="var(--primary-navy)" />
                    {unreadCount > 0 && (
                      <span style={{
                        position: 'absolute',
                        top: '-4px',
                        right: '-4px',
                        backgroundColor: 'var(--kiet-crimson)',
                        color: '#ffffff',
                        fontSize: '0.6875rem',
                        fontWeight: '800',
                        width: '18px',
                        height: '18px',
                        borderRadius: '50%',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        boxShadow: '0 2px 4px rgba(0,0,0,0.2)'
                      }}>
                        {unreadCount}
                      </span>
                    )}
                  </button>

                  {/* Dropdown Box */}
                  {showNotifs && (
                    <div style={{
                      position: 'absolute',
                      right: 0,
                      top: '46px',
                      width: '320px',
                      backgroundColor: '#ffffff',
                      border: '1px solid var(--border-subtle)',
                      borderRadius: 'var(--radius-md)',
                      boxShadow: 'var(--shadow-lg)',
                      zIndex: 200,
                      padding: '12px'
                    }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px', paddingBottom: '6px', borderBottom: '1px solid var(--border-subtle)' }}>
                        <span style={{ fontWeight: '700', fontSize: '0.875rem', color: 'var(--primary-navy)' }}>
                          Alerts & Notifications
                        </span>
                        <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                          {unreadCount} unread
                        </span>
                      </div>

                      {notifications.length === 0 ? (
                        <div style={{ padding: '16px 8px', textAlign: 'center', color: 'var(--text-muted)', fontSize: '0.8125rem' }}>
                          No recent notifications
                        </div>
                      ) : (
                        <div style={{ maxHeight: '260px', overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '8px' }}>
                          {notifications.map((n) => (
                            <div
                              key={n.id}
                              onClick={() => handleMarkRead(n.id)}
                              style={{
                                padding: '8px 10px',
                                borderRadius: '6px',
                                backgroundColor: n.isRead ? 'transparent' : 'var(--bg-subtle)',
                                cursor: 'pointer',
                                fontSize: '0.8125rem',
                                borderLeft: n.isRead ? '2px solid transparent' : '3px solid var(--kiet-crimson)'
                              }}
                            >
                              <div style={{ fontWeight: n.isRead ? '500' : '700', color: 'var(--text-primary)', marginBottom: '2px' }}>
                                {n.title}
                              </div>
                              <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
                                {n.message}
                              </div>
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
                  onClick={() => onSelectTab(currentUser.role === 'STUDENT' ? 'student-dashboard' : (currentUser.role === 'SYSTEM_ADMIN' ? 'admin-dashboard' : 'operator-dashboard'))}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '10px',
                    backgroundColor: 'var(--bg-subtle)',
                    padding: '4px 12px 4px 6px',
                    borderRadius: 'var(--radius-full)',
                    border: '1px solid var(--border-subtle)',
                    cursor: 'pointer'
                  }}
                  title="Go to Dashboard Workspace"
                >
                  <div style={{
                    width: '32px',
                    height: '32px',
                    borderRadius: '50%',
                    backgroundColor: 'var(--primary-navy)',
                    color: '#f8b81f',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontWeight: '800',
                    fontSize: '0.8125rem',
                    border: '1px solid #f8b81f'
                  }}>
                    {currentUser.fullName ? currentUser.fullName.charAt(0).toUpperCase() : 'U'}
                  </div>
                  <div style={{ textAlign: 'left', lineHeight: 1.2 }}>
                    <div style={{ fontSize: '0.8125rem', fontWeight: '700', color: 'var(--primary-navy)' }}>
                      {currentUser.fullName}
                    </div>
                    <div style={{ fontSize: '0.6875rem', color: 'var(--kiet-crimson)', fontWeight: '600' }}>
                      {currentUser.role} {currentUser.hostelCode ? `• ${currentUser.hostelCode}` : ''}
                    </div>
                  </div>
                </div>

                {/* Logout Button */}
                <button
                  onClick={onLogout}
                  className="btn btn-outline btn-sm"
                  title="Sign Out"
                  style={{ padding: '6px 10px' }}
                >
                  <LogOut size={16} />
                </button>
              </>
            ) : (
              <>
                <button
                  onClick={() => onSelectTab('login')}
                  className="btn btn-navy btn-sm"
                  style={{ fontWeight: '700' }}
                >
                  <GraduationCap size={16} color="#f8b81f" />
                  Sign In
                </button>
              </>
            )}

            {/* Mobile Menu Trigger */}
            <button
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              style={{
                display: 'none',
                background: 'none',
                border: 'none',
                cursor: 'pointer',
                padding: '4px'
              }}
              className="mobile-menu-btn"
              aria-label="Toggle Navigation Menu"
            >
              {mobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
            </button>
          </div>
        </div>
      </div>

      {/* Mobile Drawer Menu */}
      {mobileMenuOpen && (
        <div style={{
          backgroundColor: '#ffffff',
          borderBottom: '1px solid var(--border-subtle)',
          padding: '16px 20px',
          display: 'flex',
          flexDirection: 'column',
          gap: '10px'
        }}>
          <button onClick={() => { onSelectTab('landing'); setMobileMenuOpen(false); }} className="btn btn-outline" style={{ justifyContent: 'flex-start' }}>Overview</button>
          <button onClick={() => { onSelectTab('how-it-works'); setMobileMenuOpen(false); }} className="btn btn-outline" style={{ justifyContent: 'flex-start' }}>How It Works</button>
          <button onClick={() => { onSelectTab('faq'); setMobileMenuOpen(false); }} className="btn btn-outline" style={{ justifyContent: 'flex-start' }}>Help & FAQ</button>
          {!isFullyAuthenticated && (
            <button onClick={() => { onSelectTab('login'); setMobileMenuOpen(false); }} className="btn btn-primary" style={{ marginTop: '8px' }}>Sign In</button>
          )}
        </div>
      )}

      <style>{`
        @media (max-width: 820px) {
          .desktop-nav { display: none !important; }
          .mobile-menu-btn { display: block !important; }
        }
      `}</style>
    </header>
  );
}
