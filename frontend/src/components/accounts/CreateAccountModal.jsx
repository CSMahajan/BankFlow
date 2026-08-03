import React, { useState } from 'react';
import { createBankAccount } from '../../api/bankService';

const CreateAccountModal = ({ isOpen, onClose, onAccountCreated }) => {
  const [accountType, setAccountType] = useState('SAVINGS');
  const [initialDeposit, setInitialDeposit] = useState('');
  const [branchName, setBranchName] = useState(''); // 🟢 Added branchName state
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      await createBankAccount({
        accountType: accountType,
        initialDeposit: initialDeposit ? parseFloat(initialDeposit) : 0,
        branchName: branchName.trim(), // 🟢 Included in API payload
      });

      setLoading(false);
      setInitialDeposit('');
      setBranchName('');
      setAccountType('SAVINGS');
      onAccountCreated();
      onClose();
    } catch (err) {
      console.error('Failed to create account:', err);
      setError(
        err.response?.data?.message ||
        'Failed to open bank account. Please try again.'
      );
      setLoading(false);
    }
  };

  return (
    <div style={modalStyles.overlay}>
      <div style={modalStyles.modal}>
        <div style={modalStyles.header}>
          <h3 style={modalStyles.title}>Open New Bank Account</h3>
          <button style={modalStyles.closeBtn} onClick={onClose}>
            ✕
          </button>
        </div>

        {error && <div style={modalStyles.errorBox}>{error}</div>}

        <form onSubmit={handleSubmit} style={modalStyles.form}>
          <div style={modalStyles.field}>
            <label style={modalStyles.label}>Account Type</label>
            <select
              value={accountType}
              onChange={(e) => setAccountType(e.target.value)}
              style={modalStyles.input}
              required
            >
              <option value="SAVINGS">Savings Account</option>
              <option value="CURRENT">Current Account</option>
            </select>
          </div>

          {/* 🟢 Branch Name Input Field */}
          <div style={modalStyles.field}>
            <label style={modalStyles.label}>Branch Name</label>
            <input
              type="text"
              placeholder="e.g. Main Branch, Downtown"
              value={branchName}
              onChange={(e) => setBranchName(e.target.value)}
              required
              style={modalStyles.input}
            />
          </div>

          <div style={modalStyles.field}>
            <label style={modalStyles.label}>Initial Deposit (₹) - Optional</label>
            <input
              type="number"
              step="0.01"
              placeholder="0.00"
              value={initialDeposit}
              onChange={(e) => setInitialDeposit(e.target.value)}
              min="0"
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
              disabled={loading}
              style={modalStyles.submitBtn}
            >
              {loading ? 'Opening Account...' : 'Confirm & Open'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

const modalStyles = {
  overlay: { position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0, 0, 0, 0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 },
  modal: { backgroundColor: '#ffffff', borderRadius: '16px', padding: '24px', width: '100%', maxWidth: '420px', boxShadow: '0 10px 25px rgba(0,0,0,0.1)' },
  header: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' },
  title: { margin: 0, fontSize: '18px', fontFamily: 'Georgia, serif', color: '#111827' },
  closeBtn: { border: 'none', background: 'none', fontSize: '18px', cursor: 'pointer', color: '#6b7280' },
  form: { display: 'flex', flexDirection: 'column', gap: '14px' },
  field: { display: 'flex', flexDirection: 'column', gap: '4px' },
  label: { fontSize: '12px', fontWeight: '700', color: '#374151' },
  input: { padding: '10px 12px', borderRadius: '8px', border: '1px solid #d1d5db', fontSize: '14px', outline: 'none' },
  actions: { display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '10px' },
  cancelBtn: { padding: '10px 16px', borderRadius: '8px', border: '1px solid #d1d5db', backgroundColor: '#fff', cursor: 'pointer', fontWeight: '600', color: '#374151' },
  submitBtn: { padding: '10px 20px', borderRadius: '8px', border: 'none', backgroundColor: '#0d6360', color: '#fff', cursor: 'pointer', fontWeight: '700' },
  errorBox: { backgroundColor: '#fee2e2', color: '#991b1b', padding: '10px', borderRadius: '8px', fontSize: '13px', marginBottom: '12px' },
};

export default CreateAccountModal;
