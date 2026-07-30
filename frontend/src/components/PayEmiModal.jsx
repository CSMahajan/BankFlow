import React, { useEffect, useState } from 'react';
import { payEmi } from '../api/bankService';

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

const modalStyles = {
    overlay: { position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 },
    modal: { backgroundColor: '#fff', borderRadius: '16px', padding: '24px', width: '100%', maxWidth: '420px' },
    header: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' },
    title: { margin: 0, fontSize: '18px', fontFamily: 'Georgia, serif' },
    closeBtn: { border: 'none', background: 'none', fontSize: '18px', cursor: 'pointer' },
    field: { display: 'flex', flexDirection: 'column', gap: '4px', marginBottom: '14px' },
    label: { fontWeight: '600', fontSize: '13px' },
    input: { padding: '10px', borderRadius: '8px', border: '1px solid #d1d5db' },
    errorBox: {
        backgroundColor: '#fee2e2', color: '#b91c1c', padding: '10px', borderRadius: '8px', marginBottom: '14px', fontSize: '14px'
    },
    actions: { display: 'flex', justifyContent: 'flex-end', gap: '10px' },
    cancelBtn: { padding: '10px 16px', borderRadius: '8px', border: '1px solid #d1d5db', background: '#fff', cursor: 'pointer' },
    submitBtn: { padding: '10px 18px', borderRadius: '8px', border: 'none', backgroundColor: '#0d6360', color: '#fff', cursor: 'pointer', fontWeight: '600' }
};

export default PayEmiModal;