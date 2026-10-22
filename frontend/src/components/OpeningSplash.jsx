import { useState, useEffect } from 'react';
import { ShieldCheck, Sparkles, Building2, Zap } from 'lucide-react';

export default function OpeningSplash({ onFinish }) {
  const [phase, setPhase] = useState(0); // 0: Enter, 1: Middle, 2: Welcome, 3: Exit
  const [progress, setProgress] = useState(10);
  const [exiting, setExiting] = useState(false);

  useEffect(() => {
    // Phase timings
    const t1 = setTimeout(() => {
      setPhase(1);
      setProgress(45);
    }, 550);

    const t2 = setTimeout(() => {
      setPhase(2);
      setProgress(85);
    }, 1100);

    const t3 = setTimeout(() => {
      setProgress(100);
      setPhase(3);
    }, 1600);

    const t4 = setTimeout(() => {
      setExiting(true);
    }, 1900);

    const t5 = setTimeout(() => {
      if (onFinish) onFinish();
    }, 2450);

    const handleKeyDown = (e) => {
      if (e.key === 'Escape') {
        setExiting(true);
        setTimeout(() => {
          if (onFinish) onFinish();
        }, 300);
      }
    };
    window.addEventListener('keydown', handleKeyDown);

    return () => {
      clearTimeout(t1);
      clearTimeout(t2);
      clearTimeout(t3);
      clearTimeout(t4);
      clearTimeout(t5);
      window.removeEventListener('keydown', handleKeyDown);
    };
  }, [onFinish]);

  const handleSkip = () => {
    setExiting(true);
    setTimeout(() => {
      if (onFinish) onFinish();
    }, 300);
  };

  const statusText = [
    'Connecting to Institutional Core Gateway...',
    'Synchronizing Deterministic Routing NOC...',
    'Welcome to CampusSetu • KIET Portal',
    'Launching Workspace...'
  ][phase] || 'Welcome to CampusSetu';

  return (
    <div
      style={{
        position: 'fixed',
        inset: 0,
        zIndex: 99999,
        backgroundColor: '#07162c',
        backgroundImage: `
          radial-gradient(circle at 50% 40%, rgba(185, 28, 28, 0.28) 0%, rgba(11, 37, 69, 0.95) 55%, #07162c 100%),
          linear-gradient(rgba(255, 255, 255, 0.02) 1px, transparent 1px),
          linear-gradient(90deg, rgba(255, 255, 255, 0.02) 1px, transparent 1px)
        `,
        backgroundSize: '100% 100%, 40px 40px, 40px 40px',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        color: '#ffffff',
        fontFamily: "'Inter', sans-serif",
        opacity: exiting ? 0 : 1,
        transform: exiting ? 'scale(1.04)' : 'scale(1)',
        filter: exiting ? 'blur(8px)' : 'none',
        transition: 'opacity 0.55s cubic-bezier(0.4, 0, 0.2, 1), transform 0.55s cubic-bezier(0.4, 0, 0.2, 1), filter 0.55s ease',
        pointerEvents: exiting ? 'none' : 'auto',
        overflow: 'hidden'
      }}
    >
      {/* Background Animated Ambient Orbs */}
      <div
        style={{
          position: 'absolute',
          top: '30%',
          left: '50%',
          transform: 'translate(-50%, -50%)',
          width: '420px',
          height: '420px',
          borderRadius: '50%',
          background: 'radial-gradient(circle, rgba(220, 38, 38, 0.25) 0%, rgba(248, 184, 31, 0.12) 40%, transparent 70%)',
          filter: 'blur(45px)',
          animation: 'openingPulse 3s ease-in-out infinite alternate',
          pointerEvents: 'none'
        }}
      />

      {/* Skip Button */}
      <button
        type="button"
        onClick={handleSkip}
        style={{
          position: 'absolute',
          top: '24px',
          right: '28px',
          background: 'rgba(255, 255, 255, 0.08)',
          border: '1px solid rgba(255, 255, 255, 0.16)',
          borderRadius: '20px',
          padding: '6px 14px',
          fontSize: '0.75rem',
          fontWeight: '600',
          color: '#cbd5e1',
          cursor: 'pointer',
          backdropFilter: 'blur(8px)',
          transition: 'all 0.15s ease'
        }}
        onMouseEnter={(e) => { e.currentTarget.style.backgroundColor = 'rgba(255, 255, 255, 0.18)'; }}
        onMouseLeave={(e) => { e.currentTarget.style.backgroundColor = 'rgba(255, 255, 255, 0.08)'; }}
      >
        Skip intro (Esc)
      </button>

      {/* Main Content Card */}
      <div
        style={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          textAlign: 'center',
          maxWidth: '520px',
          padding: '0 24px',
          zIndex: 1,
          animation: 'openingFadeIn 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards'
        }}
      >
        {/* Animated KIET Shield Emblem */}
        <div
          style={{
            position: 'relative',
            width: '84px',
            height: '84px',
            marginBottom: '22px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center'
          }}
        >
          {/* Outer Pulsing Rotating Glow Ring */}
          <div
            style={{
              position: 'absolute',
              inset: '-8px',
              borderRadius: '24px',
              border: '2px solid rgba(248, 184, 31, 0.35)',
              boxShadow: '0 0 35px rgba(220, 38, 38, 0.4), inset 0 0 20px rgba(248, 184, 31, 0.2)',
              animation: 'openingRingSpin 10s linear infinite'
            }}
          />

          {/* Core Crest Badge */}
          <div
            style={{
              width: '100%',
              height: '100%',
              borderRadius: '20px',
              background: 'linear-gradient(135deg, #b91c1c 0%, #991b1b 50%, #0b2545 100%)',
              border: '2px solid #f8b81f',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              boxShadow: '0 10px 30px rgba(0, 0, 0, 0.45)',
              transform: 'scale(1)',
              animation: 'openingEmblemPop 0.9s cubic-bezier(0.34, 1.56, 0.64, 1) forwards'
            }}
          >
            <ShieldCheck size={44} color="#f8b81f" strokeWidth={2.2} />
          </div>
        </div>

        {/* Institution Badge */}
        <div
          style={{
            display: 'inline-flex',
            alignItems: 'center',
            gap: '8px',
            backgroundColor: 'rgba(255, 255, 255, 0.08)',
            border: '1px solid rgba(248, 184, 31, 0.3)',
            borderRadius: '9999px',
            padding: '4px 14px',
            fontSize: '0.72rem',
            fontWeight: '800',
            letterSpacing: '0.08em',
            color: '#f8b81f',
            textTransform: 'uppercase',
            marginBottom: '14px',
            backdropFilter: 'blur(8px)'
          }}
        >
          <Building2 size={13} color="#f8b81f" />
          <span>KIET Group of Institutions</span>
        </div>

        {/* Platform Name */}
        <h1
          style={{
            fontSize: '2.5rem',
            fontWeight: '900',
            letterSpacing: '-0.025em',
            margin: '0 0 6px 0',
            background: 'linear-gradient(180deg, #ffffff 30%, #e2e8f0 100%)',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
            textShadow: '0 4px 20px rgba(0, 0, 0, 0.4)'
          }}
        >
          CampusSetu
        </h1>

        {/* Subtitle */}
        <p
          style={{
            fontSize: '0.9rem',
            color: '#94a3b8',
            margin: '0 0 28px 0',
            lineHeight: 1.5,
            fontWeight: '500'
          }}
        >
          Institutional Grievance & Student Welfare Governance Platform
        </p>

        {/* Progress Bar Container */}
        <div
          style={{
            width: '280px',
            height: '4px',
            backgroundColor: 'rgba(255, 255, 255, 0.1)',
            borderRadius: '9999px',
            overflow: 'hidden',
            position: 'relative',
            marginBottom: '14px'
          }}
        >
          <div
            style={{
              height: '100%',
              width: `${progress}%`,
              background: 'linear-gradient(90deg, #f8b81f 0%, #ef4444 60%, #38bdf8 100%)',
              borderRadius: '9999px',
              boxShadow: '0 0 12px rgba(248, 184, 31, 0.8)',
              transition: 'width 0.45s cubic-bezier(0.4, 0, 0.2, 1)'
            }}
          />
        </div>

        {/* Dynamic Status Text */}
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            fontSize: '0.78rem',
            color: '#cbd5e1',
            minHeight: '22px'
          }}
        >
          <span
            style={{
              width: '6px',
              height: '6px',
              borderRadius: '50%',
              backgroundColor: '#f8b81f',
              boxShadow: '0 0 8px #f8b81f',
              animation: 'openingDotBlink 1s infinite alternate'
            }}
          />
          <span style={{ fontWeight: '600', letterSpacing: '0.01em' }}>
            {statusText}
          </span>
        </div>
      </div>

      {/* Footer Institution Accents */}
      <div
        style={{
          position: 'absolute',
          bottom: '24px',
          display: 'flex',
          alignItems: 'center',
          gap: '12px',
          fontSize: '0.7rem',
          color: '#64748b',
          letterSpacing: '0.04em',
          textTransform: 'uppercase'
        }}
      >
        <span>NAAC 'A+' Accredited</span>
        <span>•</span>
        <span>Autonomous Institute</span>
        <span>•</span>
        <span>Ghaziabad, NCR</span>
      </div>

      <style>{`
        @keyframes openingFadeIn {
          from { opacity: 0; transform: translateY(14px); }
          to { opacity: 1; transform: translateY(0); }
        }
        @keyframes openingEmblemPop {
          0% { transform: scale(0.6) rotate(-10deg); opacity: 0; }
          70% { transform: scale(1.08) rotate(3deg); }
          100% { transform: scale(1) rotate(0); opacity: 1; }
        }
        @keyframes openingRingSpin {
          0% { transform: rotate(0deg); }
          100% { transform: rotate(360deg); }
        }
        @keyframes openingPulse {
          0% { transform: translate(-50%, -50%) scale(0.9); opacity: 0.6; }
          100% { transform: translate(-50%, -50%) scale(1.15); opacity: 0.95; }
        }
        @keyframes openingDotBlink {
          0% { opacity: 0.3; transform: scale(0.8); }
          100% { opacity: 1; transform: scale(1.2); }
        }
      `}</style>
    </div>
  );
}
