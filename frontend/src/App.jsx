import React, { useEffect, useState } from 'react';
import axios from 'axios';

function App() {
  const [dashboardData, setDashboardData] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // FD Calculator local state
  const [fdAmount, setFdAmount] = useState(25000);
  const [fdTenure, setFdTenure] = useState(3);

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        const token = localStorage.getItem('token');

        const response = await axios.get('/api/v1/dashboard/summary', {
          headers: {
            Authorization: token ? `Bearer ${token}` : '',
            'Content-Type': 'application/json',
          },
        });

        const data = response.data;
        setDashboardData(data);

        // Extract transactions array safely
        if (Array.isArray(data)) {
          setTransactions(data);
        } else if (Array.isArray(data?.transactions)) {
          setTransactions(data.transactions);
        } else if (Array.isArray(data?.recentTransactions)) {
          setTransactions(data.recentTransactions);
        } else if (Array.isArray(data?.content)) {
          setTransactions(data.content);
        } else {
          setTransactions([]);
        }

        setLoading(false);
      } catch (err) {
        console.error('Failed to fetch dashboard data:', err);
        setError('Unable to load dashboard data.');
        setTransactions([]);
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  if (loading) {
    return (
      <div style={styles.loadingContainer}>
        <div style={styles.spinner}>Loading BankFlow...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div style={styles.loadingContainer}>
        <div style={{ color: '#dc2626', fontWeight: 'bold' }}>{error}</div>
      </div>
    );
  }

  const totalBalance = dashboardData?.totalBalance ?? dashboardData?.balance ?? 0;
  const totalAccounts = dashboardData?.totalAccounts ?? dashboardData?.accountCount ?? 2;

  // Simple maturity calculation for FD widget
  const calculateMaturity = () => {
    const rate = 0.07; // 7% interest
    const maturity = fdAmount * Math.pow(1 + rate, fdTenure);
    return Math.round(maturity).toLocaleString('en-IN');
  };

  return (
    <div style={styles.layout}>
      {/* Sidebar Navigation */}
      <aside style={styles.sidebar}>
        <div style={styles.navSection}>
          <div style={{ ...styles.navItem, ...styles.navItemActive }}>
            <span style={styles.navIcon}>📊</span>
            <span>Overview</span>
          </div>
          <div style={styles.navItem}>
            <span style={styles.navIcon}>💳</span>
            <span>Accounts</span>
          </div>
          <div style={styles.navItem}>
            <span style={styles.navIcon}>⇅</span>
            <span>Transactions</span>
          </div>
          <div style={styles.navItem}>
            <span style={styles.navIcon}>🪙</span>
            <span>Fixed deposits</span>
          </div>
        </div>

        <div style={styles.sidebarFooter}>
          <div style={styles.navItem}>
            <span style={styles.navIcon}>?</span>
            <span>Help & support</span>
          </div>
          <div style={styles.userProfile}>
            <div style={styles.avatar}>MC</div>
            <div>
              <div style={styles.userName}>Maya Choudhary</div>
              <div style={styles.userRole}>Customer account</div>
            </div>
          </div>
        </div>
      </aside>

      {/* Main Content Area */}
      <main style={styles.mainContent}>
        {/* Teal Hero Banner */}
        <section style={styles.heroCard}>
          <div>
            <div style={styles.heroSublabel}>
              TOTAL AVAILABLE BALANCE <span style={styles.dotIndicator}>●</span>
            </div>
            <div style={styles.heroAmount}>
              ₹{Number(totalBalance).toLocaleString('en-IN')}
            </div>
            <div style={styles.heroSubtext}>Across {totalAccounts} active accounts</div>
          </div>

          <div style={styles.heroButtons}>
            <button style={styles.btnSecondary}>+ Add money</button>
            <button style={styles.btnPrimary}>↗ Transfer</button>
          </div>
        </section>

        {/* 2-Column Grid */}
        <div style={styles.gridTwoColumns}>
          {/* Your Accounts Card */}
          <div style={styles.card}>
            <div style={styles.cardHeader}>
              <div>
                <span style={styles.sectionTag}>YOUR ACCOUNTS</span>
                <h2 style={styles.sectionTitle}>Everyday banking</h2>
              </div>
              <a href="#accounts" style={styles.linkText}>View all →</a>
            </div>

            <div style={styles.accountList}>
              <div style={styles.accountRow}>
                <div style={styles.accountIconBox}>📄</div>
                <div style={styles.accountMeta}>
                  <div style={styles.accountName}>Primary savings</div>
                  <div style={styles.accountDetails}>•••• 4821 · Bengaluru</div>
                </div>
                <div style={styles.accountBalance}>
                  <div style={styles.balanceAmount}>₹{Number(totalBalance * 0.7 || 186450).toLocaleString('en-IN')}</div>
                  <div style={styles.balanceLabel}>Available balance</div>
                </div>
              </div>

              <div style={styles.accountRow}>
                <div style={{ ...styles.accountIconBox, backgroundColor: '#fef3c7' }}>🧳</div>
                <div style={styles.accountMeta}>
                  <div style={styles.accountName}>Travel fund</div>
                  <div style={styles.accountDetails}>•••• 9204 · Bengaluru</div>
                </div>
                <div style={styles.accountBalance}>
                  <div style={styles.balanceAmount}>₹82,000</div>
                  <div style={styles.balanceLabel}>Available balance</div>
                </div>
              </div>
            </div>
          </div>

          {/* Recent Activity Card */}
          <div style={styles.card}>
            <div style={styles.cardHeader}>
              <div>
                <span style={styles.sectionTag}>RECENT ACTIVITY</span>
                <h2 style={styles.sectionTitle}>Latest transactions</h2>
              </div>
              <a href="#activity" style={styles.linkText}>See activity →</a>
            </div>

            <div style={styles.transactionList}>
              {Array.isArray(transactions) && transactions.length > 0 ? (
                transactions.map((tx, idx) => {
                  const isDebit = tx.amount < 0 || tx.description?.toLowerCase().includes('payment') || tx.description?.toLowerCase().includes('transfer to');
                  const amount = Math.abs(tx.amount || 0);

                  return (
                    <div key={tx.id || idx} style={styles.txRow}>
                      <div style={isDebit ? styles.txCircleDebit : styles.txCircleCredit}>
                        {isDebit ? '↗' : '↙'}
                      </div>
                      <div style={styles.txDetails}>
                        <div style={styles.txTitle}>{tx.description || tx.merchantName || 'Transaction'}</div>
                        <div style={styles.txSub}>
                          {tx.date || 'Recent'} · {isDebit ? 'Debit' : 'Credit'}
                        </div>
                      </div>
                      <div style={isDebit ? styles.txAmountDebit : styles.txAmountCredit}>
                        {isDebit ? '-₹' : '+₹'}{amount.toLocaleString('en-IN')}
                      </div>
                    </div>
                  );
                })
              ) : (
                <div style={styles.emptyText}>No recent activity.</div>
              )}
            </div>
          </div>
        </div>

        {/* Fixed Deposit Calculator Footer Card */}
        <section style={styles.fdBanner}>
          <div style={styles.fdInfo}>
            <div style={styles.fdBadge}>%</div>
            <div>
              <span style={styles.sectionTag}>GROW YOUR SAVINGS</span>
              <h3 style={styles.fdTitle}>Fixed deposit calculator</h3>
              <p style={styles.fdSub}>See what your savings could become with a guaranteed return.</p>
            </div>
          </div>

          <div style={styles.fdControls}>
            <div style={styles.inputGroup}>
              <label style={styles.inputLabel}>Deposit amount</label>
              <select 
                value={fdAmount} 
                onChange={(e) => setFdAmount(Number(e.target.value))}
                style={styles.selectInput}
              >
                <option value={25000}>25000</option>
                <option value={50000}>50000</option>
                <option value={100000}>100000</option>
              </select>
            </div>

            <div style={styles.inputGroup}>
              <label style={styles.inputLabel}>Tenure</label>
              <select 
                value={fdTenure} 
                onChange={(e) => setFdTenure(Number(e.target.value))}
                style={styles.selectInput}
              >
                <option value={1}>1 year</option>
                <option value={3}>3 years</option>
                <option value={5}>5 years</option>
              </select>
            </div>

            <div style={styles.maturityBlock}>
              <span style={styles.maturityLabel}>AT MATURITY</span>
              <div style={styles.maturityValue}>₹{calculateMaturity()}</div>
            </div>

            <button style={styles.fdButton}>Open an FD →</button>
          </div>
        </section>
      </main>
    </div>
  );
}

