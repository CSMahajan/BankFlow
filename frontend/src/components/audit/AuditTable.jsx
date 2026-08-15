import { tableHeader, tableCell } from "../../styles/tableStyles";
import { actionDisplayNames } from "./auditConfig";

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

            // Authentication
            case "LOGIN":
                return {
                    backgroundColor: "#ede9fe",
                    color: "#6d28d9",
                };

            case "USER_REGISTERED":
                return {
                    backgroundColor: "#dbeafe",
                    color: "#1d4ed8",
                };

            case "VERIFICATION_EMAIL_RESENT":
                return {
                    backgroundColor: "#fef3c7",
                    color: "#92400e",
                };

            case "PASSWORD_RESET":
            case "PASSWORD_CHANGED":
                return {
                    backgroundColor: "#fee2e2",
                    color: "#b91c1c",
                };

            case "EMAIL_VERIFIED":
                return {
                    backgroundColor: "#dcfce7",
                    color: "#15803d",
                };


            // KYC
            case "KYC_DOCUMENT_UPLOADED":
                return {
                    backgroundColor: "#e0f2fe",
                    color: "#0369a1",
                };

            case "KYC_DOCUMENT_VERIFIED":
                return {
                    backgroundColor: "#dcfce7",
                    color: "#15803d",
                };

            case "KYC_DOCUMENT_REJECTED":
                return {
                    backgroundColor: "#fee2e2",
                    color: "#b91c1c",
                };


            // Accounts
            case "ACCOUNT_CREATED":
                return {
                    backgroundColor: "#dbeafe",
                    color: "#1d4ed8",
                };

            case "ACCOUNT_ACTIVATED":
                return {
                    backgroundColor: "#dcfce7",
                    color: "#15803d",
                };

            case "ACCOUNT_FROZEN":
                return {
                    backgroundColor: "#fee2e2",
                    color: "#b91c1c",
                };


            // Transfers
            case "MONEY_TRANSFER":
                return {
                    backgroundColor: "#fef3c7",
                    color: "#92400e",
                };

            case "SCHEDULED_TRANSFER_CREATED":
                return {
                    backgroundColor: "#dbeafe",
                    color: "#1d4ed8",
                };

            case "SCHEDULED_TRANSFER_CANCELLED":
                return {
                    backgroundColor: "#fee2e2",
                    color: "#b91c1c",
                };

            case "SCHEDULED_TRANSFER_EXECUTED":
                return {
                    backgroundColor: "#dcfce7",
                    color: "#15803d",
                };


            // Fixed Deposit
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


            // Loans
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


            // Cards
            case "CARD_ISSUED":
                return {
                    backgroundColor: "#dbeafe",
                    color: "#1d4ed8",
                };

            case "CARD_ACTIVATED":
                return {
                    backgroundColor: "#dcfce7",
                    color: "#15803d",
                };

            case "CARD_FROZEN":
            case "CARD_BLOCKED":
                return {
                    backgroundColor: "#fee2e2",
                    color: "#b91c1c",
                };

            case "CARD_UNBLOCKED":
                return {
                    backgroundColor: "#dcfce7",
                    color: "#15803d",
                };

            case "CARD_LIMIT_UPDATED":
                return {
                    backgroundColor: "#fef3c7",
                    color: "#92400e",
                };


            // Profile
            case "PROFILE_UPDATED":
                return {
                    backgroundColor: "#f3e8ff",
                    color: "#7e22ce",
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
                                        whiteSpace: "nowrap",
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
                                        {actionDisplayNames[log.action] ?? log.action.replaceAll("_", " ")}
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