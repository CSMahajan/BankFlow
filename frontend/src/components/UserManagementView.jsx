import { useEffect, useState } from "react";
import { fetchUsers, fetchUserDetails } from "../api/bankService";
import { tableHeader, tableCell } from "../styles/tableStyles";
import PageCard from "./PageCard";
import UserDetailsDrawer from "./UserDetailsDrawer";

const UserManagementView = () => {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [search, setSearch] = useState("");
    const [roleFilter, setRoleFilter] = useState("ALL");
    const [drawerOpen, setDrawerOpen] = useState(false);
    const [selectedUserId, setSelectedUserId] = useState(null);
    const [selectedUser, setSelectedUser] = useState(null);
    const [detailsLoading, setDetailsLoading] = useState(false);

    useEffect(() => {
        loadUsers();
    }, []);

    const loadUsers = async () => {
        try {
            setLoading(true);
            setError("");

            const response = await fetchUsers();
            setUsers(response);
        } catch (err) {
            console.error(err);
            console.log(err.response);
            console.log(err.response?.data);

            setError("Failed to load users.");
        } finally {
            setLoading(false);
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

    const closeDrawer = () => {
        setDrawerOpen(false);
        setSelectedUserId(null);
        setSelectedUser(null);
    };

    if (loading) {
        return <p>Loading users...</p>;
    }

    if (error) {
        return <p>{error}</p>;
    }

    const filteredUsers = users.filter((user) => {
        const matchesSearch =
            user.fullName.toLowerCase().includes(search.toLowerCase()) ||
            user.email.toLowerCase().includes(search.toLowerCase());

        const matchesRole =
            roleFilter === "ALL" || user.role === roleFilter;

        return matchesSearch && matchesRole;
    });

    return (
        <div>
            <PageCard title="👥 User Management">
                <div
                    style={{
                        overflowX: "auto",
                        border: "1px solid #e2e8f0",
                        borderRadius: "10px",
                    }}
                >

                    <div style={{ marginBottom: "20px" }}>
                        <input
                            type="text"
                            placeholder="Search by name or email..."
                            value={search}
                            onChange={(e) => setSearch(e.target.value)}
                            style={{
                                width: "320px",
                                padding: "10px 14px",
                                border: "1px solid #cbd5e1",
                                borderRadius: "8px",
                                fontSize: "14px",
                                outline: "none",
                            }}
                        />
                    </div>

                    <div
                        style={{
                            display: "flex",
                            gap: "10px",
                            marginBottom: "20px",
                        }}
                    >
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
                            {filteredUsers.map((user) => (
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
                                                backgroundColor:
                                                    user.role === "ADMIN"
                                                        ? "#dbeafe"
                                                        : "#dcfce7",
                                                color:
                                                    user.role === "ADMIN"
                                                        ? "#1d4ed8"
                                                        : "#15803d",
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
                                        {new Date(user.createdAt).toLocaleDateString("en-IN")}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
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