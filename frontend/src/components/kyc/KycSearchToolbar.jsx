import React from "react";

const KycSearchToolbar = ({
    search,
    setSearch,
    status,
    setStatus,
    setPage,
}) => {

    return (
        <div style={styles.toolbar}>

            <div style={styles.searchWrapper}>

                <input
                    placeholder="Search customer, email..."
                    value={search}
                    onChange={(e) => {
                        setPage(0);
                        setSearch(e.target.value);
                    }}
                    style={styles.searchInput}
                />

                {
                    search && (
                        <button
                            style={styles.clearButton}
                            onClick={() => {
                                setPage(0);
                                setSearch("");
                            }}
                        >
                            ✕
                        </button>
                    )
                }

            </div>


            <select
                value={status}
                onChange={(e) => {
                    setPage(0);
                    setStatus(e.target.value);
                }}
                style={styles.filterSelect}
            >

                <option value="ALL">
                    All Status
                </option>

                <option value="PENDING">
                    Pending
                </option>

                <option value="VERIFIED">
                    Verified
                </option>

                <option value="REJECTED">
                    Rejected
                </option>

            </select>

        </div>
    );
};


const styles = {

    toolbar: {
        display: "flex",
        justifyContent: "space-between",
        gap: "16px",
        marginBottom: "24px",
    },

    searchInput: {
        flex: 1,
        padding: "12px 40px 12px 14px",
        borderRadius: "10px",
        border: "1px solid #d1d5db",
        fontSize: "14px",
    },

    filterSelect: {
        width: "180px",
        padding: "12px",
        borderRadius: "10px",
        border: "1px solid #d1d5db",
        fontSize: "14px",
    },

    searchWrapper: {
        flex: 1,
        position: "relative",
        display: "flex",
        alignItems: "center"
    },

    clearButton: {
        position: "absolute",
        right: "12px",
        border: "none",
        background: "transparent",
        cursor: "pointer",
        fontSize: "16px",
        color: "#64748b"
    },

};


export default KycSearchToolbar;