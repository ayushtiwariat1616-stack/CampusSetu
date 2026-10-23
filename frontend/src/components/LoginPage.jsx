import { useState } from 'react';
import {
  User,
  Shield,
  AlertCircle,
  CheckCircle,
  Lock,
  Building,
  GraduationCap,
  Server,
  UserPlus,
  KeyRound,
  Eye,
  EyeOff
} from 'lucide-react';
import { api } from '../api';

export default function LoginPage({ currentUser, onLoginSuccess, onLogout }) {
  const [tab, setTab] = useState(currentUser?.role === 'OPERATOR' ? 'operator' : 'password'); // 'password', 'google', 'operator', 'register'
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [operatorKeyInput, setOperatorKeyInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  // Register Form State
  const [regFullName, setRegFullName] = useState('');
  const [regEmail, setRegEmail] = useState('');
  const [regPassword, setRegPassword] = useState('');
  const [regDepartment, setRegDepartment] = useState('AIML');
  const [regHostel, setRegHostel] = useState('ARYABHATT');
  const [regGender, setRegGender] = useState('MALE');
  const [regRoom, setRegRoom] = useState('');
  const [regRoll, setRegRoll] = useState('');

  // When operator is authenticated but step 2 key is not yet verified
  const needsKey = currentUser && (currentUser.role === 'OPERATOR' || currentUser.role === 'SYSTEM_ADMIN') && !currentUser.operatorKeyVerified;

  const handleDirectLogin = async (e) => {
    e.preventDefault();
    if (!email.trim() || !password) {
      setError('Please provide your institutional email and password.');
      return;
    }

    setLoading(true);
    setError('');
    try {
      const res = await api.directLogin(email.trim(), password);
      if (res?.data) {
        if ((res.data.role === 'OPERATOR' || res.data.role === 'SYSTEM_ADMIN') && !res.data.operatorKeyVerified) {
          onLoginSuccess(res.data);
          setTab('operator');
        } else {
          setSuccessMsg('Authentication successful. Redirecting to workspace...');
          setTimeout(() => onLoginSuccess(res.data), 400);
        }
      }
    } catch (err) {
      setError(err.message || 'Login failed. Please verify credentials.');
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    if (!regFullName.trim() || !regEmail.trim() || !regPassword) {
      setError('Full name, institutional email, and password are required.');
      return;
    }

    setLoading(true);
    setError('');
    try {
      const res = await api.registerStudent({
        fullName: regFullName.trim(),
        email: regEmail.trim(),
        password: regPassword,
        departmentCode: regDepartment,
        hostelCode: regHostel,
        roomNumber: regRoom.trim(),
        studentRollNumber: regRoll.trim(),
        gender: regGender,
      });
      if (res?.data) {
        setSuccessMsg('Account registered successfully! Welcome to CampusSetu.');
        setTimeout(() => onLoginSuccess(res.data), 500);
      }
    } catch (err) {
      setError(err.message || 'Registration failed.');
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyKey = async (e) => {
    if (e) e.preventDefault();
    if (!operatorKeyInput.trim()) {
      setError('Please enter your individual operator key.');
      return;
    }

    setLoading(true);
    setError('');
    try {
      const res = await api.verifyOperatorKey(operatorKeyInput.trim());
      setSuccessMsg('Operator cryptographic key verified. Access granted.');
      setTimeout(() => onLoginSuccess(res.data), 400);
    } catch (err) {
      const errMsg = err.message || 'Operator verification failed.';
      if (errMsg.toLowerCase().includes('session expired') || errMsg.toLowerCase().includes('not authenticated') || errMsg.toLowerCase().includes('access denied')) {
        setError('Session expired. Please sign in again with your official email and password.');
        if (onLogout) onLogout();
      } else {
        setError(errMsg);
      }
    } finally {
      setLoading(false);
    }
  };

  const fillCredentials = (userEmail, pass = 'CampusSetu@2026', key = '') => {
    if (onLogout && needsKey) {
      onLogout();
    }
    setEmail(userEmail);
    setPassword(pass);
    if (key) {
      setOperatorKeyInput(key);
      setTab('operator');
    } else {
      setTab('password');
    }
    setError('');
  };

  return (
    <div className="animate-fade-in" style={{ padding: '40px 0 60px' }}>
      <div className="container" style={{ maxWidth: '580px' }}>
        <div className="card" style={{ boxShadow: 'var(--shadow-card)', padding: '32px' }}>
          {/* Header */}
          <div style={{ textAlign: 'center', marginBottom: '24px' }}>
            <div style={{
              width: '52px',
              height: '52px',
              borderRadius: '14px',
              background: 'linear-gradient(135deg, #0f2942 0%, #0d9488 100%)',
              color: '#ffffff',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              margin: '0 auto 12px',
              fontSize: '1.5rem',
              fontWeight: '800',
              boxShadow: '0 4px 12px rgba(13, 148, 136, 0.25)'
            }}>
              CS
            </div>
            <h1 style={{ fontSize: '1.6rem', fontWeight: '800', color: 'var(--primary-navy)', letterSpacing: '-0.02em' }}>
              Sign In to CampusSetu
            </h1>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem', marginTop: '4px' }}>
              KIET Group of Institutions • Grievance & Academic Services
            </p>
          </div>

          {/* Navigation Tabs */}
          {!needsKey && (
            <div style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(4, 1fr)',
              gap: '4px',
              backgroundColor: 'var(--bg-subtle)',
              padding: '4px',
              borderRadius: 'var(--radius-md)',
              marginBottom: '22px'
            }}>
              <button
                type="button"
                onClick={() => { setTab('password'); setError(''); }}
                style={{
                  padding: '8px 4px',
                  borderRadius: '6px',
                  border: 'none',
                  backgroundColor: tab === 'password' ? '#ffffff' : 'transparent',
                  color: tab === 'password' ? 'var(--primary-navy)' : 'var(--text-muted)',
                  fontWeight: tab === 'password' ? '700' : '600',
                  boxShadow: tab === 'password' ? 'var(--shadow-sm)' : 'none',
                  cursor: 'pointer',
                  fontSize: '0.78rem',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '4px'
                }}
              >
                <Lock size={13} />
                Portal Sign-In
              </button>
              <button
                type="button"
                onClick={() => { setTab('google'); setError(''); }}
                style={{
                  padding: '8px 4px',
                  borderRadius: '6px',
                  border: 'none',
                  backgroundColor: tab === 'google' ? '#ffffff' : 'transparent',
                  color: tab === 'google' ? 'var(--primary-navy)' : 'var(--text-muted)',
                  fontWeight: tab === 'google' ? '700' : '600',
                  boxShadow: tab === 'google' ? 'var(--shadow-sm)' : 'none',
                  cursor: 'pointer',
                  fontSize: '0.78rem',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '4px'
                }}
              >
                <svg width="13" height="13" viewBox="0 0 24 24">
                  <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" />
                  <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" />
                  <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z" />
                  <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z" />
                </svg>
                Google SSO
              </button>
              <button
                type="button"
                onClick={() => { setTab('operator'); setError(''); }}
                style={{
                  padding: '8px 4px',
                  borderRadius: '6px',
                  border: 'none',
                  backgroundColor: tab === 'operator' ? '#ffffff' : 'transparent',
                  color: tab === 'operator' ? 'var(--primary-navy)' : 'var(--text-muted)',
                  fontWeight: tab === 'operator' ? '700' : '600',
                  boxShadow: tab === 'operator' ? 'var(--shadow-sm)' : 'none',
                  cursor: 'pointer',
                  fontSize: '0.78rem',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '4px'
                }}
              >
                <Shield size={13} />
                Operator 2FA
              </button>
              <button
                type="button"
                onClick={() => { setTab('register'); setError(''); }}
                style={{
                  padding: '8px 4px',
                  borderRadius: '6px',
                  border: 'none',
                  backgroundColor: tab === 'register' ? '#ffffff' : 'transparent',
                  color: tab === 'register' ? 'var(--primary-navy)' : 'var(--text-muted)',
                  fontWeight: tab === 'register' ? '700' : '600',
                  boxShadow: tab === 'register' ? 'var(--shadow-sm)' : 'none',
                  cursor: 'pointer',
                  fontSize: '0.78rem',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '4px'
                }}
              >
                <UserPlus size={13} />
                Register
              </button>
            </div>
          )}

          {error && (
            <div style={{
              backgroundColor: '#fee2e2',
              border: '1px solid #fecaca',
              color: '#dc2626',
              padding: '10px 14px',
              borderRadius: 'var(--radius-md)',
              fontSize: '0.8125rem',
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              marginBottom: '16px'
            }}>
              <AlertCircle size={16} />
              {error}
            </div>
          )}

          {successMsg && (
            <div style={{
              backgroundColor: '#d1fae5',
              border: '1px solid #a7f3d0',
              color: '#059669',
              padding: '10px 14px',
              borderRadius: 'var(--radius-md)',
              fontSize: '0.8125rem',
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              marginBottom: '16px'
            }}>
              <CheckCircle size={16} />
              {successMsg}
            </div>
          )}

          {/* STEP 2: OPERATOR KEY PROMPT */}
          {needsKey ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div style={{
                backgroundColor: 'var(--accent-teal-subtle)',
                border: '1px solid #99f6e4',
                padding: '14px',
                borderRadius: 'var(--radius-md)',
                fontSize: '0.8125rem'
              }}>
                <div style={{ fontWeight: '700', color: 'var(--accent-teal)', marginBottom: '4px' }}>
                  Two-Factor Authentication: Operator Key Required
                </div>
                <div style={{ color: 'var(--text-secondary)' }}>
                  Authenticated as <strong>{currentUser.email}</strong>.
                  Enter your individual cryptographic key to unlock operational queues.
                </div>
              </div>

              <form onSubmit={handleVerifyKey} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
                <div className="form-group" style={{ margin: 0 }}>
                  <label className="form-label">Cryptographic Operator Key</label>
                  <input
                    type="password"
                    className="form-input"
                    placeholder="OPK-XXXX-XXXX-XXXX"
                    value={operatorKeyInput}
                    onChange={(e) => setOperatorKeyInput(e.target.value)}
                    autoFocus
                  />
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '4px' }}>
                    Authorized under institutional roster assignment.
                  </div>
                </div>

                <button
                  type="submit"
                  disabled={loading}
                  className="btn btn-primary"
                  style={{ width: '100%', padding: '12px' }}
                >
                  <KeyRound size={16} />
                  {loading ? 'Verifying Key...' : 'Verify Key & Open Queue'}
                </button>

                <button
                  type="button"
                  onClick={() => {
                    setError('');
                    if (onLogout) onLogout();
                  }}
                  className="btn btn-outline"
                  style={{ width: '100%', padding: '10px', fontSize: '0.8125rem' }}
                >
                  ← Sign in with a different account
                </button>
              </form>
            </div>
          ) : tab === 'password' ? (
            /* TAB 1: DIRECT INSTITUTIONAL SIGN IN */
            <form onSubmit={handleDirectLogin} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div className="form-group" style={{ margin: 0 }}>
                <label className="form-label">Official College Email</label>
                <input
                  type="email"
                  className="form-input"
                  placeholder="rollno.student@kiet.edu or staff@kiet.edu"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                  autoFocus
                />
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '4px' }}>
                  Must be your registered <code>@kiet.edu</code> address.
                </div>
              </div>

              <div className="form-group" style={{ margin: 0 }}>
                <label className="form-label" style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span>Password</span>
                  <span
                    onClick={() => setShowPassword(!showPassword)}
                    style={{ cursor: 'pointer', color: 'var(--accent-teal)', fontSize: '0.75rem', display: 'flex', alignItems: 'center', gap: '4px' }}
                  >
                    {showPassword ? <EyeOff size={13} /> : <Eye size={13} />}
                    {showPassword ? 'Hide' : 'Show'}
                  </span>
                </label>
                <input
                  type={showPassword ? 'text' : 'password'}
                  className="form-input"
                  placeholder="Enter your password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
              </div>

              <button
                type="submit"
                disabled={loading}
                className="btn btn-primary"
                style={{ width: '100%', padding: '12px' }}
              >
                <Lock size={16} />
                {loading ? 'Authenticating...' : 'Sign In to CampusSetu'}
              </button>
            </form>
          ) : tab === 'google' ? (
            /* TAB 2: GOOGLE WORKSPACE SSO */
            <div style={{ display: 'flex', flexDirection: 'column', gap: '18px' }}>
              <div style={{
                border: '1px solid var(--border-subtle)',
                padding: '16px',
                borderRadius: 'var(--radius-md)',
                backgroundColor: 'var(--bg-subtle)',
                fontSize: '0.8125rem',
                color: 'var(--text-secondary)'
              }}>
                <div style={{ fontWeight: '700', color: 'var(--primary-navy)', marginBottom: '4px' }}>
                  Institutional Google OIDC Authentication
                </div>
                <div>
                  Sign in through your college-managed Google account. Domain boundary checking ensures only <code>@kiet.edu</code> accounts can access student data.
                </div>
              </div>

              <a
                href="/oauth2/authorization/google"
                className="btn btn-outline"
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '10px',
                  padding: '12px',
                  fontWeight: '700',
                  color: 'var(--primary-navy)',
                  textDecoration: 'none'
                }}
              >
                <svg width="18" height="18" viewBox="0 0 24 24">
                  <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" />
                  <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" />
                  <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z" />
                  <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z" />
                </svg>
                Continue with Google (@kiet.edu)
              </a>
            </div>
          ) : tab === 'operator' ? (
            /* TAB 3: OPERATOR DESK */
            <form onSubmit={handleDirectLogin} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div style={{
                border: '1px solid var(--border-subtle)',
                padding: '14px',
                borderRadius: 'var(--radius-md)',
                backgroundColor: 'var(--bg-subtle)',
                fontSize: '0.8125rem'
              }}>
                <div style={{ fontWeight: '700', color: 'var(--primary-navy)', marginBottom: '4px' }}>
                  Operator Desk (Step 1: Credential Verification)
                </div>
                <div style={{ color: 'var(--text-secondary)' }}>
                  Hostel Wardens, Rectors, Department Incharges, and System Administrators log in here.
                </div>
              </div>

              <div className="form-group" style={{ margin: 0 }}>
                <label className="form-label">Staff / Operator Email</label>
                <input
                  type="email"
                  className="form-input"
                  placeholder="warden.hostel@kiet.edu"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
              </div>

              <div className="form-group" style={{ margin: 0 }}>
                <label className="form-label">Staff Password</label>
                <input
                  type="password"
                  className="form-input"
                  placeholder="Enter staff password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
              </div>

              <button
                type="submit"
                disabled={loading}
                className="btn btn-navy"
                style={{ width: '100%', padding: '12px' }}
              >
                <Shield size={16} />
                {loading ? 'Authenticating...' : 'Sign In as Operator'}
              </button>
            </form>
          ) : (
            /* TAB 4: STUDENT REGISTRATION */
            <form onSubmit={handleRegister} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
              <div style={{
                backgroundColor: 'var(--accent-teal-subtle)',
                border: '1px solid #99f6e4',
                padding: '12px',
                borderRadius: 'var(--radius-md)',
                fontSize: '0.8125rem',
                color: 'var(--text-secondary)'
              }}>
                New student self-onboarding. Branches and hostels are auto-linked to KIET complaint queues.
              </div>

              <div className="form-group" style={{ margin: 0 }}>
                <label className="form-label">Full Name</label>
                <input
                  type="text"
                  className="form-input"
                  placeholder="e.g. Ayush Sharma"
                  value={regFullName}
                  onChange={(e) => setRegFullName(e.target.value)}
                  required
                />
              </div>

              <div className="form-group" style={{ margin: 0 }}>
                <label className="form-label">Institutional Email</label>
                <input
                  type="email"
                  className="form-input"
                  placeholder="rollno.student@kiet.edu"
                  value={regEmail}
                  onChange={(e) => setRegEmail(e.target.value)}
                  required
                />
              </div>

              <div className="form-group" style={{ margin: 0 }}>
                <label className="form-label">Password (min. 6 characters)</label>
                <input
                  type="password"
                  className="form-input"
                  placeholder="Create password"
                  value={regPassword}
                  onChange={(e) => setRegPassword(e.target.value)}
                  required
                />
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
                <div className="form-group" style={{ margin: 0 }}>
                  <label className="form-label">Department / Branch</label>
                  <select
                    className="form-select"
                    value={regDepartment}
                    onChange={(e) => setRegDepartment(e.target.value)}
                  >
                    <option value="AIML">AIML</option>
                    <option value="CSE">CSE</option>
                    <option value="IT">IT</option>
                    <option value="ECE">ECE</option>
                    <option value="EN">EN</option>
                    <option value="ME">ME</option>
                    <option value="CE">CE</option>
                    <option value="MCA">MCA</option>
                  </select>
                </div>
                <div className="form-group" style={{ margin: 0 }}>
                  <label className="form-label">Hostel Residence</label>
                  <select
                    className="form-select"
                    value={regHostel}
                    onChange={(e) => {
                      const h = e.target.value;
                      setRegHostel(h);
                      if (['GARGI', 'SAROJINI', 'SARASWATI'].includes(h)) {
                        setRegGender('FEMALE');
                      } else if (['CV_RAMAN', 'CHANDRAGUPT', 'TAGORE', 'ARYABHATT', 'VIVEKANAND', 'CHANAKYA'].includes(h)) {
                        setRegGender('MALE');
                      }
                    }}
                  >
                    <option value="CV_RAMAN">C.V. Raman Hostel (1st Year Boys)</option>
                    <option value="CHANDRAGUPT">Chandragupt Hostel (1st Year Boys)</option>
                    <option value="TAGORE">Tagore Hostel (2nd Year Boys)</option>
                    <option value="ARYABHATT">Aryabhatt Hostel (2nd & 3rd Year Boys)</option>
                    <option value="VIVEKANAND">Vivekanand Hostel (2nd & 3rd Year Boys)</option>
                    <option value="CHANAKYA">Chanakya Hostel (4th Year Boys)</option>
                    <option value="GARGI">Gargi Hostel (1st Year Girls)</option>
                    <option value="SAROJINI">Sarojini Hostel (2nd & 3rd Year Girls)</option>
                    <option value="SARASWATI">Saraswati Hostel (2nd, 3rd & 4th Year Girls)</option>
                    <option value="DAY_SCHOLAR">Day Scholar (Transit / Day Boarder)</option>
                  </select>
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '10px' }}>
                <div className="form-group" style={{ margin: 0 }}>
                  <label className="form-label">Gender *</label>
                  <select
                    className="form-select"
                    value={regGender}
                    onChange={(e) => setRegGender(e.target.value)}
                  >
                    <option value="MALE">Male</option>
                    <option value="FEMALE">Female (SOS Enabled)</option>
                  </select>
                </div>
                <div className="form-group" style={{ margin: 0 }}>
                  <label className="form-label">Room Number</label>
                  <input
                    type="text"
                    className="form-input"
                    placeholder="e.g. 105 or 204"
                    value={regRoom}
                    onChange={(e) => setRegRoom(e.target.value)}
                  />
                </div>
                <div className="form-group" style={{ margin: 0 }}>
                  <label className="form-label">Roll Number</label>
                  <input
                    type="text"
                    className="form-input"
                    placeholder="e.g. 25002324"
                    value={regRoll}
                    onChange={(e) => setRegRoll(e.target.value)}
                  />
                </div>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="btn btn-primary"
                style={{ width: '100%', padding: '12px', marginTop: '4px' }}
              >
                <UserPlus size={16} />
                {loading ? 'Creating Account...' : 'Complete Student Registration'}
              </button>
            </form>
          )}

          {/* Quick Access Campus Directory Accounts */}
          <div style={{
            marginTop: '28px',
            borderTop: '1px solid var(--border-subtle)',
            paddingTop: '20px'
          }}>
            <div style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              marginBottom: '12px'
            }}>
              <div style={{ fontSize: '0.8125rem', fontWeight: '800', color: 'var(--primary-navy)', letterSpacing: '0.01em', display: 'flex', alignItems: 'center', gap: '6px' }}>
                <span className="pulse-dot" style={{ backgroundColor: '#10b981' }}></span>
                Campus Accounts Fast-Access (One-Click Fill)
              </div>
              <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>
                Default: <code>CampusSetu@2026</code>
              </span>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '8px' }}>
              {/* Srijan Shrivastava */}
              <button
                type="button"
                onClick={() => fillCredentials('srijan.25002324@kiet.edu')}
                className="btn btn-outline"
                style={{
                  justifyContent: 'flex-start',
                  textAlign: 'left',
                  padding: '9px 12px',
                  borderRadius: 'var(--radius-md)',
                  fontSize: '0.78rem',
                  border: '1px solid #cbd5e1'
                }}
              >
                <div style={{ display: 'flex', flexDirection: 'column', gap: '2px', width: '100%' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span style={{ fontWeight: '800', color: 'var(--primary-navy)' }}>Srijan Shrivastava</span>
                    <span className="badge badge-submitted" style={{ fontSize: '0.65rem', padding: '1px 6px' }}>Male Student</span>
                  </div>
                  <span style={{ color: 'var(--text-muted)', fontSize: '0.72rem' }}>
                    Aryabhatt 105 • CSE (2nd/3rd Yr Boys)
                  </span>
                </div>
              </button>

              {/* Ananya Singh */}
              <button
                type="button"
                onClick={() => fillCredentials('ananya.25001142@kiet.edu')}
                className="btn btn-outline"
                style={{
                  justifyContent: 'flex-start',
                  textAlign: 'left',
                  padding: '9px 12px',
                  borderRadius: 'var(--radius-md)',
                  fontSize: '0.78rem',
                  border: '1px solid #fca5a5',
                  backgroundColor: '#fffdfd'
                }}
              >
                <div style={{ display: 'flex', flexDirection: 'column', gap: '2px', width: '100%' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span style={{ fontWeight: '800', color: '#991b1b' }}>Ananya Singh</span>
                    <span className="badge" style={{ fontSize: '0.65rem', padding: '1px 6px', backgroundColor: '#fee2e2', color: '#dc2626', fontWeight: '800' }}>Female • SOS Active</span>
                  </div>
                  <span style={{ color: '#b91c1c', fontSize: '0.72rem' }}>
                    Sarojini 204 • IT (2nd/3rd Yr Girls)
                  </span>
                </div>
              </button>

              {/* Ayush Sharma */}
              <button
                type="button"
                onClick={() => fillCredentials('ayush.25004108@kiet.edu')}
                className="btn btn-outline"
                style={{
                  justifyContent: 'flex-start',
                  textAlign: 'left',
                  padding: '9px 12px',
                  borderRadius: 'var(--radius-md)',
                  fontSize: '0.78rem',
                  border: '1px solid #cbd5e1'
                }}
              >
                <div style={{ display: 'flex', flexDirection: 'column', gap: '2px', width: '100%' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span style={{ fontWeight: '800', color: 'var(--primary-navy)' }}>Ayush Sharma</span>
                    <span className="badge badge-submitted" style={{ fontSize: '0.65rem', padding: '1px 6px' }}>Male Student</span>
                  </div>
                  <span style={{ color: 'var(--text-muted)', fontSize: '0.72rem' }}>
                    Aryabhatt 214 • AIML (2nd/3rd Yr Boys)
                  </span>
                </div>
              </button>

              {/* Warden Aryabhatt */}
              <button
                type="button"
                onClick={() => fillCredentials('warden.aryabhatt@kiet.edu', 'CampusSetu@2026', 'OPK-ARYA-WARD-2026')}
                className="btn btn-outline"
                style={{
                  justifyContent: 'flex-start',
                  textAlign: 'left',
                  padding: '9px 12px',
                  borderRadius: 'var(--radius-md)',
                  fontSize: '0.78rem',
                  border: '1px solid #cbd5e1'
                }}
              >
                <div style={{ display: 'flex', flexDirection: 'column', gap: '2px', width: '100%' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span style={{ fontWeight: '800', color: 'var(--primary-navy)' }}>Warden Aryabhatt</span>
                    <span className="badge badge-acknowledged" style={{ fontSize: '0.65rem', padding: '1px 6px' }}>Operator</span>
                  </div>
                  <span style={{ color: 'var(--text-muted)', fontSize: '0.72rem' }}>
                    Key: OPK-ARYA-WARD-2026
                  </span>
                </div>
              </button>

              {/* Warden Tagore */}
              <button
                type="button"
                onClick={() => fillCredentials('warden.tagore@kiet.edu', 'CampusSetu@2026', 'OPK-TAGO-WARD-2026')}
                className="btn btn-outline"
                style={{
                  justifyContent: 'flex-start',
                  textAlign: 'left',
                  padding: '9px 12px',
                  borderRadius: 'var(--radius-md)',
                  fontSize: '0.78rem',
                  border: '1px solid #cbd5e1'
                }}
              >
                <div style={{ display: 'flex', flexDirection: 'column', gap: '2px', width: '100%' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span style={{ fontWeight: '800', color: 'var(--primary-navy)' }}>Warden Tagore</span>
                    <span className="badge badge-acknowledged" style={{ fontSize: '0.65rem', padding: '1px 6px' }}>Operator</span>
                  </div>
                  <span style={{ color: 'var(--text-muted)', fontSize: '0.72rem' }}>
                    Tagore (2nd Yr Boys) • Key: OPK-TAGO...
                  </span>
                </div>
              </button>

              {/* Warden Sarojini */}
              <button
                type="button"
                onClick={() => fillCredentials('warden.sarojini@kiet.edu', 'CampusSetu@2026', 'OPK-SARO-WARD-2026')}
                className="btn btn-outline"
                style={{
                  justifyContent: 'flex-start',
                  textAlign: 'left',
                  padding: '9px 12px',
                  borderRadius: 'var(--radius-md)',
                  fontSize: '0.78rem',
                  border: '1px solid #cbd5e1'
                }}
              >
                <div style={{ display: 'flex', flexDirection: 'column', gap: '2px', width: '100%' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span style={{ fontWeight: '800', color: 'var(--primary-navy)' }}>Warden Sarojini</span>
                    <span className="badge badge-acknowledged" style={{ fontSize: '0.65rem', padding: '1px 6px' }}>Operator</span>
                  </div>
                  <span style={{ color: 'var(--text-muted)', fontSize: '0.72rem' }}>
                    Sarojini (2nd/3rd Yr Girls) • Key: OPK-SARO...
                  </span>
                </div>
              </button>

              {/* Dean Student Welfare (Dean SW & ICC) */}
              <button
                type="button"
                onClick={() => fillCredentials('dean.sw@kiet.edu', 'CampusSetu@2026', 'OPK-DEAN-SW-2026')}
                className="btn btn-outline"
                style={{
                  justifyContent: 'flex-start',
                  textAlign: 'left',
                  padding: '9px 12px',
                  borderRadius: 'var(--radius-md)',
                  fontSize: '0.78rem',
                  border: '1px solid #fecaca',
                  backgroundColor: '#fff1f2'
                }}
              >
                <div style={{ display: 'flex', flexDirection: 'column', gap: '2px', width: '100%' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span style={{ fontWeight: '800', color: '#b91c1c' }}>🚨 Dean SW (Anti-Harassment Desk)</span>
                    <span className="badge" style={{ backgroundColor: '#dc2626', color: '#ffffff', fontSize: '0.65rem', padding: '1px 6px' }}>Executive</span>
                  </div>
                  <span style={{ color: '#991b1b', fontSize: '0.72rem' }}>
                    Dean Student Welfare & ICC • Key: OPK-DEAN-SW-2026
                  </span>
                </div>
              </button>

              {/* System Admin */}
              <button
                type="button"
                onClick={() => fillCredentials('admin.campussetu@kiet.edu', 'CampusSetu@2026', 'OPK-ADMIN-ROOT-2026')}
                className="btn btn-outline"
                style={{
                  justifyContent: 'flex-start',
                  textAlign: 'left',
                  padding: '9px 12px',
                  borderRadius: 'var(--radius-md)',
                  fontSize: '0.78rem',
                  border: '1px solid #cbd5e1',
                  gridColumn: '1 / -1'
                }}
              >
                <div style={{ display: 'flex', flexDirection: 'column', gap: '2px', width: '100%' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span style={{ fontWeight: '800', color: 'var(--kiet-crimson)' }}>System Administrator (Super User)</span>
                    <span className="badge badge-resolution_proposed" style={{ fontSize: '0.65rem', padding: '1px 6px' }}>Admin Root</span>
                  </div>
                  <span style={{ color: 'var(--text-muted)', fontSize: '0.72rem' }}>
                    Director General Grievance Cell • Key: OPK-ADMIN-ROOT-2026
                  </span>
                </div>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
