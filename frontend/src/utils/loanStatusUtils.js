export const getLoanStatusStyle = (status) => {
    const baseStyle = {
        display: 'inline-block',
        padding: '4px 10px',
        borderRadius: '12px',
        fontSize: '12px',
        fontWeight: '600'
    };

    switch (status) {
        case 'PENDING':
            return {
                ...baseStyle,
                backgroundColor: '#fef3c7',
                color: '#92400e'
            };

        case 'ACTIVE':
            return {
                ...baseStyle,
                backgroundColor: '#dcfce7',
                color: '#15803d'
            };

        case 'REJECTED':
            return {
                ...baseStyle,
                backgroundColor: '#fee2e2',
                color: '#b91c1c'
            };

        case 'PAID_OFF':
            return {
                ...baseStyle,
                backgroundColor: '#dbeafe',
                color: '#1d4ed8'
            };

        default:
            return {
                ...baseStyle,
                backgroundColor: '#f3f4f6',
                color: '#374151'
            };
    }
};