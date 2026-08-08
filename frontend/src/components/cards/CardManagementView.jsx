import React, { useEffect, useState } from 'react';
import { fetchAllCards, blockCard, unblockCard } from '../../api/bankService';
import { formatDate, formatCurrency } from '../../utils/formatUtils';
import modalStyles from "../../styles/modalStyles";
import toast from "react-hot-toast";
import PageCard from '../PageCard';

const CardManagementView = () => {
    const [cards, setCards] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [search, setSearch] = useState("");
    const [cardStatusFilter, setCardStatusFilter] = useState("ALL");
    const [expandedCardId, setExpandedCardId] = useState(null);
    const [showBlockModal, setShowBlockModal] = useState(false);
    const [selectedCard, setSelectedCard] = useState(null);

    const handleToggleStatus = async () => {

        try {

            const updatedCard =
                selectedCard.cardStatus === "BLOCKED"
                    ? await unblockCard(selectedCard.id)
                    : await blockCard(selectedCard.id);

            setCards(prev =>
                prev.map(card =>
                    card.id === updatedCard.id
                        ? {
                            ...card,
                            ...updatedCard,
                        }
                        : card
                )
            );
            setSelectedCard(updatedCard);
            setShowBlockModal(false);
            toast.success(
                selectedCard.cardStatus === "BLOCKED"
                    ? "Card unblocked successfully."
                    : "Card blocked successfully."
            );

        } catch (err) {
            console.error(err);
            toast.error("Failed to update card status.");
        }

    };
    const handleStatusClick = (card) => {
        setSelectedCard(card);
        setShowBlockModal(true);
    };

    useEffect(() => {
        const loadCards = async () => {
            try {
                const data = await fetchAllCards();
                setCards(data);
            } catch (err) {
                console.error(err);
                setError("Unable to load cards.");
            } finally {
                setLoading(false);
            }
        };
        loadCards();
    }, []);

    if (loading) {
        return <p>Loading cards...</p>;
    }

    if (error) {
        return <p>{error}</p>;
    }

    const activeCount = cards.filter(
        card => card.cardStatus === "ACTIVE"
    ).length;

    const blockedCount = cards.filter(
        card => card.cardStatus === "BLOCKED"
    ).length;

    const frozenCount = cards.filter(
        card => card.cardStatus === "FROZEN"
    ).length;

    const filteredCards = cards.filter((card) => {
        const matchesSearch =
            (card.customerName ?? "").toLowerCase().includes(search.toLowerCase()) ||
            (card.maskedCardNumber ?? "").toLowerCase().includes(search.toLowerCase()) ||
            (card.accountNumber ?? "").toLowerCase().includes(search.toLowerCase());

        const matchesStatus =
            cardStatusFilter === "ALL" ||
            card.cardStatus === cardStatusFilter;

        return matchesSearch && matchesStatus;
    });

    const getCardStatusStyle = (status) => {

        switch (status) {

            case "ACTIVE":
                return {
                    background: "#dcfce7",
                    color: "#15803d",
                };

            case "FROZEN":
                return {
                    background: "#fef3c7",
                    color: "#92400e",
                };

            case "BLOCKED":
                return {
                    background: "#fee2e2",
                    color: "#b91c1c",
                };

            default:
                return {
                    background: "#f3f4f6",
                    color: "#6b7280",
                };
        }
    };

    const cardSummaryCards = [
        {
            title: "Active Cards",
            icon: "🟢",
            count: activeCount,
            style: {
                background: "#ECFDF5",
                border: "1px solid #A7F3D0",
            },
        },
        {
            title: "Blocked Cards",
            icon: "🔒",
            count: blockedCount,
            style: {
                background: "#FEF2F2",
                border: "1px solid #FECACA",
            },
        },
        {
            title: "Frozen Cards",
            icon: "❄️",
            count: frozenCount,
            style: {
                background: "#EFF6FF",
                border: "1px solid #BFDBFE",
            },
        }
    ];

    return (
        <PageCard title="🏦 Card Management">

            <div style={styles.pageHeader}>

                <div>
                    <p style={styles.subtitle}>
                        View debit and credit cards and block or unblock them when required.
                    </p>
                </div>

                <div style={styles.pendingBadge}>
                    {cards.length} Cards
                </div>

            </div>

            <div style={styles.summaryGrid}>
                {cardSummaryCards.map((card) => (
                    <div
                        key={card.title}
                        style={{
                            ...styles.summaryCard,
                            ...card.style,
                        }}
                    >
                        <div style={styles.summaryTitle}>
                            <span>{card.icon}</span>
                            <span>{card.title}</span>
                        </div>
                        <div style={styles.summaryValue}>
                            {card.count}
                        </div>
                    </div>
                ))}
            </div>

            {cards.length === 0 ? (
                <p>No cards.</p>
            ) : (
                <>
                    <div style={styles.toolbar}>

                        <input
                            placeholder="Search customer, card or account..."
                            value={search}
                            onChange={(e) => setSearch(e.target.value)}
                            style={styles.searchInput}
                        />

                        <select
                            value={cardStatusFilter}
                            onChange={(e) => setCardStatusFilter(e.target.value)}
                            style={styles.filterSelect}
                        >
                            <option value="ALL">All Status</option>
                            <option value="ACTIVE">Active</option>
                            <option value="FROZEN">Frozen</option>
                            <option value="BLOCKED">Blocked</option>
                        </select>

                    </div>
                    <hr
                        style={{
                            border: "none",
                            borderTop: "1px solid #e5e7eb",
                            margin: "28px 0",
                        }}
                    />
                    <div
                        style={{
                            overflowX: "auto",
                            border: "1px solid #e5e7eb",
                            borderRadius: "12px",
                        }}
                    >
                        <table style={{
                            width: '100%', borderCollapse: 'collapse'
                        }}>
                            <thead>
                                < tr >
                                    <th style={styles.header}>Customer</th>
                                    <th style={styles.header}>Card Number</th>
                                    <th style={styles.header}>Type</th>
                                    <th style={styles.header}>Daily Limit</th>
                                    <th style={styles.header}>Status</th>
                                </tr>
                            </thead>

                            <tbody>
                                {filteredCards.map((card, index) => (
                                    <>
                                        <tr
                                            key={card.id}
                                            onClick={() =>
                                                setExpandedCardId(
                                                    expandedCardId === card.id ? null : card.id
                                                )
                                            }
                                            style={{
                                                cursor: "pointer",
                                                transition: ".15s",
                                                background:
                                                    expandedCardId === card.id
                                                        ? "#eff6ff"
                                                        : index % 2 === 0
                                                            ? "#ffffff"
                                                            : "#fafafa",

                                            }}
                                            onMouseEnter={(e) => {
                                                if (expandedCardId !== card.id) {
                                                    e.currentTarget.style.background = "#eff6ff";
                                                }
                                            }}
                                            onMouseLeave={(e) => {
                                                if (expandedCardId !== card.id) {
                                                    e.currentTarget.style.background =
                                                        index % 2 === 0 ? "#ffffff" : "#fafafa";
                                                }
                                            }}
                                        >
                                            <td style={styles.cell}>{card.customerName}</td>
                                            <td style={styles.cell}>
                                                <span
                                                    style={{
                                                        fontFamily: "monospace",
                                                        fontWeight: 600,
                                                        color: "#15803d",
                                                    }}
                                                >
                                                    {card.maskedCardNumber}
                                                </span>
                                            </td>
                                            <td style={styles.cell}>{card.cardType}</td>
                                            <td style={styles.cell}>{formatCurrency(card.dailyLimit)}</td>
                                            <td style={styles.cell}>
                                                <span
                                                    style={{
                                                        padding: "5px 10px",
                                                        borderRadius: "999px",
                                                        background: "#fef3c7",
                                                        color: "#92400e",
                                                        fontWeight: 600,
                                                        fontSize: "12px",
                                                        ...getCardStatusStyle(card.cardStatus),
                                                    }}
                                                >
                                                    {card.cardStatus}
                                                </span>
                                            </td>
                                        </tr>
                                        {expandedCardId === card.id && (
                                            <tr>
                                                <td
                                                    colSpan={5}
                                                    style={{
                                                        padding: "18px",
                                                        background: "#fff",
                                                        borderBottom: "1px solid #e5e7eb",
                                                    }}
                                                >
                                                    <div style={styles.cardDetailsContainer}>
                                                        <div style={styles.detailsHeader}>
                                                            <div>
                                                                <h3 style={styles.customerName}>
                                                                    👤 {card.customerName}
                                                                </h3>

                                                                <p style={styles.detailsSubtitle}>
                                                                    Manage card status and review card information.
                                                                </p>
                                                            </div>
                                                        </div>
                                                        <div style={styles.cardDetailsGrid}>
                                                            <div
                                                                style={{
                                                                    ...styles.detailCard,
                                                                    background: "#eff6ff",
                                                                    border: "1px solid #bfdbfe",
                                                                }}
                                                            >
                                                                <div style={styles.detailLabel}>
                                                                    💳 Daily Limit
                                                                </div>
                                                                <div
                                                                    style={{
                                                                        ...styles.detailValue,
                                                                        fontSize: "22px",
                                                                        fontWeight: 700,
                                                                        color: "#1d4ed8",
                                                                    }}
                                                                >
                                                                    {formatCurrency(card.dailyLimit)}
                                                                </div>
                                                            </div>
                                                            <div
                                                                style={{
                                                                    ...styles.detailCard,
                                                                    background: "#ecfdf5",
                                                                    border: "1px solid #a7f3d0",
                                                                }}
                                                            >
                                                                <div style={styles.detailLabel}>
                                                                    🟢 Status
                                                                </div>

                                                                <div style={styles.detailValue}>
                                                                    <span
                                                                        style={{
                                                                            padding: "4px 10px",
                                                                            borderRadius: "999px",
                                                                            fontWeight: 600,
                                                                            fontSize: "12px",
                                                                            ...getCardStatusStyle(card.cardStatus),
                                                                        }}
                                                                    >
                                                                        {card.cardStatus}
                                                                    </span>
                                                                </div>
                                                            </div>
                                                            <div style={styles.detailCard}>
                                                                <div style={styles.detailLabel}>
                                                                    🔢 Account Number
                                                                </div>

                                                                <div
                                                                    style={{
                                                                        ...styles.detailValue,
                                                                        fontFamily: "monospace",
                                                                    }}
                                                                >
                                                                    {card.accountNumber}
                                                                </div>
                                                            </div>
                                                            <div style={styles.detailCard}>
                                                                <div style={styles.detailLabel}>
                                                                    💳 Card Number
                                                                </div>

                                                                <div style={styles.detailValue}>
                                                                    {card.maskedCardNumber}
                                                                </div>
                                                            </div>
                                                            <div style={styles.detailCard}>
                                                                <div style={styles.detailLabel}>
                                                                    📄 Card Type
                                                                </div>

                                                                <div style={styles.detailValue}>
                                                                    {card.cardType}
                                                                </div>
                                                            </div>
                                                            <div style={styles.detailCard}>
                                                                <div style={styles.detailLabel}>
                                                                    📅 Expiry Date
                                                                </div>

                                                                <div style={styles.detailValue}>
                                                                    {formatDate(card.expiryDate)}
                                                                </div>
                                                            </div>
                                                        </div>
                                                        <hr
                                                            style={{
                                                                border: "none",
                                                                borderTop: "1px solid #e5e7eb",
                                                                margin: "28px 0",
                                                            }}
                                                        />
                                                        <div style={styles.actionSection}>
                                                            <div style={styles.actionInfo}>
                                                                <h4 style={styles.actionTitle}>
                                                                    Card Actions
                                                                </h4>

                                                                <p style={styles.actionSubtitle}>
                                                                    Block or unblock this card when required.
                                                                </p>
                                                            </div>
                                                        </div>
                                                        <div style={styles.detailsActions}>
                                                            <button
                                                                style={
                                                                    card.cardStatus === "BLOCKED"
                                                                        ? styles.unblockButton
                                                                        : styles.blockButton
                                                                }
                                                                onClick={() => handleStatusClick(card)}
                                                            >
                                                                {card.cardStatus === "BLOCKED"
                                                                    ? "Unblock Card"
                                                                    : "Block Card"}
                                                            </button>
                                                        </div>
                                                    </div>
                                                </td>
                                            </tr>
                                        )}

                                        {showBlockModal && (
                                            <div style={modalStyles.overlay}>
                                                <div style={modalStyles.modal}>

                                                    <h3>
                                                        {selectedCard.cardStatus === "BLOCKED"
                                                            ? "Unblock Card"
                                                            : "Block Card"}
                                                    </h3>

                                                    <p style={{ color: "#64748b", marginBottom: "20px" }}>
                                                        Please review the card details before continuing.
                                                    </p>

                                                    <div style={styles.confirmationCard}>

                                                        <div style={styles.confirmationRow}>
                                                            <strong>Customer</strong>
                                                            <span>{selectedCard.customerName}</span>
                                                        </div>

                                                        <div style={styles.confirmationRow}>
                                                            <strong>Account Number</strong>
                                                            <span
                                                                style={{
                                                                    fontFamily: "monospace",
                                                                }}
                                                            >
                                                                {selectedCard.accountNumber}
                                                            </span>
                                                        </div>
                                                        <div style={styles.confirmationRow}>
                                                            <strong>Card Number</strong>
                                                            <span
                                                                style={{
                                                                    fontFamily: "monospace",
                                                                }}
                                                            >
                                                                {selectedCard.maskedCardNumber}
                                                            </span>
                                                        </div>

                                                        <div style={styles.confirmationRow}>
                                                            <strong>Card Type</strong>
                                                            <span>{selectedCard.cardType}</span>
                                                        </div>

                                                        <div style={styles.confirmationRow}>
                                                            <strong>Daily Limit</strong>
                                                            <span>
                                                                {formatCurrency(selectedCard.dailyLimit)}
                                                            </span>
                                                        </div>

                                                    </div>

                                                    <div
                                                        style={{
                                                            ...styles.alertCard,
                                                            ...(selectedCard.cardStatus === "BLOCKED"
                                                                ? styles.successAlert
                                                                : styles.warningAlert),
                                                        }}
                                                    >
                                                        {selectedCard.cardStatus === "BLOCKED" ? (
                                                            <>
                                                                <strong>ℹ️ Information</strong>
                                                                <br />
                                                                Unblocking this card will immediately allow the customer to use the card again for purchases, ATM withdrawals and online transactions.
                                                            </>
                                                        ) : (
                                                            <>
                                                                <strong>⚠️ Important</strong>
                                                                <br />
                                                                You are about to change the status of this card.
                                                                Blocking immediately prevents the card from being used for purchases, ATM withdrawals and online transactions.
                                                                Only unblock the card after confirming it is safe to reactivate.
                                                            </>
                                                        )}
                                                    </div>

                                                    <div
                                                        style={{
                                                            display: "flex",
                                                            justifyContent: "flex-end",
                                                            gap: "12px",
                                                            marginTop: "24px",
                                                        }}
                                                    >
                                                        <button
                                                            style={styles.cancelButton}
                                                            onClick={() => setShowBlockModal(false)}
                                                            onMouseEnter={(e) => {
                                                                e.currentTarget.style.background = "#f8fafc";
                                                            }}
                                                            onMouseLeave={(e) => {
                                                                e.currentTarget.style.background = "#ffffff";
                                                            }}
                                                        >
                                                            Cancel
                                                        </button>

                                                        <button
                                                            onClick={handleToggleStatus}
                                                            style={
                                                                selectedCard.cardStatus === "BLOCKED"
                                                                    ? styles.unblockButton
                                                                    : styles.blockButton
                                                            }
                                                        >
                                                            {selectedCard.cardStatus === "BLOCKED"
                                                                ? "Unblock Card"
                                                                : "Block Card"}
                                                        </button>

                                                    </div>

                                                </div>
                                            </div>
                                        )}
                                    </>
                                ))}
                            </tbody>
                        </table>
                    </div >
                </>
            )}
        </PageCard >
    );
};

