import { useState } from 'react';
import {
  CheckCircle2,
  ArrowRight,
  Building,
  GraduationCap,
  FileText,
  EyeOff,
  Lock
} from 'lucide-react';

export default function LandingPage({ onSelectTab }) {
  const [simCategory, setSimCategory] = useState('HOSTEL_FACILITY');
  const [simLocation, setSimLocation] = useState('HOSTEL');
  const [simHostel, setSimHostel] = useState('ARYABHATT');
  const [simDept, setSimDept] = useState('AIML');

  const getSimResult = () => {
    if (simLocation === 'HOSTEL') {
      return {
        queue: `QUEUE_HOSTEL_${simHostel}_WARDEN`,
        office: `${simHostel === 'ARYABHATT' ? 'Aryabhatt Hostel' : simHostel} Warden Desk`,
        escalation: `${simHostel} Rector -> Chief Rector`,
        privacy: 'Confined exclusively to this hostel warden inbox. Does not broadcast to college administrators.'
      };
    }
    if (simLocation === 'DEPARTMENT') {
      return {
        queue: `QUEUE_DEPT_${simDept}`,
        office: `${simDept} Department Facility Incharge`,
        escalation: 'Central Academic Grievance Cell',
        privacy: 'Routed by affected classroom/lab location, regardless of student branch.'
      };
    }
    return {
      queue: simCategory === 'IT_AND_NETWORK' ? 'QUEUE_SERVICE_IT' : 'QUEUE_SERVICE_MAINTENANCE',
      office: simCategory === 'IT_AND_NETWORK' ? 'Campus IT & NOC Desk' : 'Central Estate Maintenance',
      escalation: 'Directorate Operations',
      privacy: 'Directly assigned to specialized technical service queue.'
    };
  };

  const simResult = getSimResult();

  return (
    <div className="animate-fade-in" style={{ paddingBottom: '60px' }}>
      {/* Hero Section */}
      <section style={{
        backgroundColor: '#ffffff',
        borderBottom: '1px solid var(--border-subtle)',
        padding: '56px 0 48px'
      }}>
        <div className="container">
          <div style={{ maxWidth: '820px' }}>
            <div style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '6px',
              padding: '4px 12px',
              backgroundColor: 'var(--accent-teal-subtle)',
              border: '1px solid #99f6e4',
              borderRadius: 'var(--radius-full)',
              color: 'var(--accent-teal)',
              fontSize: '0.8125rem',
              fontWeight: '700',
              marginBottom: '16px'
            }}>
              <span>KIET Group of Institutions</span>
              <span>•</span>
              <span>Operational Service Platform</span>
            </div>

            <h1 style={{
              fontSize: 'clamp(2rem, 4vw, 2.75rem)',
              fontWeight: '800',
              color: 'var(--primary-navy)',
              lineHeight: 1.15,
              letterSpacing: '-0.03em',
              marginBottom: '18px'
            }}>
              Realistic Campus Complaints, Deterministic Routing & Academic Assistance
            </h1>

            <p style={{
              fontSize: '1.125rem',
              color: 'var(--text-secondary)',
              lineHeight: 1.6,
              marginBottom: '28px'
            }}>
              CampusSetu replaces informal messages and generic dashboards with enforced accountability.
              Every complaint routes to an exact designated office, repairs require student verification before closure,
              and academic course notes are indexed with page-level citations.
            </p>

            <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap', alignItems: 'center', marginBottom: '24px' }}>
              <button
                onClick={() => onSelectTab('login')}
                className="btn btn-primary btn-lg"
              >
                Sign In to Portal
                <ArrowRight size={18} />
              </button>
              <button
                onClick={() => onSelectTab('how-it-works')}
                className="btn btn-outline btn-lg"
              >
                Inspect Routing Architecture
              </button>
              <button
                onClick={() => onSelectTab('academics')}
                className="btn btn-outline btn-lg"
              >
                <FileText size={18} />
                Course Notes & Assistant
              </button>
            </div>

            {/* Official Enterprise Operations Note */}
            <div style={{
              backgroundColor: 'var(--bg-subtle)',
              borderLeft: '4px solid var(--primary-navy)',
              padding: '12px 16px',
              borderRadius: '0 8px 8px 0',
              fontSize: '0.8125rem',
              color: 'var(--text-secondary)',
              lineHeight: 1.5
            }}>
              <strong>Institutional Operations:</strong> Enterprise service portal adhering to KIET organizational hierarchies, departmental laboratory protocols, and hostel administration standards.
            </div>
          </div>
        </div>
      </section>

      {/* 4 Pillars Grid */}
      <section style={{ padding: '48px 0' }}>
        <div className="container">
          <div style={{ textAlign: 'center', maxWidth: '640px', margin: '0 auto 36px' }}>
            <h2 style={{ fontSize: '1.75rem', fontWeight: '800', color: 'var(--primary-navy)', marginBottom: '8px' }}>
              Built for Real Operations, Not Decorative Dashboards
            </h2>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.9375rem' }}>
              Enforcing four strict principles to guarantee accountability, privacy, and actionable outcomes.
            </p>
          </div>

          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))',
            gap: '20px'
          }}>
            {/* Pillar 1 */}
            <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              <div style={{
                width: '44px',
                height: '44px',
                borderRadius: '10px',
                backgroundColor: 'var(--accent-teal-subtle)',
                color: 'var(--accent-teal)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
              }}>
                <Building size={22} />
              </div>
              <h3 style={{ fontSize: '1.125rem', fontWeight: '700', color: 'var(--primary-navy)' }}>
                Deterministic Scoped Routing
              </h3>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', lineHeight: 1.5 }}>
                Issues in Aryabhatt Hostel go exclusively to Aryabhatt’s warden. Lab issues go to the specific department.
                No unassigned tickets or silent drops.
              </p>
            </div>

            {/* Pillar 2 */}
            <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              <div style={{
                width: '44px',
                height: '44px',
                borderRadius: '10px',
                backgroundColor: '#eff6ff',
                color: '#2563eb',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
              }}>
                <CheckCircle2 size={22} />
              </div>
              <h3 style={{ fontSize: '1.125rem', fontWeight: '700', color: 'var(--primary-navy)' }}>
                Student-Confirmed Closure
              </h3>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', lineHeight: 1.5 }}>
                An operator marking maintenance complete only proposes resolution. The ticket stays active until the student
                inspects and confirms the repair.
              </p>
            </div>

            {/* Pillar 3 */}
            <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              <div style={{
                width: '44px',
                height: '44px',
                borderRadius: '10px',
                backgroundColor: '#fef3c7',
                color: '#d97706',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
              }}>
                <EyeOff size={22} />
              </div>
              <h3 style={{ fontSize: '1.125rem', fontWeight: '700', color: 'var(--primary-navy)' }}>
                Privacy-Preserving Incidents
              </h3>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', lineHeight: 1.5 }}>
                Campus-wide water, power, or Wi-Fi outages are aggregated into sanitized summaries. Zero student names or
                room numbers are ever exposed.
              </p>
            </div>

            {/* Pillar 4 */}
            <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              <div style={{
                width: '44px',
                height: '44px',
                borderRadius: '10px',
                backgroundColor: '#f3e8ff',
                color: '#7c3aed',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
              }}>
                <GraduationCap size={22} />
              </div>
              <h3 style={{ fontSize: '1.125rem', fontWeight: '700', color: 'var(--primary-navy)' }}>
                Academic Notes & Citations
              </h3>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', lineHeight: 1.5 }}>
                Department-scoped course materials, lecture decks, and syllabi with grounded Q&A providing exact
                document and page citations.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Interactive Routing Simulator */}
      <section style={{ backgroundColor: '#ffffff', padding: '48px 0', borderTop: '1px solid var(--border-subtle)' }}>
        <div className="container">
          <div style={{ maxWidth: '880px', margin: '0 auto' }}>
            <div style={{ textAlign: 'center', marginBottom: '28px' }}>
              <span className="badge badge-acknowledged" style={{ marginBottom: '8px' }}>Interactive Architecture</span>
              <h2 style={{ fontSize: '1.6rem', fontWeight: '800', color: 'var(--primary-navy)' }}>
                Deterministic Queue Routing & Dispatch Engine
              </h2>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', marginTop: '4px' }}>
                Test how CampusSetu dispatches complaints according to institutional scoping rules:
              </p>
            </div>

            <div className="card" style={{ backgroundColor: 'var(--bg-main)', border: '2px solid #e2e8f0' }}>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '16px', marginBottom: '20px' }}>
                <div className="form-group" style={{ margin: 0 }}>
                  <label className="form-label">Location Type</label>
                  <select
                    className="form-select"
                    value={simLocation}
                    onChange={(e) => setSimLocation(e.target.value)}
                  >
                    <option value="HOSTEL">Hostel Residence</option>
                    <option value="DEPARTMENT">Academic Department</option>
                    <option value="CAMPUS_FACILITY">Campus-Wide Facility</option>
                  </select>
                </div>

                {simLocation === 'HOSTEL' && (
                  <div className="form-group" style={{ margin: 0 }}>
                    <label className="form-label">Hostel</label>
                    <select
                      className="form-select"
                      value={simHostel}
                      onChange={(e) => setSimHostel(e.target.value)}
                    >
                      <option value="ARYABHATT">Aryabhatt Hostel (1st Year Boys)</option>
                      <option value="CV_RAMAN">C.V. Raman Hostel (Boys)</option>
                      <option value="TAGORE">Tagore Hostel (Boys)</option>
                      <option value="SARASWATI">Saraswati Hostel (Girls)</option>
                      <option value="GARGI">Gargi Hostel (Girls)</option>
                    </select>
                  </div>
                )}

                {simLocation === 'DEPARTMENT' && (
                  <div className="form-group" style={{ margin: 0 }}>
                    <label className="form-label">Department</label>
                    <select
                      className="form-select"
                      value={simDept}
                      onChange={(e) => setSimDept(e.target.value)}
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

                <div className="form-group" style={{ margin: 0 }}>
                  <label className="form-label">Category</label>
                  <select
                    className="form-select"
                    value={simCategory}
                    onChange={(e) => setSimCategory(e.target.value)}
                  >
                    <option value="HOSTEL_FACILITY">Hostel Fixture / Fan / Furniture</option>
                    <option value="LAB_EQUIPMENT">Lab System / GPU / Hardware</option>
                    <option value="IT_AND_NETWORK">Campus Wi-Fi / LAN / ERP</option>
                    <option value="ELECTRICAL_PLUMBING">Electrical / Plumbing Outage</option>
                    <option value="CLEANLINESS">Sanitation / Housekeeping</option>
                  </select>
                </div>
              </div>

              {/* Simulation Result Box */}
              <div style={{
                backgroundColor: '#ffffff',
                border: '1px solid var(--border-subtle)',
                borderRadius: 'var(--radius-md)',
                padding: '16px',
                display: 'flex',
                flexDirection: 'column',
                gap: '10px'
              }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '8px' }}>
                  <span style={{ fontSize: '0.75rem', fontWeight: '700', color: 'var(--text-muted)', textTransform: 'uppercase' }}>
                    Calculated Destination
                  </span>
                  <code style={{ fontSize: '0.75rem', backgroundColor: 'var(--bg-subtle)', padding: '2px 8px', borderRadius: '4px' }}>
                    {simResult.queue}
                  </code>
                </div>

                <div style={{ fontSize: '1.125rem', fontWeight: '800', color: 'var(--accent-teal)' }}>
                  {simResult.office}
                </div>

                <div style={{ fontSize: '0.8125rem', color: 'var(--text-secondary)' }}>
                  <strong>Escalation Authority:</strong> {simResult.escalation}
                </div>

                <div style={{ fontSize: '0.8125rem', color: 'var(--text-muted)', fontStyle: 'italic' }}>
                  {simResult.privacy}
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
