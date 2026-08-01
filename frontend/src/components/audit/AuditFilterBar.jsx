const AuditFilterBar = ({
    search,
    setSearch,
    roleFilter,
    setRoleFilter,
    moduleFilter,
    setModuleFilter,
    actionFilter,
    setActionFilter,
    availableActions,
    moduleActions,
    moduleDisplayNames,
    actionDisplayNames,
    loadLogs,
    setCurrentPage,
    styles,
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
                onChange={(e) => {
                    setSearch(e.target.value);
                    setCurrentPage(0);
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

            {/* Refresh */}

            <button
                onClick={loadLogs}
                style={styles.refreshButton}
            >
                ↻ Refresh
            </button>

        </div>
    );

};

export default AuditFilterBar;