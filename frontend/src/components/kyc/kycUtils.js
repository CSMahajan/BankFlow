export const getStatusColor = (status) => {

    switch (status) {

        case "VERIFIED":
            return {
                background: "#dcfce7",
                color: "#166534"
            };

        case "PENDING":
            return {
                background: "#fef3c7",
                color: "#92400e"
            };

        case "REJECTED":
            return {
                background: "#fee2e2",
                color: "#991b1b"
            };

        default:
            return {
                background: "#e5e7eb",
                color: "#374151"
            };

    }

};