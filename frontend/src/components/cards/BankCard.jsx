import { getCardStatusStyle } from "../../utils/cardStatusUtils";

const BankCard = ({ card }) => {
    const statusStyle = getCardStatusStyle(card.cardStatus);
    return (
        <div
            style={{
                position: "relative",
                overflow: "hidden",

                background:
                    card.cardType === "CREDIT"
                        ? "linear-gradient(135deg, #0f172a, #1e293b, #334155)"
                        : "linear-gradient(135deg, #0b1f4d, #1e3a8a, #3b82f6)",
                color: "#ffffff",
                borderRadius: "22px",
                padding: "28px",
                minHeight: "235px",
                display: "flex",
                flexDirection: "column",
                justifyContent: "space-between",
                boxShadow: "0 18px 40px rgba(0,0,0,0.18)",
                transition: "transform 0.3s ease, box-shadow 0.3s ease",
                cursor: "pointer",
            }}

            onMouseEnter={(e) => {
                e.currentTarget.style.transform =
                    "translateY(-2px) scale(1.015)";
                e.currentTarget.style.boxShadow =
                    "0 28px 55px rgba(0,0,0,0.25)";
            }}

            onMouseLeave={(e) => {
                e.currentTarget.style.transform =
                    "translateY(0) scale(1)";
                e.currentTarget.style.boxShadow =
                    "0 18px 40px rgba(0,0,0,0.18)";
            }}
        >
            <div
                style={{
                    position: "absolute",
                    top: "-40%",
                    left: "-20%",
                    width: "70%",
                    height: "220%",
                    background:
                        "linear-gradient(to right, rgba(255,255,255,0), rgba(255,255,255,0.12), rgba(255,255,255,0))",
                    transform: "rotate(28deg)",
                    pointerEvents: "none",
                }}
            />

            <div
                style={{
                    position: "absolute",
                    width: "220px",
                    height: "220px",
                    borderRadius: "50%",
                    background: "rgba(255,255,255,0.05)",
                    top: "-80px",
                    right: "-70px",
                }}
            />

            <div
                style={{
                    position: "absolute",
                    width: "140px",
                    height: "140px",
                    borderRadius: "50%",
                    background: "rgba(255,255,255,0.04)",
                    bottom: "-45px",
                    left: "-45px",
                }}
            />

            <div
                style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "flex-start",
                }}
            >

                <div>
                    <div
                        style={{
                            fontFamily: "Times New Roman",
                            fontWeight: "800",
                            fontSize: "30px",
                        }}
                    >
                        BankFlow
                    </div>

                    <div
                        style={{
                            marginTop: "2px",
                            opacity: 0.85,
                            fontSize: "13px",
                            letterSpacing: "2px",
                        }}
                    >
                        {card.cardType}
                    </div>
                </div>
                <span
                    style={{
                        ...statusStyle,
                        padding: "4px 10px",
                        borderRadius: "999px",
                        fontWeight: "700",
                        fontSize: "10px",
                    }}
                >
                    {statusStyle.icon} {card.cardStatus}
                </span>
            </div>
            <div
                style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                }}
            >

                <div
                    style={{
                        width: "60px",
                        height: "44px",
                        borderRadius: "8px",
                        background:
                            "linear-gradient(135deg,#caa64d,#f4d03f,#b8860b)",
                        position: "relative",
                        overflow: "hidden",
                        boxShadow: "inset 0 0 6px rgba(0,0,0,.25)",
                        border: "1px solid rgba(255,255,255,0.15)",
                    }}
                >

                    <div
                        style={{
                            position: "absolute",
                            top: "50%",
                            left: 0,
                            right: 0,
                            borderTop: "1px solid rgba(0,0,0,.25)",
                        }}
                    />

                    <div
                        style={{
                            position: "absolute",
                            left: "50%",
                            top: 0,
                            bottom: 0,
                            borderLeft: "1px solid rgba(0,0,0,.25)",
                        }}
                    />

                </div>

                <div
                    style={{
                        fontSize: "24px",
                        opacity: 0.85,
                    }}
                >
                    📶
                </div>
            </div>
            <div
                style={{
                    fontSize: "24px",
                    letterSpacing: "4px",
                    fontFamily: "monospace",
                    fontWeight: "700",
                }}
            >
                {card.maskedCardNumber}
            </div>

            <div>
                <div style={{ opacity: 0.8, fontSize: "11px" }}>
                    CARD HOLDER
                </div>

                <strong>
                    {card.cardHolderName.toUpperCase()}
                </strong>
            </div>

            <div
                style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "flex-end",
                    marginTop: "8px",
                }}
            >

                <div>
                    <div
                        style={{
                            opacity: 0.8,
                            fontSize: "11px",
                        }}
                    >
                        VALID THRU
                    </div>

                    <strong>
                        {new Date(card.expiryDate).toLocaleDateString("en-IN", {
                            month: "2-digit",
                            year: "2-digit",
                        })}
                    </strong>
                </div>

                <div
                    style={{
                        textAlign: "right",
                    }}
                >
                    <div
                        style={{
                            fontSize: "11px",
                            letterSpacing: "3px",
                            opacity: 0.75,
                        }}
                    >
                        BANKFLOW
                    </div>

                    <div
                        style={{
                            fontWeight: "800",
                            fontSize: "20px",
                            letterSpacing: "3px",
                        }}
                    >
                        {card.cardType === "CREDIT"
                            ? "INFINITE"
                            : "PREMIUM"}
                    </div>
                </div>

            </div>

        </div>
    );

};

export default BankCard;