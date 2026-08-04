import React from "react";
import { formatDate } from "../utils/formatUtils";

const UserInformationSection = ({
    user,
    styles,
}) => {

    return (
        <>
            <div style={styles.infoSection}>
                <h3 style={styles.sectionTitle}>
                    Customer Information
                </h3>
                <div style={styles.infoRow}>
                    <span>User ID</span>
                    <strong>{user.id}</strong>
                </div>
                <div style={styles.infoRow}>
                    <span>Email</span>
                    <strong>{user.email}</strong>
                </div>
                <div style={styles.infoRow}>
                    <span>Joined</span>
                    <strong>
                        {formatDate(user.createdAt)}
                    </strong>
                </div>
            </div>
            <div style={styles.infoSection}>

                <h3 style={styles.sectionTitle}>
                    Quick Status
                </h3>

                <div style={styles.statusList}>

                    <div style={styles.statusRow}>
                        <span>🏦 Bank Accounts</span>
                        <span style={styles.greenStatus}>
                            {user.accountCount > 0 ? "Available" : "None"}
                        </span>
                    </div>

                    <div style={styles.statusRow}>
                        <span>💳 Cards</span>
                        <span
                            style={
                                user.cardCount > 0
                                    ? styles.greenStatus
                                    : styles.grayStatus
                            }
                        >
                            {user.cardCount > 0 ? "Issued" : "Not Issued"}
                        </span>
                    </div>

                    <div style={styles.statusRow}>
                        <span>📄 Loans</span>
                        <span
                            style={
                                user.loanCount > 0
                                    ? styles.orangeStatus
                                    : styles.grayStatus
                            }
                        >
                            {user.loanCount > 0 ? "Exists" : "None"}
                        </span>
                    </div>

                    <div style={styles.statusRow}>
                        <span>💰 Fixed Deposits</span>
                        <span
                            style={
                                user.fixedDepositCount > 0
                                    ? styles.greenStatus
                                    : styles.grayStatus
                            }
                        >
                            {user.fixedDepositCount > 0 ? "Available" : "None"}
                        </span>
                    </div>
                </div>
            </div>
        </>
    );

};

export default UserInformationSection;