const styles = {

    pageHeader: {
        display: "flex",
        justifyContent: "space-between",
        alignItems: "flex-start",
        marginBottom: "28px",
    },

    header: {
        background: "#f8fafc",
        color: "#334155",
        fontWeight: 700,
        fontSize: "14px",
        padding: "14px 16px",
        textAlign: "left",
        borderBottom: "1px solid #e5e7eb",
        whiteSpace: "nowrap",
    },

    title: {
        margin: 0,
        fontSize: "30px",
        fontWeight: 700,
    },

    subtitle: {
        marginTop: "6px",
        color: "#64748b",
        fontSize: "15px",
    },

    pendingBadge: {
        background: "#FEF3C7",
        color: "#92400E",
        padding: "8px 14px",
        borderRadius: "999px",
        fontWeight: 700,
        fontSize: "14px",
        alignSelf: "flex-start",
    },

    cell: {
        padding: "16px",
        borderBottom: "1px solid #f1f5f9",
        fontSize: "14px",
        color: "#334155",
    },

    summaryGrid: {
        display: "grid",
        gridTemplateColumns: "repeat(3, 1fr)",
        gap: "18px",
        marginBottom: "26px",
    },

    summaryCard: {
        border: "1px solid #e5e7eb",
        borderRadius: "12px",
        padding: "18px",
        background: "#fff",
        display: "flex",
        flexDirection: "column",
        alignItems: "flex-start",
    },

    summaryValue: {
        fontSize: "30px",
        fontWeight: 700,
        color: "#0f172a",
    },

    summaryLabel: {
        marginTop: "8px",
        color: "#64748b",
        fontSize: "14px",
    },

    toolbar: {
        display: "flex",
        justifyContent: "space-between",
        gap: "16px",
        marginBottom: "24px",
    },

    searchInput: {
        flex: 1,
        padding: "12px 14px",
        borderRadius: "10px",
        border: "1px solid #d1d5db",
        fontSize: "14px",
    },

    filterSelect: {
        width: "180px",
        padding: "12px",
        borderRadius: "10px",
        border: "1px solid #d1d5db",
        fontSize: "14px",
    },

    cardDetailsContainer: {
        background: "#ffffff",
        border: "1px solid #e5e7eb",
        borderRadius: "14px",
        padding: "14px",
        boxShadow: "0 2px 8px rgba(15, 23, 42, 0.05)",
    },

    cardDetailsGrid: {
        display: "grid",
        gridTemplateColumns: "repeat(2, 1fr)",
        gap: "20px 32px",
    },

    detailItem: {
        display: "flex",
        flexDirection: "column",
    },

    detailLabel: {
        fontSize: "13px",
        color: "#64748b",
        marginBottom: "6px",
    },

    detailValue: {
        fontWeight: 600,
        color: "#0f172a",
        fontSize: "15px",
    },

    detailsActions: {
        width: "100%",
        display: "flex",
        justifyContent: "flex-end",
        gap: "12px",
    },

    detailCard: {
        background: "#ffffff",
        border: "1px solid #e5e7eb",
        borderRadius: "10px",
        padding: "16px",
    },

    approveButton: {
        background: "#16a34a",
        color: "#fff",
        border: "none",
        padding: "12px 22px",
        borderRadius: "8px",
        cursor: "pointer",
        fontWeight: 600,
        minWidth: "170px",
    },

    rejectButton: {
        background: "#fff",
        color: "#dc2626",
        border: "1px solid #dc2626",
        padding: "12px 22px",
        borderRadius: "8px",
        cursor: "pointer",
        fontWeight: 600,
        minWidth: "170px",
    },

    detailsHeader: {
        marginBottom: "24px",
    },

    detailsTitle: {
        margin: 0,
        fontSize: "18px",
        fontWeight: 700,
        color: "#0f172a",
    },

    detailsSubtitle: {
        marginTop: "4px",
        fontSize: "14px",
        color: "#64748b",
    },

    summaryTitle: {
        display: "flex",
        alignItems: "center",
        gap: "8px",
        fontWeight: 600,
        color: "#334155",
        marginBottom: "18px",
    },

    summarySubtext: {
        marginTop: "6px",
        color: "#64748b",
        fontSize: "13px",
    },

    customerName: {
        margin: 0,
        fontSize: "22px",
        fontWeight: 700,
        color: "#0f172a",
    },

    actionSection: {
        display: "flex",
        flexDirection: "column",
        gap: "12px",
        marginTop: "20px",
    },

    actionInfo: {
        display: "flex",
        flexDirection: "column",
    },

    actionTitle: {
        margin: 0,
        fontSize: "17px",
        fontWeight: 700,
        color: "#0f172a",
    },

    actionSubtitle: {
        marginTop: "4px",
        fontSize: "14px",
        color: "#64748b",
    },

    rejectModalTitle: {
        margin: 0,
        fontSize: "22px",
        fontWeight: 700,
        color: "#991b1b",
    },

    rejectModalSubtitle: {
        marginTop: "10px",
        color: "#64748b",
        lineHeight: 1.5,
        marginBottom: "24px",
    },

    loanSummaryCard: {
        background: "#f8fafc",
        border: "1px solid #e2e8f0",
        borderRadius: "10px",
        padding: "16px",
        marginBottom: "20px",
    },

    loanSummaryName: {
        fontWeight: 700,
        fontSize: "17px",
        color: "#0f172a",
    },

    loanSummaryMeta: {
        marginTop: "6px",
        color: "#64748b",
        fontSize: "14px",
    },

    loanSummaryAmount: {
        marginTop: "16px",
        paddingTop: "16px",
        borderTop: "1px solid #e5e7eb",
    },

    loanSummaryAmountLabel: {
        fontSize: "13px",
        color: "#64748b",
    },

    loanSummaryAmountValue: {
        marginTop: "4px",
        fontSize: "24px",
        fontWeight: 700,
        color: "#1d4ed8",
    },

    rejectReasonSection: {
        marginTop: "24px",
    },

    rejectReasonLabel: {
        display: "block",
        marginBottom: "8px",
        fontWeight: 600,
        color: "#334155",
        fontSize: "14px",
    },

    rejectTextarea: {
        width: "100%",
        minHeight: "120px",
        resize: "vertical",
        padding: "12px",
        borderRadius: "10px",
        border: "1px solid #cbd5e1",
        fontSize: "14px",
        lineHeight: 1.5,
        outline: "none",
        boxSizing: "border-box",
    },

    characterCount: {
        marginTop: "8px",
        textAlign: "right",
        fontSize: "12px",
        color: "#64748b",
    },

    cancelButton: {
        background: "#ffffff",
        color: "#475569",
        border: "1px solid #cbd5e1",
        padding: "10px 18px",
        borderRadius: "8px",
        cursor: "pointer",
        fontWeight: 600,
        minWidth: "120px",
    },

    confirmRejectButton: {
        background: "#dc2626",
        color: "#ffffff",
        border: "none",
        borderRadius: "8px",
        padding: "10px 18px",
        fontWeight: 600,
        cursor: "pointer",
    },

    blockButton: {
        background: "#dc2626",
        color: "#fff",
        border: "none",
        padding: "10px 18px",
        borderRadius: "8px",
        cursor: "pointer",
        fontWeight: 600,
        minWidth: "180px",
    },

    unblockButton: {
        background: "#16a34a",
        color: "#fff",
        border: "none",
        padding: "10px 18px",
        borderRadius: "8px",
        cursor: "pointer",
        fontWeight: 600,
        minWidth: "180px",
    },

    confirmationCard: {
        border: "1px solid #e5e7eb",
        borderRadius: "10px",
        padding: "16px",
        display: "flex",
        flexDirection: "column",
        gap: "14px",
        background: "#fafafa",
    },

    confirmationRow: {
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
    },

    alertCard: {
        marginTop: "20px",
        padding: "14px 16px",
        borderRadius: "10px",
        fontSize: "14px",
        lineHeight: "1.6",
    },

    warningAlert: {
        background: "#FEFCE8",
        border: "1px solid #FDE68A",
        color: "#92400e",
    },

    successAlert: {
        background: "#ECFDF5",
        border: "1px solid #A7F3D0",
        color: "#15803d",
    },
};

export default CardManagementView;