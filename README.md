# CampusSetu 🎓

**Smart Campus Grievance Redressal & Academic Verification System**  
Built for **KIET Group of Institutions, Delhi-NCR, Ghaziabad**.

---

## 🌟 Key Features

- **Institutional Authentication**: Secure email login (`@kiet.edu`), student registration with automatic college roster verification, and role-based access control.
- **Automated SLA Engine**: Real-time SLA tracking for grievance response (24h) and resolution (72h) with automated escalation.
- **Evidence Attachment Handling**: Secure file upload and download for complaint evidence.
- **Role-Based Portals**:
  - **Student Portal**: Lodge complaints, track status, view academic docs, inspect notifications.
  - **Staff / Operator Desk**: Resolve issues, record notes, manage attachments.
  - **Admin Control Room**: Department analytics, audit trails, on-demand SLA triggers.
  - **Dean Oversight**: Institutional-wide escalations and critical academic audits.
- **Academic Document Verification**: Cryptographic checksum verification and multi-page syllabus/evaluation document viewing.
- **Audit Logging**: Comprehensive, immutable audit trail for security and regulatory compliance.

---

## 🏗️ Architecture

```
CampusSetu/
├── backend/                  # Spring Boot 3.3.5 (Java 17)
│   ├── src/main/java/edu/kiet/campussetu/
│   │   ├── academic/         # Syllabus, question papers & verification
│   │   ├── audit/            # Immutable audit event logging
│   │   ├── auth/             # Login, registration, token handling
│   │   ├── common/           # Unified API responses & exception handling
│   │   ├── complaint/        # Grievance lifecycle & SLA escalation
│   │   ├── config/           # Security & data initialization
│   │   ├── notification/     # In-app alerts & notifications
│   │   └── user/             # User profiles, roster validation
│   └── src/test/java/        # Comprehensive integration test suite (16 tests)
└── frontend/                 # React 19 + Vite SPA
    ├── src/
    │   ├── components/       # Dashboards (Student, Staff, Operator, Admin, Dean)
    │   ├── api.js            # Unified REST client with error handling
    │   └── App.jsx           # Routing and state management
```

---

## 🚀 Getting Started

### Prerequisites
- **Java 17+**
- **Node.js 18+** & **npm**

### 1. Backend Setup
```bash
cd backend
# Run persistent Spring Boot application on port 8081
./mvnw.cmd spring-boot:run
```
*Persistent database files are stored locally in `backend/data/campussetudb`.*

### 2. Frontend Setup
```bash
cd frontend
# Install dependencies
npm install

# Start Vite dev server on port 5173
npm run dev
```

### 3. Run Automated Tests
```bash
cd backend
./mvnw.cmd test
```

---

## 👥 Default Evaluation Credentials

All institutional test accounts are pre-seeded with password: `CampusSetu@2026`

| Role | Email |
|------|-------|
| Student | `ayush.2327csai1094@kiet.edu` |
| Operator / Staff | `hostel.warden@kiet.edu` |
| Administrator | `admin@kiet.edu` |
| Dean | `dean.academics@kiet.edu` |

---

## 📄 License

Developed for academic and campus administration use at KIET Group of Institutions.
