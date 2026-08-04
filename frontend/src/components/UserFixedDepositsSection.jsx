import { formatCurrency, formatDate } from "../utils/formatUtils";

const UserFixedDepositsSection = ({
    fixedDeposits,
    fixedDepositsLoading,
    showFixedDeposits,
    styles,
}) => {

    const getFdStatusStyle = (status) => {

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

    return (
        <>
            {showFixedDeposits && (
                <div style={styles.section}>
                    <h3 style={styles.sectionTitle}>
                        Fixed Deposits
                    </h3>
                    {fixedDepositsLoading ? (
                        <p>Loading fixed deposits...</p>
                    ) : fixedDeposits.length === 0 ? (
                        <div style={styles.emptyAccounts}>
                            No fixed deposits found.
                        </div>
                    ) : (
                        fixedDeposits.map(fd => (
                            <div
                                key={fd.fdNumber}
                                style={styles.accountCard}
                            >
                                <div style={styles.fixedDepositContent}>
                                    <div style={styles.accountHeader}>
                                        <strong>
                                            💰 Fixed Deposit
                                        </strong>
                                        <span
                                            style={{
                                                ...styles.accountStatus,
                                                ...getFdStatusStyle(fd.status),
                                            }}
                                        >
                                            {fd.status.replaceAll("_", " ")}
                                        </span>
                                    </div>
                                    <div style={styles.accountNumber}>
                                        {fd.fdNumber}
                                    </div>
                                    {fd.status === "ACTIVE" && (
                                        <>
                                            <div style={styles.loanMeta}>
                                                Interest {fd.interestRate}%
                                            </div>

                                            <div style={styles.loanMeta}>
                                                Matures : {formatDate(fd.maturityDate)}
                                            </div>
                                        </>
                                    )}
                                </div>
                                {fd.status === "ACTIVE" && (
                                    <div style={styles.balanceSection}>
                                        <div style={styles.balanceLabel}>
                                            Maturity Amount
                                        </div>

                                        <div style={styles.balanceValue}>
                                            {formatCurrency(fd.maturityAmount)}
                                        </div>
                                    </div>
                                )}
                            </div>
                        ))
                    )}
                </div>
            )}

        </>
    );
};

const styles = {
    fixedDepositContent: {
        flex: 1,
    },
}

export default UserFixedDepositsSection;