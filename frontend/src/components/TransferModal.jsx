import React, { useState, useEffect } from 'react';
import { transferFunds } from '../api/bankService';

const TransferModal = ({ isOpen, onClose, onTransferSuccess, accounts = [] }) => {
  const [sourceAccount, setSourceAccount] = useState('');
  const [targetAccount, setTargetAccount] = useState('');
  const [amount, setAmount] = useState('');
  const [remark, setRemark] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  // Auto-select the first account if available when modal opens
  useEffect(() => {
    if (accounts.length > 0 && !sourceAccount) {
      setSourceAccount(accounts[0].accountNumber);
    }
  }, [accounts, sourceAccount]);

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);

    try {
      await transferFunds({
        sourceAccountNumber: sourceAccount,
        targetAccountNumber: targetAccount,
        amount: parseFloat(amount),
        remark: remark,
      });
      setSubmitting(false);
      setAmount('');
      setTargetAccount('');
      setRemark('');
      onTransferSuccess();
      onClose();
    } catch (err) {
      console.error('Transfer error:', err);
      setError(
        err.response?.data?.message ||
          'Transfer failed. Please check the account numbers and balance.'
      );
      setSubmitting(false);
    }
  };

  return (
    <div style={modalStyles.overlay}>
      <div style={modalStyles.modal}>
        <div style={modalStyles.header}>
          <h3 style={modalStyles.title}>Transfer Funds</h3>
          <button style={modalStyles.closeBtn} onClick={onClose}>
            ✕
          </button>
        </div>

        {error && <div style={modalStyles.errorBox}>{error}</div>}

        <form onSubmit={handleSubmit} style={modalStyles.form}>
          <div style={modalStyles.field}>
            <label style={modalStyles.label}>From Account</label>
            {accounts.length > 0 ? (
              <select
                value={sourceAccount}
                onChange={(e) => setSourceAccount(e.target.value)}
                required
                style={modalStyles.input}
              >
                {accounts.map((acc) => (
                  <option key={acc.accountNumber} value={acc.accountNumber}>
                    {acc.accountNumber} ({acc.accountType}) - ₹{acc.currentBalance}
                  </option>
                ))}
              </select>
            ) : (
              <input
                type="text"
                placeholder="e.g. BF5891164768"
                value={sourceAccount}
                onChange={(e) => setSourceAccount(e.target.value)}
                required
                style={modalStyles.input}
              />
            )}
          </div>

          <div style={modalStyles.field}>
            <label style={modalStyles.label}>Target Account Number</label>
            <input
              type="text"
              placeholder="e.g. BF8490652259"
              value={targetAccount}
              onChange={(e) => setTargetAccount(e.target.value)}
              required
              style={modalStyles.input}
            />
          </div>

          <div style={modalStyles.field}>
            <label style={modalStyles.label}>Amount (₹)</label>
            <input
              type="number"
              step="0.01"
              placeholder="0.00"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              required
              min="1"
              style={modalStyles.input}
            />
          </div>

          <div style={modalStyles.field}>
            <label style={modalStyles.label}>Remark</label>
            <input
              type="text"
              placeholder="e.g. Rent payment"
              value={remark}
              onChange={(e) => setRemark(e.target.value)}
              style={modalStyles.input}
            />
          </div>

          <div style={modalStyles.actions}>
            <button
              type="button"
              onClick={onClose}
              style={modalStyles.cancelBtn}
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting}
              style={modalStyles.submitBtn}
            >
              {submitting ? 'Processing...' : 'Confirm Transfer'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

const modalStyles = {
  overlay: { position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0, 0, 0, 0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 },
  modal: { backgroundColor: '#ffffff', borderRadius: '16px', padding: '24px', width: '100%', maxWidth: '440px', boxShadow: '0 10px 25px rgba(0,0,0,0.1)' },
  header: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' },
  title: { margin: 0, fontSize: '18px', fontFamily: 'Georgia, serif' },
  closeBtn: { border: 'none', background: 'none', fontSize: '18px', cursor: 'pointer' },
  form: { display: 'flex', flexDirection: 'column', gap: '14px' },
  field: { display: 'flex', flexDirection: 'column', gap: '4px' },
  label: { fontSize: '12px', fontWeight: '700', color: '#374151' },
  input: { padding: '10px 12px', borderRadius: '8px', border: '1px solid #d1d5db', fontSize: '14px', outline: 'none' },
  actions: { display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '10px' },
  cancelBtn: { padding: '10px 16px', borderRadius: '8px', border: '1px solid #d1d5db', backgroundColor: '#fff', cursor: 'pointer', fontWeight: '600' },
  submitBtn: { padding: '10px 20px', borderRadius: '8px', border: 'none', backgroundColor: '#0d6360', color: '#fff', cursor: 'pointer', fontWeight: '700' },
  errorBox: { backgroundColor: '#fee2e2', color: '#991b1b', padding: '10px', borderRadius: '8px', fontSize: '13px', marginBottom: '12px' },
};

export default TransferModal;
