export const getAccountStatusStyle = (status) => {
    switch (status) {
        case "ACTIVE":
            return {
                backgroundColor: "#dcfce7",
                color: "#15803d",
            };

        default:
            return {
                backgroundColor: "#fee2e2",
                color: "#b91c1c",
            };
    }
};