import React, { useState } from 'react';
import { createFixedDeposit } from "../api/bankService";
import { FD_RATES, FD_TENURES } from "./fdConfig";

const FdManagementView = ({ initialConfig, onFdCreated, accounts = [] }) => {
  const [sourceAccountNumber, setSourceAccountNumber] = useState(
    accounts[0]?.accountNumber || initialConfig?.sourceAccountNumber || ''
  );
  const [depositAmount, setDepositAmount] = useState(
    initialConfig?.depositAmount ?? ""
  );
  const [tenureYears, setTenureYears] = useState(initialConfig?.tenureYears || 3);
  const [loading, setLoading] = useState(false);
  const [successMsg, setSuccessMsg] = useState(null);
  const [errorMsg, setErrorMsg] = useState(null);

  const handleCreateFd = async (e) => {
    e.preventDefault();
    setLoading(true);
    setSuccessMsg(null);
    setErrorMsg(null);

    try {
      await createFixedDeposit({
        sourceAccountNumber,
        depositAmount: Number(depositAmount),
        tenureYears: Number(tenureYears),
      });

      setSuccessMsg('Your Fixed Deposit has been opened successfully.');
      setSourceAccountNumber(accounts[0]?.accountNumber ?? "");
      setDepositAmount("");
      setTenureYears(FD_TENURES[0]);
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

          <select
            value={sourceAccountNumber}
            onChange={(e) => setSourceAccountNumber(e.target.value)}
            required
            style={styles.input}
          >
            <option value="">Select Source Account</option>

            {accounts.map((account) => (
              <option
                key={account.accountNumber}
                value={account.accountNumber}
              >
                {account.accountNumber}
              </option>
            ))}
          </select>
          {sourceAccountNumber && (
            <div style={styles.balanceInfo}>
              Available Balance:&nbsp;
              <strong>
                {new Intl.NumberFormat("en-IN", {
                  style: "currency",
                  currency: "INR",
                }).format(
                  accounts.find(
                    account => account.accountNumber === sourceAccountNumber
                  )?.currentBalance ?? 0
                )}
              </strong>
            </div>
          )}
        </div>

        <div style={styles.inputGroup}>
          <label style={styles.label}>Deposit Amount (₹)</label>
          <input
            type="number"
            step="0.01"
            min="10000"
            value={depositAmount}
            placeholder="Minimum ₹10,000"
            onChange={(e) => setDepositAmount(e.target.value)}
            required
            autoFocus
            style={styles.input}
          />
        </div>

        <div style={styles.inputGroup}>
          <label style={styles.label}>Tenure (Years)</label>
          <select
            value={tenureYears}
            onChange={(e) => setTenureYears(Number(e.target.value))}
            style={styles.input}
            disabled={
              loading ||
              !sourceAccountNumber ||
              !depositAmount
            }
          >
            {FD_TENURES.map(year => (
              <option
                key={year}
                value={year}
              >
                {year} Year{year > 1 ? "s" : ""}
              </option>
            ))}
          </select>
        </div>

        <div style={styles.inputGroup}>
          <label style={styles.label}>Applicable Interest Rate</label>
          <input
            type="text"
            value={FD_RATES[tenureYears]}
            disabled
            style={{ ...styles.input, backgroundColor: '#f3f4f6', fontWeight: '700', color: '#0d6360' }}
          />
        </div>

        <button type="submit"
          disabled={
            loading ||
            !sourceAccountNumber ||
            !depositAmount
          }
          style={styles.button}>
          {loading ? 'Processing FD Creation...' : 'Confirm & Open Fixed Deposit'}
        </button>
      </form>
    </div>
  );
};

const styles = {
  card: { backgroundColor: '#ffffff', borderRadius: '16px', padding: '32px', maxWidth: '600px', border: '1px solid #eef0ec' },
  balanceInfo: { marginTop: "6px", fontSize: "13px", color: "#64748b" },
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
