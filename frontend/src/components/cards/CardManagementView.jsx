import React, { useEffect, useState } from 'react';
import { fetchAllCards, blockCard, unblockCard, fetchCardSummary } from '../../api/bankService';
import { formatDate, formatCurrency } from '../../utils/formatUtils';
import { getCardStatusStyle } from '../../utils/cardStatusUtils';
import modalStyles from "../../styles/modalStyles";
import toast from "react-hot-toast";
import PageCard from '../PageCard';

const CardManagementView = ({
    refreshDashboard,
}) => {
    const [cards, setCards] = useState([]);
    const [pageData, setPageData] = useState(null);
    const [cardSummary, setCardSummary] = useState(null);
    const [currentPage, setCurrentPage] = useState(0);
    const [search, setSearch] = useState("");
    const [searchInput, setSearchInput] = useState("");
    const [statusFilter, setStatusFilter] = useState("ALL");
    const [loading, setLoading] = useState(true);
    const [searchLoading, setSearchLoading] = useState(false);
    const [error, setError] = useState('');
    const [expandedCardId, setExpandedCardId] = useState(null);
    const [showBlockModal, setShowBlockModal] = useState(false);
    const [selectedCard, setSelectedCard] = useState(null);
    const [actionLoading, setActionLoading] = useState(false);

    const handleToggleStatus = async () => {

        try {

            setActionLoading(true);

            if (selectedCard.cardStatus === "BLOCKED") {
                await unblockCard(selectedCard.id);
            } else {
                await blockCard(selectedCard.id);
            }

            await loadCards();
            await loadCardSummary();
            await refreshDashboard?.();

            const wasBlocked = selectedCard.cardStatus === "BLOCKED";

            setSelectedCard(null);
            setShowBlockModal(false);

            toast.success(
                wasBlocked
                    ? "Card unblocked successfully."
                    : "Card blocked successfully."
            );

        } catch (err) {

            console.error(err);
            toast.error("Failed to update card status.");

        } finally {
            setActionLoading(false);
        }
    };

    const handleStatusClick = (card) => {
        setSelectedCard(card);
        setShowBlockModal(true);
    };

    const loadCards = async () => {

        try {

            setLoading(true);
            setError("");

            const response = await fetchAllCards({
                page: currentPage,
                size: 10,
                search,
                status: statusFilter
            });

            setCards(response.content);
            setPageData(response);

        } catch (err) {

            console.error(err);
            setError("Unable to load cards.");

        } finally {

            setLoading(false);
            setSearchLoading(false);
        }
    };

    const loadCardSummary = async () => {

        try {

            const response = await fetchCardSummary();

            setCardSummary(response);

        } catch (err) {

            console.error(
                "Failed to load card summary",
                err
            );

        }
    };


    useEffect(() => {
        loadCards();
    }, [
        currentPage,
        search,
        statusFilter
    ]);

    useEffect(() => {

        loadCardSummary();

    }, []);

    useEffect(() => {
        setExpandedCardId(null);
    }, [
        currentPage,
        search,
        statusFilter
    ]);

    useEffect(() => {

        const value = searchInput.trim();

        if (value === search) {
            return;
        }

        setSearchLoading(true);

        const timer = setTimeout(() => {
            setCurrentPage(0);
            setSearch(value);
        }, 500);


        return () => clearTimeout(timer);

    }, [searchInput]);

    if (loading && !pageData) {
        return <p>Loading cards...</p>;
    }

    if (error) {
        return <p>{error}</p>;
    }

    const totalCards = cardSummary?.totalCards ?? 0;

    const activeCount = cardSummary?.activeCards ?? 0;

    const blockedCount = cardSummary?.blockedCards ?? 0;

    const frozenCount = cardSummary?.frozenCards ?? 0;

    const filteredCount = pageData?.totalElements ?? 0;

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
        <>
            <PageCard title="🏦 Card Management">

                <div style={styles.pageHeader}>

                    <div>
                        <p style={styles.subtitle}>
                            View debit and credit cards and block or unblock them when required.
                        </p>
                    </div>

                    <div style={styles.pendingBadge}>
                        {totalCards} Cards
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

                <div style={styles.toolbar}>

                    <div style={{
                        display: "flex",
                        flex: 1,
                        gap: "8px",
                        position: "relative"
                    }}>

                        <input
                            placeholder="Search customer, card or account..."
                            value={searchInput}
                            onChange={(e) => {
                                setSearchInput(e.target.value);
                            }}
                            style={styles.searchInput}
                        />

                        {searchLoading && (
                            <span
                                style={{
                                    position: "absolute",
                                    right: "15px",
                                    top: "12px",
                                    color: "#64748b",
                                    fontSize: "13px"
                                }}
                            >
                                Searching...
                            </span>
                        )}

                        {searchInput && (
                            <button
                                onClick={() => {
                                    setSearchInput("");
                                }}
                                style={styles.clearButton}
                            >
                                ✕
                            </button>
                        )}

                    </div>

                    <select
                        value={statusFilter}
                        onChange={(e) => {
                            setCurrentPage(0);
                            setStatusFilter(e.target.value);
                        }}
                        style={styles.filterSelect}
                    >
                        <option value="ALL">All Status</option>
                        <option value="ACTIVE">Active</option>
                        <option value="FROZEN">Frozen</option>
                        <option value="BLOCKED">Blocked</option>
                    </select>

                </div>

                <div style={styles.resultInfo}>
                    Showing {filteredCount} cards
                    {search && ` matching "${search}"`}
                </div>

                {cards.length === 0 ? (
                    <div
                        style={{
                            padding: "40px",
                            textAlign: "center",
                        }}
                    >
                        <h3 style={{ color: "#334155" }}>
                            No cards found
                        </h3>

                        <p style={{ color: "#64748b" }}>
                            Try changing your search or filter criteria.
                        </p>
                    </div>
                ) : (
                    <>
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
                                    {cards.map((card, index) => (
                                        <React.Fragment key={card.id}>
                                            <tr
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
                                                                        ...getCardStatusStyle(card.cardStatus),
                                                                    }}
                                                                >
                                                                    <div style={styles.detailLabel}>
                                                                        {getCardStatusStyle(card.cardStatus).icon} Status
                                                                    </div>

                                                                    <div
                                                                        style={{
                                                                            ...styles.detailValue,
                                                                            color: getCardStatusStyle(card.cardStatus).color,
                                                                            fontWeight: 700,
                                                                        }}
                                                                    >
                                                                        {card.cardStatus}
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
                                        </React.Fragment>
                                    ))}
                                </tbody>
                            </table>
                        </div >
                        {
                            pageData && pageData.totalPages > 1 && (

                                <div style={styles.pagination}>

                                    <button
                                        disabled={pageData.first}
                                        onClick={() =>
                                            setCurrentPage(prev => prev - 1)
                                        }
                                        style={styles.pageButton}
                                    >
                                        ← Previous
                                    </button>


                                    <span>
                                        Page {pageData.number + 1}
                                        {" "}of{" "}
                                        {pageData.totalPages}
                                    </span>


                                    <button
                                        disabled={pageData.last}
                                        onClick={() =>
                                            setCurrentPage(prev => prev + 1)
                                        }
                                        style={styles.pageButton}
                                    >
                                        Next →
                                    </button>

                                </div>

                            )
                        }
                    </>
                )}
            </PageCard>
            {
                showBlockModal && (
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
                                    disabled={actionLoading}
                                    onClick={handleToggleStatus}
                                    style={
                                        selectedCard.cardStatus === "BLOCKED"
                                            ? styles.unblockButton
                                            : styles.blockButton
                                    }
                                >
                                    {actionLoading ? "Updating..." :
                                        selectedCard.cardStatus === "BLOCKED"
                                            ? "Unblock Card"
                                            : "Block Card"
                                    }
                                </button>

                            </div>

                        </div>
                    </div>
                )
            }
        </>
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

    pagination: {
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        gap: "16px",
        marginTop: "24px",
    },

    pageButton: {
        padding: "8px 16px",
        borderRadius: "8px",
        border: "1px solid #d1d5db",
        background: "#fff",
        cursor: "pointer",
        fontWeight: 600,
    },

    clearButton: {
        width: "45px",
        borderRadius: "10px",
        border: "1px solid #d1d5db",
        background: "#fff",
        cursor: "pointer",
        fontSize: "18px"
    }
};

export default CardManagementView;