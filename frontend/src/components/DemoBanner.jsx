import { useState } from 'react';
import { User, Shield, GraduationCap, Building2, Server, HelpCircle, Wrench, X, ChevronUp, ChevronDown } from 'lucide-react';

export default function DemoBanner({ currentUser, onSwitchUser }) {
  const [open, setOpen] = useState(false);
  const [dismissed, setDismissed] = useState(false);

  const personas = [
    { label: 'Ayush (Student - Aryabhatt R214)', key: 'STUDENT_ARYABHATT', icon: User, role: 'STUDENT' },
    { label: 'Aryabhatt Warden', key: 'WARDEN_ARYABHATT', icon: Building2, role: 'OPERATOR' },
    { label: 'Aryabhatt Rector', key: 'RECTOR_ARYABHATT', icon: Shield, role: 'OPERATOR' },
    { label: 'Chief Rector', key: 'CHIEF_RECTOR', icon: Shield, role: 'OPERATOR' },
    { label: 'AIML Dept Incharge', key: 'OPERATOR_AIML', icon: GraduationCap, role: 'OPERATOR' },
    { label: 'Campus IT Desk', key: 'OPERATOR_IT', icon: Server, role: 'OPERATOR' },
    { label: 'System Admin', key: 'SYSTEM_ADMIN', icon: Shield, role: 'SYSTEM_ADMIN' },
    { label: 'Unverified Guest', key: 'PENDING_STUDENT', icon: HelpCircle, role: 'PENDING' },
  ];

  if (dismissed) return null;

  return (
    <div style={{
      position: 'fixed',
      bottom: '16px',
      right: '16px',
      zIndex: 9999,
      fontFamily: 'inherit'
    }}>
      {!open ? (
        <button
          onClick={() => setOpen(true)}
          style={{
            backgroundColor: '#0f2942',
            color: '#ffffff',
            border: '1px solid rgba(255, 255, 255, 0.2)',
            borderRadius: '24px',
            padding: '8px 14px',
            boxShadow: '0 4px 16px rgba(15, 41, 66, 0.35)',
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            fontSize: '0.78rem',
            fontWeight: '700',
            cursor: 'pointer',
            transition: 'transform 0.2s, box-shadow 0.2s',
          }}
          title="Open Evaluation & Role Switcher Sandbox"
        >
          <Wrench size={14} color="#2dd4bf" />
          <span>Role Switcher Sandbox</span>
          <ChevronUp size={14} />
        </button>
      ) : (
        <div style={{
          width: '320px',
          backgroundColor: '#091827',
          color: '#e2e8f0',
          border: '1px solid #1e293b',
          borderRadius: '12px',
          boxShadow: '0 8px 30px rgba(0, 0, 0, 0.45)',
          padding: '14px',
          fontSize: '0.8125rem'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '10px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <span style={{
                backgroundColor: '#0d9488',
                color: '#ffffff',
                padding: '2px 6px',
                borderRadius: '4px',
                fontWeight: '700',
                fontSize: '0.68rem',
                letterSpacing: '0.04em'
              }}>
                PILOT SANDBOX
              </span>
              <span style={{ fontSize: '0.75rem', color: '#94a3b8', fontWeight: '600' }}>Instant Persona Switch</span>
            </div>
            <div style={{ display: 'flex', gap: '4px' }}>
              <button
                onClick={() => setOpen(false)}
                style={{ background: 'none', border: 'none', color: '#94a3b8', cursor: 'pointer', padding: '2px' }}
                title="Minimize"
              >
                <ChevronDown size={16} />
              </button>
              <button
                onClick={() => setDismissed(true)}
                style={{ background: 'none', border: 'none', color: '#94a3b8', cursor: 'pointer', padding: '2px' }}
                title="Dismiss Completely"
              >
                <X size={16} />
              </button>
            </div>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '5px', maxHeight: '260px', overflowY: 'auto' }}>
            {personas.map((p) => {
              const isActive = currentUser && currentUser.email && (
                (p.key === 'STUDENT_ARYABHATT' && currentUser.email.includes('ayush')) ||
                (p.key === 'WARDEN_ARYABHATT' && currentUser.email.includes('warden')) ||
                (p.key === 'RECTOR_ARYABHATT' && currentUser.email.includes('rector.aryabhatt')) ||
                (p.key === 'CHIEF_RECTOR' && currentUser.email.includes('chiefrector')) ||
                (p.key === 'OPERATOR_AIML' && currentUser.email.includes('aiml')) ||
                (p.key === 'OPERATOR_IT' && currentUser.email.includes('it.service')) ||
                (p.key === 'SYSTEM_ADMIN' && currentUser.email.includes('admin')) ||
                (p.key === 'PENDING_STUDENT' && currentUser.email.includes('unverified'))
              );

              return (
                <button
                  key={p.key}
                  onClick={() => { onSwitchUser(p.key); setOpen(false); }}
                  style={{
                    backgroundColor: isActive ? '#0d9488' : '#1e293b',
                    color: isActive ? '#ffffff' : '#cbd5e1',
                    border: isActive ? '1px solid #14b8a6' : '1px solid #334155',
                    padding: '6px 10px',
                    borderRadius: '6px',
                    cursor: 'pointer',
                    fontSize: '0.75rem',
                    fontWeight: isActive ? '700' : '500',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '8px',
                    textAlign: 'left'
                  }}
                >
                  <p.icon size={13} />
                  <span style={{ flex: 1 }}>{p.label}</span>
                  {isActive && <span style={{ fontSize: '0.65rem', color: '#a7f3d0' }}>Active</span>}
                </button>
              );
            })}
          </div>

          <div style={{ marginTop: '8px', paddingTop: '8px', borderTop: '1px solid #1e293b', fontSize: '0.68rem', color: '#64748b', textAlign: 'center' }}>
            Persistent DB active: <code>./data/campussetudb</code>
          </div>
        </div>
      )}
    </div>
  );
}
