import {
  Layers,
  ArrowRight,
  Building,
  GraduationCap,
  Wifi
} from 'lucide-react';

export default function HowItWorksPage({ onSelectTab }) {
  return (
    <div className="animate-fade-in" style={{ padding: '36px 0 60px' }}>
      <div className="container" style={{ maxWidth: '900px' }}>
        <div style={{ textAlign: 'center', marginBottom: '36px' }}>
          <span className="badge badge-acknowledged" style={{ marginBottom: '8px' }}>Operational Blueprint</span>
          <h1 style={{ fontSize: '2rem', fontWeight: '800', color: 'var(--primary-navy)' }}>
            How CampusSetu Works
          </h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '1rem', marginTop: '6px' }}>
            A deterministic, configuration-driven service architecture for college facility management.
          </p>
        </div>

        {/* 5-Step Lifecycle Pipeline */}
        <div className="card" style={{ marginBottom: '32px', padding: '28px' }}>
          <h2 style={{ fontSize: '1.25rem', fontWeight: '800', color: 'var(--primary-navy)', marginBottom: '18px' }}>
            The 5-Stage Complaint Lifecycle
          </h2>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))', gap: '12px' }}>
            <div style={{ padding: '14px', borderRadius: '8px', backgroundColor: 'var(--status-submitted-bg)', border: '1px solid #fde68a' }}>
              <div style={{ fontSize: '0.75rem', fontWeight: '800', color: 'var(--status-submitted)' }}>STAGE 1</div>
              <div style={{ fontWeight: '700', fontSize: '0.9375rem', marginTop: '4px' }}>Submitted</div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '4px' }}>
                Dispatched instantly to designated scoped queue.
              </div>
            </div>

            <div style={{ padding: '14px', borderRadius: '8px', backgroundColor: 'var(--status-ack-bg)', border: '1px solid #bfdbfe' }}>
              <div style={{ fontSize: '0.75rem', fontWeight: '800', color: 'var(--status-ack)' }}>STAGE 2</div>
              <div style={{ fontWeight: '700', fontSize: '0.9375rem', marginTop: '4px' }}>Acknowledged</div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '4px' }}>
                Assigned operator logs case receipt.
              </div>
            </div>

            <div style={{ padding: '14px', borderRadius: '8px', backgroundColor: 'var(--status-progress-bg)', border: '1px solid #ddd6fe' }}>
              <div style={{ fontSize: '0.75rem', fontWeight: '800', color: 'var(--status-progress)' }}>STAGE 3</div>
              <div style={{ fontWeight: '700', fontSize: '0.9375rem', marginTop: '4px' }}>In Progress</div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '4px' }}>
                Technician or electrician dispatched with work slip.
              </div>
            </div>

            <div style={{ padding: '14px', borderRadius: '8px', backgroundColor: 'var(--status-proposed-bg)', border: '1px solid #a5f3fc' }}>
              <div style={{ fontSize: '0.75rem', fontWeight: '800', color: 'var(--status-proposed)' }}>STAGE 4</div>
              <div style={{ fontWeight: '700', fontSize: '0.9375rem', marginTop: '4px' }}>Resolution Proposed</div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '4px' }}>
                Staff records fix details. Awaiting student check.
              </div>
            </div>

            <div style={{ padding: '14px', borderRadius: '8px', backgroundColor: 'var(--status-closed-bg)', border: '1px solid #a7f3d0' }}>
              <div style={{ fontSize: '0.75rem', fontWeight: '800', color: 'var(--status-closed)' }}>STAGE 5</div>
              <div style={{ fontWeight: '700', fontSize: '0.9375rem', marginTop: '4px' }}>Closed</div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '4px' }}>
                Student confirms repair was successful.
              </div>
            </div>
          </div>
        </div>

        {/* Essential Scenarios Walkthrough */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          {/* Scenario A */}
          <div className="card">
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '8px' }}>
              <Building size={20} color="#0d9488" />
              <h3 style={{ fontSize: '1.15rem', fontWeight: '800', color: 'var(--primary-navy)' }}>
                Scenario A: Hostel Room Fixture Failure (Aryabhatt Hostel)
              </h3>
            </div>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', lineHeight: 1.6 }}>
              A student in Aryabhatt Hostel Room 214 experiences a ceiling fan regulator failure. When submitted, the complaint
              is deterministically routed directly to <code>QUEUE_HOSTEL_ARYABHATT_WARDEN</code>. It never enters unrelated
              queues or central administration inboxes. If unresolved within SLA, the warden escalates it with justification
              to the configured Aryabhatt Rector, who can further forward to the Chief Rector if institutional sanction is needed.
            </p>
          </div>

          {/* Scenario B */}
          <div className="card">
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '8px' }}>
              <GraduationCap size={20} color="#2563eb" />
              <h3 style={{ fontSize: '1.15rem', fontWeight: '800', color: 'var(--primary-navy)' }}>
                Scenario B: Department Laboratory or Classroom Defect (AIML)
              </h3>
            </div>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', lineHeight: 1.6 }}>
              A workstation GPU failure in AIML Lab 3 routes directly to <code>QUEUE_DEPT_AIML</code>. Routing is determined
              by the affected location rather than the reporter's branch (e.g. if an ECE student attends an open elective in an
              AIML lab, the issue routes to the AIML lab incharge).
            </p>
          </div>

          {/* Scenario C */}
          <div className="card">
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '8px' }}>
              <Wifi size={20} color="#7c3aed" />
              <h3 style={{ fontSize: '1.15rem', fontWeight: '800', color: 'var(--primary-navy)' }}>
                Scenario C: Central Campus Service Outages (IT & Infrastructure)
              </h3>
            </div>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', lineHeight: 1.6 }}>
              Wi-Fi access point captive portal loops or central water pump line failures dispatch directly to the specialized
              Network Operations Center (<code>QUEUE_SERVICE_IT</code>) or Central Estate Maintenance (<code>QUEUE_SERVICE_MAINTENANCE</code>).
            </p>
          </div>

          {/* Scenario D */}
          <div className="card">
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '8px' }}>
              <Layers size={20} color="#d97706" />
              <h3 style={{ fontSize: '1.15rem', fontWeight: '800', color: 'var(--primary-navy)' }}>
                Scenario D: Community Shared Incidents (Sanitized Outages)
              </h3>
            </div>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', lineHeight: 1.6 }}>
              When a water pump breaks in Aryabhatt B-Wing, dozens of students would ordinarily file duplicate tickets.
              Operators publish a sanitized public incident. Students click "I am affected too" once. Individual private complaints
              are preserved, student names and room numbers remain confidential, and resolving the shared incident notifies
              students without automatically closing their private inspection tickets.
            </p>
          </div>
        </div>

        <div style={{ textAlign: 'center', marginTop: '32px' }}>
          <button onClick={() => onSelectTab('login')} className="btn btn-primary btn-lg">
            Access Service Portal
            <ArrowRight size={18} />
          </button>
        </div>
      </div>
    </div>
  );
}
