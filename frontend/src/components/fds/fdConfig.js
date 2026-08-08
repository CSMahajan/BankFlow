export const FD_CONFIG = {
    1: {
        interestRate: 6.5,
        label: "6.5% p.a.",
    },
    3: {
        interestRate: 7.0,
        label: "7.0% p.a.",
    },
    5: {
        interestRate: 7.5,
        label: "7.5% p.a.",
    },
};

export const FD_TENURES = Object.keys(FD_CONFIG).map(Number);

export const FD_RATES = Object.fromEntries(
    Object.entries(FD_CONFIG).map(([tenure, config]) => [
        tenure,
        config.label,
    ])
);