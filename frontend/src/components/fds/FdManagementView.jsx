import React, { useEffect, useState } from "react";
import { createFixedDeposit } from "../../api/bankService";
import { FD_CONFIG, FD_TENURES } from "./fdConfig";
import { formatDate, formatCurrency } from "../../utils/formatUtils";
import toast from "react-hot-toast";

const FdManagementView = ({ initialConfig, onFdCreated, accounts = [] }) => {

  const [sourceAccountNumber, setSourceAccountNumber] = useState(
    initialConfig?.sourceAccountNumber ?? ""
  );
  const [depositAmount, setDepositAmount] = useState(
    initialConfig?.depositAmount ?? 0
  );
  const [tenureYears, setTenureYears] = useState(initialConfig?.tenureYears || 3);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const activeAccounts = accounts.filter(
      acc => acc.accountStatus === "ACTIVE"
    );

    if (!sourceAccountNumber && activeAccounts.length > 0) {
      setSourceAccountNumber(activeAccounts[0].accountNumber);
    }
  }, [accounts, sourceAccountNumber]);

  const activeAccounts = accounts.filter(
    acc => acc.accountStatus === "ACTIVE"
  );

  const selectedAccount = activeAccounts.find(
    acc => acc.accountNumber === sourceAccountNumber
  );

  const handleCreateFd = async (e) => {
    e.preventDefault();

    if (!selectedAccount) {
      toast.error("Please select a valid account.");
      return;
    }

    if (Number(depositAmount) > selectedAccount.currentBalance) {
      toast.error("Insufficient balance in selected account.");
      return;
    }

    if (Number(depositAmount) < 10000) {
      toast.error("Minimum FD amount is ₹10,000.");
      return;
    }
    setLoading(true);

    try {

      await createFixedDeposit({
        sourceAccountNumber,
        depositAmount: Number(depositAmount),
        tenureYears: Number(tenureYears),
      });

      toast.success('Your Fixed Deposit has been opened successfully.');
      if (activeAccounts.length > 0) {
        setSourceAccountNumber(activeAccounts[0].accountNumber);
      } else {
        setSourceAccountNumber("");
      }
      setDepositAmount(0);
      setTenureYears(FD_TENURES[0]);

      await onFdCreated?.();
    } catch (err) {
      console.error('Failed to create FD:', err);
      toast.error(err.response?.data?.message || 'Failed to create Fixed Deposit.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.card}>
      <h2 style={styles.title}>📄 Open Fixed Deposit</h2>
      <p style={styles.subtitle}>Lock in high returns with guaranteed interest rates.</p>

      <form onSubmit={handleCreateFd} style={styles.form}>
        <div style={styles.inputGroup}>
          <label style={styles.label}>Source Account Number</label>

          <select
            value={sourceAccountNumber}
            onChange={(e) => setSourceAccountNumber(e.target.value)}
            required
            style={styles.input}
          >
            {activeAccounts.map(acc => (
              <option
                key={acc.accountNumber}
                value={acc.accountNumber}
              >
                {acc.accountType} • {acc.accountNumber}
              </option>
            ))}
          </select>
          {selectedAccount && (
            <div style={styles.balanceInfo}>
              Available Balance:&nbsp;
              <strong>
                {formatCurrency(selectedAccount.currentBalance)}
              </strong>
            </div>
          )}
        </div>

        <div style={styles.inputGroup}>
          <label style={styles.label}>Deposit Amount (₹)</label>
          <input
            type="number"
            step="0.01"
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
            value={FD_CONFIG[tenureYears].label}
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
};

export default FdManagementView;
