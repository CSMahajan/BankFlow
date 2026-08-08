export const getUserRoleStyle = (role) => {
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