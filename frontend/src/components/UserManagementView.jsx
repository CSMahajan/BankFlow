import { useEffect, useState } from "react";
import { fetchUsers } from "../api/bankService";
import { tableHeader, tableCell } from "../styles/tableStyles";
import PageCard from "./PageCard";

const UserManagementView = () => {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [search, setSearch] = useState("");
    const [roleFilter, setRoleFilter] = useState("ALL");

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
            <h2>User Management</h2>

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
                                <tr key={user.id}>
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
        </div>
    );
};

export default UserManagementView;