import React from "react";


const KycPagination = ({
    totalPages,
    page,
    setPage
}) => {

    if (totalPages <= 1) {
        return null;
    }


    return (
        <div style={styles.pagination}>

            <button
                disabled={page === 0}
                onClick={() =>
                    setPage(prev => prev - 1)
                }
                style={styles.pageButton}
            >
                ← Previous
            </button>


            <span>
                Page {page + 1}
                {" "}of{" "}
                {totalPages}
            </span>


            <button
                disabled={page + 1 >= totalPages}
                onClick={() =>
                    setPage(prev => prev + 1)
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


export default KycPagination;