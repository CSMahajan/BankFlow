import React, { useState, useEffect } from 'react';
import API from '../api/axios';
import TransferModal from './TransferModal'; // 🟢 1. Import your TransferModal component

const AccountsView = () => {
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isTransferOpen, setIsTransferOpen] = useState(false); // 🟢 2. State to manage modal visibility

  const fetchAccounts = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await API.get('/accounts/my-accounts');
      setAccounts(response.data || []);
    } catch (err) {
      console.error('Failed to fetch accounts:', err);
      setError('Unable to load your accounts.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAccounts();
  }, []);

  const formatCurrency = (val) =>
    new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 2,
    }).format(val || 0);

  const totalBalance = accounts.reduce((sum, acc) => sum + (Number(acc.currentBalance) || 0), 0);
  const savingsAccounts = accounts.filter((acc) => acc.accountType === 'SAVINGS');
  const currentAccounts = accounts.filter((acc) => acc.accountType === 'CURRENT');

  return (
    <div style={styles.container}>
      {/* Summary & Quick Actions Header */}
      <div style={styles.summaryCard}>
        <div>
          <span style={styles.summaryLabel}>Total Consolidated Balance</span>
          <h2 style={styles.summaryAmount}>{formatCurrency(totalBalance)}</h2>
        </div>
        <div style={styles.quickActions}>
          {/* 🟢 3. Trigger modal open on button click */}
          <button style={styles.actionBtnPrimary} onClick={() => setIsTransferOpen(true)}>
            💸 Transfer Funds
          </button>
          <button style={styles.actionBtnSecondary} onClick={fetchAccounts}>
            🔄 Refresh
          </button>
        </div>
      </div>

      {error && <div style={styles.errorBox}>⚠️ {error}</div>}

      {loading ? (
        <div style={{ color: '#6b7280' }}>Loading accounts...</div>
      ) : accounts.length === 0 ? (
        <div style={styles.emptyCard}>
          <div style={{ fontSize: '40px' }}>🏦</div>
          <h3 style={{ margin: '8px 0 4px 0' }}>No Active Accounts Found</h3>
          <p style={{ color: '#6b7280', fontSize: '14px' }}>
            Use the <strong>"+ Open New Bank Account"</strong> button above to open an account.
          </p>
        </div>
      ) : (
        <div style={styles.sectionsGrid}>
          {/* SAVINGS */}
          <div style={styles.section}>
            <div style={styles.sectionHeader}>
              <h3 style={styles.sectionTitle}>💰 Savings Accounts</h3>
              <span style={styles.countBadge}>{savingsAccounts.length} Active</span>
            </div>
            {savingsAccounts.length === 0 ? (
              <p style={styles.noneText}>No active Savings account.</p>
            ) : (
              savingsAccounts.map((acc) => (
                <div key={acc.accountNumber} style={styles.accountCard}>
                  <div style={styles.cardTop}>
                    <span style={styles.accNumber}>{acc.accountNumber}</span>
                    <span style={styles.badgeSavings}>SAVINGS</span>
                  </div>
                  <div style={styles.cardBottom}>
                    <span style={styles.balanceLabel}>Holder: {acc.userName || localStorage.getItem('fullName') || 'Customer'}</span>
                    <span style={styles.balanceAmount}>{formatCurrency(acc.currentBalance)}</span>
                  </div>
                </div>
              ))
            )}
          </div>

          {/* CURRENT */}
          <div style={styles.section}>
            <div style={styles.sectionHeader}>
              <h3 style={styles.sectionTitle}>🏢 Current Accounts</h3>
              <span style={styles.countBadge}>{currentAccounts.length} Active</span>
            </div>
            {currentAccounts.length === 0 ? (
              <p style={styles.noneText}>No active Current account.</p>
            ) : (
              currentAccounts.map((acc) => (
                <div key={acc.accountNumber} style={styles.accountCard}>
                  <div style={styles.cardTop}>
                    <span style={styles.accNumber}>{acc.accountNumber}</span>
                    <span style={styles.badgeCurrent}>CURRENT</span>
                  </div>
                  <div style={styles.cardBottom}>
                    <span style={styles.balanceLabel}>Holder: {acc.userName || localStorage.getItem('fullName') || 'Customer'}</span>
                    <span style={styles.balanceAmount}>{formatCurrency(acc.currentBalance)}</span>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      )}

      {/* 🟢 4. Include the TransferModal component here */}
      <TransferModal
        isOpen={isTransferOpen}
        onClose={() => setIsTransferOpen(false)}
        accounts={accounts}
        onTransferSuccess={fetchAccounts}
      />
    </div>
  );
};

const styles = {
  container: { display: 'flex', flexDirection: 'column', gap: '20px' },
  summaryCard: { backgroundColor: '#0d6360', color: '#ffffff', borderRadius: '16px', padding: '24px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' },
  summaryLabel: { fontSize: '13px', opacity: 0.9, textTransform: 'uppercase', letterSpacing: '0.5px' },
  summaryAmount: { margin: '4px 0 0 0', fontSize: '32px', fontWeight: '800' },
  quickActions: { display: 'flex', gap: '10px' },
  actionBtnPrimary: { backgroundColor: '#ffffff', color: '#0d6360', border: 'none', padding: '10px 16px', borderRadius: '8px', fontWeight: '700', cursor: 'pointer' },
  actionBtnSecondary: { backgroundColor: 'transparent', color: '#ffffff', border: '1px solid rgba(255,255,255,0.4)', padding: '10px 16px', borderRadius: '8px', fontWeight: '700', cursor: 'pointer' },
  sectionsGrid: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px' },
  section: { backgroundColor: '#ffffff', borderRadius: '16px', padding: '20px', border: '1px solid #eef0ec', display: 'flex', flexDirection: 'column', gap: '16px' },
  sectionHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'center' },
  sectionTitle: { margin: 0, fontSize: '16px', color: '#111827' },
  countBadge: { fontSize: '12px', backgroundColor: '#f3f4f6', padding: '2px 8px', borderRadius: '12px', color: '#4b5563' },
  accountCard: { backgroundColor: '#f9fafb', borderRadius: '12px', padding: '16px', border: '1px solid #e5e7eb', display: 'flex', flexDirection: 'column', gap: '12px' },
  cardTop: { display: 'flex', justifyContent: 'space-between', alignItems: 'center' },
  accNumber: { fontFamily: 'monospace', fontWeight: '700', fontSize: '14px' },
  badgeSavings: { backgroundColor: '#dcfce7', color: '#15803d', padding: '3px 8px', borderRadius: '6px', fontSize: '11px', fontWeight: '700' },
  badgeCurrent: { backgroundColor: '#e0f2fe', color: '#0369a1', padding: '3px 8px', borderRadius: '6px', fontSize: '11px', fontWeight: '700' },
  cardBottom: { display: 'flex', flexDirection: 'column', gap: '4px' },
  balanceLabel: { fontSize: '12px', color: '#6b7280' },
  balanceAmount: { fontSize: '20px', fontWeight: '800', color: '#0d6360' },
  noneText: { fontSize: '13px', color: '#9ca3af', fontStyle: 'italic', margin: 0 },
  errorBox: { backgroundColor: '#fee2e2', color: '#991b1b', padding: '12px', borderRadius: '8px', fontSize: '13px' },
  emptyCard: { backgroundColor: '#ffffff', borderRadius: '16px', padding: '40px', textAlign: 'center', border: '1px solid #eef0ec' },
};

export default AccountsView;
