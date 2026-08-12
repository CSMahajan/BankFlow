import React from "react";
import { formatCurrency } from "../utils/formatUtils";

const UserSummaryCards = ({
    user,
    styles,
    loadAccounts,
    loadCards,
    loadLoans,
    loadFixedDeposits,
    showAccounts,
    showCards,
    showLoans,
    showFixedDeposits,
}) => {

    const handleMouseEnter = (e) => {
        e.currentTarget.style.transform = "translateY(-3px)";
        e.currentTarget.style.boxShadow = "0 10px 22px rgba(0,0,0,.08)";
    };

    const handleMouseLeave = (e) => {
        e.currentTarget.style.transform = "translateY(0)";
        e.currentTarget.style.boxShadow = "none";
    };
    return (
        <>
            <div style={styles.financialCard}>
                <div style={styles.financialLabel}>
                    💰 Total Balance
                </div>
                <div style={styles.financialValue}>
                    {formatCurrency(user.totalBalance)}
                </div>
            </div>

            <div style={styles.loanCard}>
                <div style={styles.financialLabel}>
                    🏦 Outstanding Loan
                </div>
                <div style={styles.loanValue}>
                    {formatCurrency(user.outstandingLoanAmount)}
                </div>
            </div>
            <div style={styles.statsGrid}>
                <div
                    style={styles.statCard}
                    onClick={loadAccounts}
                    onMouseEnter={handleMouseEnter}
                    onMouseLeave={handleMouseLeave}
                >
                    <div style={styles.statIcon}>🏦</div>
                    <div style={styles.statTitle}>
                        Accounts
                    </div>
                    <div style={styles.statValue}>
                        {user.accountCount}
                    </div>
                    <div style={styles.statFooter}>
                        {showAccounts ? "Hide Accounts ↑" : "View Accounts →"}
                    </div>
                </div>
                <div
                    style={styles.statCard}
                    onClick={loadCards}
                    onMouseEnter={handleMouseEnter}
                    onMouseLeave={handleMouseLeave}
                >
                    <div style={styles.statIcon}>💳</div>
                    <div style={styles.statTitle}>
                        Cards
                    </div>
                    <div style={styles.statValue}>
                        {user.cardCount}
                    </div>
                    <div style={styles.statFooter}>
                        {showCards ? "Hide Cards ↑" : "View Cards →"}
                    </div>
                </div>
                <div
                    style={styles.statCard}
                    onClick={loadLoans}
                    onMouseEnter={handleMouseEnter}
                    onMouseLeave={handleMouseLeave}
                >
                    <div style={styles.statIcon}>📄</div>
                    <div style={styles.statTitle}>
                        Loans
                    </div>
                    <div style={styles.statValue}>
                        {user.loanCount}
                    </div>
                    <div style={styles.statFooter}>
                        {showLoans ? "Hide Loans ↑" : "View Loans →"}
                    </div>
                </div>
                <div
                    style={styles.statCard}
                    onClick={loadFixedDeposits}
                    onMouseEnter={handleMouseEnter}
                    onMouseLeave={handleMouseLeave}
                >
                    <div style={styles.statIcon}>💰</div>
                    <div style={styles.statTitle}>
                        Fixed Deposits
                    </div>
                    <div style={styles.statValue}>
                        {user.fixedDepositCount}
                    </div>
                    <div style={styles.statFooter}>
                        {showFixedDeposits ? "Hide Fixed Deposits ↑" : "View Fixed Deposits →"}
                    </div>
                </div>
            </div>
        </>
    );

};

export default UserSummaryCards;