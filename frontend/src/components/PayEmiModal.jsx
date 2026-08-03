import React, { useEffect, useState } from 'react';
import { payEmi } from '../api/bankService';
import modalStyles from '../styles/modalStyles';
import toast from "react-hot-toast";
import { formatDate, formatCurrency } from "../utils/formatUtils";

const PayEmiModal = ({
    isOpen,
    onClose,
    loan,
    accounts,
    onPaymentSuccess
}) => {
    const [sourceAccountNumber, setSourceAccountNumber] = useState('');
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

    const handleSubmit = async (e) => {
        e.preventDefault();

        setLoading(true);
        setError(null);

        try {
            await payEmi({
                loanNumber: loan.loanNumber,
                sourceAccountNumber
            });

            setError(null);

            if (activeAccounts.length > 0) {
                setSourceAccountNumber(activeAccounts[0].accountNumber);
            } else {
                setSourceAccountNumber("");
            }

            await onPaymentSuccess();
            toast.success("EMI Paid successfully");
            onClose();
        } catch (err) {
            console.error(err);

            setError(
                err.response?.data?.message ||
                'Failed to pay EMI.'
            );
        } finally {
            setLoading(false);
        }
    };
    if (!isOpen || !loan) return null;

    return (
        <div style={modalStyles.overlay}>
            <div style={modalStyles.modal}>
                <form onSubmit={handleSubmit}>

                    <div style={modalStyles.header}>
                        <h3 style={modalStyles.title}>Pay EMI</h3>

                        <button
                            style={modalStyles.closeBtn}
                            onClick={() => {
                                setError(null);
                                onClose();
                            }}
                        >
                            ✕
                        </button>
                    </div>

                    {error && (
                        <div style={modalStyles.errorBox}>
                            {error}
                        </div>
                    )}

                    <div style={modalStyles.field}>
                        <label style={modalStyles.label}>Loan Number</label>

                        <input
                            value={loan.loanNumber}
                            disabled
                            style={modalStyles.input}
                        />
                    </div>

                    <div style={modalStyles.field}>
                        <label style={modalStyles.label}>Monthly EMI</label>

                        <input
                            value={`₹${loan.monthlyEmi}`}
                            disabled
                            style={modalStyles.input}
                        />
                    </div>

                    <div style={modalStyles.field}>
                        <label style={modalStyles.label}>
                            Pay From Account
                        </label>

                        {activeAccounts.length === 0 ? (

                            <div style={modalStyles.errorBox}>
                                No active accounts available for EMI payment.
                            </div>

                        ) : (

                            <>
                                <select
                                    value={sourceAccountNumber}
                                    onChange={(e) =>
                                        setSourceAccountNumber(e.target.value)
                                    }
                                    style={modalStyles.input}
                                >
                                    {activeAccounts.map((account) => (
                                        <option
                                            key={account.accountNumber}
                                            value={account.accountNumber}
                                        >
                                            {account.accountType} • {account.accountNumber}
                                        </option>
                                    ))}
                                </select>

                                {selectedAccount && (
                                    <div
                                        style={{
                                            marginTop: "6px",
                                            fontSize: "13px",
                                            color: "#64748b",
                                        }}
                                    >
                                        Available Balance:&nbsp;
                                        <strong>
                                            {formatCurrency(selectedAccount.currentBalance)}
                                        </strong>
                                    </div>
                                )}

                            </>

                        )}
                    </div>

                    <div style={modalStyles.actions}>
                        <button
                            style={modalStyles.cancelBtn}
                            onClick={() => {
                                setError(null);
                                onClose();
                            }}
                        >
                            Cancel
                        </button>

                        <button
                            type="submit"
                            disabled={loading || activeAccounts.length === 0}
                            style={modalStyles.submitBtn}
                        >
                            {loading ? 'Processing...' : 'Pay EMI'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default PayEmiModal;