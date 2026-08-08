import { formatCurrency } from "../utils/formatUtils";
import { getAccountStatusStyle } from "../utils/accountStatusUtils";

const UserAccountsSection = ({
    accounts,
    accountsLoading,
    showAccounts,
    styles,
}) => {

    return (
        showAccounts && (
            <div style={styles.section}>
                <h3 style={styles.sectionTitle}>
                    Linked Accounts
                </h3>
                {accountsLoading ? (
                    <p>Loading accounts...</p>
                ) : accounts.length === 0 ? (
                    <div style={styles.emptyAccounts}>
                        No accounts found.
                    </div>
                ) : (
                    accounts.map((account) => (
                        <div
                            key={account.accountNumber}
                            style={styles.accountCard}
                        >
                            <div style={styles.accountContent}>
                                <div style={styles.accountHeader}>
                                    <strong>
                                        {account.accountType} Account
                                    </strong>
                                    <span
                                        style={{
                                            ...styles.accountStatus,
                                            ...getAccountStatusStyle(account.accountStatus),
                                        }}
                                    >
                                        {account.accountStatus}
                                    </span>
                                </div>
                                <div style={styles.accountNumber}>
                                    {account.accountNumber}
                                </div>
                                <div style={styles.branchName}>
                                    📍 {account.branchName}
                                </div>
                            </div>
                            <div style={styles.balanceSection}>
                                <div style={styles.balanceLabel}>
                                    Available Balance
                                </div>
                                <div style={styles.balanceValue}>
                                    {formatCurrency(account.currentBalance)}
                                </div>
                            </div>
                        </div>
                    ))
                )}
            </div>
        )
    );
};

const styles = {
    accountContent: {
        flex: 1,
    },
}

export default UserAccountsSection;
