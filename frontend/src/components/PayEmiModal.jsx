import React, { useEffect, useState } from 'react';
import { payEmi } from '../api/bankService';
import modalStyles from '../styles/modalStyles';

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
        if (accounts?.length > 0) {
            setSourceAccountNumber(accounts[0].accountNumber);
        }
    }, [accounts]);

    const handleSubmit = async (e) => {
        e.preventDefault();

        setLoading(true);
        setError(null);

        try {
            await payEmi({
                loanNumber: loan.loanNumber,
                sourceAccountNumber
            });

            await onPaymentSuccess();

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
                            onClick={onClose}
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

                        <select
                            value={sourceAccountNumber}
                            onChange={(e) =>
                                setSourceAccountNumber(e.target.value)
                            }
                            style={modalStyles.input}
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

                    <div style={modalStyles.actions}>
                        <button
                            style={modalStyles.cancelBtn}
                            onClick={onClose}
                        >
                            Cancel
                        </button>

                        <button
                            type="submit"
                            disabled={loading}
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