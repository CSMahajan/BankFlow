import { tableHeader, tableCell } from "../../styles/tableStyles";

const AuditTable = ({
    filteredLogs
}) => {

    const getRoleBadgeStyle = (role) => {
        switch (role) {
            case "ADMIN":
                return {
                    backgroundColor: "#dbeafe",
                    color: "#1d4ed8",
                };

            default:
                return {
                    backgroundColor: "#dcfce7",
                    color: "#15803d",
                };
        }
    };

    const getActionBadgeStyle = (action) => {
        switch (action) {
            case "LOGIN":
                return {
                    backgroundColor: "#ede9fe",
                    color: "#6d28d9",
                };

            case "ACCOUNT_CREATED":
                return {
                    backgroundColor: "#dbeafe",
                    color: "#1d4ed8",
                };

            case "MONEY_TRANSFER":
                return {
                    backgroundColor: "#fef3c7",
                    color: "#92400e",
                };

            case "FD_CREATED":
                return {
                    backgroundColor: "#dcfce7",
                    color: "#15803d",
                };

            case "FD_CLOSED":
                return {
                    backgroundColor: "#fee2e2",
                    color: "#b91c1c",
                };

            case "LOAN_APPLIED":
                return {
                    backgroundColor: "#e0f2fe",
                    color: "#0369a1",
                };

            case "LOAN_APPROVED":
                return {
                    backgroundColor: "#dcfce7",
                    color: "#15803d",
                };

            case "LOAN_REJECTED":
                return {
                    backgroundColor: "#fee2e2",
                    color: "#b91c1c",
                };

            case "EMI_PAID":
                return {
                    backgroundColor: "#ecfccb",
                    color: "#3f6212",
                };

            case "PROFILE_UPDATED":
                return {
                    backgroundColor: "#f3e8ff",
                    color: "#7e22ce",
                };

            case "CARD_ISSUED":
                return {
                    backgroundColor: "#dbeafe",
                    color: "#1d4ed8",
                };

            case "CARD_FROZEN":
                return {
                    backgroundColor: "#fee2e2",
                    color: "#b91c1c",
                };

            case "CARD_ACTIVATED":
                return {
                    backgroundColor: "#dcfce7",
                    color: "#15803d",
                };

            case "CARD_LIMIT_UPDATED":
                return {
                    backgroundColor: "#fef3c7",
                    color: "#92400e",
                };

            default:
                return {
                    backgroundColor: "#f3f4f6",
                    color: "#374151",
                };
        }
    };

    return (
        <div
            style={{
                overflowX: "auto",
                maxHeight: "600px",
                overflowY: "auto",
            }}
        >
            <table
                style={{
                    width: "100%",
                    borderCollapse: "collapse"
                }}
            >

                <thead
                    style={{
                        position: "sticky",
                        top: 0,
                        backgroundColor: "#f9fafb",
                        zIndex: 1,
                    }}
                >

                    <tr>
                        <th style={tableHeader}>Time</th>
                        <th style={tableHeader}>User</th>
                        <th style={tableHeader}>Role</th>
                        <th
                            style={{
                                ...tableHeader,
                                minWidth: "220px",
                            }}
                        >
                            Action
                        </th>
                        <th style={tableHeader}>Description</th>
                    </tr>

                </thead>

                <tbody>

                    {filteredLogs.length === 0 ? (

                        <tr>

                            <td
                                colSpan={5}
                                style={{
                                    ...tableCell,
                                    textAlign: "center",
                                    padding: "40px",
                                    color: "#64748b",
                                }}
                            >
                                No audit logs found.
                            </td>

                        </tr>

                    ) : (

                        filteredLogs.map((log, index) => (

                            <tr
                                key={log.id}
                                style={{
                                    backgroundColor:
                                        index % 2 === 0 ? "#ffffff" : "#eef6ff",
                                }}
                            >

                                <td style={tableCell}>
                                    {new Date(log.createdAt).toLocaleString("en-IN", {
                                        day: "2-digit",
                                        month: "short",
                                        year: "numeric",
                                        hour: "2-digit",
                                        minute: "2-digit",
                                    })}
                                </td>

                                <td style={tableCell}>
                                    {log.performedBy}
                                </td>

                                <td style={tableCell}>
                                    <span
                                        style={{
                                            padding: "4px 10px",
                                            borderRadius: "999px",
                                            fontSize: "12px",
                                            fontWeight: "600",
                                            ...getRoleBadgeStyle(log.role),
                                        }}
                                    >
                                        {log.role}
                                    </span>
                                </td>

                                <td
                                    style={{
                                        ...tableCell,
                                        minWidth: "220px",
                                    }}
                                >
                                    <span
                                        style={{
                                            padding: "4px 10px",
                                            borderRadius: "999px",
                                            fontSize: "12px",
                                            fontWeight: "600",
                                            whiteSpace: "nowrap",
                                            ...getActionBadgeStyle(log.action),
                                        }}
                                    >
                                        {log.action.replaceAll("_", " ")}
                                    </span>
                                </td>

                                <td
                                    style={{
                                        ...tableCell,
                                        minWidth: "320px",
                                    }}
                                >
                                    {log.description}
                                </td>
                            </tr>
                        ))
                    )}
                </tbody>
            </table>
        </div>
    );

};

export default AuditTable;