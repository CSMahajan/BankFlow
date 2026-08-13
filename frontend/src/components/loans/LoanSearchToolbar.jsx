import React from "react";

const LoanSearchToolbar = ({
    search,
    setSearch,
    loanTypeFilter,
    setLoanTypeFilter,
    setCurrentPage,
}) => {

    return (
        <div style={styles.toolbar}>

            <input
                placeholder="Search loan, customer or account..."
                value={search}
                onChange={(e) => {
                    setCurrentPage(0);
                    setSearch(e.target.value);
                }}
                style={styles.searchInput}
            />


            <select
                value={loanTypeFilter}
                onChange={(e) => {
                    setCurrentPage(0);
                    setLoanTypeFilter(e.target.value);
                }}
                style={styles.filterSelect}
            >

                <option value="ALL">
                    All Types
                </option>

                <option value="PERSONAL">
                    Personal
                </option>

                <option value="HOME">
                    Home
                </option>

                <option value="VEHICLE">
                    Vehicle
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
    },

};


export default LoanSearchToolbar;