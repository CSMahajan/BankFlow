import { formatDate, formatCurrency } from "../utils/formatUtils";

const UserCardsSection = ({
    cards,
    cardsLoading,
    showCards,
    styles,
}) => {

    const getCardStatusStyle = (status) => {
        return status === "ACTIVE"
            ? {
                backgroundColor: "#dcfce7",
                color: "#15803d",
            }
            : {
                backgroundColor: "#fee2e2",
                color: "#b91c1c",
            };
    };

    return (
        <>
            {showCards && (
                <div style={styles.section}>
                    <h3 style={styles.sectionTitle}>
                        Cards
                    </h3>
                    {cardsLoading ? (
                        <p>Loading cards...</p>
                    ) : cards.length === 0 ? (
                        <div style={styles.emptyAccounts}>
                            No cards issued.
                        </div>
                    ) : (
                        cards.map(card => (
                            <div
                                key={card.cardNumber}
                                style={styles.accountCard}
                            >
                                <div style={styles.cardContent}>
                                    <div style={styles.accountHeader}>
                                        <strong>
                                            {card.cardType === "DEBIT"
                                                ? "💳 Debit Card"
                                                : "💎 Credit Card"}
                                        </strong>
                                        <span
                                            style={{
                                                ...styles.accountStatus,
                                                backgroundColor:
                                                    card.cardStatus === "ACTIVE"
                                                        ? "#dcfce7"
                                                        : "#fee2e2",
                                                color:
                                                    card.cardStatus === "ACTIVE"
                                                        ? "#15803d"
                                                        : "#b91c1c",
                                            }}
                                        >
                                            {card.cardStatus}
                                        </span>
                                    </div>
                                    <div style={styles.accountNumber}>
                                        **** **** **** {card.cardNumber.slice(-4)}
                                    </div>
                                    <div style={styles.branchName}>
                                        Expires {formatDate(card.expiryDate)}
                                    </div>
                                </div>
                                <div style={styles.balanceSection}>
                                    <div style={styles.balanceLabel}>
                                        Daily Limit
                                    </div>
                                    <div style={styles.balanceValue}>
                                        {formatCurrency(card.dailyLimit)}
                                    </div>
                                </div>
                            </div>
                        ))
                    )}
                </div>
            )}
        </>
    );

};

const styles = {
    cardContent: {
        flex: 1,
    },
}

export default UserCardsSection;
