import React from 'react';

const SummaryCard = ({ icon, title, value, subtitle }) => {
    return (
        <div
            style={{
                backgroundColor: '#ffffff',
                border: '1px solid #e5e7eb',
                borderRadius: '16px',
                padding: '20px',
                boxShadow: '0 1px 3px rgba(0,0,0,0.05)',
                display: 'flex',
                flexDirection: 'column',
                gap: '10px',
            }}
        >
            <div
                style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '8px',
                    fontWeight: '600',
                    color: '#4b5563',
                }}
            >
                <span style={{ fontSize: '20px' }}>{icon}</span>
                {title}
            </div>

            <div
                style={{
                    fontSize: '30px',
                    fontWeight: '700',
                    color: '#111827',
                }}
            >
                {value}
            </div>

            {subtitle && (
                <div
                    style={{
                        fontSize: '13px',
                        color: '#6b7280',
                    }}
                >
                    {subtitle}
                </div>
            )}
        </div>
    );
};

export default SummaryCard;