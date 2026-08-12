import { useEffect, useState } from "react";
import { fetchUsers, fetchUserDetails } from "../api/bankService";
import { tableHeader, tableCell } from "../styles/tableStyles";
import PageCard from "./PageCard";
import UserDetailsDrawer from "./UserDetailsDrawer";
import { getUserRoleStyle } from "../utils/userRoleUtils";
import { formatDate } from "../utils/formatUtils";
import styles from '../styles/userManagementStyles';

const UserManagementView = () => {
    const [users, setUsers] = useState([]);
    const [pageData, setPageData] = useState(null);
    const [currentPage, setCurrentPage] = useState(0);
    const [searchInput, setSearchInput] = useState("");
    const [loading, setLoading] = useState(true);
    const [tableLoading, setTableLoading] = useState(false);
    const [error, setError] = useState("");
    const [search, setSearch] = useState("");
    const [roleFilter, setRoleFilter] = useState("ALL");
    const [drawerOpen, setDrawerOpen] = useState(false);
    const [selectedUserId, setSelectedUserId] = useState(null);
    const [selectedUser, setSelectedUser] = useState(null);
    const [detailsLoading, setDetailsLoading] = useState(false);
    const [searchLoading, setSearchLoading] = useState(false);

    useEffect(() => {
        loadUsers();
    }, [
        currentPage,
        search,
        roleFilter
    ]);

    useEffect(() => {

        const value = searchInput.trim();

        if (value === search) {
            return;
        }

        setSearchLoading(true);

        const timer = setTimeout(() => {

            setCurrentPage(0);
            setSearch(value);

        }, 400);


        return () => clearTimeout(timer);

    }, [searchInput]);

    const loadUsers = async () => {

        try {

            if (!pageData) {
                setLoading(true);
            } else {
                setTableLoading(true);
            }

            setError("");

            const response = await fetchUsers({
                page: currentPage,
                size: 10,
                search,
                role: roleFilter,
            });


            setUsers(response.content);
            setPageData(response);


        } catch (err) {

            console.error(err);
            setError("Failed to load users.");

        } finally {

            setLoading(false);
            setTableLoading(false);
            setSearchLoading(false);

        }
    };

    const openUserDetails = async (userId) => {

        try {

            if (detailsLoading) return;

            setDrawerOpen(true);
            setSelectedUserId(userId);
            setSelectedUser(null);
            setDetailsLoading(true);

            const response = await fetchUserDetails(userId);

            setSelectedUser(response);

        } catch (err) {

            console.error(err);
            setError("Unable to load user details.");

        } finally {

            setDetailsLoading(false);

        }

    };

    if (loading) {
        return <p>Loading users...</p>;
    }

    if (error) {
        return <p>{error}</p>;
    }

    const closeDrawer = () => {
        setDrawerOpen(false);
        setSelectedUserId(null);
        setSelectedUser(null);
    };

    return (
        <div>
            <PageCard title="👥 User Management">


                <div style={styles.toolbar}>

                    <div style={styles.searchWrapper}>

                        <input
                            type="text"
                            placeholder="Search customer name or email..."
                            value={searchInput}
                            onChange={(e) => {
                                setSearchInput(e.target.value);
                            }}
                            style={styles.searchInput}
                        />


                        {searchLoading && (
                            <span style={styles.searchStatus}>
                                Searching...
                            </span>
                        )}


                        {searchInput && (
                            <button
                                style={styles.clearButton}
                                onClick={() => {
                                    setSearchInput("");
                                    setSearch("");
                                    setCurrentPage(0);
                                }}
                            >
                                ✕
                            </button>
                        )}
                    </div>
                    <select
                        value={roleFilter}
                        onChange={(e) => {
                            setCurrentPage(0);
                            setRoleFilter(e.target.value);
                        }}
                        style={styles.filterSelect}
                    >
                        <option value="ALL">
                            All Roles
                        </option>
                        <option value="CUSTOMER">
                            Customers
                        </option>
                        <option value="ADMIN">
                            Admins
                        </option>
                    </select>
                </div>
                <div style={styles.resultRow}>
                    <div style={styles.resultInfo}>
                        Showing {pageData?.totalElements ?? 0} users
                        {search && ` matching "${search}"`}
                    </div>
                    {tableLoading && (
                        <span style={styles.loadingText}>
                            Updating...
                        </span>
                    )}
                </div>
                <hr
                    style={{
                        border: "none",
                        borderTop: "1px solid #e5e7eb",
                        margin: "28px 0",
                    }}
                />

                <div
                    style={{
                        ...styles.tableContainer,
                        opacity: tableLoading ? 0.6 : 1,
                        transition: "opacity .2s ease",
                    }}
                >
                    <table
                        style={{
                            width: "100%",
                            borderCollapse: "collapse",
                            marginTop: "20px"
                        }}
                    >
                        <thead>
                            <tr>
                                <th style={tableHeader}>Full Name</th>
                                <th style={tableHeader}>Email</th>
                                <th style={tableHeader}>Role</th>
                                {roleFilter !== "ADMIN" && (
                                    <th style={tableHeader}>Accounts</th>
                                )}
                                <th style={tableHeader}>Joined</th>
                            </tr>
                        </thead>

                        <tbody>
                            {users.map((user) => (
                                <tr
                                    key={user.id}
                                    onClick={() => openUserDetails(user.id)}
                                    style={{
                                        cursor: "pointer",
                                        background:
                                            selectedUserId === user.id
                                                ? "#eef6ff"
                                                : "#fff",
                                        transition: ".18s",
                                    }}
                                    onMouseEnter={(e) => {
                                        if (selectedUserId !== user.id) {
                                            e.currentTarget.style.background = "#f8fafc";
                                        }
                                    }}
                                    onMouseLeave={(e) => {
                                        if (selectedUserId !== user.id) {
                                            e.currentTarget.style.background = "#fff";
                                        }
                                    }}
                                >
                                    <td style={tableCell}>{user.fullName}</td>
                                    <td style={tableCell}>{user.email}</td>
                                    <td style={tableCell}>
                                        <span
                                            style={{
                                                padding: "4px 10px",
                                                borderRadius: "20px",
                                                fontSize: "12px",
                                                fontWeight: 600,
                                                ...getUserRoleStyle(user.role)
                                            }}
                                        >
                                            {user.role}
                                        </span>
                                    </td>
                                    {roleFilter !== "ADMIN" && (
                                        <td style={tableCell}>
                                            {user.role === "ADMIN" ? "—" : user.accountCount}
                                        </td>
                                    )}
                                    <td style={tableCell}>
                                        {formatDate(user.createdAt)}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>

                {
                    pageData && pageData.totalPages > 1 && (

                        <div style={styles.pagination}>

                            <button
                                disabled={pageData.first}
                                onClick={() =>
                                    setCurrentPage(prev => prev - 1)
                                }
                                style={{
                                    ...styles.pageButton,
                                    ...(pageData.first && styles.disabledPageButton)
                                }}
                            >
                                ← Previous
                            </button>


                            <span style={styles.pageInfo}>
                                Page {pageData.number + 1} of {pageData.totalPages}
                            </span>


                            <button
                                disabled={pageData.last}
                                onClick={() =>
                                    setCurrentPage(prev => prev + 1)
                                }
                                style={{
                                    ...styles.pageButton,
                                    ...(pageData.last && styles.disabledPageButton)
                                }}
                            >
                                Next →
                            </button>

                        </div>

                    )
                }
            </PageCard>
            <UserDetailsDrawer
                open={drawerOpen}
                loading={detailsLoading}
                user={selectedUser}
                onClose={closeDrawer}
            />
        </div>
    );
};

export default UserManagementView;