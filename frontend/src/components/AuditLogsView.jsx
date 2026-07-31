import { useEffect, useState } from "react";
import { fetchAuditLogs } from "../api/bankService";
import PageCard from "./PageCard";
import { tableHeader, tableCell } from "../styles/tableStyles";

const AuditLogsView = () => {

    const [logs, setLogs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [search, setSearch] = useState("");
    const [roleFilter, setRoleFilter] = useState("ALL");
    const [moduleFilter, setModuleFilter] = useState("ALL");

    useEffect(() => {
        loadLogs();
    }, []);

    const loadLogs = async () => {

        try {
            setLoading(true);

            const response = await fetchAuditLogs();

            setLogs(response.content);

        } catch (err) {

            console.error(err);
            setError("Failed to load audit logs.");

        } finally {

            setLoading(false);
        }
    };

    if (loading) return <p>Loading audit logs...</p>;

    if (error) return <p>{error}</p>;

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

            default:
                return {
                    backgroundColor: "#f3f4f6",
                    color: "#374151",
                };
        }
    };

    const moduleActions = {
        LOANS: [
            "LOAN_APPLIED",
            "LOAN_APPROVED",
            "LOAN_REJECTED",
            "EMI_PAID",
        ],

        ACCOUNTS: [
            "ACCOUNT_CREATED",
        ],

        TRANSFERS: [
            "MONEY_TRANSFER",
        ],

        FIXED_DEPOSITS: [
            "FD_CREATED",
            "FD_CLOSED",
        ],

        PROFILE: [
            "PROFILE_UPDATED",
        ],

        LOGIN: [
            "LOGIN",
        ],
    };

    const filteredLogs = logs.filter((log) => {

        const matchesSearch =
            log.performedBy.toLowerCase().includes(search.toLowerCase()) ||
            log.description.toLowerCase().includes(search.toLowerCase());

        const matchesRole =
            roleFilter === "ALL" ||
            log.role === roleFilter;

        const matchesModule =
            moduleFilter === "ALL" ||
            moduleActions[moduleFilter].includes(log.action);

        return (
            matchesSearch &&
            matchesRole &&
            matchesModule
        );
    });

    return (

        <PageCard
            title="Audit Logs"
            subtitle="Track important banking activities."
        >

            <div
                style={{
                    display: "flex",
                    alignItems: "center",
                    gap: "12px",
                    marginBottom: "16px",
                    flexWrap: "wrap",
                }}
            >
                <input
                    type="text"
                    placeholder="🔍 Search user or description..."
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                    style={{
                        width: "340px",
                        padding: "10px 14px",
                        border: "1px solid #cbd5e1",
                        borderRadius: "8px",
                        fontSize: "14px",
                        outline: "none",
                    }}
                />
            </div>

            {/* Role Filter */}

            <div
                style={{
                    display: "flex",
                    alignItems: "center",
                    gap: "12px",
                    marginBottom: "16px",
                    flexWrap: "wrap",
                }}
            >

                <strong
                    style={{
                        minWidth: "70px",
                        color: "#475569",
                    }}
                >
                    Role
                </strong>

                {["ALL", "CUSTOMER", "ADMIN"].map((role) => (
                    <button
                        key={role}
                        onClick={() => setRoleFilter(role)}
                        style={{
                            padding: "8px 16px",
                            borderRadius: "20px",
                            border: "1px solid #cbd5e1",
                            cursor: "pointer",
                            backgroundColor:
                                roleFilter === role ? "#1e293b" : "#ffffff",
                            color:
                                roleFilter === role ? "#ffffff" : "#334155",
                            fontWeight: "600",
                        }}
                    >
                        {role === "ALL"
                            ? "All"
                            : role === "CUSTOMER"
                                ? "Customers"
                                : "Admins"}
                    </button>
                ))}

            </div>

            {/* Module Filter */}

            <div
                style={{
                    display: "flex",
                    alignItems: "flex-start",
                    gap: "12px",
                    marginBottom: "24px",
                }}
            >

                <strong
                    style={{
                        minWidth: "70px",
                        color: "#475569",
                        paddingTop: "8px",
                    }}
                >
                    Module
                </strong>

                <div
                    style={{
                        display: "flex",
                        gap: "10px",
                        flexWrap: "wrap",
                    }}
                >
                    {[
                        ["ALL", "All"],
                        ["LOANS", "Loans"],
                        ["ACCOUNTS", "Accounts"],
                        ["FIXED_DEPOSITS", "Fixed Deposits"],
                        ["TRANSFERS", "Transfers"],
                        ["PROFILE", "Profile"],
                        ["LOGIN", "Login"],
                    ].map(([value, label]) => (
                        <button
                            key={value}
                            onClick={() => setModuleFilter(value)}
                            style={{
                                padding: "8px 16px",
                                borderRadius: "20px",
                                border: "1px solid #cbd5e1",
                                cursor: "pointer",
                                backgroundColor:
                                    moduleFilter === value ? "#0d6360" : "#ffffff",
                                color:
                                    moduleFilter === value ? "#ffffff" : "#374151",
                                fontWeight: "600",
                            }}
                        >
                            {label}
                        </button>
                    ))}
                </div>

            </div>

            <div
                style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    marginBottom: "16px",
                }}
            >
                <div
                    style={{
                        color: "#64748b",
                        fontSize: "14px",
                    }}
                >
                    Showing <strong>{filteredLogs.length}</strong> of{" "}
                    <strong>{logs.length}</strong> audit logs
                </div>

                {(search || roleFilter !== "ALL" || moduleFilter !== "ALL") && (
                    <button
                        onClick={() => {
                            setSearch("");
                            setRoleFilter("ALL");
                            setModuleFilter("ALL");
                        }}
                        style={{
                            padding: "8px 14px",
                            border: "1px solid #cbd5e1",
                            borderRadius: "8px",
                            backgroundColor: "#fff",
                            cursor: "pointer",
                            fontWeight: "600",
                            color: "#475569",
                        }}
                    >
                        Clear Filters
                    </button>
                )}
            </div>

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
                            <th style={tableHeader}>Action</th>
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

                                    <td style={tableCell}>
                                        <span
                                            style={{
                                                padding: "4px 10px",
                                                borderRadius: "999px",
                                                fontSize: "12px",
                                                fontWeight: "600",
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
        </PageCard>

    );

};

export default AuditLogsView;