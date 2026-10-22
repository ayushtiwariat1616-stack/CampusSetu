import { useState, useEffect, useCallback } from 'react';
import {
  Users,
  FileSpreadsheet,
  Key,
  CheckCircle,
  Copy,
  Clock,
  UserCheck,
  Upload,
  Download,
  ShieldAlert
} from 'lucide-react';
import { api } from '../api';

export default function AdminDashboard({ currentUser }) {
  const [activeTab, setActiveTab] = useState('roster'); // 'roster', 'keys', 'pending', 'audit'
  const [operators, setOperators] = useState([]);
  const [pendingUsers, setPendingUsers] = useState([]);
  const [auditLogs, setAuditLogs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // CSV Import State
  const [csvFile, setCsvFile] = useState(null);
  const [previewData, setPreviewData] = useState(null);

  // Key Generation Result Modal
  const [generatedKeyResult, setGeneratedKeyResult] = useState(null);

  const loadAdminData = useCallback(async () => {
    try {
      if (activeTab === 'keys') {
        const res = await api.getOperatorsList();
        if (res?.data) setOperators(res.data);
      } else if (activeTab === 'pending') {
        const res = await api.getPendingUsers();
        if (res?.data) setPendingUsers(res.data);
      } else if (activeTab === 'audit') {
        const res = await api.getAuditLogs();
        if (res?.data?.content) setAuditLogs(res.data.content);
      }
    } catch (err) {
      setError(err.message);
    }
  }, [activeTab]);

  useEffect(() => {
    loadAdminData();
  }, [loadAdminData]);

  const handlePreviewCsv = async (e) => {
    e.preventDefault();
    if (!csvFile) return;
    setLoading(true);
    setError('');
    try {
      const data = new FormData();
      data.append('file', csvFile);
      const res = await api.previewRoster(data);
      if (res?.data) {
        setPreviewData(res.data);
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleApplyCsv = async () => {
    if (!csvFile) return;
    setLoading(true);
    setError('');
    try {
      const data = new FormData();
      data.append('file', csvFile);
      const res = await api.applyRoster(data);
      setSuccess(`Roster successfully synced: ${res.data.importedCount} rows imported, ${res.data.syncedUsers} active users updated.`);
      setPreviewData(null);
      setCsvFile(null);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleGenerateKey = async (userId) => {
    setLoading(true);
    setError('');
    try {
      const res = await api.generateOperatorKey(userId);
      if (res?.data) {
        setGeneratedKeyResult(res.data);
        setSuccess('New operator key generated and recorded in audit log.');
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleApprovePendingUser = async (userId) => {
    try {
      await api.approveUser(userId, {
        role: 'STUDENT',
        departmentCode: 'AIML',
        hostelCode: 'ARYABHATT',
        roomNumber: '101',
        studentRollNumber: '25009999'
      });
      setSuccess('User approved and onboarded.');
      loadAdminData();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleRunSlaCheck = async () => {
    setLoading(true);
    setError('');
    try {
      const res = await api.triggerSlaCheck();
      setSuccess(`SLA Sentinel Scan Complete: ${res.data.scannedCount} active complaints evaluated, ${res.data.escalatedCount} breached tickets escalated.`);
      loadAdminData();
    } catch (err) {
      setError(err.message || 'SLA scan failed');
    } finally {
      setLoading(false);
    }
  };

  const sampleCsvContent = `email,fullName,roleType,departmentCode,hostelCode,roomNumber,studentRollNumber,serviceScope
ayush.25004108@kiet.edu,Ayush Sharma,STUDENT,AIML,ARYABHATT,214,25004108,
warden.aryabhatt@kiet.edu,Hostel Warden Aryabhatt,HOSTEL_WARDEN,,ARYABHATT,,,
rector.aryabhatt@kiet.edu,Prof. Rector Aryabhatt,RECTOR,,ARYABHATT,,,
chiefrector@kiet.edu,Chief Rector Hostels,CHIEF_RECTOR,,,,ALL_HOSTELS
aiml.operator@kiet.edu,AIML Lab Incharge,DEPARTMENT_OPERATOR,AIML,,,,,
it.service@kiet.edu,Network Engineer,SERVICE_OPERATOR,,,,,IT_SERVICES
admin.campussetu@kiet.edu,System Admin,SYSTEM_ADMIN,,,,,CAMPUS_WIDE`;

  const downloadSampleCsv = () => {
    const blob = new Blob([sampleCsvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', 'kiet_roster_sample.csv');
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <div className="animate-fade-in" style={{ padding: '24px 0 60px' }}>
      <div className="container">
        {/* Admin Header */}
        <div className="card" style={{ marginBottom: '20px', padding: '20px 24px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <h1 style={{ fontSize: '1.35rem', fontWeight: '800', color: 'var(--primary-navy)' }}>
                System Administration & Governance
              </h1>
              <span className="badge badge-acknowledged">Directorate Privileges</span>
            </div>
            <div style={{ fontSize: '0.8125rem', color: 'var(--text-secondary)', marginTop: '4px' }}>
              Admin Account: <strong>{currentUser.email}</strong> • Managing Institutional Roster, Operator Keys & Audit Logs
            </div>
          </div>

          <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
            <button
              onClick={() => setActiveTab('roster')}
              className={`btn btn-sm ${activeTab === 'roster' ? 'btn-navy' : 'btn-outline'}`}
            >
              <FileSpreadsheet size={14} />
              Roster Import
            </button>
            <button
              onClick={() => setActiveTab('keys')}
              className={`btn btn-sm ${activeTab === 'keys' ? 'btn-navy' : 'btn-outline'}`}
            >
              <Key size={14} />
              Operator Keys
            </button>
            <button
              onClick={() => setActiveTab('pending')}
              className={`btn btn-sm ${activeTab === 'pending' ? 'btn-navy' : 'btn-outline'}`}
            >
              <Users size={14} />
              Pending Onboarding
            </button>
            <button
              onClick={() => setActiveTab('audit')}
              className={`btn btn-sm ${activeTab === 'audit' ? 'btn-navy' : 'btn-outline'}`}
            >
              <Clock size={14} />
              Audit Logs
            </button>
            <button
              onClick={handleRunSlaCheck}
              disabled={loading}
              className="btn btn-sm btn-primary"
              style={{ backgroundColor: '#0d9488' }}
              title="Inspect active tickets and automatically escalate SLA breaches"
            >
              <ShieldAlert size={14} />
              {loading ? 'Evaluating SLA...' : 'Auto-Escalate SLA'}
            </button>
          </div>
        </div>

        {/* Global Messages */}
        {error && (
          <div style={{ backgroundColor: '#fee2e2', color: '#dc2626', padding: '12px', borderRadius: '8px', marginBottom: '16px' }}>
            {error}
          </div>
        )}
        {success && (
          <div style={{ backgroundColor: '#d1fae5', color: '#059669', padding: '12px', borderRadius: '8px', marginBottom: '16px' }}>
            {success}
          </div>
        )}

        {/* MODAL: ONE-TIME DISPLAY OF GENERATED OPERATOR KEY */}
        {generatedKeyResult && (
          <div style={{
            position: 'fixed',
            inset: 0,
            backgroundColor: 'rgba(15, 23, 42, 0.75)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 1000,
            padding: '20px'
          }}>
            <div className="card" style={{ maxWidth: '480px', width: '100%', padding: '28px', backgroundColor: '#ffffff' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#059669', fontWeight: '800', marginBottom: '8px' }}>
                <CheckCircle size={24} />
                New Operator Key Generated
              </div>
              <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', marginBottom: '16px' }}>
                Copy this cryptographic key now and transmit it securely to the operator. It is hashed on the server and will <strong>never be displayed again</strong>.
              </p>

              <div style={{
                backgroundColor: 'var(--bg-subtle)',
                border: '2px dashed var(--accent-teal)',
                padding: '14px',
                borderRadius: '8px',
                textAlign: 'center',
                fontFamily: 'monospace',
                fontSize: '1.15rem',
                fontWeight: '800',
                color: 'var(--primary-navy)',
                letterSpacing: '0.05em',
                marginBottom: '14px',
                userSelect: 'all'
              }}>
                {generatedKeyResult.rawKey}
              </div>

              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginBottom: '20px' }}>
                Key Prefix: <code>{generatedKeyResult.prefix}</code> • Valid for 90 days.
              </div>

              <div style={{ display: 'flex', gap: '8px', justifyContent: 'flex-end' }}>
                <button
                  onClick={() => {
                    navigator.clipboard.writeText(generatedKeyResult.rawKey);
                    alert('Key copied to clipboard!');
                  }}
                  className="btn btn-outline"
                >
                  <Copy size={16} />
                  Copy Key
                </button>
                <button
                  onClick={() => setGeneratedKeyResult(null)}
                  className="btn btn-primary"
                >
                  I Have Recorded This Key
                </button>
              </div>
            </div>
          </div>
        )}

        {/* TAB 1: ROSTER CSV IMPORT */}
        {activeTab === 'roster' && (
          <div style={{ maxWidth: '840px', margin: '0 auto' }}>
            <div className="card" style={{ marginBottom: '20px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
                <div>
                  <h3 style={{ fontSize: '1.15rem', fontWeight: '800', color: 'var(--primary-navy)' }}>
                    Institutional Roster Import & Sync
                  </h3>
                  <p style={{ fontSize: '0.8125rem', color: 'var(--text-muted)' }}>
                    Upload official CSV mapping student rolls, rooms, hostels, and staff role assignments.
                  </p>
                </div>
                <button onClick={downloadSampleCsv} className="btn btn-outline btn-sm">
                  <Download size={14} />
                  Sample CSV Template
                </button>
              </div>

              <form onSubmit={handlePreviewCsv} style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                <input
                  type="file"
                  accept=".csv,text/csv"
                  className="form-input"
                  style={{ flex: 1 }}
                  onChange={(e) => setCsvFile(e.target.files[0])}
                />
                <button type="submit" disabled={!csvFile || loading} className="btn btn-primary">
                  <Upload size={14} />
                  Inspect & Preview
                </button>
              </form>
            </div>

            {/* Preview Results Table */}
            {previewData && (
              <div className="card">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px' }}>
                  <div style={{ display: 'flex', gap: '12px' }}>
                    <span className="badge badge-acknowledged">Total: {previewData.totalRows}</span>
                    <span className="badge badge-closed">Valid: {previewData.validCount}</span>
                    {previewData.invalidCount > 0 && <span className="badge badge-reopened">Invalid: {previewData.invalidCount}</span>}
                  </div>
                  <button
                    onClick={handleApplyCsv}
                    disabled={previewData.validCount === 0 || loading}
                    className="btn btn-primary"
                  >
                    Commit & Sync {previewData.validCount} Roster Records
                  </button>
                </div>

                <div style={{ maxHeight: '300px', overflowY: 'auto' }}>
                  <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.8125rem' }}>
                    <thead>
                      <tr style={{ textAlign: 'left', borderBottom: '2px solid var(--border-subtle)', background: 'var(--bg-subtle)' }}>
                        <th style={{ padding: '8px' }}>Line</th>
                        <th style={{ padding: '8px' }}>Email</th>
                        <th style={{ padding: '8px' }}>Full Name</th>
                        <th style={{ padding: '8px' }}>Role</th>
                        <th style={{ padding: '8px' }}>Hostel / Dept</th>
                        <th style={{ padding: '8px' }}>Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      {previewData.rows?.map((r, i) => (
                        <tr key={i} style={{ borderBottom: '1px solid var(--border-subtle)' }}>
                          <td style={{ padding: '8px' }}>{r.lineNumber}</td>
                          <td style={{ padding: '8px' }}><code>{r.entry?.email || 'N/A'}</code></td>
                          <td style={{ padding: '8px' }}>{r.entry?.fullName || 'N/A'}</td>
                          <td style={{ padding: '8px' }}>{r.entry?.roleType || 'N/A'}</td>
                          <td style={{ padding: '8px' }}>{r.entry?.hostelCode || r.entry?.departmentCode || '-'}</td>
                          <td style={{ padding: '8px' }}>
                            {r.valid ? (
                              <span style={{ color: '#059669', fontWeight: '700' }}>✓ Valid</span>
                            ) : (
                              <span style={{ color: '#dc2626', fontWeight: '600' }}>✗ {r.message}</span>
                            )}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}
          </div>
        )}

        {/* TAB 2: OPERATOR KEYS MANAGEMENT */}
        {activeTab === 'keys' && (
          <div className="card">
            <h3 style={{ fontSize: '1.15rem', fontWeight: '800', color: 'var(--primary-navy)', marginBottom: '4px' }}>
              Individual Operator Key Management
            </h3>
            <p style={{ fontSize: '0.8125rem', color: 'var(--text-muted)', marginBottom: '16px' }}>
              Rotate or issue individual random keys. Keys are hashed with BCrypt.
            </p>

            <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.8125rem' }}>
              <thead>
                <tr style={{ textAlign: 'left', borderBottom: '2px solid var(--border-subtle)', background: 'var(--bg-subtle)' }}>
                  <th style={{ padding: '10px' }}>Name & Email</th>
                  <th style={{ padding: '10px' }}>Role</th>
                  <th style={{ padding: '10px' }}>Assigned Domain</th>
                  <th style={{ padding: '10px' }}>Action</th>
                </tr>
              </thead>
              <tbody>
                {operators.map((op) => (
                  <tr key={op.id} style={{ borderBottom: '1px solid var(--border-subtle)' }}>
                    <td style={{ padding: '10px' }}>
                      <div style={{ fontWeight: '700' }}>{op.fullName}</div>
                      <div style={{ color: 'var(--text-muted)' }}>{op.email}</div>
                    </td>
                    <td style={{ padding: '10px' }}>
                      <span className="badge badge-acknowledged">{op.primaryRole}</span>
                    </td>
                    <td style={{ padding: '10px' }}>
                      {op.hostelCode ? `Hostel: ${op.hostelCode}` : (op.departmentCode ? `Dept: ${op.departmentCode}` : 'Campus-Wide')}
                    </td>
                    <td style={{ padding: '10px' }}>
                      <button
                        onClick={() => handleGenerateKey(op.id)}
                        className="btn btn-outline btn-sm"
                      >
                        <Key size={14} />
                        Rotate / Generate Key
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {/* TAB 3: PENDING USERS ONBOARDING */}
        {activeTab === 'pending' && (
          <div className="card">
            <h3 style={{ fontSize: '1.15rem', fontWeight: '800', color: 'var(--primary-navy)', marginBottom: '4px' }}>
              Pending Institutional Verification
            </h3>
            <p style={{ fontSize: '0.8125rem', color: 'var(--text-muted)', marginBottom: '16px' }}>
              Self-registered Google accounts that do not match pre-approved roster entries. Require manual approval.
            </p>

            {pendingUsers.length === 0 ? (
              <div style={{ textAlign: 'center', padding: '30px', color: 'var(--text-muted)' }}>
                No pending onboarding requests.
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                {pendingUsers.map((u) => (
                  <div key={u.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '12px', border: '1px solid var(--border-subtle)', borderRadius: '8px' }}>
                    <div>
                      <div style={{ fontWeight: '700', color: 'var(--primary-navy)' }}>{u.fullName}</div>
                      <div style={{ fontSize: '0.8125rem', color: 'var(--text-muted)' }}>{u.email}</div>
                    </div>
                    <button
                      onClick={() => handleApprovePendingUser(u.id)}
                      className="btn btn-primary btn-sm"
                    >
                      <UserCheck size={14} />
                      Approve as Student (AIML R214)
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* TAB 4: AUDIT LOGS */}
        {activeTab === 'audit' && (
          <div className="card">
            <h3 style={{ fontSize: '1.15rem', fontWeight: '800', color: 'var(--primary-navy)', marginBottom: '16px' }}>
              Immutable Regulatory Audit Trail
            </h3>

            <div style={{ maxHeight: '400px', overflowY: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.8125rem' }}>
                <thead>
                  <tr style={{ textAlign: 'left', borderBottom: '2px solid var(--border-subtle)', background: 'var(--bg-subtle)' }}>
                    <th style={{ padding: '8px' }}>Timestamp</th>
                    <th style={{ padding: '8px' }}>Category</th>
                    <th style={{ padding: '8px' }}>Actor</th>
                    <th style={{ padding: '8px' }}>Action</th>
                    <th style={{ padding: '8px' }}>Details</th>
                  </tr>
                </thead>
                <tbody>
                  {auditLogs.map((log) => (
                    <tr key={log.id} style={{ borderBottom: '1px solid var(--border-subtle)' }}>
                      <td style={{ padding: '8px', color: 'var(--text-muted)', whiteSpace: 'nowrap' }}>
                        {new Date(log.createdAt).toLocaleString()}
                      </td>
                      <td style={{ padding: '8px' }}>
                        <span className="badge badge-acknowledged" style={{ fontSize: '0.65rem' }}>{log.eventCategory}</span>
                      </td>
                      <td style={{ padding: '8px' }}><code>{log.actorEmail}</code></td>
                      <td style={{ padding: '8px', fontWeight: '600' }}>{log.action}</td>
                      <td style={{ padding: '8px', color: 'var(--text-secondary)' }}>{log.details}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
