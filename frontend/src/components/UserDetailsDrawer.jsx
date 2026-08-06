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
    const [showAccounts, setShowAccounts] = useState(false);
    const [cards, setCards] = useState([]);
    const [cardsLoading, setCardsLoading] = useState(false);
    const [showCards, setShowCards] = useState(false);
    const [loans, setLoans] = useState([]);
    const [loansLoading, setLoansLoading] = useState(false);
    const [showLoans, setShowLoans] = useState(false);
    const [fixedDeposits, setFixedDeposits] = useState([]);
    const [fixedDepositsLoading, setFixedDepositsLoading] = useState(false);
    const [showFixedDeposits, setShowFixedDeposits] = useState(false);

    if (!open) return null;

    const loadAccounts = async () => {
        if (showAccounts) {
            setShowAccounts(false);
            return;
        }
        try {
            setAccountsLoading(true);
            const response = await fetchUserAccounts(user.id);
            setAccounts(response);
            setShowAccounts(true);
        } finally {
            setAccountsLoading(false);
        }
    };


    const loadCards = async () => {
        if (showCards) {
            setShowCards(false);
            return;
        }
        try {
            setCardsLoading(true);
            const response = await fetchUserCards(user.id);
            setCards(response);
            setShowCards(true);
        } finally {
            setCardsLoading(false);
        }
    };

    const loadLoans = async () => {
        if (showLoans) {
            setShowLoans(false);
            return;
        }
        try {
            setLoansLoading(true);
            const response = await fetchUserLoans(user.id);
            setLoans(response);
            setShowLoans(true);
        } finally {
            setLoansLoading(false);
        }
    };

    const loadFixedDeposits = async () => {
        if (showFixedDeposits) {
            setShowFixedDeposits(false);
            return;
        }
        try {
            setFixedDepositsLoading(true);
            const response =
                await fetchUserFixedDeposits(user.id);
            setFixedDeposits(response);
            setShowFixedDeposits(true);
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
                            showAccounts={showAccounts}
                            showCards={showCards}
                            showLoans={showLoans}
                            showFixedDeposits={showFixedDeposits}
                        />

                        <UserAccountsSection
                            accounts={accounts}
                            accountsLoading={accountsLoading}
                            showAccounts={showAccounts}
                            styles={styles}
                        />

                        <UserCardsSection
                            cards={cards}
                            cardsLoading={cardsLoading}
                            showCards={showCards}
                            styles={styles}
                        />

                        <UserLoansSection
                            loans={loans}
                            loansLoading={loansLoading}
                            showLoans={showLoans}
                            styles={styles}
                        />

                        <UserFixedDepositsSection
                            fixedDeposits={fixedDeposits}
                            fixedDepositsLoading={fixedDepositsLoading}
                            showFixedDeposits={showFixedDeposits}
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