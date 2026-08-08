import { formatCurrency, formatDate } from "../utils/formatUtils";
import { getFixedDepositStatusStyle } from "../utils/fixedDepositStatusUtils";

const UserFixedDepositsSection = ({
    fixedDeposits,
    fixedDepositsLoading,
    showFixedDeposits,
    styles,
}) => {

    return (
        showFixedDeposits && (
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
                                            ...getFixedDepositStatusStyle(fd.status),
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
        )
    );
};

const styles = {
    fixedDepositContent: {
        flex: 1,
    },
}

export default UserFixedDepositsSection;