import React, { useState, useEffect } from 'react';
import API from '../api/axios';

const AccountsCard = () => {
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchSummary = async () => {
      try {
        const response = await API.get('/dashboard/summary');
        setSummary(response.data);
      } catch (err) {
        console.error('Failed to fetch dashboard summary:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchSummary();
  }, []);

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 2,
    }).format(amount || 0);
  };

  if (loading) return <div style={{ color: '#6b7280', fontSize: '14px' }}>Loading summary...</div>;

  return (
    <div style={styles.card}>
      <div style={styles.header}>
        <h3 style={styles.title}>Your Accounts</h3>
        <span style={styles.countBadge}>{summary?.activeAccountsCount || 1} Active</span>
      </div>

      <div style={styles.accountList}>
        {/* Primary Savings / Operations Account */}
        <div style={styles.accountItem}>
          <div style={styles.iconBox}>🏦</div>
          <div style={styles.accountInfo}>
            <div style={styles.accountName}>Total Account Balance</div>
            <div style={styles.accountType}>Savings / Current</div>
          </div>
          <div style={styles.accountBalance}>
            {formatCurrency(summary?.totalAccountBalance)}
          </div>
        </div>

        {/* Fixed Deposit Summary Account */}
        <div style={styles.accountItem}>
          <div style={styles.iconBox}>🪙</div>
          <div style={styles.accountInfo}>
            <div style={styles.accountName}>Fixed Deposits (FD)</div>
            <div style={styles.accountType}>{summary?.activeFdCount || 0} Active FDs</div>
          </div>
          <div style={{ ...styles.accountBalance, color: '#0d6360' }}>
            {formatCurrency(summary?.totalFdInvestment)}
          </div>
        </div>
      </div>
    </div>
  );
};

const styles = {
  card: {
    backgroundColor: '#ffffff',
    borderRadius: '16px',
    padding: '24px',
    border: '1px solid #eef0ec',
    boxShadow: '0 2px 8px rgba(0,0,0,0.02)',
    display: 'flex',
    flexDirection: 'column',
    gap: '16px',
  },
  header: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  title: {
    margin: 0,
    fontSize: '18px',
    fontFamily: 'Georgia, serif',
    color: '#111827',
  },
  countBadge: {
    backgroundColor: '#e2ece9',
    color: '#0f4c42',
    padding: '4px 10px',
    borderRadius: '12px',
    fontSize: '12px',
    fontWeight: '700',
  },
  accountList: {
    display: 'flex',
    flexDirection: 'column',
    gap: '12px',
  },
  accountItem: {
    display: 'flex',
    alignItems: 'center',
    padding: '14px',
    borderRadius: '12px',
    backgroundColor: '#f9fafb',
    border: '1px solid #f3f4f6',
    gap: '12px',
  },
  iconBox: {
    width: '40px',
    height: '40px',
    borderRadius: '10px',
    backgroundColor: '#ffffff',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontSize: '18px',
    boxShadow: '0 1px 3px rgba(0,0,0,0.05)',
  },
  accountInfo: {
    flex: 1,
  },
  accountName: {
    fontSize: '14px',
    fontWeight: '700',
    color: '#1f2937',
  },
  accountType: {
    fontSize: '12px',
    color: '#6b7280',
    marginTop: '2px',
  },
  accountBalance: {
    fontSize: '15px',
    fontWeight: '700',
    color: '#111827',
  },
};

export default AccountsCard;
