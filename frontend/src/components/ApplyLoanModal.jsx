import React, { useEffect, useState } from 'react';
import { applyLoan } from '../api/bankService';
import modalStyles from '../styles/modalStyles';
import toast from "react-hot-toast";

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
            toast.success("Loan applied successfully");
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

export default ApplyLoanModal;
