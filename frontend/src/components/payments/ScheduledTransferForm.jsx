import React, { useEffect, useState } from "react";
import { createScheduledTransfer } from "../../api/bankService";
import { formatCurrency, getTodayDate } from "../../utils/formatUtils";
import { getActiveAccounts, getSelectedAccount } from "../../utils/accountUtils";
import toast from "react-hot-toast";

const ScheduledTransferForm = ({
    accounts = [],
    onSuccess,
    onCancel,
}) => {

    const [sourceAccountNumber, setSourceAccountNumber] = useState('');
    const [recipientAccountNumber, setRecipientAccountNumber] = useState('');
    const [amount, setAmount] = useState('');
    const [remark, setRemark] = useState('');
    const today = getTodayDate();
    const [frequency, setFrequency] = useState("MONTHLY");
    const [startDate, setStartDate] = useState(today);
    const [submitting, setSubmitting] = useState(false);

    const activeAccounts = getActiveAccounts(accounts);

    useEffect(() => {
        if (!sourceAccountNumber && activeAccounts.length > 0) {
            setSourceAccountNumber(activeAccounts[0].accountNumber);
        }
    }, [activeAccounts, sourceAccountNumber]);

    const selectedAccount = getSelectedAccount(
        activeAccounts,
        sourceAccountNumber
    );

    const resetForm = () => {
        setAmount("");
        setRecipientAccountNumber("");
        setRemark("");
        setFrequency("MONTHLY");
        setStartDate(today);
        setSourceAccountNumber(activeAccounts[0]?.accountNumber ?? "");
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSubmitting(true);

        try {
            await createScheduledTransfer({
                sourceAccountNumber: sourceAccountNumber,
                recipientAccountNumber: recipientAccountNumber,
                amount: parseFloat(amount),
                description: remark,
                frequency,
                startDate,
            });
            resetForm();
            toast.success("Scheduled transfer created successfully.");
            await onSuccess?.();
        } catch (err) {
            console.error("Scheduled transfer error:", err);

            toast.error(
                err.response?.data?.message ??
                "Unable to schedule transfer. Please verify the details."
            );
        } finally {
            setSubmitting(false);
        }
    };
    return (
        <>
            <form onSubmit={handleSubmit} style={styles.form}>
                <div style={styles.field}>
                    <label style={styles.label}>Source Account Number</label>

                    {accounts.length > 0 ? (
                        <>
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
                        </>
                    ) : (
                        <input
                            type="text"
                            placeholder="e.g. BF5891164768"
                            value={sourceAccountNumber}
                            onChange={(e) => setSourceAccountNumber(e.target.value)}
                            required
                            style={styles.input}
                        />
                    )}
                </div>

                <div style={styles.field}>
                    <label style={styles.label}>Recipient Account Number</label>
                    <input
                        type="text"
                        placeholder="Enter recipient account number"
                        value={recipientAccountNumber}
                        onChange={(e) => setRecipientAccountNumber(e.target.value)}
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

                <div style={styles.field}>
                    <label style={styles.label}>Frequency</label>

                    <select
                        value={frequency}
                        onChange={(e) => setFrequency(e.target.value)}
                        style={styles.input}
                    >
                        <option value="DAILY">Daily</option>
                        <option value="WEEKLY">Weekly</option>
                        <option value="MONTHLY">Monthly</option>
                    </select>
                </div>

                <div style={styles.field}>
                    <label style={styles.label}>Start Date</label>

                    <input
                        type="date"
                        value={startDate}
                        min={today}
                        onChange={(e) => setStartDate(e.target.value)}
                        required
                        style={styles.input}
                    />
                </div>

                <div style={styles.actions}>
                    {onCancel && (
                        <button
                            type="button"
                            onClick={onCancel}
                            style={styles.cancelBtn}
                        >
                            Cancel
                        </button>
                    )}
                    <button
                        type="submit"
                        disabled={submitting}
                        style={styles.submitBtn}
                    >
                        {submitting ? 'Scheduling...' : 'Schedule Transfer'}
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

export default ScheduledTransferForm;