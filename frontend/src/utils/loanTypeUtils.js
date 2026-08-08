export const getLoanTypeStyle = (loanType) => {
    switch (loanType) {
        case "HOME":
            return {
                background: "#dbeafe",
                color: "#1d4ed8",
            };

        case "PERSONAL":
            return {
                background: "#fef3c7",
                color: "#92400e",
            };

        case "VEHICLE":
            return {
                background: "#dcfce7",
                color: "#15803d",
            };

        default:
            return {};
    }
};

export const getLoanTypeIcon = (loanType) => {
    switch (loanType) {
        case "HOME":
            return "🏠";
        case "PERSONAL":
            return "👤";
        case "VEHICLE":
            return "🚗";
        default:
            return "📄";
    }
};