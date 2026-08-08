export const getCardStatusStyle = (status) => {
    switch (status) {
        case "ACTIVE":
            return {
                background: "#dcfce7",
                color: "#15803d",
                icon: "🟢",
            };

        case "FROZEN":
            return {
                background: "#fee2e2",
                color: "#b91c1c",
                icon: "🔒",
            };

        default:
            return {
                background: "#f3f4f6",
                color: "#6b7280",
                icon: "⚪",
            };
    }
};