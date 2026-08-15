import React, { useState, useEffect } from 'react';
import { ChevronDownIcon } from "@heroicons/react/24/outline";
import ProfileView from './ProfileView';
import LoanApprovalsView from './loans/LoanApprovalsView';
import AdminDashboardOverview from './AdminDashboardOverview';
import UserManagementView from "./UserManagementView";
import AccountManagementView from './accounts/AccountManagementView';
import CardManagementView from './cards/CardManagementView';
import KycManagementView from './kyc/KycManagementView';
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
  const [isProfileMenuOpen, setIsProfileMenuOpen] = useState(false);
  const [hoveredItem, setHoveredItem] = useState(null);

  const handleProfileUpdated = (updatedName) => {
    setAdminName(updatedName);
  };

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
          <button
            style={styles.brandButton}
            onClick={() => setActiveTab("overview")}
            title="Go to Dashboard"
            onMouseEnter={(e) => {
              e.currentTarget.style.opacity = "0.8";
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.opacity = "1";
            }}
          >
            🛡️ Admin Control
          </button>
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
                backgroundColor: activeTab === 'kyc' ? '#1e293b' : 'transparent',
                color: activeTab === 'kyc' ? '#ffffff' : '#374151',
              }}
              onClick={() => setActiveTab('kyc')}
            >
              🪪 KYC Verification
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
      </aside>

      {/* Main Content Area */}
      <main style={styles.mainContent}>
        <header style={styles.topHeader}>
          <div>
            <h1 style={styles.headerTitle}>
              Welcome back, {adminName}!
            </h1>

            <p style={styles.headerSubtitle}>
              System operations & administration.
            </p>
          </div>

          <div style={styles.headerActions}>

            <div style={styles.profileMenuWrapper}>

              <button
                style={styles.profileMenuButton}
                onClick={() =>
                  setIsProfileMenuOpen(!isProfileMenuOpen)
                }
              >
                <div style={styles.headerAvatar}>
                  {adminName.charAt(0).toUpperCase()}
                </div>

                <span style={styles.headerUserName}>
                  {adminName}
                </span>

                <ChevronDownIcon
                  style={{
                    ...styles.dropdownArrow,
                    transform: isProfileMenuOpen
                      ? "rotate(180deg)"
                      : "rotate(0deg)",
                  }}
                />
              </button>

              {isProfileMenuOpen && (
                <div style={styles.profileDropdown}>

                  <button
                    style={{
                      ...styles.dropdownItem,
                      backgroundColor:
                        hoveredItem === "dashboard"
                          ? "#f3f8f7"
                          : "transparent",
                    }}
                    onMouseEnter={() => setHoveredItem("dashboard")}
                    onMouseLeave={() => setHoveredItem(null)}
                    onClick={() => {
                      setActiveTab("overview");
                      setIsProfileMenuOpen(false);
                    }}
                  >
                    🏠 Dashboard
                  </button>

                  <button
                    style={{
                      ...styles.dropdownItem,
                      backgroundColor:
                        hoveredItem === "profile"
                          ? "#f3f8f7"
                          : "transparent",
                    }}
                    onMouseEnter={() => setHoveredItem("profile")}
                    onMouseLeave={() => setHoveredItem(null)}
                    onClick={() => {
                      setActiveTab("profile");
                      setIsProfileMenuOpen(false);
                    }}
                  >
                    👤 My Profile
                  </button>

                  <div style={styles.dropdownDivider} />

                  <button
                    style={{
                      ...styles.dropdownItem,
                      backgroundColor:
                        hoveredItem === "logout"
                          ? "#fef2f2"
                          : "transparent",
                      color:
                        hoveredItem === "logout"
                          ? "#dc2626"
                          : "#374151",
                    }}
                    onMouseEnter={() => setHoveredItem("logout")}
                    onMouseLeave={() => setHoveredItem(null)}
                    onClick={onLogout}
                  >
                    🚪 Logout
                  </button>

                </div>
              )}

            </div>
          </div>
        </header>

        {activeTab === 'profile' && (
          <ProfileView
            onProfileUpdated={handleProfileUpdated}
          />
        )}

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

        {activeTab === 'kyc' && (
          <KycManagementView
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
  mainContent: { flex: 1, padding: '32px' },
  topHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '28px', paddingBottom: '16px', borderBottom: '1px solid #e2e8f0' },
  headerTitle: { margin: 0, fontSize: '24px', fontFamily: 'Georgia, serif', color: '#0f172a' },
  headerSubtitle: { margin: '4px 0 0 0', fontSize: '13px', color: '#64748b' },
  card: { backgroundColor: '#ffffff', padding: '24px', borderRadius: '12px', border: '1px solid #e2e8f0', boxShadow: '0 1px 3px rgba(0,0,0,0.05)' },
  mutedText: { color: '#64748b', fontSize: '14px' },
  headerActions: {
    display: "flex",
    alignItems: "center",
    gap: "18px",
  },

  profileMenuWrapper: {
    position: "relative",
  },

  profileMenuButton: {
    display: "flex",
    alignItems: "center",
    gap: "7px",
    border: "none",
    background: "transparent",
    padding: "5px 7px",
    borderRadius: "9px",
    cursor: "pointer",
    transition: "background-color 0.2s ease",
  },

  headerAvatar: {
    width: "36px",
    height: "36px",
    borderRadius: "50%",
    backgroundColor: "#0f172a",
    color: "#ffffff",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    fontWeight: "700",
    fontSize: "14px",
    flexShrink: 0,
  },

  headerUserName: {
    fontSize: "14px",
    fontWeight: "700",
    color: "#111827",
    whiteSpace: "nowrap",
  },

  dropdownArrow: {
    width: "19px",
    height: "19px",
    color: "#4b5563",
    marginLeft: "1px",
    flexShrink: 0,
    transition: "transform 0.2s ease",
  },

  profileDropdown: {
    position: "absolute",
    top: "calc(100% + 8px)",
    right: 0,
    width: "170px",
    backgroundColor: "#ffffff",
    border: "1px solid #e5e7eb",
    borderRadius: "10px",
    padding: "6px",
    boxShadow: "0 8px 24px rgba(0,0,0,0.10)",
    zIndex: 1000,
  },

  dropdownItem: {
    width: "100%",
    border: "none",
    background: "transparent",
    padding: "10px 11px",
    borderRadius: "7px",
    textAlign: "left",
    fontSize: "13px",
    fontWeight: "600",
    color: "#374151",
    cursor: "pointer",
    transition: "background-color 0.15s ease, color 0.15s ease",
  },

  dropdownDivider: {
    height: "1px",
    backgroundColor: "#eef0ec",
    margin: "4px 0",
  },

  brandButton: {
    border: "none",
    background: "transparent",
    padding: 0,
    fontSize: "20px",
    fontWeight: "800",
    fontFamily: "Georgia, serif",
    color: "#0f172a",
    cursor: "pointer",
    textAlign: "left",
  },
};

export default AdminDashboard;
