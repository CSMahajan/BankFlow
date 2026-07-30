import React, { useEffect, useState } from 'react';
import API from '../api/axios';

const FinancialSummaryCard = () => {
    const [analytics, setAnalytics] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const fetchAnalytics = async () => {
        setLoading(true);
        setError(null);

        try {
            const response = await API.get('/dashboard/analytics/monthly');
            setAnalytics(response.data);
        } catch (err) {
            console.error(err);
            setError('Unable to load financial summary.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchAnalytics();
    }, []);

    if (loading) {
        return <div>Loading financial summary...</div>;
    }

    if (error) {
        return <div>{error}</div>;
    }

    return (
        <div
            style={{
                backgroundColor: '#ffffff',
                borderRadius: '16px',
                padding: '20px',
                border: '1px solid #eef0ec',
            }}
        >
            <h3 style={{ marginTop: 0 }}>Financial Summary</h3>

            <div
                style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(3, 1fr)',
                    gap: '16px',
                    marginTop: '16px',
                }}
            >
                <div style={cardStyle}>
                    <div style={labelStyle}>Income</div>
                    <div style={{ ...valueStyle, color: '#15803d' }}>
                        ₹{Number(analytics.totalIncome).toLocaleString('en-IN')}
                    </div>
                </div>

                <div style={cardStyle}>
                    <div style={labelStyle}>Expense</div>
                    <div style={{ ...valueStyle, color: '#dc2626' }}>
                        ₹{Number(analytics.totalExpense).toLocaleString('en-IN')}
                    </div>
                </div>

                <div style={cardStyle}>
                    <div style={labelStyle}>Net Cash Flow</div>
                    <div
                        style={{
                            ...valueStyle,
                            color:
                                analytics.netCashFlow >= 0
                                    ? '#15803d'
                                    : '#dc2626',
                        }}
                    >
                        ₹{Number(analytics.netCashFlow).toLocaleString('en-IN')}
                    </div>
                </div>
            </div>
        </div>
    );
};

const cardStyle = {
    backgroundColor: '#f9fafb',
    border: '1px solid #e5e7eb',
    borderRadius: '12px',
    padding: '16px',
};

const labelStyle = {
    fontSize: '13px',
    color: '#6b7280',
    marginBottom: '8px',
};

const valueStyle = {
    fontSize: '22px',
    fontWeight: '700',
};

export default FinancialSummaryCard;