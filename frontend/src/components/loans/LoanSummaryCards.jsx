import React from "react";

const LoanSummaryCards = ({
    personalCount,
    homeCount,
    vehicleCount,
}) => {

    const loanTypeCards = [
        {
            title: "Personal Loans",
            icon: "👤",
            count: personalCount,
            style: {
                background: "#FEFCE8",
                border: "1px solid #FDE68A",
            },
        },
        {
            title: "Home Loans",
            icon: "🏠",
            count: homeCount,
            style: {
                background: "#EFF6FF",
                border: "1px solid #BFDBFE",
            },
        },
        {
            title: "Vehicle Loans",
            icon: "🚗",
            count: vehicleCount,
            style: {
                background: "#ECFDF5",
                border: "1px solid #A7F3D0",
            },
        },
    ];


    return (
        <div style={styles.summaryGrid}>

            {loanTypeCards.map((card) => (

                <div
                    key={card.title}
                    style={{
                        ...styles.summaryCard,
                        ...card.style,
                    }}
                >

                    <div style={styles.summaryTitle}>
                        <span>{card.icon}</span>
                        <span>{card.title}</span>
                    </div>


                    <div style={styles.summaryValue}>
                        {card.count}
                    </div>


                    <div style={styles.summarySubtext}>
                        Pending Applications
                    </div>

                </div>

            ))}

        </div>
    );
};


const styles = {

    summaryGrid: {
        display: "grid",
        gridTemplateColumns: "repeat(3, 1fr)",
        gap: "18px",
        marginBottom: "26px",
    },


    summaryCard: {
        border: "1px solid #e5e7eb",
        borderRadius: "12px",
        padding: "18px",
        background: "#fff",
        display: "flex",
        flexDirection: "column",
        alignItems: "flex-start",
    },


    summaryTitle: {
        display: "flex",
        alignItems: "center",
        gap: "8px",
        fontWeight: 600,
        color: "#334155",
        marginBottom: "18px",
    },


    summaryValue: {
        fontSize: "30px",
        fontWeight: 700,
        color: "#0f172a",
    },


    summarySubtext: {
        marginTop: "6px",
        color: "#64748b",
        fontSize: "13px",
    },

};


export default LoanSummaryCards;