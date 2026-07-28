import React, { useState } from 'react';
import API from '../api/axios';

const FdManagementView = ({ initialConfig, onFdCreated, accounts = [] }) => {
  const [sourceAccountNumber, setSourceAccountNumber] = useState(
    accounts[0]?.accountNumber || initialConfig?.sourceAccountNumber || ''
  );
  const [depositAmount, setDepositAmount] = useState(
    initialConfig?.principal || initialConfig?.depositAmount || 24045.93
  );
  const [tenureYears, setTenureYears] = useState(initialConfig?.tenureYears || 3);
  const [loading, setLoading] = useState(false);
  const [successMsg, setSuccessMsg] = useState(null);
  const [errorMsg, setErrorMsg] = useState(null);

  // Updated interest rate mapping: 6.5% for 1 yr, 7% for 3 yrs, 7.5% for 5 yrs
  const interestRates = {
    1: '6.5% p.a.',
    3: '7.0% p.a.',
    5: '7.5% p.a.',
  };

  const handleCreateFd = async (e) => {
    e.preventDefault();
    setLoading(true);
    setSuccessMsg(null);
    setErrorMsg(null);

    try {
      await API.post('/fd/create', {
        sourceAccountNumber: sourceAccountNumber,
        depositAmount: Number(depositAmount),
        tenureYears: Number(tenureYears),
      });

      setSuccessMsg('Fixed Deposit created successfully!');
      setTimeout(() => {
        if (onFdCreated) onFdCreated();
      }, 1200);
    } catch (err) {
      console.error('Failed to create FD:', err);
      setErrorMsg(err.response?.data?.message || 'Failed to create Fixed Deposit.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.card}>
      <h2 style={styles.title}>📄 Open Fixed Deposit</h2>
      <p style={styles.subtitle}>Lock in high returns with guaranteed interest rates.</p>

      {successMsg && <div style={styles.successBox}>✅ {successMsg}</div>}
      {errorMsg && <div style={styles.errorBox}>⚠️ {errorMsg}</div>}

      <form onSubmit={handleCreateFd} style={styles.form}>
        <div style={styles.inputGroup}>
          <label style={styles.label}>Source Account Number</label>
          <input
            type="text"
            value={sourceAccountNumber}
            onChange={(e) => setSourceAccountNumber(e.target.value)}
            placeholder="e.g. BF5891164768"
            required
            style={styles.input}
          />
        </div>

        <div style={styles.inputGroup}>
          <label style={styles.label}>Deposit Amount (₹)</label>
          <input
            type="number"
            step="0.01"
            value={depositAmount}
            onChange={(e) => setDepositAmount(e.target.value)}
            required
            style={styles.input}
          />
        </div>

        <div style={styles.inputGroup}>
          <label style={styles.label}>Tenure (Years)</label>
          <select
            value={tenureYears}
            onChange={(e) => setTenureYears(Number(e.target.value))}
            style={styles.input}
          >
            <option value={1}>1 Year (6.5%)</option>
            <option value={3}>3 Years (7%)</option>
            <option value={5}>5 Years (7.5%)</option>
          </select>
        </div>

        <div style={styles.inputGroup}>
          <label style={styles.label}>Applicable Interest Rate</label>
          <input
            type="text"
            value={interestRates[tenureYears] || '7.0% p.a.'}
            disabled
            style={{ ...styles.input, backgroundColor: '#f3f4f6', fontWeight: '700', color: '#0d6360' }}
          />
        </div>

        <button type="submit" disabled={loading} style={styles.button}>
          {loading ? 'Processing FD Creation...' : 'Confirm & Open Fixed Deposit'}
        </button>
      </form>
    </div>
  );
};

const styles = {
  card: { backgroundColor: '#ffffff', borderRadius: '16px', padding: '32px', maxWidth: '600px', border: '1px solid #eef0ec' },
  title: { margin: '0 0 4px 0', fontSize: '22px', fontFamily: 'Georgia, serif', color: '#111827' },
  subtitle: { margin: '0 0 24px 0', fontSize: '13px', color: '#6b7280' },
  form: { display: 'flex', flexDirection: 'column', gap: '16px' },
  inputGroup: { display: 'flex', flexDirection: 'column', gap: '6px' },
  label: { fontSize: '13px', fontWeight: '600', color: '#374151' },
  input: { padding: '10px 14px', borderRadius: '8px', border: '1px solid #d1d5db', fontSize: '14px', outline: 'none' },
  button: { backgroundColor: '#0d6360', color: '#ffffff', padding: '12px', borderRadius: '8px', border: 'none', fontWeight: '700', fontSize: '14px', cursor: 'pointer', marginTop: '12px' },
  successBox: { backgroundColor: '#dcfce7', color: '#15803d', padding: '12px', borderRadius: '8px', fontSize: '13px', marginBottom: '16px' },
  errorBox: { backgroundColor: '#fee2e2', color: '#991b1b', padding: '12px', borderRadius: '8px', fontSize: '13px', marginBottom: '16px' },
};

export default FdManagementView;
