import React, { useEffect, useState } from 'react';
import { applyLoan } from '../api/bankService';
import modalStyles from '../styles/modalStyles';
import toast from "react-hot-toast";
import { formatDate, formatCurrency } from "../utils/formatUtils";

const ApplyLoanModal = ({ isOpen, onClose, accounts, onLoanApplied }) => {
    const [sourceAccountNumber, setSourceAccountNumber] = useState('');
    const [loanType, setLoanType] = useState('PERSONAL');
    const [principalAmount, setPrincipalAmount] = useState('');
    const [tenureMonths, setTenureMonths] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

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

    if (!isOpen) return null;

    const handleSubmit = async (e) => {
        e.preventDefault();

        setLoading(true);
        setError(null);

        try {
            await applyLoan({
                accountNumber: sourceAccountNumber,
                loanType,
                principalAmount: parseFloat(principalAmount),
                tenureMonths: parseInt(tenureMonths, 10)
            });

            if (activeAccounts.length > 0) {
                setSourceAccountNumber(activeAccounts[0].accountNumber);
            } else {
                setSourceAccountNumber("");
            }
            setError(null);
            setLoanType("PERSONAL");
            setPrincipalAmount("");
            setTenureMonths("");
            if (activeAccounts.length > 0) {
                setSourceAccountNumber(activeAccounts[0].accountNumber);
            } else {
                setSourceAccountNumber("");
            }
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
                    <button style={modalStyles.closeBtn}
                        onClick={() => {
                            setError(null);
                            onClose();
                        }}>
                        ✕
                    </button>
                </div>

                {error && <div style={modalStyles.errorBox}>{error}</div>}
                {activeAccounts.length === 0 ? (

                    <div style={modalStyles.errorBox}>
                        No active accounts available for loan application.
                    </div>

                ) : (

                    <>
                        <form onSubmit={handleSubmit} style={modalStyles.form}>
                            <div style={modalStyles.field}>
                                <label style={modalStyles.label}>Source Account Number</label>
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
                                    onClick={() => {
                                        setError(null);
                                        onClose();
                                    }}
                                    style={modalStyles.cancelBtn}
                                >
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    disabled={loading || activeAccounts.length === 0}
                                    style={modalStyles.submitBtn}
                                >
                                    {loading ? 'Applying...' : 'Apply Loan'}
                                </button>
                            </div>
                        </form>
                    </>
                )}
            </div>
        </div>
    );
};

const styles = {
    balanceInfo: { marginTop: "6px", fontSize: "13px", color: "#64748b" },
    input: { padding: '10px 14px', borderRadius: '8px', border: '1px solid #d1d5db', fontSize: '14px', outline: 'none' },
};

export default ApplyLoanModal;
