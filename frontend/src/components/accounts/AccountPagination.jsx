import React from "react";

const AccountPagination = ({
    pageData,
    setCurrentPage
}) => {

    if (!pageData || pageData.totalPages <= 1) {
        return null;
    }

    return (
        <div style={styles.pagination}>

            <button
                disabled={pageData.first}
                onClick={() =>
                    setCurrentPage(prev => prev - 1)
                }
                style={styles.pageButton}
            >
                ← Previous
            </button>


            <span>
                Page {pageData.number + 1}
                {" "}of{" "}
                {pageData.totalPages}
            </span>


            <button
                disabled={pageData.last}
                onClick={() =>
                    setCurrentPage(prev => prev + 1)
                }
                style={styles.pageButton}
            >
                Next →
            </button>

        </div>
    );
};


const styles = {

    pagination: {
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        gap: "16px",
        marginTop: "24px",
    },

    pageButton: {
        padding: "8px 16px",
        borderRadius: "8px",
        border: "1px solid #d1d5db",
        background: "#fff",
        cursor: "pointer",
        fontWeight: 600,
    }

};


export default AccountPagination;