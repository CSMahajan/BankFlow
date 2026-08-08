export const getFixedDepositStatusStyle = (status) => {
    switch (status) {
        case "ACTIVE":
            return {
                backgroundColor: "#dcfce7",
                color: "#15803d",
            };

        case "PREMATURE_CLOSED":
            return {
                backgroundColor: "#fef3c7",
                color: "#b45309",
            };

        case "CLOSED":
            return {
                backgroundColor: "#dbeafe",
                color: "#1d4ed8",
            };

        default:
            return {
                backgroundColor: "#f3f4f6",
                color: "#6b7280",
            };
    }
};