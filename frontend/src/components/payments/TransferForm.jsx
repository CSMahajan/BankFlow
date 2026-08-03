import React, { useEffect, useState } from "react";
import { transferFunds } from "../../api/bankService";

const TransferForm = ({
    accounts = [],
    onSuccess,
    onCancel,
}) => {

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

    const selectedAccount = accounts.find(
        (acc) => acc.accountNumber === sourceAccount
    );

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
            if (accounts.length > 0) {
                setSourceAccount(accounts[0].accountNumber);
            } else {
                setSourceAccount('');
            }
            onSuccess?.();
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
        <>
            {error && <div style={styles.errorBox}>{error}</div>}

            <form onSubmit={handleSubmit} style={styles.form}>
                <div style={styles.field}>
                    <label style={styles.label}>Source Account Number</label>

                    {accounts.length > 0 ? (
                        <>
                            <select
                                value={sourceAccount}
                                onChange={(e) => setSourceAccount(e.target.value)}
                                required
                                style={styles.input}
                            >
                                {accounts
                                    .filter(acc => acc.accountStatus === "ACTIVE")
                                    .map((acc) => (
                                        <option
                                            key={acc.accountNumber}
                                            value={acc.accountNumber}
                                        >
                                            {acc.accountNumber}
                                        </option>
                                    ))}
                            </select>

                            {selectedAccount && (
                                <div style={styles.balanceInfo}>
                                    Available Balance:&nbsp;
                                    <strong>
                                        {new Intl.NumberFormat("en-IN", {
                                            style: "currency",
                                            currency: "INR",
                                        }).format(selectedAccount.currentBalance)}
                                    </strong>
                                </div>
                            )}
                        </>
                    ) : (
                        <input
                            type="text"
                            placeholder="e.g. BF5891164768"
                            value={sourceAccount}
                            onChange={(e) => setSourceAccount(e.target.value)}
                            required
                            style={styles.input}
                        />
                    )}
                </div>

                <div style={styles.field}>
                    <label style={styles.label}>Target Account Number</label>
                    <input
                        type="text"
                        placeholder="e.g. BF8490652259"
                        value={targetAccount}
                        onChange={(e) => setTargetAccount(e.target.value)}
                        required
                        style={styles.input}
                    />
                </div>

                <div style={styles.field}>
                    <label style={styles.label}>Amount (₹)</label>
                    <input
                        type="number"
                        step="0.01"
                        placeholder="0.00"
                        value={amount}
                        onChange={(e) => setAmount(e.target.value)}
                        required
                        min="1"
                        style={styles.input}
                    />
                </div>

                <div style={styles.field}>
                    <label style={styles.label}>Remark</label>
                    <input
                        type="text"
                        placeholder="e.g. Rent payment"
                        value={remark}
                        onChange={(e) => setRemark(e.target.value)}
                        style={styles.input}
                    />
                </div>

                <div style={styles.actions}>
                    <button
                        type="button"
                        onClick={onCancel}
                        style={styles.cancelBtn}
                    >
                        Cancel
                    </button>
                    <button
                        type="submit"
                        disabled={submitting}
                        style={styles.submitBtn}
                    >
                        {submitting ? 'Processing...' : 'Confirm Transfer'}
                    </button>
                </div>
            </form>
        </>
    );
};

const styles = {
    form: { display: 'flex', flexDirection: 'column', gap: '14px' },
    field: { display: 'flex', flexDirection: 'column', gap: '4px' },
    label: {
        fontSize: "13px",
        fontWeight: "600",
        color: "#374151",
    },
    input: { padding: '10px 14px', borderRadius: '8px', border: '1px solid #d1d5db', fontSize: '14px', outline: 'none' },
    actions: { display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '10px' },
    cancelBtn: { padding: '10px 16px', borderRadius: '8px', border: '1px solid #d1d5db', backgroundColor: '#fff', cursor: 'pointer', fontWeight: '600' },
    submitBtn: { padding: '10px 20px', borderRadius: '8px', border: 'none', backgroundColor: '#0d6360', color: '#fff', cursor: 'pointer', fontWeight: '700' },
    errorBox: { backgroundColor: '#fee2e2', color: '#991b1b', padding: '10px', borderRadius: '8px', fontSize: '13px', marginBottom: '12px' },
    balanceInfo: {
        marginTop: "6px",
        fontSize: "13px",
        color: "#64748b",
    },

    balanceLabel: {
        fontSize: "12px",
        color: "#6b7280",
    },

    balanceValue: {
        fontSize: "13px",
        fontWeight: "700",
        color: "#0d6360",
    },
};

export default TransferForm;