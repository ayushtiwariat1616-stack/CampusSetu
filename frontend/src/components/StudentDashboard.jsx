import { useState, useEffect, useCallback } from 'react';
import {
  PlusCircle,
  Clock,
  CheckCircle2,
  XCircle,
  RotateCcw,
  MessageSquare,
  Paperclip,
  AlertTriangle,
  Building,
  Layers,
  BookOpen,
  Send,
  Eye,
  Check
} from 'lucide-react';
import { api } from '../api';

export default function StudentDashboard({ currentUser }) {
  const [activeTab, setActiveTab] = useState('my-complaints'); // 'my-complaints', 'submit', 'shared-incidents', 'academics'
  const [complaints, setComplaints] = useState([]);
  const [selectedComplaint, setSelectedComplaint] = useState(null);
  const [complaintDetail, setComplaintDetail] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Submit Complaint Form State
  const [formData, setFormData] = useState({
    title: '',
    category: 'HOSTEL_FACILITY',
    locationType: 'HOSTEL',
    specificHostelCode: currentUser?.hostelCode || 'ARYABHATT',
    specificDepartmentCode: currentUser?.departmentCode || 'AIML',
    locationDetail: currentUser?.roomNumber ? `Aryabhatt Hostel Room ${currentUser.roomNumber}` : '',
    urgency: 'MEDIUM',
    description: '',
    sensitive: false
  });
  const [selectedFile, setSelectedFile] = useState(null);
  const [routingPreview, setRoutingPreview] = useState(null);

  // Resolution feedback / reject / reopen modals
  const [feedbackInput, setFeedbackInput] = useState('');
  const [rejectReasonInput, setRejectReasonInput] = useState('');
  const [reopenReasonInput, setReopenReasonInput] = useState('');
  const [replyInput, setReplyInput] = useState('');

  // Shared incidents state
  const [incidents, setIncidents] = useState([]);
  const [subscribedIncidents, setSubscribedIncidents] = useState(new Set());

  // Academic Hub State
  const [acadDocs, setAcadDocs] = useState([]);
  const [acadSemester, setAcadSemester] = useState(6);
  const [qaQuestion, setQaQuestion] = useState('');
  const [qaResponse, setQaResponse] = useState(null);
  const [qaLoading, setQaLoading] = useState(false);

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

  const loadMyComplaints = useCallback(async () => {
    try {
      const res = await api.getMyComplaints();
      if (res?.data?.content) {
        setComplaints(res.data.content);
        if (selectedComplaint) {
          loadComplaintDetail(selectedComplaint.id);
        }
      }
    } catch (err) {
      setError(err.message);
    }
  }, [selectedComplaint, loadComplaintDetail]);

  const loadSharedIncidents = useCallback(async () => {
    try {
      const res = await api.getActiveIncidents();
      if (res?.data) {
        setIncidents(res.data);
        const subSet = new Set();
        for (const inc of res.data) {
          const subRes = await api.getIncidentSubscriptionStatus(inc.id).catch(() => null);
          if (subRes?.data) subSet.add(inc.id);
        }
        setSubscribedIncidents(subSet);
      }
    } catch {
      // Ignored
    }
  }, []);

  const loadAcademicDocs = useCallback(async () => {
    try {
      const res = await api.getAcademicDocuments(acadSemester);
      if (res?.data) setAcadDocs(res.data);
    } catch {
      // Ignored
    }
  }, [acadSemester]);

  const updateRoutingPreview = useCallback(async () => {
    try {
      const res = await api.getRoutingPreview({
        category: formData.category,
        locationType: formData.locationType,
        hostelCode: formData.locationType === 'HOSTEL' ? formData.specificHostelCode : '',
        departmentCode: formData.locationType === 'DEPARTMENT' ? formData.specificDepartmentCode : '',
      });
      if (res?.data) {
        setRoutingPreview(res.data);
      }
    } catch {
      // Ignored
    }
  }, [formData.category, formData.locationType, formData.specificHostelCode, formData.specificDepartmentCode]);

  useEffect(() => {
    loadMyComplaints();
    loadSharedIncidents();
    loadAcademicDocs();
  }, [loadMyComplaints, loadSharedIncidents, loadAcademicDocs]);

  // Live routing preview on form change
  useEffect(() => {
    if (activeTab === 'submit') {
      updateRoutingPreview();
    }
  }, [activeTab, updateRoutingPreview]);

  const handleSubmitComplaint = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    try {
      const res = await api.submitComplaint(formData);
      const newComplaint = res.data;

      // If user uploaded an evidence photo/PDF
      if (selectedFile && newComplaint?.id) {
        await api.uploadComplaintAttachment(newComplaint.id, selectedFile)
          .catch((err) => console.error('Attachment upload warning:', err));
      }

      setSuccess(`Complaint ${newComplaint.trackingNumber} submitted successfully!`);
      setFormData({
        title: '',
        category: 'HOSTEL_FACILITY',
        locationType: 'HOSTEL',
        specificHostelCode: currentUser?.hostelCode || 'ARYABHATT',
        specificDepartmentCode: currentUser?.departmentCode || 'AIML',
        locationDetail: '',
        urgency: 'MEDIUM',
        description: '',
        sensitive: false
      });
      setSelectedFile(null);
      await loadMyComplaints();
      setActiveTab('my-complaints');
    } catch (err) {
      setError(err.message || 'Submission failed');
    } finally {
      setLoading(false);
    }
  };

  const handleConfirmResolution = async (id) => {
    try {
      await api.confirmResolution(id, feedbackInput);
      setFeedbackInput('');
      setSuccess('Resolution confirmed. Complaint is now Closed.');
      loadMyComplaints();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleRejectResolution = async (id) => {
    if (!rejectReasonInput.trim()) {
      setError('Please provide a reason explaining why the repair is unsatisfactory.');
      return;
    }
    try {
      await api.rejectResolution(id, rejectReasonInput.trim());
      setRejectReasonInput('');
      setSuccess('Resolution rejected. Ticket returned to maintenance team.');
      loadMyComplaints();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleReopen = async (id) => {
    if (!reopenReasonInput.trim()) {
      setError('A documented reason is required to reopen this ticket.');
      return;
    }
    try {
      await api.reopenComplaint(id, reopenReasonInput.trim());
      setReopenReasonInput('');
      setSuccess('Complaint reopened. Re-dispatch recorded in history.');
      loadMyComplaints();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleAddReply = async (id) => {
    if (!replyInput.trim()) return;
    try {
      await api.addPublicNote(id, replyInput.trim());
      setReplyInput('');
      loadComplaintDetail(id);
    } catch (err) {
      setError(err.message);
    }
  };

  const handleToggleIncidentSub = async (incidentId) => {
    try {
      const res = await api.toggleSubscription(incidentId);
      const isSub = res.data;
      setSubscribedIncidents((prev) => {
        const next = new Set(prev);
        if (isSub) next.add(incidentId);
        else next.delete(incidentId);
        return next;
      });
      loadSharedIncidents();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleAskQuestion = async (e) => {
    e.preventDefault();
    if (!qaQuestion.trim()) return;
    setQaLoading(true);
    setQaResponse(null);
    try {
      const res = await api.askAcademicQuestion(qaQuestion.trim());
      if (res?.data) {
        setQaResponse(res.data);
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setQaLoading(false);
    }
  };

  return (
    <div className="animate-fade-in" style={{ padding: '24px 0 60px' }}>
      <div className="container">
        {/* Student Profile Header */}
        <div className="card" style={{ marginBottom: '24px', padding: '20px 24px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <h1 style={{ fontSize: '1.35rem', fontWeight: '800', color: 'var(--primary-navy)' }}>
                {currentUser?.fullName || 'Student Workspace'}
              </h1>
              <span className="badge badge-acknowledged">Verified Student</span>
            </div>
            <div style={{ fontSize: '0.8125rem', color: 'var(--text-secondary)', marginTop: '4px' }}>
              Department: <strong>{currentUser?.departmentCode || 'AIML'}</strong> • Residence: <strong>{currentUser?.hostelCode || 'Aryabhatt Hostel'}</strong> (Room {currentUser?.roomNumber || '214'}) • Roll #{currentUser?.studentRollNumber || '25004108'}
            </div>
          </div>

          <div style={{ display: 'flex', gap: '8px' }}>
            <button
              onClick={() => setActiveTab('submit')}
              className="btn btn-primary"
            >
              <PlusCircle size={16} />
              Submit Complaint
            </button>
          </div>
        </div>

        {/* Global Notifications / Alerts */}
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
            <CheckCircle2 size={18} />
            {success}
          </div>
        )}

        {/* Navigation Tabs */}
        <div style={{
          display: 'flex',
          gap: '8px',
          borderBottom: '1px solid var(--border-subtle)',
          marginBottom: '20px',
          overflowX: 'auto',
          paddingBottom: '4px'
        }}>
          <button
            onClick={() => { setActiveTab('my-complaints'); setSelectedComplaint(null); }}
            className={`btn btn-sm ${activeTab === 'my-complaints' ? 'btn-navy' : 'btn-outline'}`}
          >
            My Complaints ({complaints.length})
          </button>
          <button
            onClick={() => setActiveTab('submit')}
            className={`btn btn-sm ${activeTab === 'submit' ? 'btn-navy' : 'btn-outline'}`}
          >
            <PlusCircle size={14} />
            Submit Complaint
          </button>
          <button
            onClick={() => setActiveTab('shared-incidents')}
            className={`btn btn-sm ${activeTab === 'shared-incidents' ? 'btn-navy' : 'btn-outline'}`}
          >
            <Layers size={14} />
            Shared Outages ({incidents.length})
          </button>
          <button
            onClick={() => setActiveTab('academics')}
            className={`btn btn-sm ${activeTab === 'academics' ? 'btn-navy' : 'btn-outline'}`}
          >
            <BookOpen size={14} />
            Academic Notes & Assistant
          </button>
        </div>

        {/* TAB 1: SUBMIT COMPLAINT FORM */}
        {activeTab === 'submit' && (
          <div className="card" style={{ maxWidth: '820px', margin: '0 auto' }}>
            <h2 style={{ fontSize: '1.25rem', fontWeight: '800', color: 'var(--primary-navy)', marginBottom: '4px' }}>
              Submit a Campus Facility Issue
            </h2>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.8125rem', marginBottom: '20px' }}>
              Your complaint will be routed deterministically according to category and affected location.
            </p>

            <form onSubmit={handleSubmitComplaint}>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '16px' }}>
                <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                  <label className="form-label">Issue Summary / Title *</label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. Ceiling fan speed regulator burned out"
                    className="form-input"
                    value={formData.title}
                    onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Category *</label>
                  <select
                    className="form-select"
                    value={formData.category}
                    onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                  >
                    <option value="HOSTEL_FACILITY">Hostel Fixture / Furniture / Fan</option>
                    <option value="LAB_EQUIPMENT">Lab System / GPU / Hardware</option>
                    <option value="CLASSROOM_INFRA">Classroom Projector / Desk</option>
                    <option value="IT_AND_NETWORK">Campus Wi-Fi / LAN / ERP</option>
                    <option value="ELECTRICAL_PLUMBING">Electrical / Plumbing Outage</option>
                    <option value="CLEANLINESS">Sanitation / Housekeeping</option>
                    <option value="MESS_FOOD">Hostel Mess / Dining Hall</option>
                    <option value="OTHER">Other Operational Issue</option>
                  </select>
                </div>

                <div className="form-group">
                  <label className="form-label">Location Type *</label>
                  <select
                    className="form-select"
                    value={formData.locationType}
                    onChange={(e) => setFormData({ ...formData, locationType: e.target.value })}
                  >
                    <option value="HOSTEL">Hostel Residence</option>
                    <option value="DEPARTMENT">Academic Department / Lab</option>
                    <option value="CAMPUS_FACILITY">Campus Service Facility</option>
                  </select>
                </div>

                {formData.locationType === 'HOSTEL' && (
                  <div className="form-group">
                    <label className="form-label">Hostel *</label>
                    <select
                      className="form-select"
                      value={formData.specificHostelCode}
                      onChange={(e) => setFormData({ ...formData, specificHostelCode: e.target.value })}
                    >
                      <option value="ARYABHATT">Aryabhatt Hostel (1st Year Boys)</option>
                      <option value="CV_RAMAN">C.V. Raman Hostel (Boys)</option>
                      <option value="TAGORE">Tagore Hostel (Boys)</option>
                      <option value="SARASWATI">Saraswati Hostel (Girls)</option>
                      <option value="GARGI">Gargi Hostel (Girls)</option>
                    </select>
                  </div>
                )}

                {formData.locationType === 'DEPARTMENT' && (
                  <div className="form-group">
                    <label className="form-label">Department *</label>
                    <select
                      className="form-select"
                      value={formData.specificDepartmentCode}
                      onChange={(e) => setFormData({ ...formData, specificDepartmentCode: e.target.value })}
                    >
                      <option value="AIML">Artificial Intelligence & ML</option>
                      <option value="CSE">Computer Science & Engg</option>
                      <option value="IT">Information Technology</option>
                      <option value="ECE">Electronics & Communication</option>
                      <option value="EN">Electrical & Electronics</option>
                      <option value="ME">Mechanical Engineering</option>
                      <option value="CE">Civil Engineering</option>
                      <option value="MCA">Computer Applications</option>
                    </select>
                  </div>
                )}

                <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                  <label className="form-label">Exact Room / Location Detail *</label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. Room 214, 2nd Floor, Left Wing"
                    className="form-input"
                    value={formData.locationDetail}
                    onChange={(e) => setFormData({ ...formData, locationDetail: e.target.value })}
                  />
                </div>

                <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                  <label className="form-label">Detailed Description *</label>
                  <textarea
                    rows={4}
                    required
                    placeholder="Describe what occurred, any safety hazards, or specific symptoms..."
                    className="form-textarea"
                    value={formData.description}
                    onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Urgency</label>
                  <select
                    className="form-select"
                    value={formData.urgency}
                    onChange={(e) => setFormData({ ...formData, urgency: e.target.value })}
                  >
                    <option value="LOW">Low (Cosmetic / Non-critical)</option>
                    <option value="MEDIUM">Medium (Standard Maintenance)</option>
                    <option value="HIGH">High (Active disruption / Fan failure)</option>
                    <option value="CRITICAL">Critical (Immediate safety / Flood / Spark)</option>
                  </select>
                </div>

                <div className="form-group">
                  <label className="form-label">Attach Photo / Evidence (Optional)</label>
                  <input
                    type="file"
                    accept="image/*,application/pdf"
                    className="form-input"
                    onChange={(e) => setSelectedFile(e.target.files[0])}
                  />
                </div>
              </div>

              {/* Dynamic Live Routing Preview Box */}
              {routingPreview && (
                <div style={{
                  backgroundColor: 'var(--accent-teal-subtle)',
                  border: '1px solid #99f6e4',
                  borderRadius: 'var(--radius-md)',
                  padding: '14px',
                  margin: '16px 0',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '12px'
                }}>
                  <Building size={24} color="#0d9488" />
                  <div style={{ fontSize: '0.8125rem' }}>
                    <div style={{ fontWeight: '700', color: 'var(--primary-navy)' }}>
                      Designated Receiving Office: {routingPreview.receivingOffice}
                    </div>
                    <div style={{ color: 'var(--text-secondary)' }}>
                      Assigned Queue: <code>{routingPreview.queueCode}</code> • Rule: {routingPreview.ruleApplied}
                    </div>
                  </div>
                </div>
              )}

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', marginTop: '16px' }}>
                <button
                  type="button"
                  onClick={() => setActiveTab('my-complaints')}
                  className="btn btn-outline"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  className="btn btn-primary"
                  style={{ minWidth: '160px' }}
                >
                  {loading ? 'Submitting...' : 'Register Complaint'}
                </button>
              </div>
            </form>
          </div>
        )}

        {/* TAB 2: MY COMPLAINTS LIST & DETAIL */}
        {activeTab === 'my-complaints' && (
          <div style={{ display: 'grid', gridTemplateColumns: selectedComplaint ? '1fr 1.3fr' : '1fr', gap: '20px' }}>
            {/* List View */}
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
                <h2 style={{ fontSize: '1.125rem', fontWeight: '700', color: 'var(--primary-navy)' }}>
                  Submitted Grievances
                </h2>
                <button onClick={loadMyComplaints} className="btn btn-outline btn-sm">
                  Refresh
                </button>
              </div>

              {complaints.length === 0 ? (
                <div className="card" style={{ textAlign: 'center', padding: '40px 20px', color: 'var(--text-muted)' }}>
                  <CheckCircle2 size={36} style={{ margin: '0 auto 12px', color: '#94a3b8' }} />
                  <div style={{ fontWeight: '700', marginBottom: '4px' }}>No Complaints Registered</div>
                  <div style={{ fontSize: '0.8125rem' }}>You have not submitted any facility issues yet.</div>
                  <button
                    onClick={() => setActiveTab('submit')}
                    className="btn btn-primary btn-sm"
                    style={{ marginTop: '16px' }}
                  >
                    Submit New Complaint
                  </button>
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                  {complaints.map((c) => {
                    const isSelected = selectedComplaint?.id === c.id;
                    const badgeClass = `badge badge-${c.status.toLowerCase()}`;

                    return (
                      <div
                        key={c.id}
                        onClick={() => {
                          setSelectedComplaint(c);
                          loadComplaintDetail(c.id);
                        }}
                        className="card"
                        style={{
                          padding: '16px',
                          cursor: 'pointer',
                          borderColor: isSelected ? 'var(--accent-teal)' : 'var(--border-subtle)',
                          backgroundColor: isSelected ? 'var(--accent-teal-subtle)' : '#ffffff',
                          transition: 'all 0.15s ease'
                        }}
                      >
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' }}>
                          <span style={{ fontSize: '0.75rem', fontWeight: '700', color: 'var(--text-muted)' }}>
                            {c.trackingNumber}
                          </span>
                          <span className={badgeClass}>{c.status.replace('_', ' ')}</span>
                        </div>

                        <div style={{ fontWeight: '700', color: 'var(--primary-navy)', fontSize: '0.9375rem', marginBottom: '4px' }}>
                          {c.title}
                        </div>

                        <div style={{ fontSize: '0.8125rem', color: 'var(--text-secondary)', marginBottom: '8px' }}>
                          {c.locationDetail}
                        </div>

                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                          <span>Queue: <code>{c.currentQueueCode.replace('QUEUE_', '')}</code></span>
                          <span>{new Date(c.createdAt).toLocaleDateString()}</span>
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>

            {/* Detail View Panel */}
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
                    <div style={{ fontSize: '0.8125rem', color: 'var(--text-muted)', marginTop: '2px' }}>
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

                {/* Description */}
                <div style={{ backgroundColor: 'var(--bg-subtle)', padding: '14px', borderRadius: 'var(--radius-md)', fontSize: '0.875rem' }}>
                  <div style={{ fontWeight: '700', color: 'var(--text-secondary)', marginBottom: '4px' }}>Original Report:</div>
                  <p style={{ color: 'var(--text-primary)', lineHeight: 1.5 }}>{complaintDetail.complaint.description}</p>
                </div>

                {/* CRITICAL: STUDENT RESOLUTION CONFIRMATION WORKFLOW */}
                {complaintDetail.complaint.status === 'RESOLUTION_PROPOSED' && (
                  <div style={{
                    backgroundColor: '#ecfeff',
                    border: '2px solid #06b6d4',
                    borderRadius: 'var(--radius-md)',
                    padding: '18px'
                  }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#0891b2', fontWeight: '800', marginBottom: '8px' }}>
                      <CheckCircle2 size={20} />
                      Resolution Proposed by Maintenance Team
                    </div>
                    <div style={{ fontSize: '0.875rem', color: 'var(--text-primary)', marginBottom: '12px', background: '#ffffff', padding: '10px', borderRadius: '6px' }}>
                      <strong>Work Summary:</strong> "{complaintDetail.complaint.resolutionSummary}"
                    </div>
                    <p style={{ fontSize: '0.8125rem', color: 'var(--text-secondary)', marginBottom: '14px' }}>
                      Please inspect the fixture/facility. Did this resolve the issue completely?
                    </p>

                    <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                      <div style={{ display: 'flex', gap: '8px' }}>
                        <input
                          type="text"
                          placeholder="Optional feedback on repair quality..."
                          className="form-input"
                          value={feedbackInput}
                          onChange={(e) => setFeedbackInput(e.target.value)}
                        />
                        <button
                          onClick={() => handleConfirmResolution(complaintDetail.complaint.id)}
                          className="btn btn-primary"
                          style={{ whiteSpace: 'nowrap' }}
                        >
                          <Check size={16} />
                          Confirm & Close
                        </button>
                      </div>

                      <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                        <input
                          type="text"
                          placeholder="If issue persists, explain reason to reject..."
                          className="form-input"
                          value={rejectReasonInput}
                          onChange={(e) => setRejectReasonInput(e.target.value)}
                        />
                        <button
                          onClick={() => handleRejectResolution(complaintDetail.complaint.id)}
                          className="btn btn-danger"
                          style={{ whiteSpace: 'nowrap' }}
                        >
                          <XCircle size={16} />
                          Reject Fix
                        </button>
                      </div>
                    </div>
                  </div>
                )}

                {/* REOPEN CLOSED COMPLAINT WORKFLOW */}
                {complaintDetail.complaint.status === 'CLOSED' && (
                  <div style={{
                    backgroundColor: 'var(--bg-subtle)',
                    border: '1px solid var(--border-subtle)',
                    borderRadius: 'var(--radius-md)',
                    padding: '14px'
                  }}>
                    <div style={{ fontWeight: '700', fontSize: '0.875rem', marginBottom: '4px', color: 'var(--primary-navy)' }}>
                      Ticket Closed
                    </div>
                    <div style={{ fontSize: '0.8125rem', color: 'var(--text-muted)', marginBottom: '10px' }}>
                      Notes: {complaintDetail.complaint.closureNotes || 'Student confirmed repair.'}
                    </div>
                    <div style={{ display: 'flex', gap: '8px' }}>
                      <input
                        type="text"
                        placeholder="Documented reason to reopen..."
                        className="form-input"
                        value={reopenReasonInput}
                        onChange={(e) => setReopenReasonInput(e.target.value)}
                      />
                      <button
                        onClick={() => handleReopen(complaintDetail.complaint.id)}
                        className="btn btn-outline btn-sm"
                        style={{ whiteSpace: 'nowrap' }}
                      >
                        <RotateCcw size={14} />
                        Reopen Ticket
                      </button>
                    </div>
                  </div>
                )}

                {/* Attachments Section */}
                {complaintDetail.attachments?.length > 0 && (
                  <div>
                    <div style={{ fontWeight: '700', fontSize: '0.875rem', marginBottom: '6px' }}>Uploaded Evidence</div>
                    <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
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

                {/* Public Conversation & Updates */}
                <div>
                  <div style={{ fontWeight: '700', fontSize: '0.875rem', marginBottom: '8px', display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <MessageSquare size={16} />
                    Updates & Replies
                  </div>

                  <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', maxHeight: '200px', overflowY: 'auto', marginBottom: '10px' }}>
                    {complaintDetail.notes?.length === 0 ? (
                      <div style={{ color: 'var(--text-muted)', fontSize: '0.8125rem' }}>No comments yet.</div>
                    ) : (
                      complaintDetail.notes.map((n) => (
                        <div key={n.id} style={{ padding: '8px 12px', backgroundColor: 'var(--bg-subtle)', borderRadius: '6px', fontSize: '0.8125rem' }}>
                          <div style={{ display: 'flex', justifyContent: 'space-between', fontWeight: '600', marginBottom: '2px' }}>
                            <span>{n.author?.fullName || 'Staff Reply'}</span>
                            <span style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>{new Date(n.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
                          </div>
                          <div>{n.content}</div>
                        </div>
                      ))
                    )}
                  </div>

                  <div style={{ display: 'flex', gap: '8px' }}>
                    <input
                      type="text"
                      placeholder="Type a follow-up reply for maintenance..."
                      className="form-input"
                      value={replyInput}
                      onChange={(e) => setReplyInput(e.target.value)}
                    />
                    <button
                      onClick={() => handleAddReply(complaintDetail.complaint.id)}
                      className="btn btn-navy"
                    >
                      <Send size={16} />
                    </button>
                  </div>
                </div>

                {/* Lifecycle Audit Timeline */}
                <div>
                  <div style={{ fontWeight: '700', fontSize: '0.875rem', marginBottom: '10px', display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Clock size={16} />
                    Lifecycle Timeline
                  </div>

                  <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', borderLeft: '2px solid #cbd5e1', paddingLeft: '14px', marginLeft: '6px' }}>
                    {complaintDetail.events?.map((ev) => (
                      <div key={ev.id} style={{ position: 'relative', fontSize: '0.8125rem' }}>
                        <div style={{
                          position: 'absolute',
                          left: '-20px',
                          top: '4px',
                          width: '10px',
                          height: '10px',
                          borderRadius: '50%',
                          backgroundColor: 'var(--accent-teal)'
                        }} />
                        <div style={{ fontWeight: '700', color: 'var(--primary-navy)' }}>
                          {ev.eventType} • <span style={{ fontWeight: '500', color: 'var(--text-muted)' }}>{ev.actorName} ({ev.actorRole})</span>
                        </div>
                        <div style={{ color: 'var(--text-secondary)' }}>{ev.message}</div>
                        <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>
                          {new Date(ev.createdAt).toLocaleString()}
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            )}
          </div>
        )}

        {/* TAB 3: SHARED OUTAGES FEED */}
        {activeTab === 'shared-incidents' && (
          <div style={{ maxWidth: '840px', margin: '0 auto' }}>
            <div style={{ marginBottom: '20px' }}>
              <h2 style={{ fontSize: '1.35rem', fontWeight: '800', color: 'var(--primary-navy)' }}>
                Campus Community Outages & Maintenance
              </h2>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>
                Sanitized common facility issues. If you are experiencing this same problem, click "I Am Affected" to subscribe to status updates.
              </p>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              {incidents.map((inc) => {
                const isSubscribed = subscribedIncidents.has(inc.id);

                return (
                  <div key={inc.id} className="card" style={{ padding: '20px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                      <span className="badge badge-acknowledged">{inc.category.replace('_', ' ')}</span>
                      <span style={{ fontSize: '0.75rem', fontWeight: '600', color: 'var(--text-muted)' }}>
                        Incident #{inc.incidentNumber}
                      </span>
                    </div>

                    <h3 style={{ fontSize: '1.15rem', fontWeight: '800', color: 'var(--primary-navy)', marginBottom: '6px' }}>
                      {inc.title}
                    </h3>

                    <div style={{ fontSize: '0.8125rem', color: 'var(--accent-teal)', fontWeight: '600', marginBottom: '8px' }}>
                      📍 {inc.generalLocation}
                    </div>

                    <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', lineHeight: 1.5, marginBottom: '14px' }}>
                      {inc.sanitizedDescription}
                    </p>

                    {inc.publicProgressUpdates && (
                      <div style={{
                        backgroundColor: 'var(--bg-subtle)',
                        padding: '10px 14px',
                        borderRadius: '6px',
                        fontSize: '0.8125rem',
                        marginBottom: '14px',
                        borderLeft: '3px solid var(--accent-teal)'
                      }}>
                        <div style={{ fontWeight: '700', marginBottom: '2px' }}>Latest Official Update:</div>
                        <div style={{ whiteSpace: 'pre-line' }}>{inc.publicProgressUpdates}</div>
                      </div>
                    )}

                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingTop: '10px', borderTop: '1px solid var(--border-subtle)' }}>
                      <div style={{ fontSize: '0.8125rem', color: 'var(--text-muted)' }}>
                        <strong>{inc.affectedSubscriberCount}</strong> student(s) affected
                      </div>

                      <button
                        onClick={() => handleToggleIncidentSub(inc.id)}
                        className={`btn btn-sm ${isSubscribed ? 'btn-navy' : 'btn-outline'}`}
                      >
                        {isSubscribed ? '✓ Subscribed (I Am Affected)' : '+ I Am Affected Too'}
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        )}

        {/* TAB 4: ACADEMIC NOTES & GROUNDED ASSISTANT */}
        {activeTab === 'academics' && (
          <div style={{ maxWidth: '860px', margin: '0 auto' }}>
            <div style={{ marginBottom: '24px' }}>
              <h2 style={{ fontSize: '1.35rem', fontWeight: '800', color: 'var(--primary-navy)' }}>
                Academic Notes & Syllabus Retrieval Assistant
              </h2>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>
                Course documents scoped to {currentUser?.departmentCode || 'AIML'} Department. Ask questions grounded strictly in authorized curriculum PDFs.
              </p>
            </div>

            {/* Q&A Assistant Search Box */}
            <div className="card" style={{ marginBottom: '24px', backgroundColor: '#f0fdfa', border: '1px solid #99f6e4' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '10px' }}>
                <BookOpen size={18} color="#0d9488" />
                <h3 style={{ fontSize: '1rem', fontWeight: '800', color: 'var(--primary-navy)' }}>
                  Ask Curriculum Assistant (Cites Page Numbers)
                </h3>
              </div>

              <form onSubmit={handleAskQuestion} style={{ display: 'flex', gap: '8px' }}>
                <input
                  type="text"
                  placeholder="e.g. What is the admissibility condition for A* search?"
                  className="form-input"
                  value={qaQuestion}
                  onChange={(e) => setQaQuestion(e.target.value)}
                />
                <button
                  type="submit"
                  disabled={qaLoading}
                  className="btn btn-primary"
                  style={{ minWidth: '120px' }}
                >
                  {qaLoading ? 'Searching...' : 'Ask Assistant'}
                </button>
              </form>

              {/* Sample Prompts */}
              <div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap', marginTop: '8px', fontSize: '0.75rem' }}>
                <span style={{ color: 'var(--text-muted)' }}>Try:</span>
                <button
                  type="button"
                  onClick={() => setQaQuestion('What is admissibility in A* search?')}
                  style={{ background: '#ffffff', border: '1px solid #cbd5e1', borderRadius: '4px', padding: '2px 6px', cursor: 'pointer', fontSize: '0.72rem' }}
                >
                  "Admissibility in A* search"
                </button>
                <button
                  type="button"
                  onClick={() => setQaQuestion('Explain minimax and alpha beta pruning')}
                  style={{ background: '#ffffff', border: '1px solid #cbd5e1', borderRadius: '4px', padding: '2px 6px', cursor: 'pointer', fontSize: '0.72rem' }}
                >
                  "Minimax and alpha-beta pruning"
                </button>
              </div>

              {/* QA Answer & Citations Box */}
              {qaResponse && (
                <div style={{
                  marginTop: '16px',
                  backgroundColor: '#ffffff',
                  border: '1px solid #cbd5e1',
                  borderRadius: 'var(--radius-md)',
                  padding: '16px'
                }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' }}>
                    <span style={{ fontWeight: '700', fontSize: '0.875rem', color: 'var(--primary-navy)' }}>
                      Assistant Finding
                    </span>
                    <span className="badge badge-acknowledged">
                      {qaResponse.aiEnabled ? 'AI Grounded' : 'Transparent Citation Mode'}
                    </span>
                  </div>

                  <p style={{ fontSize: '0.875rem', color: 'var(--text-primary)', lineHeight: 1.6, marginBottom: '14px', whiteSpace: 'pre-line' }}>
                    {qaResponse.answer}
                  </p>

                  {qaResponse.citations?.length > 0 && (
                    <div>
                      <div style={{ fontSize: '0.75rem', fontWeight: '700', color: 'var(--text-muted)', textTransform: 'uppercase', marginBottom: '6px' }}>
                        Retrieved Document Sources & Page Citations:
                      </div>
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                        {qaResponse.citations.map((c, i) => (
                          <div key={i} style={{ backgroundColor: 'var(--bg-subtle)', padding: '10px', borderRadius: '6px', fontSize: '0.8125rem' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', fontWeight: '700', color: 'var(--accent-teal)' }}>
                              <span>📄 {c.documentTitle}</span>
                              <span>Page {c.pageNumber}</span>
                            </div>
                            <div style={{ color: 'var(--text-secondary)', marginTop: '4px', fontStyle: 'italic' }}>
                              "{c.snippet}"
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}
                </div>
              )}
            </div>

            {/* Course Documents List */}
            <div className="card">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
                <h3 style={{ fontSize: '1rem', fontWeight: '700', color: 'var(--primary-navy)' }}>
                  Semester {acadSemester} Official Notes & Syllabi
                </h3>
                <select
                  className="form-select"
                  style={{ width: 'auto', padding: '6px 12px' }}
                  value={acadSemester}
                  onChange={(e) => {
                    setAcadSemester(Number(e.target.value));
                    api.getAcademicDocuments(Number(e.target.value)).then(res => setAcadDocs(res.data || []));
                  }}
                >
                  <option value={1}>Semester 1</option>
                  <option value={2}>Semester 2</option>
                  <option value={3}>Semester 3</option>
                  <option value={4}>Semester 4</option>
                  <option value={5}>Semester 5</option>
                  <option value={6}>Semester 6</option>
                  <option value={7}>Semester 7</option>
                  <option value={8}>Semester 8</option>
                </select>
              </div>

              {acadDocs.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '30px', color: 'var(--text-muted)', fontSize: '0.875rem' }}>
                  No documents cataloged for Semester {acadSemester}.
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                  {acadDocs.map((doc) => (
                    <div key={doc.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '12px', border: '1px solid var(--border-subtle)', borderRadius: '8px' }}>
                      <div>
                        <div style={{ fontWeight: '700', color: 'var(--primary-navy)', fontSize: '0.9375rem' }}>
                          {doc.title}
                        </div>
                        <div style={{ fontSize: '0.8125rem', color: 'var(--text-muted)' }}>
                          Subject: {doc.subjectCode} ({doc.subjectName}) • Pages: {doc.pageCount}
                        </div>
                      </div>

                      <a
                        href={`/api/academics/documents/${doc.id}/file`}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="btn btn-outline btn-sm"
                      >
                        <Eye size={14} />
                        View PDF
                      </a>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
