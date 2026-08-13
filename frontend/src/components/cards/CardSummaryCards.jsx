import React from "react";

const CardSummaryCards = ({
    activeCount,
    blockedCount,
    frozenCount,
}) => {

    const cardSummaryCards = [
        {
            title: "Active Cards",
            icon: "🟢",
            count: activeCount,
            style: {
                background: "#ECFDF5",
                border: "1px solid #A7F3D0",
            },
        },
        {
            title: "Blocked Cards",
            icon: "🔒",
            count: blockedCount,
            style: {
                background: "#FEF2F2",
                border: "1px solid #FECACA",
            },
        },
        {
            title: "Frozen Cards",
            icon: "❄️",
            count: frozenCount,
            style: {
                background: "#EFF6FF",
                border: "1px solid #BFDBFE",
            },
        }
    ];


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

    };


    return (

        <div style={styles.summaryGrid}>

            {cardSummaryCards.map((card) => (

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

                </div>

            ))}

        </div>

    );

};


export default CardSummaryCards;