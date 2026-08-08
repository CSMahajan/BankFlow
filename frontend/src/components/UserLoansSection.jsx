import { formatCurrency, formatDate } from "../utils/formatUtils";
import { getLoanStatusStyle } from "../utils/loanStatusUtils";

const UserLoansSection = ({
    loans,
    loansLoading,
    showLoans,
    styles,
}) => {

    const getLoanStatusLabel = (status) => {
        switch (status) {
            case "ACTIVE":
                return "🟢 ACTIVE";
            case "PENDING":
                return "🟡 PENDING";
            case "REJECTED":
                return "🔴 REJECTED";
            case "PAID_OFF":
                return "🔵 PAID OFF";
            default:
                return status;
        }
    };

    return (
        showLoans && (
            <div style={styles.section}>
                <h3 style={styles.sectionTitle}>
                    Loans
                </h3>
                {loansLoading ? (
                    <p>Loading loans...</p>
                ) : loans.length === 0 ? (
                    <div style={styles.emptyAccounts}>
                        No loans found.
                    </div>
                ) : (
                    loans.map(loan => (
                        <div
                            key={loan.loanNumber}
                            style={styles.accountCard}
                        >
                            <div style={styles.loanContent}>
                                <div style={styles.accountHeader}>
                                    <strong>
                                        🏦 {loan.loanType} Loan
                                    </strong>
                                    <span
                                        style={{
                                            ...styles.accountStatus,
                                            ...getLoanStatusStyle(loan.status),
                                        }}
                                    >
                                        {getLoanStatusLabel(loan.status)}
                                    </span>
                                </div>
                                <div style={styles.accountNumber}>
                                    {loan.loanNumber}
                                </div>
                                {loan.status === "ACTIVE" && (
                                    <>
                                        <div style={styles.loanMeta}>
                                            EMI {formatCurrency(loan.monthlyEmi)}
                                        </div>

                                        <div style={styles.loanMeta}>
                                            Next Due : {formatDate(loan.nextDueDate)}
                                        </div>
                                    </>
                                )}
                            </div>
                            {loan.status === "ACTIVE" && (
                                <div style={styles.balanceSection}>
                                    <div style={styles.balanceLabel}>
                                        Outstanding
                                    </div>

                                    <div style={styles.balanceValue}>
                                        {formatCurrency(loan.remainingBalance)}
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
    loanContent: {
        flex: 1,
    },
}

export default UserLoansSection;