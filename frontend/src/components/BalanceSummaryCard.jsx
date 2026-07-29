import React, { useState } from 'react';
import TransferModal from './TransferModal';

const BalanceSummaryCard = ({
    accounts = [],
    refreshAccounts,
}) => {
    const [isTransferOpen, setIsTransferOpen] = useState(false);

    const totalBalance = accounts.reduce(
        (sum, acc) => sum + (Number(acc.currentBalance) || 0),
        0
    );

    const formatCurrency = (val) =>
        new Intl.NumberFormat('en-IN', {
            style: 'currency',
            currency: 'INR',
            maximumFractionDigits: 2,
        }).format(val || 0);

    return (
        <>
            <div style={styles.summaryCard}>
                <div>
                    <span style={styles.summaryLabel}>
                        Total Consolidated Balance
                    </span>

                    <h2 style={styles.summaryAmount}>
                        {formatCurrency(totalBalance)}
                    </h2>
                </div>

                <div style={styles.quickActions}>
                    <button
                        style={styles.actionBtnPrimary}
                        onClick={() => setIsTransferOpen(true)}
                    >
                        💸 Transfer Funds
                    </button>

                    <button
                        style={styles.actionBtnSecondary}
                        onClick={refreshAccounts}
                    >
                        🔄 Refresh
                    </button>
                </div>
            </div>

            <TransferModal
                isOpen={isTransferOpen}
                onClose={() => setIsTransferOpen(false)}
                accounts={accounts}
                onTransferSuccess={refreshAccounts}
            />
        </>
    );
};

const styles = {
    summaryCard: {
        backgroundColor: '#0d6360',
        color: '#ffffff',
        borderRadius: '16px',
        padding: '24px',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
    },
    summaryLabel: {
        fontSize: '13px',
        opacity: 0.9,
        textTransform: 'uppercase',
        letterSpacing: '0.5px',
    },
    summaryAmount: {
        margin: '4px 0 0 0',
        fontSize: '32px',
        fontWeight: '800',
    },
    quickActions: {
        display: 'flex',
        gap: '10px',
    },
    actionBtnPrimary: {
        backgroundColor: '#ffffff',
        color: '#0d6360',
        border: 'none',
        padding: '10px 16px',
        borderRadius: '8px',
        fontWeight: '700',
        cursor: 'pointer',
    },
    actionBtnSecondary: {
        backgroundColor: 'transparent',
        color: '#ffffff',
        border: '1px solid rgba(255,255,255,0.4)',
        padding: '10px 16px',
        borderRadius: '8px',
        fontWeight: '700',
        cursor: 'pointer',
    },
};

export default BalanceSummaryCard;