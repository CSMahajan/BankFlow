import { actionDisplayNames, moduleDisplayNames, moduleActions } from "./auditConfig";

const AuditFilterBar = ({
    search,
    setSearch,
    applySearch,
    roleFilter,
    setRoleFilter,
    moduleFilter,
    setModuleFilter,
    actionFilter,
    setActionFilter,
    availableActions,
    setCurrentPage
}) => {

    return (
        <div
            style={{
                display: "flex",
                alignItems: "center",
                gap: "16px",
                flexWrap: "wrap",
                marginBottom: "24px",
            }}
        >

            {/* Search */}

            <input
                type="text"
                placeholder="🔍 Search by user email or description..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                onKeyDown={(e) => {
                    if (e.key === "Enter") {
                        applySearch();
                    }
                }}
                style={{
                    flex: "1",
                    minWidth: "300px",
                    padding: "12px 16px",
                    border: "1px solid #d1d5db",
                    borderRadius: "10px",
                    fontSize: "14px",
                }}
            />

            {/* Role */}

            <select
                value={roleFilter}
                onChange={(e) => {
                    setRoleFilter(e.target.value);
                    setSearch(search.trim());
                    setCurrentPage(0);
                }}
                style={styles.filterSelect}
            >
                <option value="ALL">All Roles</option>
                <option value="CUSTOMER">Customer</option>
                <option value="ADMIN">Admin</option>
            </select>

            {/* Module */}

            <select
                value={moduleFilter}
                onChange={(e) => {
                    setModuleFilter(e.target.value);
                    setActionFilter("ALL");
                    setSearch(search.trim());
                    setCurrentPage(0);
                }}
                style={styles.filterSelect}
            >
                <option value="ALL">📂 All Modules</option>

                {Object.keys(moduleActions).map((module) => (
                    <option key={module} value={module}>
                        {moduleDisplayNames[module] ?? module}
                    </option>
                ))}
            </select>

            <select
                value={actionFilter}
                onChange={(e) => {
                    setActionFilter(e.target.value);
                    setSearch(search.trim());
                    setCurrentPage(0);
                }}
                style={styles.filterSelect}
            >
                <option value="ALL">⚡ All Actions</option>

                {availableActions.map((action) => (
                    <option
                        key={action}
                        value={action}
                    >
                        {actionDisplayNames[action] ?? action}
                    </option>
                ))}
            </select>

            <button
                onClick={applySearch}
                style={styles.searchButton}
            >
                🔍 Search
            </button>
        </div>
    );

};

const styles = {
    filterSelect: {
        padding: "12px 16px",
        borderRadius: "10px",
        border: "1px solid #d1d5db",
        backgroundColor: "#ffffff",
        fontSize: "14px",
        minWidth: "170px",
        cursor: "pointer",
    },

    searchButton: {
        padding: "12px 22px",
        borderRadius: "10px",
        border: "none",
        backgroundColor: "#0d6360",
        color: "#ffffff",
        fontWeight: "700",
        cursor: "pointer",
    },

};

export default AuditFilterBar;