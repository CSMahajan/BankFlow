import { useState } from "react";
import UpdateCardLimitModal from "./UpdateCardLimitModal";
import { formatCurrency } from "../../utils/formatUtils";
import { getCardStatusStyle } from "../../utils/cardStatusUtils";

const CardManagementPanel = ({
    card,
    onToggleStatus,
    onUpdateLimit,
    updating,
}) => {
    const [isLimitModalOpen, setIsLimitModalOpen] = useState(false);
    const isBlocked = card.cardStatus === "BLOCKED";
    const accountInactive = card.accountStatus !== "ACTIVE";
    const statusStyle = getCardStatusStyle(card.cardStatus);
    const canUpdateLimit =
        card.cardStatus === "ACTIVE" &&
        card.accountStatus === "ACTIVE";
    const updateLimitDisabledReason = card.cardStatus === "BLOCKED" ? "Card is blocked by the bank."
        : card.cardStatus === "FROZEN" ? "Card must be active to update the daily limit."
            : card.accountStatus !== "ACTIVE" ? "Linked account must be active." : "";
    return (
        <>
            <div
                style={{
                    backgroundColor: "#ffffff",
                    border: "1px solid #e5e7eb",
                    borderRadius: "18px",
                    padding: "22px",
                    boxShadow: "0 10px 24px rgba(0,0,0,.06)",
                }}
            >
                <div
                    style={{
                        display: "flex",
                        alignItems: "center",
                        gap: "10px",
                        marginBottom: "20px",
                    }}
                >
                    <span style={{ fontSize: "18px" }}>⚙️</span>

                    <span
                        style={{
                            fontSize: "18px",
                            fontWeight: "700",
                            color: "#1f2937",
                        }}
                    >
                        Card Controls
                    </span>
                </div>

                <div
                    style={{
                        display: "flex",
                        justifyContent: "space-between",
                        alignItems: "center",
                        marginBottom: "20px",
                    }}
                >

                    <div>
                        <div
                            style={{
                                color: "#64748b",
                                fontSize: "12px",
                                marginBottom: "4px",
                            }}
                        >
                            Account Number
                        </div>

                        <strong>{card.accountNumber}</strong>
                    </div>

                    <div style={{ textAlign: "right" }}>
                        <div
                            style={{
                                color: "#64748b",
                                fontSize: "12px",
                                marginBottom: "4px",
                            }}
                        >
                            Daily Limit
                        </div>

                        <strong>
                            {formatCurrency(card.dailyLimit)}
                        </strong>
                    </div>

                </div>

                <hr
                    style={{
                        border: "none",
                        borderTop: "1px solid #eef2f7",
                        margin: "16px 0",
                    }}
                />

                <div
                    style={{
                        display: "flex",
                        justifyContent: "space-between",
                        alignItems: "center",
                    }}
                >

                    <div>
                        <div
                            style={{
                                color: "#64748b",
                                fontSize: "12px",
                                marginBottom: "4px",
                            }}
                        >
                            Card Status
                        </div>

                        <span
                            style={{
                                ...statusStyle,
                                padding: "5px 12px",
                                borderRadius: "999px",
                                fontWeight: "700",
                                fontSize: "12px",
                            }}
                        >
                            {statusStyle.icon} {card.cardStatus}
                        </span>
                    </div>


                    <div>
                        <div
                            style={{
                                color: "#64748b",
                                fontSize: "12px",
                                marginBottom: "4px",
                            }}
                        >
                            Card Type
                        </div>

                        <strong>{card.cardType}</strong>
                    </div>

                </div>
                <div
                    style={{
                        display: "flex",
                        gap: "12px",
                        marginTop: "24px",
                    }}
                >

                    <button
                        disabled={updating || isBlocked || accountInactive}
                        onClick={() => onToggleStatus(card.id)}
                        onMouseEnter={(e) => {
                            e.currentTarget.style.transform = "translateY(-1px)";
                            e.currentTarget.style.boxShadow =
                                card.cardStatus === "ACTIVE"
                                    ? "0 8px 20px rgba(220,38,38,.25)"
                                    : "0 8px 20px rgba(22,163,74,.25)";
                        }}

                        onMouseLeave={(e) => {
                            e.currentTarget.style.transform = "translateY(0)";
                            e.currentTarget.style.boxShadow = "none";
                        }}

                        style={{
                            flex: 1,
                            padding: "13px",
                            borderRadius: "12px",
                            border: "none",
                            background:
                                card.cardStatus === "ACTIVE"
                                    ? "linear-gradient(135deg,#ef4444,#dc2626)"
                                    : "linear-gradient(135deg,#22c55e,#16a34a)",
                            color: "#ffffff",
                            fontWeight: "700",
                            fontSize: "14px",
                            transition: "all .25s ease",
                            opacity: (updating || isBlocked || accountInactive) ? 0.6 : 1,
                            cursor: (updating || isBlocked || accountInactive)
                                ? "not-allowed"
                                : "pointer",
                        }}

                        title={
                            isBlocked
                                ? "This card has been blocked by the bank. Please contact customer support."
                                : accountInactive
                                    ? "The linked account is frozen. Activate the account before managing this card."
                                    : ""
                        }
                    >
                        {updating ? "Updating..." : isBlocked
                            ? "🚫 Card Blocked" : accountInactive
                                ? "🔒 Account Frozen" : card.cardStatus === "ACTIVE"
                                    ? "❄ Freeze Card" : "✓ Activate Card"}
                    </button>

                    <button
                        disabled={!canUpdateLimit}
                        title={
                            !canUpdateLimit
                                ? updateLimitDisabledReason
                                : "Update your daily transaction limit"
                        }
                        onClick={() => setIsLimitModalOpen(true)}
                        onMouseEnter={(e) => {
                            e.currentTarget.style.backgroundColor = "#f8fafc";
                            e.currentTarget.style.borderColor = "#94a3b8";
                        }}

                        onMouseLeave={(e) => {
                            e.currentTarget.style.backgroundColor = "#ffffff";
                            e.currentTarget.style.borderColor = "#d1d5db";
                        }}

                        style={{
                            flex: 1,
                            padding: "13px",
                            borderRadius: "12px",
                            border: "1px solid #d1d5db",
                            backgroundColor: "#ffffff",
                            color: "#1f2937",
                            fontWeight: "700",
                            fontSize: "14px",
                            transition: "all .25s ease",
                            opacity: !canUpdateLimit ? 0.55 : 1,
                            cursor: !canUpdateLimit ? "not-allowed" : "pointer",
                        }}
                    >
                        {!canUpdateLimit ? "🔒 Update Disabled" : "✏ Update Limit"}
                    </button>
                </div>
            </div>

            <UpdateCardLimitModal
                isOpen={isLimitModalOpen}
                onClose={() => setIsLimitModalOpen(false)}
                currentLimit={card.dailyLimit}
                cardId={card.id}
                onSave={onUpdateLimit}
            />

        </>
    );
};

export default CardManagementPanel;