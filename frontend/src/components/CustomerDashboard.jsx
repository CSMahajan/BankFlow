import React, { useState, useEffect } from 'react';
import { ChevronDownIcon } from "@heroicons/react/24/outline";
import AccountsView from './accounts/AccountsView';
import FdCalculatorCard from './fds/FdCalculatorCard';
import FdManagementView from './fds/FdManagementView';
import ViewFds from './fds/ViewFds';
import DashboardOverview from '../components/DashboardOverview';
import CreateAccountModal from './accounts/CreateAccountModal';
import PaymentsView from "./payments/PaymentsView";
import LoansView from './loans/LoansView';
import CardsView from "./cards/CardsView";
import ProfileView from './ProfileView';
import { fetchMyAccounts, fetchDashboardSummary, fetchMonthlyAnalytics } from "../api/bankService";

const CustomerDashboard = ({ userRole, onLogout }) => {
  const [activeTab, setActiveTab] = useState("dashboard");
  const [paymentSubTab, setPaymentSubTab] = useState("transfer");
  const [fdSubTab, setFdSubTab] = useState('calculator');
  const [fdDraftConfig, setFdDraftConfig] = useState(null);
  const [isAccountModalOpen, setIsAccountModalOpen] = useState(false);
  const [accounts, setAccounts] = useState([]);
  const [loadingAccounts, setLoadingAccounts] = useState(true);
  const [accountsError, setAccountsError] = useState(null);
  const [summary, setSummary] = useState(null);
  const [analytics, setAnalytics] = useState(null);
  const [loadingAnalytics, setLoadingAnalytics] = useState(true);
  const [analyticsError, setAnalyticsError] = useState(null);
  const [loadingSummary, setLoadingSummary] = useState(true);
  const [summaryError, setSummaryError] = useState(null);
  const [hoveredItem, setHoveredItem] = useState(null);

  const [customerName, setCustomerName] = useState(
    localStorage.getItem('fullName') ||
    localStorage.getItem('name') ||
    'Customer'
  );

  const [isProfileMenuOpen, setIsProfileMenuOpen] = useState(false);

  const handleProfileUpdated = (updatedName) => {
    setCustomerName(updatedName);
  };

  const loadDashboardSummary = async () => {
    setLoadingSummary(true);
    setSummaryError(null);

    try {
      const dashboardSummaryData = await fetchDashboardSummary();
      setSummary(dashboardSummaryData);
    } catch (err) {
      console.error(err);
      setSummaryError('Unable to load dashboard summary.');
    } finally {
      setLoadingSummary(false);
    }
  };

  const loadAnalytics = async () => {
    setLoadingAnalytics(true);
    setAnalyticsError(null);

    try {
      const data = await fetchMonthlyAnalytics();
      setAnalytics(data);
    } catch (err) {
      console.error(err);
      setAnalyticsError("Unable to load financial summary.");
    } finally {
      setLoadingAnalytics(false);
    }
  };

  useEffect(() => {
    loadAccounts();
    loadDashboardSummary();
    loadAnalytics();
  }, []);

  const roleDisplay = userRole || localStorage.getItem('userRole') || 'CUSTOMER';

  const handleOpenFdFromCalc = (config) => {
    setFdDraftConfig(config);
    setFdSubTab('open');
    setActiveTab('fd');
  };

  const handleAccountCreated = async () => {
    await refreshDashboard();
  };

  const loadAccounts = async () => {
    setLoadingAccounts(true);
    setAccountsError(null);

    try {
      const accountList = await fetchMyAccounts();
      setAccounts(accountList || []);
    } catch (err) {
      console.error(err);
      setAccountsError('Unable to load accounts.');
    } finally {
      setLoadingAccounts(false);
    }
  };

  const refreshDashboard = async () => {
    await Promise.all([
      loadAccounts(),
      loadDashboardSummary(),
      loadAnalytics(),
    ]);
  };

  return (
    <div style={styles.layout}>
      {/* Sidebar Navigation */}
      <aside style={styles.sidebar}>
        <div style={styles.sidebarTop}>
          <button
            style={styles.brandButton}
            onMouseEnter={(e) => {
              e.currentTarget.style.opacity = "0.8";
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.opacity = "1";
            }}
            onClick={() => setActiveTab("dashboard")}
            title="Go to Dashboard"
          >
            🏦 BankFlow
          </button>
          <nav style={styles.nav}>
            <button
              style={{
                ...styles.navBtn,
                backgroundColor: activeTab === 'dashboard' ? '#0d6360' : 'transparent',
                color: activeTab === 'dashboard' ? '#ffffff' : '#374151',
              }}
              onClick={() => setActiveTab('dashboard')}
            >
              🏠 Dashboard
            </button>
            <button
              style={{
                ...styles.navBtn,
                backgroundColor: activeTab === 'accounts' ? '#0d6360' : 'transparent',
                color: activeTab === 'accounts' ? '#ffffff' : '#374151',
              }}
              onClick={() => setActiveTab('accounts')}
            >
              💳 Accounts
            </button>
            <div style={styles.sidebarGroup}>
              <div style={styles.groupHeader}>
                💸 Payments
              </div>

              <button
                style={{
                  ...styles.subNavLink,
                  backgroundColor:
                    activeTab === "payments" &&
                      paymentSubTab === "transfer"
                      ? "#e6f2f1"
                      : "transparent",
                  color:
                    activeTab === "payments" &&
                      paymentSubTab === "transfer"
                      ? "#0d6360"
                      : "#4b5563",
                }}
                onClick={() => {
                  setActiveTab("payments");
                  setPaymentSubTab("transfer");
                }}
              >
                💸 Transfer Money
              </button>

              <button
                style={{
                  ...styles.subNavLink,
                  backgroundColor:
                    activeTab === "payments" &&
                      paymentSubTab === "scheduled"
                      ? "#e6f2f1"
                      : "transparent",
                  color:
                    activeTab === "payments" &&
                      paymentSubTab === "scheduled"
                      ? "#0d6360"
                      : "#4b5563",
                }}
                onClick={() => {
                  setActiveTab("payments");
                  setPaymentSubTab("scheduled");
                }}
              >
                🔁 Scheduled Transfers
              </button>

              <button
                style={{
                  ...styles.subNavLink,
                  backgroundColor:
                    activeTab === "payments" &&
                      paymentSubTab === "transactions"
                      ? "#e6f2f1"
                      : "transparent",
                  color:
                    activeTab === "payments" &&
                      paymentSubTab === "transactions"
                      ? "#0d6360"
                      : "#4b5563",
                }}
                onClick={() => {
                  setActiveTab("payments");
                  setPaymentSubTab("transactions");
                }}
              >
                📜 Transactions
              </button>
            </div>
            <button
              style={{
                ...styles.navBtn,
                backgroundColor: activeTab === 'loans' ? '#0d6360' : 'transparent',
                color: activeTab === 'loans' ? '#ffffff' : '#374151',
              }}
              onClick={() => setActiveTab('loans')}
            >
              🏠 Loans
            </button>
            <button
              style={{
                ...styles.navBtn,
                backgroundColor: activeTab === 'cards' ? '#0d6360' : 'transparent',
                color: activeTab === 'cards' ? '#ffffff' : '#374151',
              }}
              onClick={() => setActiveTab('cards')}
            >
              💳 Cards
            </button>
            <div style={styles.sidebarGroup}>
              <div style={styles.groupHeader}>🪙 Fixed Deposits</div>
              <button
                style={{
                  ...styles.subNavLink,
                  backgroundColor: activeTab === 'fd' && fdSubTab === 'calculator' ? '#e6f2f1' : 'transparent',
                  color: activeTab === 'fd' && fdSubTab === 'calculator' ? '#0d6360' : '#4b5563',
                }}
                onClick={() => {
                  setActiveTab('fd');
                  setFdSubTab('calculator');
                }}
              >
                📊 FD Calculator
              </button>
              <button
                style={{
                  ...styles.subNavLink,
                  backgroundColor: activeTab === 'fd' && fdSubTab === 'open' ? '#e6f2f1' : 'transparent',
                  color: activeTab === 'fd' && fdSubTab === 'open' ? '#0d6360' : '#4b5563',
                }}
                onClick={() => {
                  setActiveTab('fd');
                  setFdSubTab('open');
                }}
              >
                📄 Open New FD
              </button>
              <button
                style={{
                  ...styles.subNavLink,
                  backgroundColor:
                    activeTab === 'fd' && fdSubTab === 'view'
                      ? '#e6f2f1'
                      : 'transparent',
                  color:
                    activeTab === 'fd' && fdSubTab === 'view'
                      ? '#0d6360'
                      : '#4b5563',
                }}
                onClick={() => {
                  setActiveTab('fd');
                  setFdSubTab('view');
                }}
              >
                💰 View FDs
              </button>
            </div>
          </nav>
        </div>
      </aside>

      {/* Main Content */}
      <main style={styles.mainContent}>
        <header style={styles.topHeader}>
          <div>
            <h1 style={styles.headerTitle}>Welcome back, {customerName}!</h1>
            <p style={styles.headerSubtitle}>
              Manage your accounts & investments.
            </p>
          </div>

          <div style={styles.headerActions}>

            <button
              style={styles.headerAccountBtn}
              onClick={() => setIsAccountModalOpen(true)}
            >
              + Open New Bank Account
            </button>

            <div style={styles.profileMenuWrapper}>

              <button
                style={styles.profileMenuButton}
                onClick={() =>
                  setIsProfileMenuOpen(!isProfileMenuOpen)
                }
              >
                <div style={styles.headerAvatar}>
                  {customerName.charAt(0).toUpperCase()}
                </div>

                <span style={styles.headerUserName}>
                  {customerName}
                </span>

                <ChevronDownIcon
                  style={{
                    ...styles.dropdownArrow,
                    transform: isProfileMenuOpen ? "rotate(180deg)" : "rotate(0deg)",
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
                      setActiveTab("dashboard");
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

        {activeTab === 'dashboard' && (
          <DashboardOverview
            accounts={accounts}
            refreshDashboard={refreshDashboard}
            summary={summary}
            loadingSummary={loadingSummary}
            summaryError={summaryError}
            analytics={analytics}
            loadingAnalytics={loadingAnalytics}
            analyticsError={analyticsError}
          />
        )}

        {activeTab === 'accounts' && (
          <AccountsView
            accounts={accounts}
            loading={loadingAccounts}
            error={accountsError}
            refreshAccounts={loadAccounts}
          />
        )}

        {activeTab === "payments" && (
          <PaymentsView
            activeTab={paymentSubTab}
            accounts={accounts}
            refreshDashboard={refreshDashboard}
          />
        )}

        {activeTab === 'loans' && (
          <LoansView
            accounts={accounts}
            refreshDashboard={refreshDashboard}
          />
        )}

        {activeTab === 'cards' && (
          <CardsView
            refreshDashboard={refreshDashboard}
          />
        )}

        {activeTab === 'fd' && fdSubTab === 'calculator' && (
          <FdCalculatorCard onOpenFd={handleOpenFdFromCalc} />
        )}

        {activeTab === 'fd' && fdSubTab === 'open' && (
          <FdManagementView
            accounts={accounts}
            initialConfig={fdDraftConfig}
            onFdCreated={async () => {
              await refreshDashboard();
              setActiveTab("accounts");
            }}
          />
        )}

        {activeTab === 'fd' && fdSubTab === 'view' && (
          <ViewFds
            onFdClosed={refreshDashboard}
          />
        )}
      </main>

      <CreateAccountModal
        isOpen={isAccountModalOpen}
        onClose={() => setIsAccountModalOpen(false)}
        onAccountCreated={handleAccountCreated}
      />
    </div>
  );
};

const styles = {
  layout: { display: 'flex', minHeight: '100vh', backgroundColor: '#f9fafb' },
  sidebar: { width: '260px', backgroundColor: '#ffffff', borderRight: '1px solid #eef0ec', padding: '24px 16px', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' },
  sidebarTop: { display: 'flex', flexDirection: 'column', gap: '24px' },
  brand: { fontSize: '20px', fontWeight: '800', fontFamily: 'Georgia, serif', color: '#0d6360' },
  nav: { display: 'flex', flexDirection: 'column', gap: '8px' },
  navBtn: { border: 'none', padding: '12px 16px', borderRadius: '8px', textAlign: 'left', fontWeight: '600', fontSize: '14px', cursor: 'pointer' },
  sidebarGroup: { display: 'flex', flexDirection: 'column', gap: '4px', marginTop: '8px' },
  groupHeader: { fontSize: '12px', fontWeight: '700', color: '#9ca3af', textTransform: 'uppercase', padding: '4px 12px' },
  subNavLink: { border: 'none', padding: '10px 16px 10px 24px', borderRadius: '6px', textAlign: 'left', fontWeight: '600', fontSize: '13px', cursor: 'pointer' },
  mainContent: { flex: 1, padding: '32px' },
  topHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '28px', paddingBottom: '16px', borderBottom: '1px solid #eef0ec' },
  headerTitle: { margin: 0, fontSize: '24px', fontFamily: 'Georgia, serif', color: '#111827' },
  headerSubtitle: { margin: '4px 0 0 0', fontSize: '13px', color: '#6b7280' },
  headerAccountBtn: { backgroundColor: '#0d6360', color: '#ffffff', border: 'none', padding: '10px 18px', borderRadius: '8px', fontWeight: '700', fontSize: '14px', cursor: 'pointer' },
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
    backgroundColor: "#0d6360",
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

  headerUserRole: {
    fontSize: "10px",
    fontWeight: "700",
    color: "#0d6360",
    letterSpacing: "0.5px",
    textTransform: "uppercase",
  },

  dropdownArrow: {
    width: "19px",
    height: "19px",
    color: "#4b5563",
    marginLeft: "1px",
    flexShrink: 0,
    transition: "transform 0.2s ease",
  },

  profileIdentity: {
    display: "flex",
    flexDirection: "column",
    alignItems: "flex-start",
    gap: "2px",
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
    color: "#0d6360",
    cursor: "pointer",
    textAlign: "left",
  },
};

export default CustomerDashboard;
