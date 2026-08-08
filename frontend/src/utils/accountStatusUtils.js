export const getAccountStatusStyle = (status) => {
    switch (status) {
        case "ACTIVE":
            return {
                icon: "🟢",
                background: "#dcfce7",
                color: "#15803d",
                border: "1px solid #86efac",
            };

        case "FROZEN":
            return {
                icon: "🔒",
                background: "#fee2e2",
                color: "#b91c1c",
                border: "1px solid #fca5a5",
            };

        case "INACTIVE":
        default:
            return {
                icon: "⚪",
                background: "#f3f4f6",
                color: "#6b7280",
                border: "1px solid #d1d5db",
            };
    }
};