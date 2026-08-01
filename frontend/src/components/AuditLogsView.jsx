import { useEffect, useState } from "react";
import { fetchAuditLogs } from "../api/bankService";
import PageCard from "./PageCard";
import { tableHeader, tableCell } from "../styles/tableStyles";
import AuditFilterBar from "./audit/AuditFilterBar";
import AuditPagination from "./audit/AuditPagination";
import { actionDisplayNames, moduleDisplayNames, moduleActions } from "./audit/auditConstants";

const AuditLogsView = () => {

    const [logs, setLogs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [search, setSearch] = useState("");
    const [roleFilter, setRoleFilter] = useState("ALL");
    const [moduleFilter, setModuleFilter] = useState("ALL");
    const [actionFilter, setActionFilter] = useState("ALL");
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    useEffect(() => {
        loadLogs();
    }, [currentPage]);

    const loadLogs = async () => {

        try {
            setLoading(true);

            const response = await fetchAuditLogs(currentPage, 10);

            setLogs(response.content);

            setTotalPages(response.totalPages);

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

    const filteredLogs = logs.filter((log) => {

        const matchesSearch =
            log.performedBy.toLowerCase().includes(search.toLowerCase()) ||
            log.description.toLowerCase().includes(search.toLowerCase());

        const matchesRole =
            roleFilter === "ALL" ||
            log.role === roleFilter;

        const matchesModule =
            moduleFilter === "ALL" ||
            moduleActions[moduleFilter]?.includes(log.action);

        const matchesAction =
            actionFilter === "ALL" ||
            log.action === actionFilter;

        return (
            matchesSearch &&
            matchesRole &&
            matchesModule &&
            matchesAction
        );
    });

    const availableActions =
        moduleFilter === "ALL"
            ? [...new Set(Object.values(moduleActions).flat())]
            : moduleActions[moduleFilter] ?? [];

    return (

        <PageCard
            title="Audit Logs"
            subtitle="Track important banking activities."
        >

            <AuditFilterBar
                search={search}
                setSearch={setSearch}
                roleFilter={roleFilter}
                setRoleFilter={setRoleFilter}
                moduleFilter={moduleFilter}
                setModuleFilter={setModuleFilter}
                actionFilter={actionFilter}
                setActionFilter={setActionFilter}
                availableActions={availableActions}
                loadLogs={loadLogs}
                setCurrentPage={setCurrentPage}
            />

            <div
                style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    marginBottom: "18px",
                }}
            >

                <div
                    style={{
                        color: "#64748b",
                        fontSize: "15px",
                    }}
                >
                    Showing <strong>{filteredLogs.length}</strong> of{" "}
                    <strong>{logs.length}</strong> audit logs
                </div>

                {(search || roleFilter !== "ALL" || moduleFilter !== "ALL" || actionFilter !== "ALL") && (
                    <button
                        onClick={() => {
                            setSearch("");
                            setRoleFilter("ALL");
                            setModuleFilter("ALL");
                            setActionFilter("ALL");
                            setCurrentPage(0);
                        }}
                        style={styles.clearButton}
                    >
                        ✕ Clear Filters
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
            <AuditPagination
                currentPage={currentPage}
                totalPages={totalPages}
                setCurrentPage={setCurrentPage}
            />
        </PageCard>
    );
};

const styles = {

    clearButton: {
        padding: "10px 18px",
        borderRadius: "10px",
        border: "1px solid #c7d2fe",
        backgroundColor: "#ffffff",
        color: "#2563eb",
        fontWeight: "700",
        cursor: "pointer",
    },
};

export default AuditLogsView;