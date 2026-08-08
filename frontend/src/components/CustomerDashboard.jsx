import React, { useState, useEffect } from 'react';
import AccountsView from './accounts/AccountsView';
import FdCalculatorCard from './fds/FdCalculatorCard';
import FdManagementView from './fds/FdManagementView';
import ViewFds from './fds/ViewFds';
import DashboardOverview from '../components/DashboardOverview';
import CreateAccountModal from './accounts/CreateAccountModal';
import PaymentsView from "./payments/PaymentsView";
import LoansView from './loans/LoansView';
import CardsView from "./cards/CardsView";
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

  const customerName =
    localStorage.getItem('fullName') ||
    localStorage.getItem('name') ||
    'Customer';

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
          <div style={styles.brand}>🏦 BankFlow</div>
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

        {/* User Footer */}
        <div style={styles.sidebarFooter}>
          <div style={styles.userProfile}>
            <div style={styles.avatar}>{customerName.charAt(0).toUpperCase()}</div>
            <div style={styles.userInfo}>
              <strong style={styles.userName}>{customerName}</strong>
              <span style={styles.roleBadge}>{roleDisplay}</span>
            </div>
          </div>
          <button style={styles.logoutBtn} onClick={onLogout} title="Log Out">
            🚪 Log Out
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main style={styles.mainContent}>
        <header style={styles.topHeader}>
          <div>
            <h1 style={styles.headerTitle}>Customer Dashboard</h1>
            <p style={styles.headerSubtitle}>
              Welcome back, <strong>{customerName}</strong>! Manage your accounts & investments.
            </p>
          </div>

          <button style={styles.headerAccountBtn} onClick={() => setIsAccountModalOpen(true)}>
            + Open New Bank Account
          </button>
        </header>

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
            refreshAccounts={loadAccounts}
            refreshSummary={loadDashboardSummary}
          />
        )}

        {activeTab === 'loans' && (
          <LoansView
            accounts={accounts}
          />
        )}

        {activeTab === 'cards' && (
          <CardsView
            refreshSummary={loadDashboardSummary}
            refreshAccounts={loadAccounts}
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
  sidebarFooter: { borderTop: '1px solid #eef0ec', paddingTop: '16px', display: 'flex', flexDirection: 'column', gap: '12px' },
  userProfile: { display: 'flex', alignItems: 'center', gap: '10px' },
  avatar: { width: '36px', height: '36px', borderRadius: '50%', backgroundColor: '#0d6360', color: '#ffffff', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: '700', fontSize: '15px' },
  userInfo: { display: 'flex', flexDirection: 'column', gap: '2px', overflow: 'hidden' },
  userName: { fontSize: '14px', color: '#111827', fontWeight: '700', lineHeight: '1.2', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' },
  roleBadge: { fontSize: '11px', fontWeight: '700', color: '#0d6360', backgroundColor: '#e6f2f1', padding: '2px 6px', borderRadius: '4px', width: 'fit-content', textTransform: 'uppercase' },
  logoutBtn: { border: '1px solid #fee2e2', padding: '10px', borderRadius: '8px', backgroundColor: '#fef2f2', cursor: 'pointer', fontWeight: '600', color: '#dc2626', fontSize: '13px', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '6px' },
  mainContent: { flex: 1, padding: '32px' },
  topHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '28px', paddingBottom: '16px', borderBottom: '1px solid #eef0ec' },
  headerTitle: { margin: 0, fontSize: '24px', fontFamily: 'Georgia, serif', color: '#111827' },
  headerSubtitle: { margin: '4px 0 0 0', fontSize: '13px', color: '#6b7280' },
  headerAccountBtn: { backgroundColor: '#0d6360', color: '#ffffff', border: 'none', padding: '10px 18px', borderRadius: '8px', fontWeight: '700', fontSize: '14px', cursor: 'pointer' },
};

export default CustomerDashboard;