// Custom CSS to replicate the UI exactly
const styles = {
  layout: {
    display: 'flex',
    minHeight: '100vh',
    backgroundColor: '#f5f6f4',
    fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Georgia, serif',
    color: '#1a1a1a',
  },
  sidebar: {
    width: '240px',
    backgroundColor: '#f5f6f4',
    borderRight: '1px solid #e5e7eb',
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'space-between',
    padding: '32px 16px',
  },
  navSection: {
    display: 'flex',
    flexDirection: 'column',
    gap: '8px',
  },
  navItem: {
    display: 'flex',
    alignItems: 'center',
    gap: '12px',
    padding: '10px 14px',
    borderRadius: '8px',
    fontSize: '14px',
    fontWeight: '500',
    color: '#4b5563',
    cursor: 'pointer',
  },
  navItemActive: {
    backgroundColor: '#e2ece9',
    color: '#0f4c42',
    fontWeight: '600',
  },
  navIcon: {
    fontSize: '16px',
  },
  sidebarFooter: {
    display: 'flex',
    flexDirection: 'column',
    gap: '16px',
  },
  userProfile: {
    display: 'flex',
    alignItems: 'center',
    gap: '12px',
    paddingTop: '16px',
    borderTop: '1px solid #e5e7eb',
  },
  avatar: {
    width: '36px',
    height: '36px',
    borderRadius: '50%',
    backgroundColor: '#e6d8c3',
    color: '#785b37',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontWeight: '700',
    fontSize: '13px',
  },
  userName: {
    fontSize: '13px',
    fontWeight: '700',
    color: '#111827',
  },
  userRole: {
    fontSize: '11px',
    color: '#6b7280',
  },
  mainContent: {
    flex: 1,
    padding: '40px',
    display: 'flex',
    flexDirection: 'column',
    gap: '24px',
    maxWidth: '1200px',
  },
  heroCard: {
    backgroundColor: '#0d6360',
    borderRadius: '16px',
    padding: '36px 40px',
    color: '#ffffff',
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  heroSublabel: {
    fontSize: '11px',
    letterSpacing: '1px',
    fontWeight: '700',
    color: '#a3d9d3',
    display: 'flex',
    alignItems: 'center',
    gap: '6px',
  },
  dotIndicator: {
    fontSize: '8px',
    color: '#52b7a5',
  },
  heroAmount: {
    fontSize: '44px',
    fontFamily: 'Georgia, serif',
    fontWeight: '400',
    margin: '12px 0 6px 0',
  },
  heroSubtext: {
    fontSize: '13px',
    color: '#a3d9d3',
  },
  heroButtons: {
    display: 'flex',
    gap: '12px',
  },
  btnSecondary: {
    backgroundColor: 'rgba(255, 255, 255, 0.15)',
    border: 'none',
    color: '#ffffff',
    padding: '10px 20px',
    borderRadius: '8px',
    fontWeight: '600',
    fontSize: '13px',
    cursor: 'pointer',
  },
  btnPrimary: {
    backgroundColor: '#ffffff',
    border: 'none',
    color: '#0d6360',
    padding: '10px 24px',
    borderRadius: '8px',
    fontWeight: '700',
    fontSize: '13px',
    cursor: 'pointer',
  },
  gridTwoColumns: {
    display: 'grid',
    gridTemplateColumns: '1fr 1fr',
    gap: '24px',
  },
  card: {
    backgroundColor: '#ffffff',
    borderRadius: '16px',
    padding: '28px',
    border: '1px solid #eef0ec',
    boxShadow: '0 1px 3px rgba(0,0,0,0.02)',
  },
  cardHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: '24px',
  },
  sectionTag: {
    fontSize: '11px',
    fontWeight: '700',
    letterSpacing: '0.5px',
    color: '#6b7280',
    display: 'block',
    marginBottom: '4px',
  },
  sectionTitle: {
    fontSize: '22px',
    fontFamily: 'Georgia, serif',
    fontWeight: '700',
    margin: 0,
    color: '#111827',
  },
  linkText: {
    fontSize: '13px',
    color: '#0d6360',
    fontWeight: '600',
    textDecoration: 'none',
  },
  accountList: {
    display: 'flex',
    flexDirection: 'column',
    gap: '20px',
  },
  accountRow: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingBottom: '16px',
    borderBottom: '1px solid #f3f4f6',
  },
  accountIconBox: {
    width: '40px',
    height: '40px',
    borderRadius: '8px',
    backgroundColor: '#e0f2fe',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontSize: '18px',
  },
  accountMeta: {
    flex: 1,
    marginLeft: '16px',
  },
  accountName: {
    fontWeight: '700',
    fontSize: '14px',
    color: '#111827',
  },
  accountDetails: {
    fontSize: '12px',
    color: '#6b7280',
    marginTop: '2px',
  },
  accountBalance: {
    textAlign: 'right',
  },
  balanceAmount: {
    fontWeight: '700',
    fontSize: '15px',
    color: '#111827',
  },
  balanceLabel: {
    fontSize: '11px',
    color: '#9ca3af',
  },
  transactionList: {
    display: 'flex',
    flexDirection: 'column',
    gap: '18px',
  },
  txRow: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  txCircleDebit: {
    width: '36px',
    height: '36px',
    borderRadius: '50%',
    backgroundColor: '#fef2f2',
    color: '#dc2626',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontWeight: 'bold',
  },
  txCircleCredit: {
    width: '36px',
    height: '36px',
    borderRadius: '50%',
    backgroundColor: '#dcfce7',
    color: '#16a34a',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontWeight: 'bold',
  },
  txDetails: {
    flex: 1,
    marginLeft: '14px',
  },
  txTitle: {
    fontWeight: '600',
    fontSize: '14px',
    color: '#111827',
  },
  txSub: {
    fontSize: '12px',
    color: '#6b7280',
    marginTop: '2px',
  },
  txAmountCredit: {
    fontWeight: '700',
    fontSize: '14px',
    color: '#16a34a',
  },
  txAmountDebit: {
    fontWeight: '700',
    fontSize: '14px',
    color: '#111827',
  },
  fdBanner: {
    backgroundColor: '#efece6',
    borderRadius: '16px',
    padding: '28px 32px',
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  fdInfo: {
    display: 'flex',
    alignItems: 'center',
    gap: '20px',
  },
  fdBadge: {
    width: '48px',
    height: '48px',
    borderRadius: '50%',
    backgroundColor: '#e6d08d',
    color: '#785b37',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontSize: '20px',
    fontWeight: 'bold',
  },
  fdTitle: {
    fontSize: '20px',
    fontFamily: 'Georgia, serif',
    fontWeight: '700',
    margin: '2px 0 4px 0',
  },
  fdSub: {
    fontSize: '13px',
    color: '#6b7280',
    margin: 0,
  },
  fdControls: {
    display: 'flex',
    alignItems: 'center',
    gap: '24px',
  },
  inputGroup: {
    display: 'flex',
    flexDirection: 'column',
    gap: '4px',
  },
  inputLabel: {
    fontSize: '11px',
    fontWeight: '700',
    color: '#6b7280',
  },
  selectInput: {
    padding: '8px 12px',
    borderRadius: '8px',
    border: '1px solid #d1d5db',
    backgroundColor: '#ffffff',
    fontSize: '13px',
    fontWeight: '600',
  },
  maturityBlock: {
    textAlign: 'left',
  },
  maturityLabel: {
    fontSize: '10px',
    fontWeight: '700',
    letterSpacing: '0.5px',
    color: '#6b7280',
  },
  maturityValue: {
    fontSize: '20px',
    fontWeight: '700',
    fontFamily: 'Georgia, serif',
    color: '#111827',
  },
  fdButton: {
    backgroundColor: '#0f4c42',
    color: '#ffffff',
    border: 'none',
    padding: '12px 20px',
    borderRadius: '8px',
    fontWeight: '700',
    fontSize: '13px',
    cursor: 'pointer',
  },
  loadingContainer: {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    height: '100vh',
    backgroundColor: '#f5f6f4',
  },
  spinner: {
    fontSize: '18px',
    color: '#0d6360',
    fontWeight: '600',
  },
  emptyText: {
    color: '#9ca3af',
    fontSize: '13px',
  },
};

export default App;
