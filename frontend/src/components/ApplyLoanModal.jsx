import React, { useEffect, useState } from 'react';
import { applyLoan } from '../api/bankService';

const ApplyLoanModal = ({ isOpen, onClose, accounts, onLoanApplied }) => {
    const [accountNumber, setAccountNumber] = useState('');
    const [loanType, setLoanType] = useState('PERSONAL');
    const [principalAmount, setPrincipalAmount] = useState('');
    const [tenureMonths, setTenureMonths] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        if (accounts?.length > 0 && !accountNumber) {
            setAccountNumber(accounts[0].accountNumber);
        }
    }, [accounts, accountNumber]);

    if (!isOpen) return null;

    const handleSubmit = async (e) => {
        e.preventDefault();

        setLoading(true);
        setError(null);

        try {
            await applyLoan({
                accountNumber,
                loanType,
                principalAmount: parseFloat(principalAmount),
                tenureMonths: parseInt(tenureMonths, 10)
            });

            setAccountNumber(accounts?.[0]?.accountNumber || '');
            setLoanType('PERSONAL');
            setPrincipalAmount('');
            setTenureMonths('');

            await onLoanApplied();

            onClose();
        } catch (err) {
            console.error('Failed to apply for loan:', err);

            setError(
                err.response?.data?.message ||
                'Failed to submit loan application.'
            );
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={modalStyles.overlay}>
            <div style={modalStyles.modal}>
                <div style={modalStyles.header}>
                    <h3 style={modalStyles.title}>Apply for Loan</h3>
                    <button style={modalStyles.closeBtn} onClick={onClose}>
                        ✕
                    </button>
                </div>

                {error && <div style={modalStyles.errorBox}>{error}</div>}

                <form onSubmit={handleSubmit} style={modalStyles.form}>
                    <div style={modalStyles.field}>
                        <label style={modalStyles.label}>Account Number</label>
                        <select
                            value={accountNumber}
                            onChange={(e) => setAccountNumber(e.target.value)}
                            style={modalStyles.input}
                            required
                        >
                            {accounts?.map((account) => (
                                <option
                                    key={account.accountNumber}
                                    value={account.accountNumber}
                                >
                                    {account.accountType} - {account.accountNumber}
                                </option>
                            ))}
                        </select>
                    </div>

                    <div style={modalStyles.field}>
                        <label style={modalStyles.label}>Loan Type</label>
                        <select
                            value={loanType}
                            onChange={(e) => setLoanType(e.target.value)}
                            style={modalStyles.input}
                            required
                        >
                            <option value="PERSONAL">Personal</option>
                            <option value="HOME">Home</option>
                            <option value="VEHICLE">Vehicle</option>
                        </select>
                    </div>

                    <div style={modalStyles.field}>
                        <label style={modalStyles.label}>Principal Amount (₹)</label>
                        <input
                            type="number"
                            min="10000"
                            step="0.01"
                            value={principalAmount}
                            onChange={(e) => setPrincipalAmount(e.target.value)}
                            placeholder="Minimum ₹10,000"
                            style={modalStyles.input}
                            required
                        />
                        <small style={{ color: '#6b7280', fontSize: '12px' }}>
                            Minimum loan amount: ₹10,000
                        </small>
                    </div>

                    <div style={modalStyles.field}>
                        <label style={modalStyles.label}>Loan Tenure (Months)</label>
                        <input
                            type="number"
                            min="6"
                            step="1"
                            value={tenureMonths}
                            onChange={(e) => setTenureMonths(e.target.value)}
                            placeholder="e.g. 60 (5 years)"
                            style={modalStyles.input}
                            required
                        />
                        <small style={{ color: '#6b7280', fontSize: '12px' }}>
                            Minimum Tenure: 6 months
                        </small>
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
                            {loading ? 'Applying...' : 'Apply Loan'}
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

export default ApplyLoanModal;
