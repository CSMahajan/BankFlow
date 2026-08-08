import React, { useState, useEffect } from 'react';
import LoanApprovalsView from './loans/LoanApprovalsView';
import AdminDashboardOverview from './AdminDashboardOverview';
import UserManagementView from "./UserManagementView";
import AccountManagementView from './accounts/AccountManagementView';
import CardManagementView from './cards/CardManagementView';
import AuditLogsView from "./audit/AuditLogsView";
import { fetchAdminDashboardSummary } from '../api/bankService';

const AdminDashboard = ({ userRole, userName, onLogout }) => {
  const [summary, setSummary] = useState(null);
  const [loadingSummary, setLoadingSummary] = useState(true);
  const [summaryError, setSummaryError] = useState(null);
  const [activeTab, setActiveTab] = useState('overview');
  const [adminName, setAdminName] = useState(
    userName || localStorage.getItem('fullName') || localStorage.getItem('name') || 'Admin User'
  );

  const loadSummary = async () => {
    setLoadingSummary(true);
    setSummaryError(null);

    try {
      const data = await fetchAdminDashboardSummary();
      setSummary(data);
    } catch (err) {
      console.error(err);
      setSummaryError("Unable to load admin dashboard.");
    } finally {
      setLoadingSummary(false);
    }
  };

  const refreshDashboard = async () => {
    await loadSummary();
  };

  useEffect(() => {
    loadSummary();
  }, []);

  const roleDisplay = userRole || localStorage.getItem('userRole') || 'ADMIN';

  return (
    <div style={styles.layout}>
      {/* Sidebar Navigation */}
      <aside style={styles.sidebar}>
        <div style={styles.sidebarTop}>
          <div style={styles.brand}>🛡️ Admin Control</div>
          <nav style={styles.nav}>
            <button
              style={{
                ...styles.navBtn,
                backgroundColor: activeTab === 'overview' ? '#1e293b' : 'transparent',
                color: activeTab === 'overview' ? '#ffffff' : '#374151',
              }}
              onClick={() => setActiveTab('overview')}
            >
              📊 System Overview
            </button>
            <button
              style={{
                ...styles.navBtn,
                backgroundColor: activeTab === 'loanApprovals' ? '#1e293b' : 'transparent',
                color: activeTab === 'loanApprovals' ? '#ffffff' : '#374151',
              }}
              onClick={() => setActiveTab('loanApprovals')}
            >
              💰 Loan Approvals
            </button>
            <button
              style={{
                ...styles.navBtn,
                backgroundColor: activeTab === 'accountManage' ? '#1e293b' : 'transparent',
                color: activeTab === 'accountManage' ? '#ffffff' : '#374151',
              }}
              onClick={() => setActiveTab('accountManage')}
            >
              🏦 Account Management
            </button>
            <button
              style={{
                ...styles.navBtn,
                backgroundColor: activeTab === 'cardManage' ? '#1e293b' : 'transparent',
                color: activeTab === 'cardManage' ? '#ffffff' : '#374151',
              }}
              onClick={() => setActiveTab('cardManage')}
            >
              💳 Card Management
            </button>
            <button
              style={{
                ...styles.navBtn,
                backgroundColor: activeTab === 'users' ? '#1e293b' : 'transparent',
                color: activeTab === 'users' ? '#ffffff' : '#374151',
              }}
              onClick={() => setActiveTab('users')}
            >
              👥 User Management
            </button>
            <button
              style={{
                ...styles.navBtn,
                backgroundColor: activeTab === 'logs' ? '#1e293b' : 'transparent',
                color: activeTab === 'logs' ? '#ffffff' : '#374151',
              }}
              onClick={() => setActiveTab('logs')}
            >
              📋 Audit Logs
            </button>
          </nav>
        </div>

        {/* User Footer */}
        <div style={styles.sidebarFooter}>
          <div style={styles.userProfile}>
            <div style={styles.avatar}>
              {adminName.charAt(0).toUpperCase()}
            </div>
            <div style={styles.userInfo}>
              <strong style={styles.userName}>{adminName}</strong>
              <span style={styles.roleBadge}>{roleDisplay}</span>
            </div>
          </div>
          <button style={styles.logoutBtn} onClick={onLogout} title="Log Out">
            🚪 Log Out
          </button>
        </div>
      </aside>

      {/* Main Content Area */}
      <main style={styles.mainContent}>
        <header style={styles.topHeader}>
          <div>
            <h1 style={styles.headerTitle}>Admin Dashboard</h1>
            <p style={styles.headerSubtitle}>
              Welcome back, <strong>{adminName}</strong>. System operations & administration.
            </p>
          </div>
        </header>

        {activeTab === 'overview' && (
          <AdminDashboardOverview
            summary={summary}
            loading={loadingSummary}
            error={summaryError}
          />
        )}

        {activeTab === 'loanApprovals' && (
          <LoanApprovalsView
            refreshDashboard={refreshDashboard}
          />
        )}

        {activeTab === 'accountManage' && (
          <AccountManagementView
            refreshDashboard={refreshDashboard}
          />
        )}

        {activeTab === 'cardManage' && (
          <CardManagementView
            refreshDashboard={refreshDashboard}
          />
        )}

        {activeTab === 'users' && (
          <UserManagementView
            refreshDashboard={refreshDashboard}
          />
        )}

        {activeTab === "logs" && (
          <AuditLogsView />
        )}
      </main>
    </div>
  );
};

