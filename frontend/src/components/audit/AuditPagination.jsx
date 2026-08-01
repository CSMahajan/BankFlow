const AuditPagination = ({
    currentPage,
    totalPages,
    setCurrentPage,
}) => {

    const styles = {
        pageButton: {
            padding: "10px 18px",
            borderRadius: "8px",
            border: "1px solid #cbd5e1",
            backgroundColor: "#ffffff",
            cursor: "pointer",
            fontWeight: "600",
            color: "#334155",
        },
    };

    return (
        <div
            style={{
                display: "flex",
                justifyContent: "flex-end",
                alignItems: "center",
                gap: "12px",
                marginTop: "20px",
                paddingTop: "20px",
                borderTop: "1px solid #e2e8f0",
            }}
        >

            <button
                disabled={currentPage === 0}
                onClick={() => setCurrentPage(prev => prev - 1)}
                style={{
                    ...styles.pageButton,
                    opacity: currentPage === 0 ? 0.45 : 1,
                    cursor: currentPage === 0 ? "not-allowed" : "pointer",
                }}
            >
                ← Previous
            </button>

            <span
                style={{
                    fontWeight: "600",
                    color: "#475569",
                }}
            >
                <span
                    style={{
                        fontWeight: "700",
                        color: "#334155",
                        minWidth: "110px",
                        textAlign: "center",
                    }}
                >
                    Page {currentPage + 1} of {totalPages}
                </span>
            </span>

            <button
                disabled={currentPage === totalPages - 1}
                onClick={() => setCurrentPage(prev => prev + 1)}
                style={{
                    ...styles.pageButton,
                    opacity: currentPage === totalPages - 1 ? 0.45 : 1,
                    cursor: currentPage === totalPages - 1 ? "not-allowed" : "pointer",
                }}
            >
                Next →
            </button>

        </div>
    );
};

export default AuditPagination;