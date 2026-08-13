const CardSearchToolbar = ({
    searchInput,
    setSearchInput,
    searchLoading,
    statusFilter,
    setStatusFilter,
    setCurrentPage,
}) => {

    return (
        <div style={styles.toolbar}>

            <div
                style={{
                    display: "flex",
                    flex: 1,
                    gap: "8px",
                    position: "relative"
                }}
            >

                <input
                    placeholder="Search customer, card or account..."
                    value={searchInput}
                    onChange={(e) => {
                        setSearchInput(e.target.value);
                    }}
                    style={styles.searchInput}
                />


                {searchLoading && (
                    <span
                        style={{
                            position: "absolute",
                            right: searchInput ? "55px" : "15px",
                            top: "12px",
                            color: "#64748b",
                            fontSize: "13px"
                        }}
                    >
                        Searching...
                    </span>
                )}


                {searchInput && (
                    <button
                        onClick={() => {
                            setSearchInput("");
                            setCurrentPage(0);
                        }}
                        style={styles.clearButton}
                    >
                        ✕
                    </button>
                )}

            </div>


            <select
                value={statusFilter}
                onChange={(e) => {
                    setCurrentPage(0);
                    setStatusFilter(e.target.value);
                }}
                style={styles.filterSelect}
            >

                <option value="ALL">
                    All Status
                </option>

                <option value="ACTIVE">
                    Active
                </option>

                <option value="FROZEN">
                    Frozen
                </option>

                <option value="BLOCKED">
                    Blocked
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
        padding: "12px 90px 12px 14px",
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


    clearButton: {
        width: "45px",
        borderRadius: "10px",
        border: "1px solid #d1d5db",
        background: "#fff",
        cursor: "pointer",
        fontSize: "18px"
    }

};


export default CardSearchToolbar;