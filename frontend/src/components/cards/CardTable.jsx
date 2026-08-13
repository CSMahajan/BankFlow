import React from "react";
import { formatDate, formatCurrency } from "../../utils/formatUtils";
import { getCardStatusStyle } from "../../utils/cardStatusUtils";
import CardDetails from './CardDetails';

const CardTable = ({
    cards,
    expandedCardId,
    setExpandedCardId,
    handleStatusClick,
}) => {

    return (
        <>
            <div
                style={{
                    overflowX: "auto",
                    border: "1px solid #e5e7eb",
                    borderRadius: "12px",
                }}
            >

                <table
                    style={{
                        width: "100%",
                        borderCollapse: "collapse"
                    }}
                >

                    <thead>
                        <tr>
                            <th style={styles.header}>Customer</th>
                            <th style={styles.header}>Card Number</th>
                            <th style={styles.header}>Type</th>
                            <th style={styles.header}>Daily Limit</th>
                            <th style={styles.header}>Status</th>
                        </tr>
                    </thead>


                    <tbody>

                        {cards.map((card, index) => (

                            <React.Fragment key={card.id}>

                                <tr
                                    onClick={() =>
                                        setExpandedCardId(
                                            expandedCardId === card.id
                                                ? null
                                                : card.id
                                        )
                                    }

                                    style={{
                                        cursor: "pointer",
                                        transition: ".15s",
                                        background:
                                            expandedCardId === card.id
                                                ? "#eff6ff"
                                                : index % 2 === 0
                                                    ? "#ffffff"
                                                    : "#fafafa",
                                    }}

                                    onMouseEnter={(e) => {

                                        if (expandedCardId !== card.id) {
                                            e.currentTarget.style.background =
                                                "#eff6ff";
                                        }

                                    }}

                                    onMouseLeave={(e) => {

                                        if (expandedCardId !== card.id) {
                                            e.currentTarget.style.background =
                                                index % 2 === 0
                                                    ? "#ffffff"
                                                    : "#fafafa";
                                        }

                                    }}
                                >

                                    <td style={styles.cell}>
                                        {card.customerName}
                                    </td>


                                    <td style={styles.cell}>

                                        <span
                                            style={{
                                                fontFamily: "monospace",
                                                fontWeight: 600,
                                                color: "#15803d",
                                            }}
                                        >
                                            {card.maskedCardNumber}
                                        </span>

                                    </td>


                                    <td style={styles.cell}>
                                        {card.cardType}
                                    </td>


                                    <td style={styles.cell}>
                                        {formatCurrency(card.dailyLimit)}
                                    </td>


                                    <td style={styles.cell}>

                                        <span
                                            style={{
                                                padding: "5px 10px",
                                                borderRadius: "999px",
                                                fontWeight: 600,
                                                fontSize: "12px",
                                                ...getCardStatusStyle(
                                                    card.cardStatus
                                                ),
                                            }}
                                        >
                                            {card.cardStatus}
                                        </span>

                                    </td>

                                </tr>


                                {expandedCardId === card.id && (

                                    <tr>

                                        <td
                                            colSpan={5}
                                            style={styles.expandedRow}
                                        >

                                            <CardDetails
                                                card={card}
                                                handleStatusClick={
                                                    handleStatusClick
                                                }
                                            />

                                        </td>

                                    </tr>

                                )}

                            </React.Fragment>

                        ))}

                    </tbody>

                </table>

            </div>
        </>
    );
};

const styles = {

    header: {
        background: "#f8fafc",
        color: "#334155",
        fontWeight: 700,
        fontSize: "14px",
        padding: "14px 16px",
        textAlign: "left",
        borderBottom: "1px solid #e5e7eb",
        whiteSpace: "nowrap",
    },


    cell: {
        padding: "16px",
        borderBottom: "1px solid #f1f5f9",
        fontSize: "14px",
        color: "#334155",
    },


    expandedRow: {
        padding: "18px",
        background: "#fff",
        borderBottom: "1px solid #e5e7eb",
    }

};

export default CardTable;