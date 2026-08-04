import React from "react";

const UserProfileHeader = ({ user, styles }) => {

    return (
        <div style={styles.profileRow}>
            <div style={styles.avatar}>
                {user.fullName.charAt(0).toUpperCase()}
            </div>
            <div style={styles.profileInfo}>
                <h3 style={styles.userName}>
                    {user.fullName}
                </h3>
            </div>
            <span
                style={{
                    ...styles.roleBadge,
                    background:
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
        </div>
    );

};

export default UserProfileHeader;