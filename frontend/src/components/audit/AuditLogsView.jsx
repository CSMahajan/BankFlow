import { useEffect, useState } from "react";
import { fetchAuditLogs } from "../../api/bankService";
import PageCard from "../PageCard";
import AuditFilterBar from "./AuditFilterBar";
import AuditPagination from "./AuditPagination";
import AuditTable from "./AuditTable";
import { moduleActions } from "./auditConfig";

const AuditLogsView = () => {

    const [logs, setLogs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [search, setSearch] = useState("");
    const [searchInput, setSearchInput] = useState("");
    const [roleFilter, setRoleFilter] = useState("ALL");
    const [moduleFilter, setModuleFilter] = useState("ALL");
    const [actionFilter, setActionFilter] = useState("ALL");
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);

    useEffect(() => {
        loadLogs();
    }, [
        currentPage,
        search,
        roleFilter,
        moduleFilter,
        actionFilter,
    ]);

    const loadLogs = async () => {
        try {
            setLoading(true);
            const response = await fetchAuditLogs({
                page: currentPage,
                size: 10,
                search,
                role: roleFilter,
                ...(actionFilter !== "ALL"
                    ? {
                        action: actionFilter,
                    }
                    : moduleFilter !== "ALL"
                        ? {
                            actions: moduleActions[moduleFilter],
                        }
                        : {}),
            });
            setLogs(response.content);
            setTotalPages(response.totalPages);
            setTotalElements(response.totalElements);
        } catch (err) {
            console.error(err);
            setError("Failed to load audit logs.");
        } finally {
            setLoading(false);
        }
    };


    if (loading) return <p>Loading audit logs...</p>;

    if (error) return <p>{error}</p>;

    const filteredLogs = logs;

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
                search={searchInput}
                setSearch={setSearchInput}
                applySearch={() => {
                    setCurrentPage(0);
                    setSearch(searchInput.trim());
                }}
                roleFilter={roleFilter}
                setRoleFilter={setRoleFilter}
                moduleFilter={moduleFilter}
                setModuleFilter={setModuleFilter}
                actionFilter={actionFilter}
                setActionFilter={setActionFilter}
                availableActions={availableActions}
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
                    <strong>{totalElements}</strong> audit logs
                </div>

                {(search || roleFilter !== "ALL" || moduleFilter !== "ALL" || actionFilter !== "ALL") && (
                    <button
                        onClick={() => {
                            setSearch("");
                            setSearchInput("");
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

            <AuditTable
                filteredLogs={filteredLogs}
            />
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