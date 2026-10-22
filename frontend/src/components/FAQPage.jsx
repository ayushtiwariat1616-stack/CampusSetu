
export default function FAQPage() {
  const faqs = [
    {
      q: 'Can a maintenance operator close my complaint without my agreement?',
      a: 'No. When an operator finishes repair work, they can only propose a resolution with a repair summary. The complaint moves to "Resolution Proposed" and remains open until you inspect the fix and confirm it. If the repair was inadequate, you can reject it with a reason to return it to active maintenance.'
    },
    {
      q: 'What is an Operator Key and why do staff need it?',
      a: 'A college email address alone does not prove operational authorization. To prevent unauthorized ticket tampering, all hostel wardens, rectors, and department technicians must supply an individual, random cryptographic operator key during login. Keys are stored only as secure BCrypt hashes with attempt throttling (5 attempts max) and 15-minute lockouts.'
    },
    {
      q: 'Who can read internal maintenance notes?',
      a: 'Internal notes are strictly restricted to authorized operators and administrative supervisors within that queue. They never appear on student-facing timelines or API responses, ensuring staff can record technical notes, parts requisition numbers, and contractor slips safely.'
    },
    {
      q: 'How does complaint routing work if I study in AIML but encounter an issue in a ME workshop?',
      a: 'Complaints route by the affected physical location, not the student’s enrolled branch. An issue in Mechanical Workshop Wing dispatches directly to the Mechanical Department Operator queue, ensuring prompt localized handling.'
    },
    {
      q: 'What happens when a Shared Community Incident is resolved?',
      a: 'Resolving a shared incident (e.g. water outage restoration) notifies all subscribed students that central utility flow is restored. However, it does NOT automatically close your private complaint—you must still confirm that water flow is working in your specific room.'
    },
    {
      q: 'How does the Academic Document Assistant work if AI is unavailable?',
      a: 'CampusSetu adheres strictly to transparent functionality. You can always browse, filter, search, and view official semester notes and syllabi. When asking questions, the assistant retrieves relevant excerpts with exact page citations. If an external AI model key is unconfigured, it transparently provides the source citations rather than returning hallucinated or fake responses.'
    }
  ];

  return (
    <div className="animate-fade-in" style={{ padding: '36px 0 60px' }}>
      <div className="container" style={{ maxWidth: '800px' }}>
        <div style={{ textAlign: 'center', marginBottom: '36px' }}>
          <span className="badge badge-acknowledged" style={{ marginBottom: '8px' }}>Support Knowledgebase</span>
          <h1 style={{ fontSize: '2rem', fontWeight: '800', color: 'var(--primary-navy)' }}>
            Frequently Asked Questions
          </h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '1rem', marginTop: '6px' }}>
            Operational policies, accountability rules, and privacy protections.
          </p>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          {faqs.map((faq, index) => (
            <div key={index} className="card" style={{ padding: '20px' }}>
              <h3 style={{ fontSize: '1.05rem', fontWeight: '700', color: 'var(--primary-navy)', marginBottom: '8px' }}>
                {faq.q}
              </h3>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', lineHeight: 1.6 }}>
                {faq.a}
              </p>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
