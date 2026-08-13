import React from "react";

const AccountSearchToolbar = ({
    search,
    setSearch,
    accountStatusFilter,
    setAccountStatusFilter,
    setCurrentPage,
}) => {

    return (
        <div style={styles.toolbar}>

            <input
                placeholder="Search customer, account number..."
                value={search}
                onChange={(e) => {
                    setCurrentPage(0);
                    setSearch(e.target.value);
                }}
                style={styles.searchInput}
            />

            <select
                value={accountStatusFilter}
                onChange={(e) => {
                    setCurrentPage(0);
                    setAccountStatusFilter(e.target.value);
                }}
                style={styles.filterSelect}
            >
                <option value="ALL">All Status</option>
                <option value="ACTIVE">Active</option>
                <option value="FROZEN">Frozen</option>
                <option value="INACTIVE">Inactive</option>
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
        padding: "12px 14px",
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
    }

};


export default AccountSearchToolbar;