import { Shield, EyeOff, Lock, FileText } from 'lucide-react';

export default function PrivacyPage() {
  return (
    <div className="animate-fade-in" style={{ padding: '36px 0 60px' }}>
      <div className="container" style={{ maxWidth: '800px' }}>
        <div style={{ textAlign: 'center', marginBottom: '36px' }}>
          <span className="badge badge-acknowledged" style={{ marginBottom: '8px' }}>Security & Privacy</span>
          <h1 style={{ fontSize: '2rem', fontWeight: '800', color: 'var(--primary-navy)' }}>
            Data Handling & Privacy Safeguards
          </h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '1rem', marginTop: '6px' }}>
            Built to respect individual privacy, protect evidence, and preserve immutable accountability.
          </p>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          <div className="card">
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '8px' }}>
              <EyeOff size={20} color="#0d9488" />
              <h3 style={{ fontSize: '1.1rem', fontWeight: '700', color: 'var(--primary-navy)' }}>
                Zero Student PII in Community Shared Incidents
              </h3>
            </div>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', lineHeight: 1.6 }}>
              When common campus failures (such as water pump repairs or central Wi-Fi switch reboots) are summarized into
              shared community incidents, student names, roll numbers, room numbers, and private complaint photos are strictly
              excluded. Students can subscribe to progress without broadcasting their identity.
            </p>
          </div>

          <div className="card">
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '8px' }}>
              <Lock size={20} color="#2563eb" />
              <h3 style={{ fontSize: '1.1rem', fontWeight: '700', color: 'var(--primary-navy)' }}>
                Protected Evidence Storage
              </h3>
            </div>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', lineHeight: 1.6 }}>
              All uploaded photos and documentation are assigned non-enumerable cryptographic UUID storage filenames on private
              storage volumes. Downloads are guarded by server-side authorization: only the submitting student and assigned queue
              operators can access evidence files. Direct public hotlinking is strictly disallowed.
            </p>
          </div>

          <div className="card">
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '8px' }}>
              <Shield size={20} color="#7c3aed" />
              <h3 style={{ fontSize: '1.1rem', fontWeight: '700', color: 'var(--primary-navy)' }}>
                Immutable Audit Logging
              </h3>
            </div>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', lineHeight: 1.6 }}>
              Every status transition, ticket escalation, operator key verification, and administrative override is recorded
              in an immutable audit repository. This guarantees transparency during supervisory reviews and dispute reconciliations.
            </p>
          </div>

          <div className="card">
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '8px' }}>
              <FileText size={20} color="#d97706" />
              <h3 style={{ fontSize: '1.1rem', fontWeight: '700', color: 'var(--primary-navy)' }}>
                Curriculum Scoping & AI Retrieval
              </h3>
            </div>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', lineHeight: 1.6 }}>
              Academic course notes and lecture documents are isolated by department and semester. Private student complaints
              are completely separated from academic document retrieval to prevent cross-contamination or inadvertent data leakage.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
