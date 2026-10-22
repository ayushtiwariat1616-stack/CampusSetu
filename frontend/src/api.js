// CampusSetu API Client with relative /api proxy integration

async function request(url, options = {}) {
  const config = {
    headers: {
      'Accept': 'application/json',
      ...(!options.isFormData && { 'Content-Type': 'application/json' }),
      ...options.headers,
    },
    ...options,
  };

  if (options.isFormData) {
    delete config.headers['Content-Type'];
  }

  const res = await fetch(url, config);
  const data = await res.json().catch(() => null);

  if (!res.ok) {
    const errorMsg = data?.message || data?.error || `Request failed with status ${res.status}`;
    throw new Error(errorMsg);
  }

  return data;
}

export const api = {
  // Auth
  getMe: () => request('/api/auth/me'),
  directLogin: (email, password) =>
    request('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    }),
  registerStudent: (data) =>
    request('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
  verifyOperatorKey: (operatorKey) =>
    request('/api/auth/verify-operator-key', {
      method: 'POST',
      body: JSON.stringify({ operatorKey }),
    }),
  logout: () => request('/api/auth/logout', { method: 'POST' }),
  demoLogin: (accountType, autoVerifyKey = false) =>
    request('/api/auth/demo-login', {
      method: 'POST',
      body: JSON.stringify({ accountType, autoVerifyKey }),
    }),

  // Organization
  getDepartments: () => request('/api/organization/departments'),
  getHostels: () => request('/api/organization/hostels'),
  getServices: () => request('/api/organization/services'),
  getHealth: () => request('/api/health'),

  // Complaints
  getRoutingPreview: (params) => {
    const q = new URLSearchParams(params).toString();
    return request(`/api/complaints/routing-preview?${q}`);
  },
  submitComplaint: (data) =>
    request('/api/complaints', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
  getMyComplaints: (page = 0, size = 20) =>
    request(`/api/complaints/my?page=${page}&size=${size}`),
  getComplaintDetails: (id) => request(`/api/complaints/${id}`),
  confirmResolution: (id, feedback) =>
    request(`/api/complaints/${id}/confirm-resolution`, {
      method: 'POST',
      body: JSON.stringify({ feedback }),
    }),
  rejectResolution: (id, reason) =>
    request(`/api/complaints/${id}/reject-resolution`, {
      method: 'POST',
      body: JSON.stringify({ reason }),
    }),
  reopenComplaint: (id, reopenReason) =>
    request(`/api/complaints/${id}/reopen`, {
      method: 'POST',
      body: JSON.stringify({ reopenReason }),
    }),
  addPublicNote: (id, content) =>
    request(`/api/complaints/${id}/notes`, {
      method: 'POST',
      body: JSON.stringify({ content }),
    }),

  // Attachments
  uploadComplaintAttachment: (complaintId, file) => {
    const formData = new FormData();
    formData.append('file', file);
    return request(`/api/attachments/complaints/${complaintId}`, {
      method: 'POST',
      body: formData,
      isFormData: true,
    });
  },
  getComplaintAttachments: (complaintId) =>
    request(`/api/attachments/complaints/${complaintId}`),
  getAttachmentDownloadUrl: (id) => `/api/attachments/${id}/download`,

  // Operator
  getOperatorQueue: (page = 0, size = 20) =>
    request(`/api/operator/queue?page=${page}&size=${size}`),
  acknowledgeComplaint: (id, message) =>
    request(`/api/operator/complaints/${id}/acknowledge`, {
      method: 'POST',
      body: JSON.stringify({ message }),
    }),
  markInProgress: (id, message) =>
    request(`/api/operator/complaints/${id}/in-progress`, {
      method: 'POST',
      body: JSON.stringify({ message }),
    }),
  proposeResolution: (id, resolutionSummary) =>
    request(`/api/operator/complaints/${id}/propose-resolution`, {
      method: 'POST',
      body: JSON.stringify({ resolutionSummary }),
    }),
  escalateComplaint: (id, escalationReason) =>
    request(`/api/operator/complaints/${id}/escalate`, {
      method: 'POST',
      body: JSON.stringify({ escalationReason }),
    }),
  addInternalNote: (id, content) =>
    request(`/api/operator/complaints/${id}/internal-note`, {
      method: 'POST',
      body: JSON.stringify({ content }),
    }),
  adminClose: (id, reason) =>
    request(`/api/operator/complaints/${id}/admin-close`, {
      method: 'POST',
      body: JSON.stringify({ reason }),
    }),
  getReports: () => request('/api/operator/reports'),

  // Incidents
  getActiveIncidents: () => request('/api/incidents'),
  getIncidentSubscriptionStatus: (id) =>
    request(`/api/incidents/${id}/subscription-status`),
  toggleSubscription: (id) =>
    request(`/api/incidents/${id}/subscribe`, { method: 'POST' }),
  createIncident: (data) =>
    request('/api/incidents', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
  updateIncidentProgress: (id, newStatus, updateMessage) =>
    request(`/api/incidents/${id}/update-progress`, {
      method: 'POST',
      body: JSON.stringify({ newStatus, updateMessage }),
    }),
  linkComplaintToIncident: (id, complaintId) =>
    request(`/api/incidents/${id}/link-complaint`, {
      method: 'POST',
      body: JSON.stringify({ complaintId }),
    }),
  unlinkComplaintFromIncident: (id, complaintId) =>
    request(`/api/incidents/${id}/unlink-complaint`, {
      method: 'POST',
      body: JSON.stringify({ complaintId }),
    }),

  // Academics
  getAcademicDocuments: (semester) => {
    const q = semester ? `?semester=${semester}` : '';
    return request(`/api/academics/documents${q}`);
  },
  searchAcademicPages: (q) =>
    request(`/api/academics/search?q=${encodeURIComponent(q)}`),
  askAcademicQuestion: (question) =>
    request('/api/academics/ask', {
      method: 'POST',
      body: JSON.stringify({ question }),
    }),
  uploadAcademicDocument: (formData) =>
    request('/api/academics/documents', {
      method: 'POST',
      body: formData,
      isFormData: true,
    }),
  deleteAcademicDocument: (id) =>
    request(`/api/academics/documents/${id}`, { method: 'DELETE' }),

  // Admin
  previewRoster: (formData) =>
    request('/api/admin/roster/preview', {
      method: 'POST',
      body: formData,
      isFormData: true,
    }),
  applyRoster: (formData) =>
    request('/api/admin/roster/apply', {
      method: 'POST',
      body: formData,
      isFormData: true,
    }),
  generateOperatorKey: (userId) =>
    request(`/api/admin/operators/${userId}/generate-key`, { method: 'POST' }),
  getPendingUsers: () => request('/api/admin/pending-users'),
  getOperatorsList: () => request('/api/admin/operators'),
  approveUser: (userId, data) =>
    request(`/api/admin/users/${userId}/approve`, {
      method: 'POST',
      body: JSON.stringify(data),
    }),
  getAuditLogs: (page = 0, size = 30) =>
    request(`/api/admin/audit-logs?page=${page}&size=${size}`),
  triggerSlaCheck: () =>
    request('/api/admin/sla-check', { method: 'POST' }),

  // Notifications
  getNotifications: (page = 0, size = 20) =>
    request(`/api/notifications?page=${page}&size=${size}`),
  markNotificationRead: (id) =>
    request(`/api/notifications/${id}/read`, { method: 'POST' }),
};
