import React from "react";
import { formatDate, formatCurrency } from "../utils/formatUtils";
import { useState } from "react";
import { fetchUserAccounts, fetchUserCards, fetchUserLoans, fetchUserFixedDeposits } from "../api/bankService";
import UserProfileHeader from "./UserProfileHeader";
import UserSummaryCards from "./UserSummaryCards";
import UserInformationSection from "./UserInformationSection";
import UserAccountsSection from "./UserAccountsSection";
import UserCardsSection from "./UserCardsSection";
import UserLoansSection from "./UserLoansSection";
import UserFixedDepositsSection from "./UserFixedDepositsSection";
import styles from "../styles/userDetailsDrawerStyles";

const UserDetailsDrawer = ({
    open,
    loading,
    user,
    onClose,
}) => {

    const [accounts, setAccounts] = useState([]);
    const [accountsLoading, setAccountsLoading] = useState(false);
    const [cards, setCards] = useState([]);
    const [cardsLoading, setCardsLoading] = useState(false);
    const [loans, setLoans] = useState([]);
    const [loansLoading, setLoansLoading] = useState(false);
    const [fixedDeposits, setFixedDeposits] = useState([]);
    const [fixedDepositsLoading, setFixedDepositsLoading] = useState(false);
    const [activeSection, setActiveSection] = useState(null);

    if (!open) return null;

    const loadAccounts = async () => {

        if (activeSection === "accounts") {
            setActiveSection(null);
            return;
        }

        try {
            setAccountsLoading(true);

            const accountList = await fetchUserAccounts(user.id);

            setAccounts(accountList);
            setActiveSection("accounts");

        } finally {
            setAccountsLoading(false);
        }
    };


    const loadCards = async () => {

        if (activeSection === "cards") {
            setActiveSection(null);
            return;
        }

        try {
            setCardsLoading(true);

            const cardList = await fetchUserCards(user.id);

            setCards(cardList);
            setActiveSection("cards");

        } finally {
            setCardsLoading(false);
        }
    };

    const loadLoans = async () => {

        if (activeSection === "loans") {
            setActiveSection(null);
            return;
        }

        try {
            setLoansLoading(true);

            const loanList = await fetchUserLoans(user.id);

            setLoans(loanList);
            setActiveSection("loans");

        } finally {
            setLoansLoading(false);
        }
    };

    const loadFixedDeposits = async () => {

        if (activeSection === "fixedDeposits") {
            setActiveSection(null);
            return;
        }

        try {
            setFixedDepositsLoading(true);

            const fixedDepositList =
                await fetchUserFixedDeposits(user.id);

            setFixedDeposits(fixedDepositList);
            setActiveSection("fixedDeposits");

        } finally {
            setFixedDepositsLoading(false);
        }
    };

    return (
        <div
            style={styles.overlay}
            onClick={onClose}
        >
            <div
                style={styles.drawer}
                onClick={(e) => e.stopPropagation()}
            >
                <div style={styles.drawerTopBar}>
                    <button
                        onClick={onClose}
                        style={styles.closeBtn}
                    >
                        ✕
                    </button>
                </div>

                {loading ? (
                    <div style={styles.loading}>
                        Loading...
                    </div>
                ) : user ? (
                    <>
                        <UserProfileHeader
                            user={user}
                            styles={styles}
                        />
                        <UserSummaryCards
                            user={user}
                            styles={styles}
                            loadAccounts={loadAccounts}
                            loadCards={loadCards}
                            loadLoans={loadLoans}
                            loadFixedDeposits={loadFixedDeposits}
                            showAccounts={activeSection === "accounts"}
                            showCards={activeSection === "cards"}
                            showLoans={activeSection === "loans"}
                            showFixedDeposits={activeSection === "fixedDeposits"}
                        />

                        <UserAccountsSection
                            accounts={accounts}
                            accountsLoading={accountsLoading}
                            showAccounts={activeSection === "accounts"}
                            styles={styles}
                        />

                        <UserCardsSection
                            cards={cards}
                            cardsLoading={cardsLoading}
                            showCards={activeSection === "cards"}
                            styles={styles}
                        />

                        <UserLoansSection
                            loans={loans}
                            loansLoading={loansLoading}
                            showLoans={activeSection === "loans"}
                            styles={styles}
                        />

                        <UserFixedDepositsSection
                            fixedDeposits={fixedDeposits}
                            fixedDepositsLoading={fixedDepositsLoading}
                            showFixedDeposits={activeSection === "fixedDeposits"}
                            styles={styles}
                        />

                        <UserInformationSection
                            user={user}
                            styles={styles}
                        />

                    </>
                ) : (
                    <p>No user found.</p>
                )}
            </div>
        </div>
    );
};

export default UserDetailsDrawer;