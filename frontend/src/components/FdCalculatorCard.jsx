import React, { useState } from 'react';

const FdCalculatorCard = ({ onOpenFd, defaultAccountNumber }) => {
    const [amount, setAmount] = useState(50000); // Default ₹50,000
    const [tenureYears, setTenureYears] = useState(3); // Default 3 years (Options: 1, 3, 5)

    // Rate mapping based on selected tenure
    const getInterestRate = (tenure) => {
        switch (Number(tenure)) {
            case 1:
                return 6.5;
            case 3:
                return 7.0;
            case 5:
                return 7.5;
            default:
                return 7.0;
        }
    };

    const interestRate = getInterestRate(tenureYears);

    // Quarterly Compounding FD Formula: A = P * (1 + r/4)^(4*t)
    const calculateMaturity = () => {
        const P = parseFloat(amount) || 0;
        const r = parseFloat(interestRate) / 100;
        const t = parseFloat(tenureYears) || 0;

        if (P <= 0 || t <= 0) return { maturityAmount: 0, totalInterest: 0 };

        const maturityAmount = P * Math.pow(1 + r / 4, 4 * t);
        const totalInterest = maturityAmount - P;

        return {
            maturityAmount: Math.round(maturityAmount),
            totalInterest: Math.round(totalInterest),
        };
    };

    const { maturityAmount, totalInterest } = calculateMaturity();

    const formatCurrency = (val) =>
        new Intl.NumberFormat('en-IN', {
            style: 'currency',
            currency: 'INR',
            maximumFractionDigits: 0,
        }).format(val || 0);

    const handleOpenFdClick = () => {
        if (onOpenFd) {
            onOpenFd({
                depositAmount: amount,
                tenureInYears: tenureYears,
                interestRate: interestRate,
                sourceAccountNumber: defaultAccountNumber || 'BF5891164768',
            });
        }
    };

    return (
        <div style={styles.card}>
            <div style={styles.header}>
                <div>
                    <h3 style={styles.title}>🪙 Fixed Deposit (FD) Calculator</h3>
                    <p style={styles.subtitle}>
                        Calculate guaranteed returns on your savings
                    </p>
                </div>
                <div style={styles.rateBadge}>{interestRate}% p.a.</div>
            </div>

            <div style={styles.grid}>
                {/* Controls Section */}
                <div style={styles.controls}>
                    {/* Amount Slider */}
                    <div style={styles.field}>
                        <div style={styles.labelRow}>
                            <label style={styles.label}>Deposit Amount</label>
                            <span style={styles.valueText}>{formatCurrency(amount)}</span>
                        </div>
                        <input
                            type="range"
                            min="10000"
                            max="1000000"
                            step="5000"
                            value={amount}
                            onChange={(e) => setAmount(Number(e.target.value))}
                            style={styles.slider}
                        />

                    </div>

                    {/* Tenure Selection Dropdown (1, 3, 5 Years) */}
                    <div style={styles.field}>
                        <div style={styles.labelRow}>
                            <label style={styles.label}>Tenure Period</label>
                            <span style={styles.valueText}>
                                {tenureYears} Year{tenureYears > 1 ? 's' : ''}
                            </span>
                        </div>
                        <select
                            value={tenureYears}
                            onChange={(e) => setTenureYears(Number(e.target.value))}
                            style={styles.selectInput}
                        >
                            <option value={1}>1 Year (6.5% p.a.)</option>
                            <option value={3}>3 Years (7.0% p.a.)</option>
                            <option value={5}>5 Years (7.5% p.a.)</option>
                        </select>
                    </div>
                </div>

                {/* Breakdown Output & Action Section */}
                <div style={styles.summaryBox}>
                    <div style={styles.summaryRow}>
                        <span style={styles.summaryLabel}>Invested Amount</span>
                        <span style={styles.summaryVal}>{formatCurrency(amount)}</span>
                    </div>
                    <div style={styles.summaryRow}>
                        <span style={styles.summaryLabel}>Total Interest Earned</span>
                        <span style={{ ...styles.summaryVal, color: '#0d6360' }}>
                            +{formatCurrency(totalInterest)}
                        </span>
                    </div>
                    <hr style={styles.divider} />
                    <div style={styles.summaryRow}>
                        <span style={styles.totalLabel}>Maturity Value</span>
                        <span style={styles.totalAmount}>
                            {formatCurrency(maturityAmount)}
                        </span>
                    </div>

                    <button style={styles.openBtn} onClick={handleOpenFdClick}>
                        Open Fixed Deposit Now →
                    </button>
                </div>
            </div>
        </div>
    );
};

const styles = {
    card: {
        backgroundColor: '#ffffff',
        borderRadius: '16px',
        padding: '28px',
        border: '1px solid #eef0ec',
        boxShadow: '0 2px 8px rgba(0,0,0,0.02)',
        display: 'flex',
        flexDirection: 'column',
        gap: '20px',
    },
    header: {
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'flex-start',
    },
    title: {
        margin: '0 0 4px 0',
        fontSize: '18px',
        fontFamily: 'Georgia, serif',
        color: '#111827',
    },
    subtitle: {
        margin: 0,
        fontSize: '13px',
        color: '#6b7280',
    },
    rateBadge: {
        backgroundColor: '#e2ece9',
        color: '#0f4c42',
        padding: '6px 12px',
        borderRadius: '20px',
        fontSize: '13px',
        fontWeight: '700',
    },
    grid: {
        display: 'grid',
        gridTemplateColumns: '1fr 1fr',
        gap: '32px',
        alignItems: 'center',
    },
    controls: {
        display: 'flex',
        flexDirection: 'column',
        gap: '20px',
    },
    field: {
        display: 'flex',
        flexDirection: 'column',
        gap: '8px',
    },
    labelRow: {
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
    },
    label: {
        fontSize: '13px',
        fontWeight: '600',
        color: '#374151',
    },
    valueText: {
        fontSize: '14px',
        fontWeight: '700',
        color: '#0d6360',
    },
    slider: {
        accentColor: '#0d6360',
        cursor: 'pointer',
        width: '100%',
    },
    selectInput: {
        padding: '10px 12px',
        borderRadius: '8px',
        border: '1px solid #d1d5db',
        fontSize: '14px',
        backgroundColor: '#ffffff',
        color: '#111827',
        cursor: 'pointer',
        outline: 'none',
    },
    summaryBox: {
        backgroundColor: '#f9fafb',
        borderRadius: '12px',
        padding: '20px',
        border: '1px solid #f3f4f6',
        display: 'flex',
        flexDirection: 'column',
        gap: '12px',
    },
    summaryRow: {
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
    },
    summaryLabel: {
        fontSize: '13px',
        color: '#6b7280',
    },
    summaryVal: {
        fontSize: '14px',
        fontWeight: '600',
        color: '#111827',
    },
    divider: {
        border: 'none',
        borderTop: '1px dashed #e5e7eb',
        margin: '4px 0',
    },
    totalLabel: {
        fontSize: '14px',
        fontWeight: '700',
        color: '#111827',
    },
    totalAmount: {
        fontSize: '20px',
        fontWeight: '800',
        color: '#0d6360',
    },
    openBtn: {
        backgroundColor: '#0d6360',
        color: '#ffffff',
        border: 'none',
        padding: '12px',
        borderRadius: '8px',
        fontWeight: '700',
        fontSize: '14px',
        cursor: 'pointer',
        marginTop: '8px',
    },
};

export default FdCalculatorCard;
