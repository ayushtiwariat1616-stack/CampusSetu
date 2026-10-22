import { useState, useEffect, useCallback } from 'react';
import {
  Inbox,
  CheckCircle,
  ArrowUpRight,
  MessageSquare,
  Lock,
  Paperclip,
  BarChart3,
  Layers,
  AlertTriangle,
  RefreshCw
} from 'lucide-react';
import { api } from '../api';

export default function OperatorDashboard({ currentUser }) {
  const [activeTab, setActiveTab] = useState('queue'); // 'queue', 'shared-incidents', 'reports'
  const [complaints, setComplaints] = useState([]);
  const [selectedComplaint, setSelectedComplaint] = useState(null);
  const [complaintDetail, setComplaintDetail] = useState(null);
  const [reports, setReports] = useState(null);
  const [incidents, setIncidents] = useState([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Action input states
  const [ackMessage, setAckMessage] = useState('');
  const [progressMessage, setProgressMessage] = useState('');
  const [resolutionSummary, setResolutionSummary] = useState('');
  const [escalateReason, setEscalateReason] = useState('');
  const [internalNote, setInternalNote] = useState('');
  const [publicReply, setPublicReply] = useState('');

  // Filter state
  const [statusFilter, setStatusFilter] = useState('ALL');

  // Shared incident creation state
  const [newIncidentTitle, setNewIncidentTitle] = useState('');
  const [newIncidentDesc, setNewIncidentDesc] = useState('');
  const [newIncidentCat, setNewIncidentCat] = useState('ELECTRICAL_PLUMBING');
  const [newIncidentLoc, setNewIncidentLoc] = useState('');
  const [incidentProgressUpdate, setIncidentProgressUpdate] = useState('');
  const [selectedIncidentId, setSelectedIncidentId] = useState(null);

  const loadComplaintDetail = useCallback(async (id) => {
    try {
      const res = await api.getComplaintDetails(id);
      if (res?.data) {
        setComplaintDetail(res.data);
      }
    } catch (err) {
      setError(err.message);
    }
  }, []);

  const loadOperatorQueue = useCallback(async () => {
    try {
      const res = await api.getOperatorQueue();
      if (res?.data?.content) {
        let list = res.data.content;
        if (statusFilter !== 'ALL') {
          list = list.filter((c) => c.status === statusFilter);
        }
        setComplaints(list);
        if (selectedComplaint) {
          loadComplaintDetail(selectedComplaint.id);
        }
      }
    } catch (err) {
      setError(err.message);
    }
  }, [statusFilter, selectedComplaint, loadComplaintDetail]);

  const loadReports = useCallback(async () => {
    try {
      const res = await api.getReports();
      if (res?.data) setReports(res.data);
    } catch {
      // Ignored
    }
  }, []);

  const loadIncidents = useCallback(async () => {
    try {
      const res = await api.getActiveIncidents();
      if (res?.data) setIncidents(res.data);
    } catch {
      // Ignored
    }
  }, []);

  useEffect(() => {
    loadOperatorQueue();
    loadReports();
    loadIncidents();
  }, [loadOperatorQueue, loadReports, loadIncidents]);

  // Actions
  const handleAcknowledge = async (id) => {
    try {
      await api.acknowledgeComplaint(id, ackMessage);
      setAckMessage('');
      setSuccess('Complaint acknowledged.');
      loadOperatorQueue();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleMarkInProgress = async (id) => {
    try {
      await api.markInProgress(id, progressMessage);
      setProgressMessage('');
      setSuccess('Status updated to In Progress.');
      loadOperatorQueue();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleProposeResolution = async (id) => {
    if (!resolutionSummary.trim()) {
      setError('Please provide a repair summary explaining what was completed.');
      return;
    }
    try {
      await api.proposeResolution(id, resolutionSummary.trim());
      setResolutionSummary('');
      setSuccess('Resolution proposed. Ticket sent to student for verification.');
      loadOperatorQueue();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleEscalate = async (id) => {
    if (!escalateReason.trim()) {
      setError('An escalation reason is required.');
      return;
    }
    try {
      await api.escalateComplaint(id, escalateReason.trim());
      setEscalateReason('');
      setSuccess('Ticket forwarded and escalated up the authority chain.');
      loadOperatorQueue();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleAddInternalNote = async (id) => {
    if (!internalNote.trim()) return;
    try {
      await api.addInternalNote(id, internalNote.trim());
      setInternalNote('');
      setSuccess('Internal note recorded (hidden from student).');
      loadComplaintDetail(id);
    } catch (err) {
      setError(err.message);
    }
  };

  const handleAddPublicReply = async (id) => {
    if (!publicReply.trim()) return;
    try {
      await api.addPublicNote(id, publicReply.trim());
      setPublicReply('');
      setSuccess('Student-visible reply posted.');
      loadComplaintDetail(id);
    } catch (err) {
      setError(err.message);
    }
  };

  const handleCreateIncident = async (e) => {
    e.preventDefault();
    if (!newIncidentTitle.trim() || !newIncidentDesc.trim()) return;
    try {
      await api.createIncident({
        title: newIncidentTitle.trim(),
        sanitizedDescription: newIncidentDesc.trim(),
        category: newIncidentCat,
        generalLocation: newIncidentLoc.trim(),
        responsibleQueueCode: currentUser.authorizedQueueCodes[0] || 'QUEUE_SERVICE_MAINTENANCE'
      });
      setNewIncidentTitle('');
      setNewIncidentDesc('');
      setNewIncidentLoc('');
      setSuccess('Sanitized shared incident published.');
      loadIncidents();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleUpdateIncidentProgress = async (id) => {
    if (!incidentProgressUpdate.trim()) return;
    try {
      await api.updateIncidentProgress(id, 'IN_PROGRESS', incidentProgressUpdate.trim());
      setIncidentProgressUpdate('');
      setSuccess('Incident progress published.');
      loadIncidents();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="animate-fade-in" style={{ padding: '24px 0 60px' }}>
      <div className="container">
        {/* Operator Scoped Header */}
        <div className="card" style={{ marginBottom: '20px', padding: '18px 24px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <h1 style={{ fontSize: '1.35rem', fontWeight: '800', color: 'var(--primary-navy)' }}>
                {currentUser.fullName || 'Operator Desk'}
              </h1>
              <span className="badge badge-closed">Key Verified</span>
            </div>
            <div style={{ fontSize: '0.8125rem', color: 'var(--text-secondary)', marginTop: '4px' }}>
              Official Staff Account: <strong>{currentUser.email}</strong> • Authorized Scope(s): <code>{currentUser.authorizedQueueCodes?.join(', ') || 'Scoped Inboxes'}</code>
            </div>
          </div>

          <div style={{ display: 'flex', gap: '8px' }}>
            <button
              onClick={() => setActiveTab('queue')}
              className={`btn btn-sm ${activeTab === 'queue' ? 'btn-navy' : 'btn-outline'}`}
            >
              <Inbox size={14} />
              Scoped Queue ({complaints.length})
            </button>
            <button
              onClick={() => setActiveTab('shared-incidents')}
              className={`btn btn-sm ${activeTab === 'shared-incidents' ? 'btn-navy' : 'btn-outline'}`}
            >
              <Layers size={14} />
              Shared Incidents ({incidents.length})
            </button>
            <button
              onClick={() => setActiveTab('reports')}
              className={`btn btn-sm ${activeTab === 'reports' ? 'btn-navy' : 'btn-outline'}`}
            >
              <BarChart3 size={14} />
              Operational Reports
            </button>
          </div>
        </div>

        {/* Global Alerts */}
        {error && (
          <div style={{
            backgroundColor: '#fee2e2',
            border: '1px solid #fecaca',
            color: '#dc2626',
            padding: '12px 16px',
            borderRadius: 'var(--radius-md)',
            fontSize: '0.875rem',
            marginBottom: '16px',
            display: 'flex',
            alignItems: 'center',
            gap: '8px'
          }}>
            <AlertTriangle size={18} />
            {error}
          </div>
        )}
        {success && (
          <div style={{
            backgroundColor: '#d1fae5',
            border: '1px solid #a7f3d0',
            color: '#059669',
            padding: '12px 16px',
            borderRadius: 'var(--radius-md)',
            fontSize: '0.875rem',
            marginBottom: '16px',
            display: 'flex',
            alignItems: 'center',
            gap: '8px'
          }}>
            <CheckCircle size={18} />
            {success}
          </div>
        )}

        {/* TAB 1: SCOPED OPERATOR QUEUE */}
        {activeTab === 'queue' && (
          <div style={{ display: 'grid', gridTemplateColumns: selectedComplaint ? '1fr 1.35fr' : '1fr', gap: '20px' }}>
            {/* Inbox List */}
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px', flexWrap: 'wrap', gap: '8px' }}>
                <div style={{ display: 'flex', gap: '6px', alignItems: 'center' }}>
                  <span style={{ fontSize: '0.8125rem', fontWeight: '700', color: 'var(--text-muted)' }}>Status Filter:</span>
                  <select
                    className="form-select"
                    style={{ width: 'auto', padding: '4px 8px', fontSize: '0.8125rem' }}
                    value={statusFilter}
                    onChange={(e) => setStatusFilter(e.target.value)}
                  >
                    <option value="ALL">All Active</option>
                    <option value="SUBMITTED">Submitted</option>
                    <option value="ACKNOWLEDGED">Acknowledged</option>
                    <option value="IN_PROGRESS">In Progress</option>
                    <option value="RESOLUTION_PROPOSED">Resolution Proposed</option>
                    <option value="CLOSED">Closed</option>
                    <option value="REOPENED">Reopened</option>
                  </select>
                </div>
                <button onClick={loadOperatorQueue} className="btn btn-outline btn-sm">
                  <RefreshCw size={12} />
                  Refresh
                </button>
              </div>

              {complaints.length === 0 ? (
                <div className="card" style={{ textAlign: 'center', padding: '40px 20px', color: 'var(--text-muted)' }}>
                  <Inbox size={36} style={{ margin: '0 auto 12px', color: '#94a3b8' }} />
                  <div style={{ fontWeight: '700', marginBottom: '4px' }}>Queue Clear</div>
                  <div style={{ fontSize: '0.8125rem' }}>No complaints currently pending in your assigned scope.</div>
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                  {complaints.map((c) => {
                    const isSelected = selectedComplaint?.id === c.id;
                    const isSos = c.category === 'WOMEN_SAFETY_SOS';
                    const isCritical = c.urgency === 'CRITICAL';
                    const badgeClass = `badge badge-${c.status.toLowerCase()}`;

                    return (
                      <div
                        key={c.id}
                        onClick={() => {
                          setSelectedComplaint(c);
                          loadComplaintDetail(c.id);
                        }}
                        className="card card-interactive"
                        style={{
                          padding: '16px',
                          cursor: 'pointer',
                          borderLeft: isSos || isCritical ? '4px solid #dc2626' : (isSelected ? '4px solid var(--accent-teal)' : '1px solid var(--border-subtle)'),
                          borderColor: isSelected ? 'var(--accent-teal)' : (isSos ? '#fca5a5' : 'var(--border-subtle)'),
                          backgroundColor: isSelected ? 'var(--accent-teal-subtle)' : (isSos ? '#fff5f5' : '#ffffff'),
                          transition: 'all 0.15s ease'
                        }}
                      >
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' }}>
                          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                            {isSos && <span className="pulse-dot" style={{ backgroundColor: '#dc2626', width: '8px', height: '8px' }}></span>}
                            <span style={{ fontSize: '0.75rem', fontWeight: '800', color: isSos ? '#dc2626' : 'var(--text-muted)' }}>
                              {c.trackingNumber}
                            </span>
                          </div>
                          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                            {isSos && (
                              <span className="badge" style={{ backgroundColor: '#dc2626', color: '#ffffff', fontSize: '0.65rem', padding: '2px 6px', fontWeight: '800' }}>
                                🚨 EMERGENCY SOS
                              </span>
                            )}
                            <span className={badgeClass}>{c.status.replace('_', ' ')}</span>
                          </div>
                        </div>

                        <div style={{ fontWeight: '700', color: isSos ? '#991b1b' : 'var(--primary-navy)', fontSize: '0.9375rem', marginBottom: '4px' }}>
                          {c.title}
                        </div>

                        <div style={{ fontSize: '0.8125rem', color: 'var(--text-secondary)', marginBottom: '8px' }}>
                          📍 {c.locationDetail}
                        </div>

                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                          <span>Reporter: <strong>{c.student?.fullName || 'Student'}</strong></span>
                          <span>Urgency: <strong style={{ color: isCritical ? '#dc2626' : 'inherit' }}>{c.urgency}</strong></span>
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>

            {/* Operator Case Workspace Panel */}
            {selectedComplaint && complaintDetail && (
              <div className="card" style={{ padding: '24px', display: 'flex', flexDirection: 'column', gap: '18px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '8px' }}>
                  <div>
                    <span className={`badge badge-${complaintDetail.complaint.status.toLowerCase()}`} style={{ marginBottom: '6px' }}>
                      {complaintDetail.complaint.status.replace('_', ' ')}
                    </span>
                    <h3 style={{ fontSize: '1.25rem', fontWeight: '800', color: 'var(--primary-navy)' }}>
                      {complaintDetail.complaint.title}
                    </h3>
                    <div style={{ fontSize: '0.8125rem', color: 'var(--text-muted)' }}>
                      Ticket #{complaintDetail.complaint.trackingNumber} • {complaintDetail.complaint.locationDetail}
                    </div>
                  </div>
                  <button
                    onClick={() => setSelectedComplaint(null)}
                    className="btn btn-outline btn-sm"
                  >
                    Close
                  </button>
                </div>

                {/* Emergency Anti-Harassment Banner */}
                {(complaintDetail.complaint.category === 'WOMEN_SAFETY_SOS' || complaintDetail.complaint.urgency === 'CRITICAL') && (
                  <div style={{
                    backgroundColor: '#fff1f2',
                    border: '1.5px solid #fecaca',
                    borderRadius: '8px',
                    padding: '12px 16px',
                    color: '#991b1b',
                    fontSize: '0.8125rem',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '10px'
                  }}>
                    <span className="pulse-dot" style={{ backgroundColor: '#dc2626', width: '10px', height: '10px' }}></span>
                    <div>
                      <strong>🚨 EXECUTIVE WOMEN'S SAFETY & ANTI-HARASSMENT DIRECT DISPATCH</strong>
                      <div>Assigned directly to Dean of Student Welfare (Dean SW) & ICC with CRITICAL urgency. Immediate security and administrative priority.</div>
                    </div>
                  </div>
                )}

                {/* Reporter Identity */}
                <div style={{ backgroundColor: 'var(--bg-subtle)', padding: '12px 16px', borderRadius: '8px', fontSize: '0.8125rem' }}>
                  <div><strong>Student Reporter:</strong> {complaintDetail.complaint.student?.fullName} ({complaintDetail.complaint.student?.email})</div>
                  <div><strong>Location:</strong> {complaintDetail.complaint.locationDetail}</div>
                  <div><strong>Reported:</strong> {new Date(complaintDetail.complaint.createdAt).toLocaleString()}</div>
                </div>

                {/* Description */}
                <div style={{ fontSize: '0.875rem', color: 'var(--text-primary)', lineHeight: 1.5, background: '#ffffff', padding: '12px', border: '1px solid var(--border-subtle)', borderRadius: '8px' }}>
                  <strong>Student Complaint Description:</strong>
                  <p style={{ marginTop: '4px' }}>{complaintDetail.complaint.description}</p>
                </div>

                {/* Evidence Attachments */}
                {complaintDetail.attachments?.length > 0 && (
                  <div>
                    <div style={{ fontWeight: '700', fontSize: '0.875rem', marginBottom: '6px' }}>Evidence Attachment</div>
                    <div style={{ display: 'flex', gap: '8px' }}>
                      {complaintDetail.attachments.map((a) => (
                        <a
                          key={a.id}
                          href={`/api/attachments/${a.id}`}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="btn btn-outline btn-sm"
                        >
                          <Paperclip size={14} />
                          {a.originalFileName} ({(a.fileSizeBytes / 1024).toFixed(1)} KB)
                        </a>
                      ))}
                    </div>
                  </div>
                )}

                {/* OPERATOR WORKFLOW ACTION CONTROLS */}
                <div style={{
                  backgroundColor: 'var(--bg-subtle)',
                  border: '1px solid var(--border-subtle)',
                  borderRadius: 'var(--radius-md)',
                  padding: '16px',
                  display: 'flex',
                  flexDirection: 'column',
                  gap: '12px'
                }}>
                  <div style={{ fontWeight: '700', fontSize: '0.875rem', color: 'var(--primary-navy)' }}>
                    Operational Action Panel
                  </div>

                  {/* Step A: Acknowledge */}
                  {complaintDetail.complaint.status === 'SUBMITTED' && (
                    <div style={{ display: 'flex', gap: '8px' }}>
                      <input
                        type="text"
                        placeholder="Optional acknowledgment note..."
                        className="form-input"
                        value={ackMessage}
                        onChange={(e) => setAckMessage(e.target.value)}
                      />
                      <button
                        onClick={() => handleAcknowledge(complaintDetail.complaint.id)}
                        className="btn btn-navy"
                        style={{ whiteSpace: 'nowrap' }}
                      >
                        Acknowledge Ticket
                      </button>
                    </div>
                  )}

                  {/* Step B: In Progress */}
                  {complaintDetail.complaint.status === 'ACKNOWLEDGED' && (
                    <div style={{ display: 'flex', gap: '8px' }}>
                      <input
                        type="text"
                        placeholder="e.g. Electrician Ram Singh dispatched..."
                        className="form-input"
                        value={progressMessage}
                        onChange={(e) => setProgressMessage(e.target.value)}
                      />
                      <button
                        onClick={() => handleMarkInProgress(complaintDetail.complaint.id)}
                        className="btn btn-primary"
                        style={{ whiteSpace: 'nowrap' }}
                      >
                        Initiate Work
                      </button>
                    </div>
                  )}

                  {/* Step C: Propose Resolution */}
                  {['ACKNOWLEDGED', 'IN_PROGRESS', 'REOPENED'].includes(complaintDetail.complaint.status) && (
                    <div style={{ borderTop: '1px solid var(--border-subtle)', paddingTop: '10px' }}>
                      <label className="form-label">Propose Resolution (Student Verification Required)</label>
                      <div style={{ display: 'flex', gap: '8px' }}>
                        <input
                          type="text"
                          required
                          placeholder="Detailed fix summary (e.g. Speed regulator replaced and tested)..."
                          className="form-input"
                          value={resolutionSummary}
                          onChange={(e) => setResolutionSummary(e.target.value)}
                        />
                        <button
                          onClick={() => handleProposeResolution(complaintDetail.complaint.id)}
                          className="btn btn-primary"
                          style={{ whiteSpace: 'nowrap' }}
                        >
                          Propose Fix
                        </button>
                      </div>
                    </div>
                  )}

                  {/* Step D: Escalate to Next Authority */}
                  <div style={{ borderTop: '1px solid var(--border-subtle)', paddingTop: '10px' }}>
                    <label className="form-label">Forward / Escalate Ticket</label>
                    <div style={{ display: 'flex', gap: '8px' }}>
                      <input
                        type="text"
                        placeholder="Mandatory escalation reason (e.g. Requires replacement budget approval)..."
                        className="form-input"
                        value={escalateReason}
                        onChange={(e) => setEscalateReason(e.target.value)}
                      />
                      <button
                        onClick={() => handleEscalate(complaintDetail.complaint.id)}
                        className="btn btn-outline"
                        style={{ whiteSpace: 'nowrap', borderColor: '#f59e0b', color: '#b45309' }}
                      >
                        <ArrowUpRight size={14} />
                        Escalate
                      </button>
                    </div>
                  </div>
                </div>

                {/* Internal Notes vs Public Reply */}
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '14px' }}>
                  {/* Internal Notes (Protected) */}
                  <div style={{ border: '1px solid #fed7aa', backgroundColor: '#fffbeb', padding: '12px', borderRadius: '8px' }}>
                    <div style={{ fontWeight: '700', fontSize: '0.8125rem', color: '#9a3412', display: 'flex', alignItems: 'center', gap: '4px', marginBottom: '6px' }}>
                      <Lock size={12} />
                      Internal Staff Notes (Protected from Student)
                    </div>
                    <div style={{ display: 'flex', gap: '6px' }}>
                      <input
                        type="text"
                        placeholder="Private maintenance record..."
                        className="form-input"
                        style={{ fontSize: '0.8125rem' }}
                        value={internalNote}
                        onChange={(e) => setInternalNote(e.target.value)}
                      />
                      <button onClick={() => handleAddInternalNote(complaintDetail.complaint.id)} className="btn btn-sm btn-navy">
                        Save
                      </button>
                    </div>
                  </div>

                  {/* Public Reply (Visible to student) */}
                  <div style={{ border: '1px solid var(--border-subtle)', backgroundColor: '#ffffff', padding: '12px', borderRadius: '8px' }}>
                    <div style={{ fontWeight: '700', fontSize: '0.8125rem', color: 'var(--primary-navy)', display: 'flex', alignItems: 'center', gap: '4px', marginBottom: '6px' }}>
                      <MessageSquare size={12} />
                      Student-Visible Reply
                    </div>
                    <div style={{ display: 'flex', gap: '6px' }}>
                      <input
                        type="text"
                        placeholder="Public message to student..."
                        className="form-input"
                        style={{ fontSize: '0.8125rem' }}
                        value={publicReply}
                        onChange={(e) => setPublicReply(e.target.value)}
                      />
                      <button onClick={() => handleAddPublicReply(complaintDetail.complaint.id)} className="btn btn-sm btn-primary">
                        Send
                      </button>
                    </div>
                  </div>
                </div>

                {/* Uploaded Evidence Attachments */}
                {complaintDetail.attachments?.length > 0 && (
                  <div>
                    <div style={{ fontWeight: '700', fontSize: '0.875rem', marginBottom: '6px' }}>Uploaded Evidence & Files</div>
                    <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap', marginBottom: '12px' }}>
                      {complaintDetail.attachments.map((a) => (
                        <a
                          key={a.id}
                          href={`/api/attachments/${a.id}/download`}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="btn btn-outline btn-sm"
                        >
                          <Paperclip size={14} />
                          {a.originalFileName} ({(a.fileSizeBytes / 1024).toFixed(1)} KB)
                        </a>
                      ))}
                    </div>
                  </div>
                )}

                {/* Audit & Notes Log */}
                <div>
                  <div style={{ fontWeight: '700', fontSize: '0.875rem', marginBottom: '8px' }}>Case History & Notes</div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '6px', maxHeight: '180px', overflowY: 'auto' }}>
                    {complaintDetail.notes?.map((n) => (
                      <div key={n.id} style={{
                        padding: '6px 10px',
                        borderRadius: '6px',
                        fontSize: '0.8125rem',
                        backgroundColor: n.internalOnly ? '#fffbeb' : 'var(--bg-subtle)',
                        border: n.internalOnly ? '1px solid #fde68a' : '1px solid var(--border-subtle)'
                      }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', fontWeight: '700', fontSize: '0.75rem' }}>
                          <span>{n.author?.fullName} {n.internalOnly && '🔒 (Internal Note)'}</span>
                          <span style={{ color: 'var(--text-muted)' }}>{new Date(n.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
                        </div>
                        <div style={{ marginTop: '2px' }}>{n.content}</div>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            )}
          </div>
        )}

        {/* TAB 2: SHARED INCIDENTS WORKSPACE */}
        {activeTab === 'shared-incidents' && (
          <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 1fr', gap: '20px' }}>
            {/* Create Incident Form */}
            <div className="card">
              <h3 style={{ fontSize: '1.125rem', fontWeight: '800', color: 'var(--primary-navy)', marginBottom: '4px' }}>
                Publish Sanitized Shared Incident
              </h3>
              <p style={{ fontSize: '0.8125rem', color: 'var(--text-muted)', marginBottom: '16px' }}>
                Use for common campus outages (water, power, Wi-Fi). Strictly sanitize: Do NOT include individual student names or room numbers.
              </p>

              <form onSubmit={handleCreateIncident} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
                <div className="form-group" style={{ margin: 0 }}>
                  <label className="form-label">Incident Title *</label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. Water Line Maintenance - Aryabhatt B-Wing"
                    className="form-input"
                    value={newIncidentTitle}
                    onChange={(e) => setNewIncidentTitle(e.target.value)}
                  />
                </div>

                <div className="form-group" style={{ margin: 0 }}>
                  <label className="form-label">General Public Location *</label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. Aryabhatt Hostel Utility Shaft B-Wing"
                    className="form-input"
                    value={newIncidentLoc}
                    onChange={(e) => setNewIncidentLoc(e.target.value)}
                  />
                </div>

                <div className="form-group" style={{ margin: 0 }}>
                  <label className="form-label">Category</label>
                  <select
                    className="form-select"
                    value={newIncidentCat}
                    onChange={(e) => setNewIncidentCat(e.target.value)}
                  >
                    <option value="ELECTRICAL_PLUMBING">Electrical / Plumbing Outage</option>
                    <option value="IT_AND_NETWORK">Campus Wi-Fi / LAN Failure</option>
                    <option value="HOSTEL_FACILITY">Common Hostel Facility</option>
                    <option value="MESS_FOOD">Mess Water / Dining Outage</option>
                  </select>
                </div>

                <div className="form-group" style={{ margin: 0 }}>
                  <label className="form-label">Sanitized Description *</label>
                  <textarea
                    rows={3}
                    required
                    placeholder="Describe the overall issue and expected restoration timeline..."
                    className="form-textarea"
                    value={newIncidentDesc}
                    onChange={(e) => setNewIncidentDesc(e.target.value)}
                  />
                </div>

                <button type="submit" className="btn btn-primary">
                  Publish Shared Community Incident
                </button>
              </form>
            </div>

            {/* Existing Incidents List & Updates */}
            <div className="card">
              <h3 style={{ fontSize: '1.125rem', fontWeight: '800', color: 'var(--primary-navy)', marginBottom: '12px' }}>
                Active Shared Incidents
              </h3>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                {incidents.map((inc) => (
                  <div key={inc.id} style={{ padding: '12px', border: '1px solid var(--border-subtle)', borderRadius: '8px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontWeight: '700', fontSize: '0.875rem', marginBottom: '2px' }}>
                      <span>{inc.title}</span>
                      <span className="badge badge-acknowledged">{inc.status}</span>
                    </div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginBottom: '8px' }}>
                      📍 {inc.generalLocation} • {inc.affectedSubscriberCount} student subscriber(s)
                    </div>

                    {/* Quick update input */}
                    <div style={{ display: 'flex', gap: '6px' }}>
                      <input
                        type="text"
                        placeholder="Post progress update..."
                        className="form-input"
                        style={{ fontSize: '0.75rem', padding: '6px' }}
                        value={selectedIncidentId === inc.id ? incidentProgressUpdate : ''}
                        onChange={(e) => {
                          setSelectedIncidentId(inc.id);
                          setIncidentProgressUpdate(e.target.value);
                        }}
                      />
                      <button
                        onClick={() => handleUpdateIncidentProgress(inc.id)}
                        className="btn btn-sm btn-navy"
                      >
                        Update
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* TAB 3: OPERATIONAL REPORTS & METRICS */}
        {activeTab === 'reports' && reports && (
          <div>
            <div style={{ marginBottom: '20px' }}>
              <h2 style={{ fontSize: '1.35rem', fontWeight: '800', color: 'var(--primary-navy)' }}>
                Scoped Performance Metrics & SLAs
              </h2>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>
                Grounded calculations computed live from active database records within your authorized operational queue.
              </p>
            </div>

            {/* Metrics Cards Grid */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px', marginBottom: '24px' }}>
              <div className="card" style={{ padding: '18px' }}>
                <div style={{ fontSize: '0.75rem', fontWeight: '700', color: 'var(--text-muted)', textTransform: 'uppercase' }}>
                  Total Handled Cases
                </div>
                <div style={{ fontSize: '2rem', fontWeight: '800', color: 'var(--primary-navy)', marginTop: '4px' }}>
                  {reports.totalComplaints}
                </div>
              </div>

              <div className="card" style={{ padding: '18px' }}>
                <div style={{ fontSize: '0.75rem', fontWeight: '700', color: 'var(--text-muted)', textTransform: 'uppercase' }}>
                  Avg Hours to Ack
                </div>
                <div style={{ fontSize: '2rem', fontWeight: '800', color: 'var(--accent-teal)', marginTop: '4px' }}>
                  {reports.avgHoursToAck}h
                </div>
                <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>SLA Target: 12-24h</div>
              </div>

              <div className="card" style={{ padding: '18px' }}>
                <div style={{ fontSize: '0.75rem', fontWeight: '700', color: 'var(--text-muted)', textTransform: 'uppercase' }}>
                  Avg Hours to Resolution
                </div>
                <div style={{ fontSize: '2rem', fontWeight: '800', color: '#2563eb', marginTop: '4px' }}>
                  {reports.avgHoursToResolution}h
                </div>
                <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>SLA Target: 48-72h</div>
              </div>

              <div className="card" style={{ padding: '18px' }}>
                <div style={{ fontSize: '0.75rem', fontWeight: '700', color: 'var(--text-muted)', textTransform: 'uppercase' }}>
                  Student-Confirmed Closure Rate
                </div>
                <div style={{ fontSize: '2rem', fontWeight: '800', color: '#059669', marginTop: '4px' }}>
                  {reports.studentConfirmedClosureRate}%
                </div>
                <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>Confirmed repairs / proposed</div>
              </div>
            </div>

            {/* Status Breakdown Table */}
            <div className="card">
              <h3 style={{ fontSize: '1rem', fontWeight: '700', color: 'var(--primary-navy)', marginBottom: '12px' }}>
                Queue Breakdown by Status
              </h3>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(140px, 1fr))', gap: '12px' }}>
                <div style={{ padding: '12px', background: 'var(--status-submitted-bg)', borderRadius: '8px', color: 'var(--status-submitted)', fontWeight: '700' }}>
                  Submitted: {reports.submitted}
                </div>
                <div style={{ padding: '12px', background: 'var(--status-ack-bg)', borderRadius: '8px', color: 'var(--status-ack)', fontWeight: '700' }}>
                  Acknowledged: {reports.acknowledged}
                </div>
                <div style={{ padding: '12px', background: 'var(--status-progress-bg)', borderRadius: '8px', color: 'var(--status-progress)', fontWeight: '700' }}>
                  In Progress: {reports.inProgress}
                </div>
                <div style={{ padding: '12px', background: 'var(--status-proposed-bg)', borderRadius: '8px', color: 'var(--status-proposed)', fontWeight: '700' }}>
                  Proposed: {reports.resolutionProposed}
                </div>
                <div style={{ padding: '12px', background: 'var(--status-closed-bg)', borderRadius: '8px', color: 'var(--status-closed)', fontWeight: '700' }}>
                  Closed: {reports.closed}
                </div>
                <div style={{ padding: '12px', background: 'var(--status-reopened-bg)', borderRadius: '8px', color: 'var(--status-reopened)', fontWeight: '700' }}>
                  Reopened: {reports.reopened}
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
