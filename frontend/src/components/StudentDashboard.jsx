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
  Check,
  X,
  Sparkles,
  UploadCloud,
  FileUp,
  Bot,
  Key,
  Copy,
  CheckCheck,
  Trash2,
  ExternalLink,
  FileText
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
    locationDetail: currentUser?.roomNumber ? `${currentUser?.hostelCode || 'Hostel'} Room ${currentUser.roomNumber}` : '',
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

  // Academic Hub & Groq Llama 3 AI State
  const [acadDocs, setAcadDocs] = useState([]);
  const [acadSemester, setAcadSemester] = useState(2); // Defaults to Semester 2
  const [qaQuestion, setQaQuestion] = useState('');
  const [qaResponse, setQaResponse] = useState(null);
  const [qaLoading, setQaLoading] = useState(false);
  const [groqApiKey, setGroqApiKey] = useState(() => localStorage.getItem('campussetu_groq_key') || '');
  const [groqModel, setGroqModel] = useState('llama-3.3-70b-versatile');
  const [showAiSettings, setShowAiSettings] = useState(false);
  const [keySavedToast, setKeySavedToast] = useState(false);
  const [selectedDocFilter, setSelectedDocFilter] = useState('');
  const [copiedResponse, setCopiedResponse] = useState(false);

  // Upload PDF State
  const [showUploadModal, setShowUploadModal] = useState(false);
  const [uploadPdfFile, setUploadPdfFile] = useState(null);
  const [uploadPdfTitle, setUploadPdfTitle] = useState('');
  const [uploadPdfSubjectCode, setUploadPdfSubjectCode] = useState('');
  const [uploadPdfSubjectName, setUploadPdfSubjectName] = useState('');
  const [uploadPdfSemester, setUploadPdfSemester] = useState(2);
  const [uploadingPdf, setUploadingPdf] = useState(false);
  const [uploadPdfMsg, setUploadPdfMsg] = useState('');
  const [uploadPdfError, setUploadPdfError] = useState('');

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
      }
    } catch (err) {
      setError(err.message);
    }
  }, []);

  const loadAcademicDocs = useCallback(async () => {
    try {
      const res = await api.getAcademicDocuments(acadSemester);
      if (res?.data) {
        setAcadDocs(res.data);
      }
    } catch (err) {
      console.warn('Academic docs load note:', err.message);
    }
  }, [acadSemester]);

  useEffect(() => {
    loadMyComplaints();
  }, [loadMyComplaints]);

  useEffect(() => {
    if (activeTab === 'shared-incidents') {
      loadSharedIncidents();
    }
    if (activeTab === 'academics') {
      loadAcademicDocs();
    }
  }, [activeTab, loadSharedIncidents, loadAcademicDocs]);

  // Real-time deterministic routing preview
  useEffect(() => {
    const fetchRoutingPreview = async () => {
      try {
        const preview = await api.previewComplaintRouting(
          formData.category,
          formData.locationType,
          formData.specificHostelCode,
          formData.specificDepartmentCode
        );
        if (preview?.data) {
          setRoutingPreview(preview.data);
        }
      } catch {
        // Fallback gracefully
      }
    };
    fetchRoutingPreview();
  }, [formData.category, formData.locationType, formData.specificHostelCode, formData.specificDepartmentCode]);

  const handleSubmitComplaint = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccess('');

    try {
      const res = await api.submitComplaint(formData);
      if (res?.data) {
        const newComplaint = res.data;

        // Upload attachment if selected
        if (selectedFile) {
          try {
            await api.uploadComplaintAttachment(newComplaint.id, selectedFile);
          } catch (fileErr) {
            console.error('Attachment upload failed:', fileErr);
          }
        }

        setSuccess(`Complaint submitted successfully! Tracking Number: ${newComplaint.trackingNumber}`);
        setSelectedFile(null);
        setFormData({
          title: '',
          category: 'HOSTEL_FACILITY',
          locationType: 'HOSTEL',
          specificHostelCode: currentUser?.hostelCode || 'ARYABHATT',
          specificDepartmentCode: currentUser?.departmentCode || 'AIML',
          locationDetail: currentUser?.roomNumber ? `${currentUser?.hostelCode || 'Hostel'} Room ${currentUser.roomNumber}` : '',
          urgency: 'MEDIUM',
          description: '',
          sensitive: false
        });
        await loadMyComplaints();
        setActiveTab('my-complaints');
      }
    } catch (err) {
      setError(err.message || 'Failed to submit complaint.');
    } finally {
      setLoading(false);
    }
  };

  const handleConfirmResolution = async (id) => {
    try {
      await api.confirmResolution(id, feedbackInput);
      setFeedbackInput('');
      setSuccess('Resolution confirmed. Ticket is now closed.');
      loadMyComplaints();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleRejectResolution = async (id) => {
    if (!rejectReasonInput.trim()) {
      setError('Please provide a specific reason for rejecting the repair.');
      return;
    }
    try {
      await api.rejectResolution(id, rejectReasonInput);
      setRejectReasonInput('');
      setSuccess('Resolution rejected. Ticket returned to in-progress queue.');
      loadMyComplaints();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleReopen = async (id) => {
    if (!reopenReasonInput.trim()) {
      setError('Please describe why the issue has recurred.');
      return;
    }
    try {
      await api.reopenComplaint(id, reopenReasonInput);
      setReopenReasonInput('');
      setSuccess('Ticket reopened.');
      loadMyComplaints();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleAddReply = async (id) => {
    if (!replyInput.trim()) return;
    try {
      await api.addComplaintReply(id, replyInput);
      setReplyInput('');
      loadComplaintDetail(id);
    } catch (err) {
      setError(err.message);
    }
  };

  const handleToggleSubscribe = async (incidentId) => {
    try {
      const res = await api.toggleSubscription(incidentId);
      if (res?.data) {
        const next = new Set(subscribedIncidents);
        if (res.data.subscribed) {
          next.add(incidentId);
        } else {
          next.delete(incidentId);
        }
        setSubscribedIncidents(next);
        loadSharedIncidents();
      }
    } catch (err) {
      setError(err.message);
    }
  };

  const handleSaveGroqKey = (key) => {
    setGroqApiKey(key);
    localStorage.setItem('campussetu_groq_key', key.trim());
    setKeySavedToast(true);
    setTimeout(() => setKeySavedToast(false), 2000);
  };

  const handleAskQuestion = async (e) => {
    if (e) e.preventDefault();
    if (!qaQuestion.trim()) return;

    setQaLoading(true);
    setQaResponse(null);
    setError('');

    try {
      const docId = selectedDocFilter ? Number(selectedDocFilter) : null;
      const res = await api.askAcademicQuestion(
        qaQuestion.trim(),
        groqApiKey.trim() || null,
        docId,
        groqModel
      );
      if (res?.data) {
        setQaResponse(res.data);
      }
    } catch (err) {
      setError(err.message || 'Error communicating with AI assistant.');
    } finally {
      setQaLoading(false);
    }
  };

  const handleUploadPdf = async (e) => {
    e.preventDefault();
    if (!uploadPdfFile) {
      setUploadPdfError('Please choose a PDF file.');
      return;
    }

    setUploadingPdf(true);
    setUploadPdfError('');
    setUploadPdfMsg('');

    try {
      const fData = new FormData();
      fData.append('file', uploadPdfFile);
      fData.append('title', uploadPdfTitle.trim() || uploadPdfFile.name.replace('.pdf', ''));
      fData.append('subjectCode', uploadPdfSubjectCode.trim() || 'CUSTOM');
      fData.append('subjectName', uploadPdfSubjectName.trim() || uploadPdfTitle.trim() || 'Student Uploaded Notes');
      fData.append('semester', uploadPdfSemester);
      fData.append('departmentCode', currentUser?.departmentCode || 'COMMON');

      const res = await api.uploadAcademicDocument(fData);
      if (res?.data) {
        setUploadPdfMsg('✅ Document indexed successfully with page extraction!');
        setUploadPdfFile(null);
        setUploadPdfTitle('');
        setUploadPdfSubjectCode('');
        setUploadPdfSubjectName('');
        await loadAcademicDocs();
        setTimeout(() => {
          setShowUploadModal(false);
          setUploadPdfMsg('');
        }, 1200);
      }
    } catch (err) {
      setUploadPdfError(err.message || 'Failed to upload and index PDF.');
    } finally {
      setUploadingPdf(false);
    }
  };

  const copyAnswerToClipboard = () => {
    if (!qaResponse?.answer) return;
    navigator.clipboard.writeText(qaResponse.answer);
    setCopiedResponse(true);
    setTimeout(() => setCopiedResponse(false), 2000);
  };

  return (
    <div className="animate-fade-in" style={{ padding: '24px 0 60px' }}>
      <div className="container">
        {/* Welcome Header */}
        <div style={{
          backgroundColor: '#ffffff',
          borderRadius: 'var(--radius-lg)',
          padding: '24px',
          marginBottom: '24px',
          border: '1px solid var(--border-subtle)',
          boxShadow: 'var(--shadow-sm)',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '16px'
        }}>
          <div>
            <div style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', color: 'var(--kiet-crimson)', fontSize: '0.75rem', fontWeight: '800', textTransform: 'uppercase', marginBottom: '4px' }}>
              <span>Student Academic & Grievance Portal</span>
              <span>•</span>
              <span style={{ color: '#0b2545' }}>KIET Group of Institutions</span>
            </div>
            <h1 style={{ fontSize: '1.5rem', fontWeight: '800', color: 'var(--primary-navy)' }}>
              Welcome back, {currentUser?.fullName || 'Student'}
            </h1>
            <div style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', marginTop: '2px' }}>
              Roll: <strong>{currentUser?.studentRollNumber || 'N/A'}</strong> • Branch: <strong>{currentUser?.departmentCode || 'AIML'}</strong> • Residence: <strong>{currentUser?.hostelCode || 'Day Scholar'} {currentUser?.roomNumber ? `(Room ${currentUser.roomNumber})` : ''}</strong>
            </div>
          </div>

          <div style={{ display: 'flex', gap: '10px' }}>
            <button
              onClick={() => setActiveTab('submit')}
              className="btn btn-primary"
            >
              <PlusCircle size={16} />
              Lodge Grievance
            </button>
            <button
              onClick={() => setActiveTab('academics')}
              className="btn btn-navy"
            >
              <Bot size={16} color="#f8b81f" />
              AI Study Assistant
            </button>
          </div>
        </div>

        {/* Status Alerts */}
        {error && (
          <div style={{
            backgroundColor: '#fee2e2',
            border: '1px solid #fecaca',
            color: '#dc2626',
            padding: '12px 16px',
            borderRadius: 'var(--radius-md)',
            marginBottom: '18px',
            fontSize: '0.875rem',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <AlertTriangle size={18} />
              <span>{error}</span>
            </div>
            <button onClick={() => setError('')} style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#dc2626' }}>
              <X size={16} />
            </button>
          </div>
        )}

        {success && (
          <div style={{
            backgroundColor: '#f0fdf4',
            border: '1px solid #bbf7d0',
            color: '#16a34a',
            padding: '12px 16px',
            borderRadius: 'var(--radius-md)',
            marginBottom: '18px',
            fontSize: '0.875rem',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <CheckCircle2 size={18} />
              <span>{success}</span>
            </div>
            <button onClick={() => setSuccess('')} style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#16a34a' }}>
              <X size={16} />
            </button>
          </div>
        )}

        {/* Real-time Status Metric Cards */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
          gap: '14px',
          marginBottom: '22px'
        }}>
          <div
            onClick={() => setActiveTab('my-complaints')}
            className="card card-interactive"
            style={{ padding: '16px 20px', cursor: 'pointer', borderLeft: '4px solid var(--primary-navy)' }}
          >
            <div style={{ fontSize: '0.75rem', fontWeight: '700', color: 'var(--text-muted)', textTransform: 'uppercase' }}>
              My Filed Tickets
            </div>
            <div style={{ fontSize: '1.75rem', fontWeight: '800', color: 'var(--primary-navy)', marginTop: '2px' }}>
              {complaints.length}
            </div>
          </div>

          <div
            onClick={() => setActiveTab('my-complaints')}
            className="card card-interactive"
            style={{ padding: '16px 20px', cursor: 'pointer', borderLeft: '4px solid var(--accent-blue)' }}
          >
            <div style={{ fontSize: '0.75rem', fontWeight: '700', color: 'var(--text-muted)', textTransform: 'uppercase' }}>
              Active In-Service
            </div>
            <div style={{ fontSize: '1.75rem', fontWeight: '800', color: 'var(--accent-blue)', marginTop: '2px' }}>
              {complaints.filter(c => c.status === 'IN_PROGRESS' || c.status === 'ACKNOWLEDGED').length}
            </div>
          </div>

          <div
            onClick={() => setActiveTab('my-complaints')}
            className="card card-interactive"
            style={{
              padding: '16px 20px',
              cursor: 'pointer',
              borderLeft: '4px solid var(--kiet-gold)',
              backgroundColor: complaints.some(c => c.status === 'RESOLUTION_PROPOSED') ? '#fffdf0' : '#ffffff'
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div style={{ fontSize: '0.75rem', fontWeight: '700', color: 'var(--text-muted)', textTransform: 'uppercase' }}>
                Inspection Needed
              </div>
              {complaints.some(c => c.status === 'RESOLUTION_PROPOSED') && (
                <span className="pulse-dot" style={{ backgroundColor: '#d97706' }}></span>
              )}
            </div>
            <div style={{ fontSize: '1.75rem', fontWeight: '800', color: '#b45309', marginTop: '2px' }}>
              {complaints.filter(c => c.status === 'RESOLUTION_PROPOSED').length}
            </div>
          </div>

          <div
            onClick={() => setActiveTab('academics')}
            className="card card-interactive"
            style={{ padding: '16px 20px', cursor: 'pointer', borderLeft: '4px solid var(--accent-teal)' }}
          >
            <div style={{ fontSize: '0.75rem', fontWeight: '700', color: 'var(--text-muted)', textTransform: 'uppercase' }}>
              AI Study Assistant
            </div>
            <div style={{ fontSize: '1.1rem', fontWeight: '800', color: 'var(--accent-teal)', marginTop: '6px', display: 'flex', alignItems: 'center', gap: '6px' }}>
              <Sparkles size={18} color="#f59e0b" />
              Llama 3 Ready
            </div>
          </div>
        </div>

        {/* Tab Selection Navigation */}
        <div style={{ display: 'flex', gap: '8px', marginBottom: '20px', borderBottom: '1px solid var(--border-subtle)', paddingBottom: '12px', flexWrap: 'wrap' }}>
          <button
            onClick={() => setActiveTab('my-complaints')}
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
            style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
          >
            <Sparkles size={14} color="#f8b81f" />
            AI Study Assistant & Notes
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

            <form onSubmit={handleSubmitComplaint} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div className="form-group">
                <label className="form-label">Subject / Short Title *</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Fan speed regulator burnt out; sparking intermittently"
                  className="form-input"
                  value={formData.title}
                  onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                />
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '14px' }}>
                <div className="form-group">
                  <label className="form-label">Issue Category *</label>
                  <select
                    className="form-select"
                    value={formData.category}
                    onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                  >
                    <option value="HOSTEL_FACILITY">Hostel Fixture / Room Electrical</option>
                    <option value="CLASSROOM_EQUIPMENT">Classroom / Lab Equipment</option>
                    <option value="IT_AND_NETWORK">Campus Wi-Fi & IT NOC</option>
                    <option value="MESS_AND_DINING">Mess & Dining Facility</option>
                    <option value="CAMPUS_MAINTENANCE">Campus Grounds & Sanitation</option>
                    <option value="TRANSPORT">College Fleet & Bus Transit</option>
                    <option value="DISCIPLINARY">Proctorial / Campus Safety</option>
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
                    <option value="DEPARTMENT">Academic Department / Classroom</option>
                    <option value="CAMPUS_SERVICE">Central Campus / General Grounds</option>
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
                      <optgroup label="Boys Hostels (Allocated by Year)">
                        <option value="CV_RAMAN">C.V. Raman Hostel (1st Year Boys)</option>
                        <option value="CHANDRAGUPT">Chandragupt Hostel (1st Year Boys)</option>
                        <option value="TAGORE">Tagore Hostel (2nd Year Boys)</option>
                        <option value="ARYABHATT">Aryabhatt Hostel (2nd & 3rd Year Boys)</option>
                        <option value="VIVEKANAND">Vivekanand Hostel (2nd & 3rd Year Boys)</option>
                        <option value="CHANAKYA">Chanakya Hostel (4th Year Boys)</option>
                      </optgroup>
                      <optgroup label="Girls Hostels (Allocated by Year)">
                        <option value="GARGI">Gargi Hostel (1st Year Girls)</option>
                        <option value="SAROJINI">Sarojini Hostel (2nd & 3rd Year Girls)</option>
                        <option value="SARASWATI">Saraswati Hostel (2nd, 3rd & 4th Year Girls)</option>
                      </optgroup>
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
                    placeholder="e.g. Room 105, 1st Floor or Room 204"
                    className="form-input"
                    value={formData.locationDetail}
                    onChange={(e) => setFormData({ ...formData, locationDetail: e.target.value })}
                  />
                </div>

                <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                  <label className="form-label">Urgency Level</label>
                  <select
                    className="form-select"
                    value={formData.urgency}
                    onChange={(e) => setFormData({ ...formData, urgency: e.target.value })}
                  >
                    <option value="LOW">Low (Cosmetic / routine)</option>
                    <option value="MEDIUM">Medium (Standard fixture malfunction)</option>
                    <option value="HIGH">High (Disrupts study / lab session)</option>
                    <option value="EMERGENCY">Emergency (Safety / Water / Electrical Hazard)</option>
                  </select>
                </div>
              </div>

              <div className="form-group">
                <label className="form-label">Full Description *</label>
                <textarea
                  required
                  rows={4}
                  placeholder="Detail the failure condition, when it started, and any symptoms..."
                  className="form-textarea"
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                />
              </div>

              {/* File Attachment Input */}
              <div className="form-group">
                <label className="form-label">Evidence / Photo Attachment (Optional, PDF/JPG/PNG)</label>
                <input
                  type="file"
                  accept="image/*,.pdf"
                  className="form-input"
                  onChange={(e) => setSelectedFile(e.target.files?.[0] || null)}
                />
                {selectedFile && (
                  <div style={{ fontSize: '0.75rem', color: 'var(--accent-teal)', marginTop: '4px' }}>
                    📎 Ready to upload: {selectedFile.name} ({(selectedFile.size / 1024).toFixed(1)} KB)
                  </div>
                )}
              </div>

              {/* Deterministic Routing Transparency Preview */}
              {routingPreview && (
                <div style={{
                  backgroundColor: 'var(--bg-subtle)',
                  border: '1px solid var(--border-subtle)',
                  borderRadius: 'var(--radius-md)',
                  padding: '14px',
                  fontSize: '0.8125rem'
                }}>
                  <div style={{ fontWeight: '700', color: 'var(--primary-navy)', marginBottom: '4px', display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Building size={16} />
                    Verified Routing Destination
                  </div>
                  <div style={{ color: 'var(--text-secondary)' }}>
                    Target Queue: <code>{routingPreview.assignedQueueCode}</code> • Assigned Desk: <strong>{routingPreview.assignedOffice}</strong>
                  </div>
                  <div style={{ color: 'var(--text-muted)', fontSize: '0.75rem', marginTop: '4px' }}>
                    Escalates to: {routingPreview.escalationTargetQueueCode || 'Central Directorate'}
                  </div>
                </div>
              )}

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', marginTop: '12px' }}>
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
          <div className="grid-complaints">
            {/* List View Column */}
            <div style={{ minWidth: 0 }}>
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

            {/* Detail View Panel Column */}
            {selectedComplaint && complaintDetail && (
              <div className="card" style={{ padding: '24px', display: 'flex', flexDirection: 'column', gap: '18px', minWidth: 0 }}>
                {/* Header with Title and pinned Close button */}
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '12px' }}>
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <span className={`badge badge-${complaintDetail.complaint.status.toLowerCase()}`} style={{ marginBottom: '6px' }}>
                      {complaintDetail.complaint.status.replace('_', ' ')}
                    </span>
                    <h3 style={{ fontSize: '1.2rem', fontWeight: '800', color: 'var(--primary-navy)', lineHeight: 1.3, marginTop: '4px' }}>
                      {complaintDetail.complaint.title}
                    </h3>
                    <div style={{ fontSize: '0.8125rem', color: 'var(--text-muted)', marginTop: '4px' }}>
                      Ticket #{complaintDetail.complaint.trackingNumber} • {complaintDetail.complaint.locationDetail}
                    </div>
                  </div>
                  <button
                    onClick={() => setSelectedComplaint(null)}
                    className="btn btn-outline btn-sm"
                    style={{ flexShrink: 0 }}
                  >
                    <X size={14} />
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
                      <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                        <input
                          type="text"
                          placeholder="Optional feedback on repair quality..."
                          className="form-input"
                          style={{ flex: 1, minWidth: '180px' }}
                          value={feedbackInput}
                          onChange={(e) => setFeedbackInput(e.target.value)}
                        />
                        <button
                          onClick={() => handleConfirmResolution(complaintDetail.complaint.id)}
                          className="btn btn-primary"
                          style={{ whiteSpace: 'nowrap', flexShrink: 0 }}
                        >
                          <Check size={16} />
                          Confirm & Close
                        </button>
                      </div>

                      <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                        <input
                          type="text"
                          placeholder="If issue persists, explain reason to reject..."
                          className="form-input"
                          style={{ flex: 1, minWidth: '180px' }}
                          value={rejectReasonInput}
                          onChange={(e) => setRejectReasonInput(e.target.value)}
                        />
                        <button
                          onClick={() => handleRejectResolution(complaintDetail.complaint.id)}
                          className="btn btn-danger"
                          style={{ whiteSpace: 'nowrap', flexShrink: 0 }}
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
                    <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                      <input
                        type="text"
                        placeholder="Documented reason to reopen..."
                        className="form-input"
                        style={{ flex: 1, minWidth: '180px' }}
                        value={reopenReasonInput}
                        onChange={(e) => setReopenReasonInput(e.target.value)}
                      />
                      <button
                        onClick={() => handleReopen(complaintDetail.complaint.id)}
                        className="btn btn-outline btn-sm"
                        style={{ whiteSpace: 'nowrap', flexShrink: 0 }}
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

                  <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', maxHeight: '200px', overflowY: 'auto', marginBottom: '10px', paddingRight: '4px' }}>
                    {complaintDetail.notes?.length === 0 ? (
                      <div style={{ color: 'var(--text-muted)', fontSize: '0.8125rem' }}>No comments yet.</div>
                    ) : (
                      complaintDetail.notes.map((n) => (
                        <div key={n.id} style={{ padding: '8px 12px', backgroundColor: 'var(--bg-subtle)', borderRadius: '6px', fontSize: '0.8125rem', wordBreak: 'break-word' }}>
                          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontWeight: '600', marginBottom: '2px', gap: '8px' }}>
                            <span style={{ color: 'var(--primary-navy)' }}>{n.author?.fullName || 'Staff Reply'}</span>
                            <span style={{ fontSize: '0.7rem', color: 'var(--text-muted)', whiteSpace: 'nowrap', flexShrink: 0 }}>
                              {new Date(n.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                            </span>
                          </div>
                          <div>{n.content}</div>
                        </div>
                      ))
                    )}
                  </div>

                  {/* Reply Input Bar with flexShrink so send button is never clipped */}
                  <div style={{ display: 'flex', gap: '8px', alignItems: 'center', width: '100%' }}>
                    <input
                      type="text"
                      placeholder="Type a follow-up reply for maintenance..."
                      className="form-input"
                      style={{ flex: 1, minWidth: 0 }}
                      value={replyInput}
                      onChange={(e) => setReplyInput(e.target.value)}
                      onKeyDown={(e) => { if (e.key === 'Enter') handleAddReply(complaintDetail.complaint.id); }}
                    />
                    <button
                      onClick={() => handleAddReply(complaintDetail.complaint.id)}
                      className="btn btn-navy"
                      style={{ flexShrink: 0, padding: '10px 14px' }}
                      title="Send reply"
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
                      <span className="badge badge-acknowledged">{inc.incidentNumber}</span>
                      <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                        Updated: {new Date(inc.updatedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </span>
                    </div>

                    <h3 style={{ fontSize: '1.125rem', fontWeight: '700', color: 'var(--primary-navy)', marginBottom: '6px' }}>
                      {inc.title}
                    </h3>
                    <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', marginBottom: '12px' }}>
                      {inc.sanitizedDescription}
                    </p>

                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '10px', backgroundColor: 'var(--bg-subtle)', padding: '10px 14px', borderRadius: 'var(--radius-md)' }}>
                      <div style={{ fontSize: '0.8125rem', color: 'var(--text-muted)' }}>
                        Location: <strong>{inc.generalLocation}</strong> • Affected: <strong>{inc.affectedSubscriberCount} students</strong>
                      </div>
                      <button
                        onClick={() => handleToggleSubscribe(inc.id)}
                        className={`btn btn-sm ${isSubscribed ? 'btn-primary' : 'btn-outline'}`}
                      >
                        {isSubscribed ? '✓ Subscribed' : 'I Am Affected Too'}
                      </button>
                    </div>

                    {inc.publicProgressUpdates && (
                      <div style={{ marginTop: '12px', fontSize: '0.8125rem', color: 'var(--accent-teal)', backgroundColor: 'var(--accent-teal-subtle)', padding: '8px 12px', borderRadius: '6px' }}>
                        📢 <strong>Latest Status Update:</strong> {inc.publicProgressUpdates}
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          </div>
        )}

        {/* TAB 4: ADVANCED AI ACADEMIC & STUDY ASSISTANT (GROQ LLAMA 3) */}
        {activeTab === 'academics' && (
          <div style={{ maxWidth: '920px', margin: '0 auto' }}>
            {/* Header with Title and AI Config Button */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '12px', marginBottom: '20px' }}>
              <div>
                <div style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', color: 'var(--kiet-crimson)', fontSize: '0.75rem', fontWeight: '800', textTransform: 'uppercase', marginBottom: '4px' }}>
                  <Sparkles size={14} color="#f8b81f" />
                  <span>Powered by Groq AI & Llama 3</span>
                </div>
                <h2 style={{ fontSize: '1.4rem', fontWeight: '800', color: 'var(--primary-navy)' }}>
                  AI Study Assistant & Document Intelligence
                </h2>
                <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>
                  Upload your own course PDFs, lecture decks, or question sheets, and ask deep questions grounded strictly in your curriculum.
                </p>
              </div>

              <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                <button
                  onClick={() => setShowUploadModal(true)}
                  className="btn btn-primary btn-sm"
                  style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
                >
                  <FileUp size={14} />
                  Upload Your PDF
                </button>
                <button
                  onClick={() => setShowAiSettings(!showAiSettings)}
                  className="btn btn-outline btn-sm"
                  style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
                >
                  <Key size={14} />
                  {groqApiKey ? 'Groq Key (Configured)' : 'Configure Groq AI'}
                </button>
              </div>
            </div>

            {/* Collapsible Groq AI Configuration Panel */}
            {showAiSettings && (
              <div className="card" style={{
                marginBottom: '20px',
                backgroundColor: '#fffdf0',
                border: '1px solid #f8b81f',
                padding: '18px'
              }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontWeight: '800', color: '#0b2545', fontSize: '0.9375rem' }}>
                    <Bot size={18} color="#f8b81f" />
                    Groq AI & Llama 3 Engine Settings
                  </div>
                  <button onClick={() => setShowAiSettings(false)} style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#64748b' }}>
                    <X size={16} />
                  </button>
                </div>
                <p style={{ fontSize: '0.8125rem', color: '#475569', marginBottom: '12px' }}>
                  Groq provides ultra-fast inference for Meta Llama 3 models. You can use our built-in academic synthesizer out of the box, or paste your free Groq API key below to activate live LLM reasoning.
                </p>

                <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap', alignItems: 'flex-end' }}>
                  <div style={{ flex: 1, minWidth: '220px' }}>
                    <label className="form-label" style={{ fontSize: '0.75rem' }}>
                      Groq API Key (Stored securely in browser localStorage)
                    </label>
                    <input
                      type="password"
                      placeholder="gsk_..."
                      className="form-input"
                      style={{ fontSize: '0.875rem' }}
                      value={groqApiKey}
                      onChange={(e) => handleSaveGroqKey(e.target.value)}
                    />
                  </div>
                  <div style={{ minWidth: '180px' }}>
                    <label className="form-label" style={{ fontSize: '0.75rem' }}>Model Architecture</label>
                    <select
                      className="form-select"
                      style={{ fontSize: '0.875rem' }}
                      value={groqModel}
                      onChange={(e) => setGroqModel(e.target.value)}
                    >
                      <option value="llama-3.3-70b-versatile">Llama 3.3 70B Versatile (Recommended)</option>
                      <option value="llama-3.1-8b-instant">Llama 3.1 8B Instant (Ultra Fast)</option>
                      <option value="llama3-70b-8192">Llama 3 70B</option>
                    </select>
                  </div>
                  <a
                    href="https://console.groq.com/keys"
                    target="_blank"
                    rel="noopener noreferrer"
                    className="btn btn-outline btn-sm"
                    style={{ whiteSpace: 'nowrap', display: 'flex', alignItems: 'center', gap: '4px' }}
                  >
                    <span>Get Free Groq Key</span>
                    <ExternalLink size={12} />
                  </a>
                </div>

                {keySavedToast && (
                  <div style={{ fontSize: '0.75rem', color: '#16a34a', fontWeight: '700', marginTop: '8px' }}>
                    ✓ Groq key updated and saved for this session!
                  </div>
                )}
              </div>
            )}

            {/* Upload PDF Modal */}
            {showUploadModal && (
              <div className="card" style={{
                marginBottom: '20px',
                backgroundColor: '#ffffff',
                border: '2px dashed var(--accent-teal)',
                padding: '24px'
              }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontWeight: '800', color: 'var(--primary-navy)', fontSize: '1rem' }}>
                    <UploadCloud size={20} color="var(--accent-teal)" />
                    Upload & Index Course PDF for AI Assistance
                  </div>
                  <button onClick={() => setShowUploadModal(false)} style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#64748b' }}>
                    <X size={18} />
                  </button>
                </div>

                <form onSubmit={handleUploadPdf} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
                  <div className="form-group" style={{ margin: 0 }}>
                    <label className="form-label">Select PDF Document *</label>
                    <input
                      type="file"
                      accept=".pdf"
                      required
                      className="form-input"
                      onChange={(e) => {
                        const file = e.target.files?.[0];
                        setUploadPdfFile(file || null);
                        if (file && !uploadPdfTitle) {
                          setUploadPdfTitle(file.name.replace('.pdf', ''));
                        }
                      }}
                    />
                  </div>

                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '10px' }}>
                    <div className="form-group" style={{ margin: 0 }}>
                      <label className="form-label">Document Title *</label>
                      <input
                        type="text"
                        required
                        placeholder="e.g. Unit 2 Tree Traversals & BST"
                        className="form-input"
                        value={uploadPdfTitle}
                        onChange={(e) => setUploadPdfTitle(e.target.value)}
                      />
                    </div>

                    <div className="form-group" style={{ margin: 0 }}>
                      <label className="form-label">Subject Code</label>
                      <input
                        type="text"
                        placeholder="e.g. BCS-201 or BAS-203"
                        className="form-input"
                        value={uploadPdfSubjectCode}
                        onChange={(e) => setUploadPdfSubjectCode(e.target.value)}
                      />
                    </div>

                    <div className="form-group" style={{ margin: 0 }}>
                      <label className="form-label">Semester</label>
                      <select
                        className="form-select"
                        value={uploadPdfSemester}
                        onChange={(e) => setUploadPdfSemester(Number(e.target.value))}
                      >
                        {[1, 2, 3, 4, 5, 6, 7, 8].map((s) => (
                          <option key={s} value={s}>Semester {s}</option>
                        ))}
                      </select>
                    </div>
                  </div>

                  {uploadPdfError && (
                    <div style={{ color: '#dc2626', fontSize: '0.8125rem' }}>{uploadPdfError}</div>
                  )}

                  {uploadPdfMsg && (
                    <div style={{ color: '#16a34a', fontSize: '0.8125rem', fontWeight: '700' }}>{uploadPdfMsg}</div>
                  )}

                  <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px', marginTop: '8px' }}>
                    <button
                      type="button"
                      onClick={() => setShowUploadModal(false)}
                      className="btn btn-outline btn-sm"
                    >
                      Cancel
                    </button>
                    <button
                      type="submit"
                      disabled={uploadingPdf}
                      className="btn btn-primary btn-sm"
                      style={{ minWidth: '120px' }}
                    >
                      {uploadingPdf ? 'Extracting Pages...' : 'Upload & Index'}
                    </button>
                  </div>
                </form>
              </div>
            )}

            {/* Q&A Assistant Search Box */}
            <div className="card" style={{ marginBottom: '24px', backgroundColor: '#ffffff', border: '1px solid var(--border-subtle)', boxShadow: 'var(--shadow-sm)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px', flexWrap: 'wrap', gap: '8px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <Bot size={20} color="var(--primary-navy)" />
                  <h3 style={{ fontSize: '1rem', fontWeight: '800', color: 'var(--primary-navy)' }}>
                    Ask AI Assistant
                  </h3>
                </div>

                {/* Scope selector */}
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.8125rem' }}>
                  <span style={{ color: 'var(--text-muted)' }}>Context Scope:</span>
                  <select
                    className="form-select"
                    style={{ padding: '4px 10px', fontSize: '0.8125rem', width: 'auto' }}
                    value={selectedDocFilter}
                    onChange={(e) => setSelectedDocFilter(e.target.value)}
                  >
                    <option value="">All Authorized Curriculum & Uploads</option>
                    {acadDocs.map((d) => (
                      <option key={d.id} value={d.id}>
                        {d.title.length > 35 ? d.title.substring(0, 35) + '...' : d.title}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <form onSubmit={handleAskQuestion} style={{ display: 'flex', gap: '8px', width: '100%' }}>
                <input
                  type="text"
                  placeholder="e.g. What is the admissibility condition for A* search? Or ask about your uploaded notes..."
                  className="form-input"
                  style={{ flex: 1, minWidth: 0 }}
                  value={qaQuestion}
                  onChange={(e) => setQaQuestion(e.target.value)}
                />
                <button
                  type="submit"
                  disabled={qaLoading}
                  className="btn btn-navy"
                  style={{ minWidth: '130px', flexShrink: 0, display: 'flex', alignItems: 'center', gap: '6px' }}
                >
                  <Sparkles size={16} color="#f8b81f" />
                  {qaLoading ? 'Analyzing...' : 'Ask Llama 3'}
                </button>
              </form>

              {/* Sample Prompts */}
              <div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap', marginTop: '10px', fontSize: '0.75rem', alignItems: 'center' }}>
                <span style={{ color: 'var(--text-muted)' }}>Try:</span>
                <button
                  type="button"
                  onClick={() => { setQaQuestion('What is admissibility in A* search?'); }}
                  style={{ background: 'var(--bg-subtle)', border: '1px solid #cbd5e1', borderRadius: '4px', padding: '3px 8px', cursor: 'pointer', fontSize: '0.75rem', color: '#0b2545' }}
                >
                  "Admissibility in A* search"
                </button>
                <button
                  type="button"
                  onClick={() => { setQaQuestion('Explain stack operations and their time complexity'); }}
                  style={{ background: 'var(--bg-subtle)', border: '1px solid #cbd5e1', borderRadius: '4px', padding: '3px 8px', cursor: 'pointer', fontSize: '0.75rem', color: '#0b2545' }}
                >
                  "Stack operations (BCS-201)"
                </button>
                <button
                  type="button"
                  onClick={() => { setQaQuestion('What are the laboratory evaluation criteria and marks distribution?'); }}
                  style={{ background: 'var(--bg-subtle)', border: '1px solid #cbd5e1', borderRadius: '4px', padding: '3px 8px', cursor: 'pointer', fontSize: '0.75rem', color: '#0b2545' }}
                >
                  "Lab marks evaluation criteria"
                </button>
                <button
                  type="button"
                  onClick={() => { setQaQuestion('Summarize the main algorithms and formulas in this syllabus'); }}
                  style={{ background: 'var(--bg-subtle)', border: '1px solid #cbd5e1', borderRadius: '4px', padding: '3px 8px', cursor: 'pointer', fontSize: '0.75rem', color: '#0b2545' }}
                >
                  "Summarize key formulas"
                </button>
              </div>

              {/* QA Answer & Citations Box */}
              {qaResponse && (
                <div style={{
                  marginTop: '18px',
                  backgroundColor: '#f8fafc',
                  border: '1px solid #cbd5e1',
                  borderRadius: 'var(--radius-md)',
                  padding: '18px',
                  wordBreak: 'break-word'
                }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px', flexWrap: 'wrap', gap: '8px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <Bot size={18} color="var(--primary-navy)" />
                      <span style={{ fontWeight: '800', fontSize: '0.9375rem', color: 'var(--primary-navy)' }}>
                        AI Assistant Analysis
                      </span>
                      <span style={{
                        backgroundColor: '#fffbeb',
                        border: '1px solid #f8b81f',
                        color: '#0b2545',
                        fontSize: '0.7rem',
                        fontWeight: '800',
                        padding: '2px 8px',
                        borderRadius: '4px'
                      }}>
                        ⚡ {qaResponse.provider || 'Groq Llama 3'}
                      </span>
                    </div>

                    <button
                      type="button"
                      onClick={copyAnswerToClipboard}
                      className="btn btn-outline btn-sm"
                      style={{ padding: '4px 8px', fontSize: '0.75rem' }}
                    >
                      {copiedResponse ? <CheckCheck size={14} color="#16a34a" /> : <Copy size={14} />}
                      <span>{copiedResponse ? 'Copied!' : 'Copy Answer'}</span>
                    </button>
                  </div>

                  <div style={{
                    fontSize: '0.875rem',
                    color: 'var(--text-primary)',
                    lineHeight: 1.6,
                    marginBottom: '14px',
                    whiteSpace: 'pre-line',
                    backgroundColor: '#ffffff',
                    padding: '14px',
                    borderRadius: '8px',
                    border: '1px solid #e2e8f0'
                  }}>
                    {qaResponse.answer}
                  </div>

                  {qaResponse.citations?.length > 0 && (
                    <div>
                      <div style={{ fontSize: '0.75rem', fontWeight: '800', color: 'var(--primary-navy)', textTransform: 'uppercase', marginBottom: '8px', letterSpacing: '0.04em' }}>
                        📑 Retrieved Document Sources & Page Citations:
                      </div>
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                        {qaResponse.citations.map((c, i) => (
                          <div key={i} style={{ backgroundColor: '#ffffff', border: '1px solid #e2e8f0', padding: '10px 12px', borderRadius: '6px', fontSize: '0.8125rem' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontWeight: '700', color: 'var(--accent-teal)', marginBottom: '4px' }}>
                              <span style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                                <FileText size={14} />
                                {c.documentTitle}
                              </span>
                              <span style={{ backgroundColor: 'var(--bg-subtle)', padding: '2px 8px', borderRadius: '4px', fontSize: '0.72rem', color: 'var(--primary-navy)' }}>
                                Page {c.pageNumber}
                              </span>
                            </div>
                            <div style={{ color: 'var(--text-secondary)', fontStyle: 'italic', fontSize: '0.8rem' }}>
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
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px', flexWrap: 'wrap', gap: '8px' }}>
                <div>
                  <h3 style={{ fontSize: '1.05rem', fontWeight: '800', color: 'var(--primary-navy)' }}>
                    Cataloged Course Documents & Lecture Guides
                  </h3>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                    Showing official curriculum materials and your personal uploads.
                  </div>
                </div>

                <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                  <select
                    className="form-select"
                    style={{ width: 'auto', padding: '6px 12px', fontSize: '0.8125rem' }}
                    value={acadSemester}
                    onChange={(e) => {
                      setAcadSemester(Number(e.target.value));
                      api.getAcademicDocuments(Number(e.target.value)).then(res => setAcadDocs(res.data || []));
                    }}
                  >
                    {[1, 2, 3, 4, 5, 6, 7, 8].map((s) => (
                      <option key={s} value={s}>Semester {s}</option>
                    ))}
                  </select>

                  <button
                    onClick={() => setShowUploadModal(true)}
                    className="btn btn-primary btn-sm"
                    style={{ whiteSpace: 'nowrap' }}
                  >
                    <PlusCircle size={14} />
                    Add PDF
                  </button>
                </div>
              </div>

              {acadDocs.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '36px 20px', color: 'var(--text-muted)', fontSize: '0.875rem' }}>
                  <FileText size={32} style={{ margin: '0 auto 10px', color: '#cbd5e1' }} />
                  <div style={{ fontWeight: '700', marginBottom: '4px' }}>No documents cataloged for Semester {acadSemester}</div>
                  <p style={{ fontSize: '0.8125rem', marginBottom: '12px' }}>
                    Be the first to upload lecture slides, question papers, or notes for this semester!
                  </p>
                  <button
                    onClick={() => { setUploadPdfSemester(acadSemester); setShowUploadModal(true); }}
                    className="btn btn-outline btn-sm"
                  >
                    Upload PDF for Semester {acadSemester}
                  </button>
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                  {acadDocs.map((doc) => (
                    <div
                      key={doc.id}
                      style={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        padding: '14px 16px',
                        border: '1px solid var(--border-subtle)',
                        borderRadius: 'var(--radius-md)',
                        backgroundColor: selectedDocFilter === String(doc.id) ? 'var(--accent-teal-subtle)' : '#ffffff',
                        transition: 'all 0.15s ease'
                      }}
                    >
                      <div style={{ flex: 1, minWidth: 0, paddingRight: '12px' }}>
                        <div style={{ fontWeight: '700', color: 'var(--primary-navy)', fontSize: '0.9375rem', marginBottom: '2px' }}>
                          {doc.title}
                        </div>
                        <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>
                          Subject: <strong>{doc.subjectCode}</strong> ({doc.subjectName}) • Semester: <strong>{doc.semester}</strong> • Pages: <strong>{doc.pageCount}</strong> • Size: <strong>{(doc.fileSizeBytes / 1024).toFixed(1)} KB</strong>
                        </div>
                      </div>

                      <div style={{ display: 'flex', gap: '8px', flexShrink: 0 }}>
                        <button
                          type="button"
                          onClick={() => {
                            setSelectedDocFilter(String(doc.id));
                            setQaQuestion(`Explain the key concepts and summaries in ${doc.title}`);
                            window.scrollTo({ top: 300, behavior: 'smooth' });
                          }}
                          className="btn btn-sm btn-navy"
                          style={{ fontSize: '0.75rem', padding: '6px 10px' }}
                          title="Ask AI specifically about this document"
                        >
                          <Sparkles size={12} color="#f8b81f" />
                          <span>Ask AI</span>
                        </button>
                        <a
                          href={`/api/academics/documents/${doc.id}/file`}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="btn btn-outline btn-sm"
                          style={{ fontSize: '0.75rem', padding: '6px 10px' }}
                        >
                          <Eye size={12} />
                          <span>PDF</span>
                        </a>
                      </div>
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
