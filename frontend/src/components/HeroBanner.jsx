import React from 'react';

const HeroBanner = ({ totalBalance, accountCount, onOpenTransfer, onRefreshBalance }) => {
  // Format currency neatly as INR (₹)
  const formattedBalance = new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 2,
  }).format(totalBalance || 0);

  return (
    <div style={styles.banner}>
      <div style={styles.leftSection}>
        <span style={styles.badge}>Total Available Balance</span>
        <h1 style={styles.balanceAmount}>{formattedBalance}</h1>
        <p style={styles.accountSubtitle}>
          Across {accountCount || 1} active {accountCount === 1 ? 'account' : 'accounts'}
        </p>
      </div>

      <div style={styles.actionButtons}>
        <button style={styles.secondaryBtn} onClick={onRefreshBalance} title="Refresh Balance">
          ➕ Add / Refresh Money
        </button>
        <button style={styles.primaryBtn} onClick={onOpenTransfer}>
          💸 Transfer Funds →
        </button>
      </div>
    </div>
  );
};

const styles = {
  banner: {
    backgroundColor: '#0d6360',
    color: '#ffffff',
    borderRadius: '16px',
    padding: '32px 36px',
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    boxShadow: '0 10px 25px rgba(13, 99, 96, 0.18)',
  },
  leftSection: {
    display: 'flex',
    flexDirection: 'column',
    gap: '6px',
  },
  badge: {
    fontSize: '12px',
    fontWeight: '700',
    textTransform: 'uppercase',
    letterSpacing: '0.8px',
    color: '#a7f3d0',
  },
  balanceAmount: {
    fontSize: '36px',
    fontWeight: '800',
    margin: 0,
    fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
  },
  accountSubtitle: {
    fontSize: '13px',
    color: '#d1fae5',
    margin: 0,
  },
  actionButtons: {
    display: 'flex',
    gap: '12px',
  },
  primaryBtn: {
    backgroundColor: '#ffffff',
    color: '#0d6360',
    border: 'none',
    padding: '12px 20px',
    borderRadius: '10px',
    fontWeight: '700',
    fontSize: '14px',
    cursor: 'pointer',
    boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
  },
  secondaryBtn: {
    backgroundColor: 'rgba(255, 255, 255, 0.15)',
    color: '#ffffff',
    border: '1px solid rgba(255, 255, 255, 0.3)',
    padding: '12px 18px',
    borderRadius: '10px',
    fontWeight: '600',
    fontSize: '14px',
    cursor: 'pointer',
  },
};

export default HeroBanner;
