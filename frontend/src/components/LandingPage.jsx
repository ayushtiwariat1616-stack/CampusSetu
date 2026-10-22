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
      const hostelNames = {
        CV_RAMAN: 'C.V. Raman Hostel (1st Year Boys)',
        CHANDRAGUPT: 'Chandragupt Hostel (1st Year Boys)',
        TAGORE: 'Tagore Hostel (2nd Year Boys)',
        ARYABHATT: 'Aryabhatt Hostel (2nd & 3rd Year Boys)',
        VIVEKANAND: 'Vivekanand Hostel (2nd & 3rd Year Boys)',
        CHANAKYA: 'Chanakya Hostel (4th Year Boys)',
        GARGI: 'Gargi Hostel (1st Year Girls)',
        SAROJINI: 'Sarojini Hostel (2nd & 3rd Year Girls)',
        SARASWATI: 'Saraswati Hostel (2nd, 3rd & 4th Year Girls)'
      };
      const displayHostel = hostelNames[simHostel] || `${simHostel} Hostel`;
      return {
        queue: `QUEUE_HOSTEL_${simHostel}_WARDEN`,
        office: `${displayHostel} Warden Desk`,
        escalation: `${displayHostel} Rector -> Chief Rector`,
        privacy: 'Confined exclusively to this hostel warden inbox. Does not broadcast to central administration.'
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
        position: 'relative',
        borderBottom: '1px solid var(--border-subtle)',
        padding: '64px 0 54px',
        backgroundImage: 'linear-gradient(90deg, #ffffff 0%, rgba(255, 255, 255, 0.96) 42%, rgba(255, 255, 255, 0.78) 65%, rgba(255, 255, 255, 0.28) 100%), url("/kiet_campus.jpg")',
        backgroundSize: 'cover',
        backgroundPosition: 'center right',
        backgroundRepeat: 'no-repeat',
        overflow: 'hidden'
      }}>
        <div className="container" style={{ position: 'relative', zIndex: 2 }}>
          <div style={{ maxWidth: '820px' }}>
            <div style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '6px',
              padding: '5px 14px',
              backgroundColor: 'rgba(255, 255, 255, 0.95)',
              border: '1px solid #99f6e4',
              borderRadius: 'var(--radius-full)',
              color: 'var(--accent-teal)',
              fontSize: '0.8125rem',
              fontWeight: '700',
              marginBottom: '16px',
              boxShadow: '0 2px 6px rgba(0,0,0,0.04)'
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
              marginBottom: '18px',
              textShadow: '0 1px 2px rgba(255,255,255,0.8)'
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
                style={{ backgroundColor: 'rgba(255, 255, 255, 0.92)', backdropFilter: 'blur(6px)' }}
              >
                Inspect Routing Architecture
              </button>
              <button
                onClick={() => onSelectTab('academics')}
                className="btn btn-outline btn-lg"
                style={{ backgroundColor: 'rgba(255, 255, 255, 0.92)', backdropFilter: 'blur(6px)' }}
              >
                <FileText size={18} />
                Course Notes & Assistant
              </button>
            </div>

            {/* Live Institutional Indicators */}
            <div style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
              gap: '12px',
              margin: '28px 0 20px'
            }}>
              <div style={{
                backgroundColor: 'rgba(255, 255, 255, 0.92)',
                backdropFilter: 'blur(10px)',
                padding: '14px 16px',
                borderRadius: 'var(--radius-md)',
                border: '1px solid rgba(226, 232, 240, 0.9)',
                boxShadow: '0 4px 12px rgba(11, 37, 69, 0.05)'
              }}>
                <div style={{ fontSize: '1.25rem', fontWeight: '800', color: 'var(--primary-navy)' }}>9 Hostels</div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Mapped by Year & Gender</div>
              </div>

              <div style={{
                backgroundColor: 'rgba(255, 255, 255, 0.92)',
                backdropFilter: 'blur(10px)',
                padding: '14px 16px',
                borderRadius: 'var(--radius-md)',
                border: '1px solid rgba(226, 232, 240, 0.9)',
                boxShadow: '0 4px 12px rgba(11, 37, 69, 0.05)'
              }}>
                <div style={{ fontSize: '1.25rem', fontWeight: '800', color: 'var(--accent-blue)' }}>8 Departments</div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Lab & Classroom Queues</div>
              </div>

              <div style={{
                backgroundColor: 'rgba(255, 255, 255, 0.92)',
                backdropFilter: 'blur(10px)',
                padding: '14px 16px',
                borderRadius: 'var(--radius-md)',
                border: '1px solid rgba(226, 232, 240, 0.9)',
                boxShadow: '0 4px 12px rgba(11, 37, 69, 0.05)'
              }}>
                <div style={{ fontSize: '1.25rem', fontWeight: '800', color: 'var(--accent-teal)' }}>100% Verified</div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Student Closure Confirmation</div>
              </div>
            </div>

            {/* Official Enterprise Operations Note */}
            <div style={{
              backgroundColor: 'rgba(255, 255, 255, 0.94)',
              backdropFilter: 'blur(10px)',
              borderLeft: '4px solid var(--primary-navy)',
              padding: '12px 16px',
              borderRadius: '0 8px 8px 0',
              fontSize: '0.8125rem',
              color: 'var(--text-secondary)',
              lineHeight: 1.5,
              boxShadow: '0 2px 8px rgba(0,0,0,0.04)'
            }}>
              <strong>Institutional Operations:</strong> Enterprise service portal adhering to KIET organizational hierarchies, departmental laboratory protocols, and hostel administration standards.
            </div>
          </div>
        </div>

        {/* Floating Campus Badge on bottom right */}
        <div style={{
          position: 'absolute',
          bottom: '16px',
          right: '24px',
          backgroundColor: 'rgba(11, 37, 69, 0.85)',
          color: '#ffffff',
          backdropFilter: 'blur(8px)',
          padding: '6px 14px',
          borderRadius: 'var(--radius-full)',
          fontSize: '0.72rem',
          fontWeight: '700',
          display: 'flex',
          alignItems: 'center',
          gap: '6px',
          boxShadow: '0 4px 12px rgba(0,0,0,0.2)',
          border: '1px solid rgba(255,255,255,0.25)',
          zIndex: 3
        }}>
          <Building size={14} color="#f8b81f" />
          <span>KIET Group of Institutions • Main Campus</span>
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
            <div className="card card-interactive" style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
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
            <div className="card card-interactive" style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
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
            <div className="card card-interactive" style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
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
            <div className="card card-interactive" style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
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
                border: '1.5px solid #cbd5e1',
                borderRadius: 'var(--radius-md)',
                padding: '20px',
                display: 'flex',
                flexDirection: 'column',
                gap: '12px',
                boxShadow: 'var(--shadow-sm)'
              }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '8px' }}>
                  <span style={{ fontSize: '0.72rem', fontWeight: '800', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.04em', display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <span className="pulse-dot" style={{ backgroundColor: '#10b981' }}></span>
                    Enforced Routing Target
                  </span>
                  <code style={{ fontSize: '0.75rem', backgroundColor: 'var(--bg-subtle)', padding: '3px 10px', borderRadius: '6px', fontWeight: '700', color: 'var(--primary-navy)', border: '1px solid #cbd5e1' }}>
                    {simResult.queue}
                  </code>
                </div>

                <div style={{ fontSize: '1.25rem', fontWeight: '800', color: 'var(--primary-navy)' }}>
                  {simResult.office}
                </div>

                <div style={{ fontSize: '0.84rem', color: 'var(--text-secondary)', display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <strong style={{ color: 'var(--primary-navy)' }}>Escalation Hierarchy:</strong> {simResult.escalation}
                </div>

                <div style={{
                  fontSize: '0.8125rem',
                  color: 'var(--text-muted)',
                  backgroundColor: 'var(--bg-subtle)',
                  padding: '8px 12px',
                  borderRadius: 'var(--radius-sm)',
                  borderLeft: '3px solid var(--accent-blue)'
                }}>
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
