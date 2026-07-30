const modalStyles = {
    overlay: {
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        backgroundColor: 'rgba(0, 0, 0, 0.5)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        zIndex: 1000
    },

    modal: {
        backgroundColor: '#ffffff',
        borderRadius: '16px',
        padding: '24px',
        width: '100%',
        maxWidth: '800px',
        maxHeight: '85vh',
        overflowY: 'auto',
        boxShadow: '0 10px 25px rgba(0,0,0,0.1)'
    },
    header: {
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: '16px'
    },

    title: {
        margin: 0,
        fontSize: '18px',
        fontFamily: 'Georgia, serif',
        color: '#111827'
    },

    closeBtn: {
        border: 'none',
        background: 'none',
        fontSize: '18px',
        cursor: 'pointer',
        color: '#6b7280'
    },

    form: {
        display: 'flex',
        flexDirection: 'column',
        gap: '14px'
    },

    field: {
        display: 'flex',
        flexDirection: 'column',
        gap: '4px'
    },

    label: {
        fontSize: '12px',
        fontWeight: '700',
        color: '#374151'
    },

    input: {
        padding: '10px 12px',
        borderRadius: '8px',
        border: '1px solid #d1d5db',
        fontSize: '14px',
        outline: 'none'
    },

    actions: {
        display: 'flex',
        justifyContent: 'flex-end',
        gap: '10px',
        marginTop: '10px'
    },

    cancelBtn: {
        padding: '10px 16px',
        borderRadius: '8px',
        border: '1px solid #d1d5db',
        backgroundColor: '#fff',
        cursor: 'pointer',
        fontWeight: '600',
        color: '#374151'
    },

    submitBtn: {
        padding: '10px 20px',
        borderRadius: '8px',
        border: 'none',
        backgroundColor: '#0d6360',
        color: '#fff',
        cursor: 'pointer',
        fontWeight: '700'
    },

    errorBox: {
        backgroundColor: '#fee2e2',
        color: '#991b1b',
        padding: '10px',
        borderRadius: '8px',
        fontSize: '13px',
        marginBottom: '12px'
    }
};

export default modalStyles;