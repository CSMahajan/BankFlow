export const formatCurrency = (amount) =>
    new Intl.NumberFormat('en-IN', {
        style: 'currency',
        currency: 'INR'
    }).format(amount);

export const formatDate = (date) => {
    if (!date) return '-';

    return new Date(date).toLocaleDateString('en-IN', {
        day: '2-digit',
        month: 'short',
        year: 'numeric'
    });
};

export const getTodayDate = () =>
    new Date().toLocaleDateString("en-CA");

export const formatDateTime = (dateTime) => {

    if (!dateTime) {
        return "-";
    }

    return new Date(dateTime).toLocaleString(
        "en-IN",
        {
            day: "2-digit",
            month: "short",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit",
            second: "2-digit",
        }
    );
};