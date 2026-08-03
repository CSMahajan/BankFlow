import { useEffect, useState } from "react";
import { fetchMyAccounts, issueCard } from "../api/bankService";
import toast from "react-hot-toast";

const IssueCardModal = ({
    isOpen,
    onClose,
    onCardIssued,
}) => {

    const [accounts, setAccounts] = useState([]);
    const [loading, setLoading] = useState(false);

    const [accountNumber, setAccountNumber] = useState("");
    const [cardType, setCardType] = useState("DEBIT");
    const [dailyLimit, setDailyLimit] = useState(50000);
    const selectedAccount = accounts.find(
        account => account.accountNumber === accountNumber
    );

    useEffect(() => {

        if (!isOpen) return;

        loadAccounts();

    }, [isOpen]);

    const loadAccounts = async () => {

        try {

            setLoading(true);

            const response = await fetchMyAccounts();

            const activeAccounts = response.filter(
                account => account.accountStatus === "ACTIVE"
            );

            setAccounts(activeAccounts);

            if (activeAccounts.length > 0) {
                setAccountNumber(activeAccounts[0].accountNumber);
            }

        } catch (err) {

            console.error(err);

        } finally {

            setLoading(false);

        }

    };

    const handleIssueCard = async () => {

        try {
            await issueCard({
                accountNumber,
                cardType,
                dailyLimit: Number(dailyLimit),
            });

            toast.success("Card issued successfully.");
            setCardType("DEBIT");
            setDailyLimit(50000);
            onClose();
            onCardIssued();

        } catch (err) {

            console.error(err);

            toast.error(
                err.response?.data?.message ??
                "Failed to issue card."
            );

        }

    };

    if (!isOpen) return null;

    return (

        <div style={styles.overlay}>

            <div style={styles.modal}>

                <h2>💳 Apply for New Card</h2>

                {loading ? (

                    <p>Loading accounts...</p>

                ) : (

                    <>
                        <div style={styles.field}>

                            <label>Linked Account</label>

                            {accounts.length === 0 ? (

                                <div style={styles.errorBox}>
                                    No active accounts available to issue a card.
                                </div>

                            ) : (

                                <>
                                    <select
                                        value={accountNumber}
                                        onChange={(e) => setAccountNumber(e.target.value)}
                                        style={styles.input}
                                    >
                                        {accounts.map((account) => (
                                            <option
                                                key={account.accountNumber}
                                                value={account.accountNumber}
                                            >
                                                {account.accountType} • {account.accountNumber}
                                            </option>
                                        ))}
                                    </select>

                                    {selectedAccount && (
                                        <div style={styles.balanceInfo}>
                                            Available Balance:&nbsp;
                                            <strong>
                                                {new Intl.NumberFormat("en-IN", {
                                                    style: "currency",
                                                    currency: "INR",
                                                }).format(selectedAccount.currentBalance)}
                                            </strong>
                                        </div>
                                    )}
                                </>

                            )}

                        </div>

                        <div style={styles.field}>

                            <label>Card Type</label>

                            <select
                                value={cardType}
                                onChange={(e) => setCardType(e.target.value)}
                                style={styles.input}
                            >
                                <option value="DEBIT">Debit Card</option>
                                <option value="CREDIT">Credit Card</option>
                            </select>

                        </div>

                        <div style={styles.field}>

                            <label>Daily Limit</label>

                            <input
                                type="number"
                                value={dailyLimit}
                                onChange={(e) => setDailyLimit(e.target.value)}
                                style={styles.input}
                            />

                        </div>

                        <div style={styles.actions}>

                            <button
                                style={styles.cancel}
                                onClick={() => {
                                    setCardType("DEBIT");
                                    setDailyLimit(50000);
                                    onClose();
                                }}
                            >
                                Cancel
                            </button>

                            <button
                                style={styles.save}
                                onClick={handleIssueCard}
                                disabled={accounts.length === 0}
                            >
                                Apply for Card
                            </button>

                        </div>

                    </>

                )}

            </div>

        </div>

    );

};

const styles = {

    overlay: {
        position: "fixed",
        inset: 0,
        background: "rgba(0,0,0,.45)",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        zIndex: 1000,
    },

    modal: {
        width: "430px",
        background: "#fff",
        borderRadius: "16px",
        padding: "28px",
        boxShadow: "0 20px 60px rgba(0,0,0,.18)",
    },

    field: {
        marginBottom: "18px",
    },

    input: {
        width: "100%",
        padding: "12px",
        borderRadius: "10px",
        border: "1px solid #d1d5db",
        marginTop: "8px",
        boxSizing: "border-box",
    },

    actions: {
        display: "flex",
        justifyContent: "flex-end",
        gap: "12px",
        marginTop: "24px",
    },

    cancel: {
        padding: "10px 18px",
        borderRadius: "10px",
        border: "1px solid #d1d5db",
        background: "#fff",
        cursor: "pointer",
    },

    save: {
        padding: "10px 20px",
        borderRadius: "10px",
        border: "none",
        background: "#0d6360",
        color: "#fff",
        cursor: "pointer",
        fontWeight: "700",
    },

    balanceInfo: {
        marginTop: "6px",
        fontSize: "13px",
        color: "#64748b",
    },

    errorBox: {
        background: "#fee2e2",
        color: "#991b1b",
        padding: "10px",
        borderRadius: "8px",
        fontSize: "13px",
    },

};

export default IssueCardModal;