const styles = {
  layout: { display: 'flex', minHeight: '100vh', backgroundColor: '#f8fafc' },
  sidebar: { width: '260px', backgroundColor: '#ffffff', borderRight: '1px solid #e2e8f0', padding: '24px 16px', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' },
  sidebarTop: { display: 'flex', flexDirection: 'column', gap: '24px' },
  brand: { fontSize: '20px', fontWeight: '800', fontFamily: 'Georgia, serif', color: '#0f172a' },
  nav: { display: 'flex', flexDirection: 'column', gap: '8px' },
  navBtn: { border: 'none', padding: '12px 16px', borderRadius: '8px', textAlign: 'left', fontWeight: '600', fontSize: '14px', cursor: 'pointer' },
  sidebarFooter: { borderTop: '1px solid #e2e8f0', paddingTop: '16px', display: 'flex', flexDirection: 'column', gap: '12px' },
  userProfile: { display: 'flex', alignItems: 'center', gap: '10px' },
  avatar: { width: '36px', height: '36px', borderRadius: '50%', backgroundColor: '#0f172a', color: '#ffffff', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: '700', fontSize: '15px' },
  userInfo: { display: 'flex', flexDirection: 'column', gap: '2px', overflow: 'hidden' },
  userName: { fontSize: '14px', color: '#0f172a', fontWeight: '700', lineHeight: '1.2', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' },
  roleBadge: { fontSize: '11px', fontWeight: '700', color: '#1e293b', backgroundColor: '#f1f5f9', padding: '2px 6px', borderRadius: '4px', width: 'fit-content', textTransform: 'uppercase' },
  logoutBtn: { border: '1px solid #fee2e2', padding: '10px', borderRadius: '8px', backgroundColor: '#fef2f2', cursor: 'pointer', fontWeight: '600', color: '#dc2626', fontSize: '13px', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '6px' },
  mainContent: { flex: 1, padding: '32px' },
  topHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '28px', paddingBottom: '16px', borderBottom: '1px solid #e2e8f0' },
  headerTitle: { margin: 0, fontSize: '24px', fontFamily: 'Georgia, serif', color: '#0f172a' },
  headerSubtitle: { margin: '4px 0 0 0', fontSize: '13px', color: '#64748b' },
  card: { backgroundColor: '#ffffff', padding: '24px', borderRadius: '12px', border: '1px solid #e2e8f0', boxShadow: '0 1px 3px rgba(0,0,0,0.05)' },
  mutedText: { color: '#64748b', fontSize: '14px' },
};

export default AdminDashboard;
