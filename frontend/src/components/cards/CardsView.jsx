import { useEffect, useState } from "react";
import { fetchMyCards, toggleCardStatus, updateCardLimit } from "../../api/bankService";
import CardManagementPanel from "./CardManagementPanel";
import IssueCardModal from "./IssueCardModal";
import CardsHeader from "./CardsHeader";
import BankCard from "./BankCard";
import toast from "react-hot-toast";

const CardsView = ({
    refreshDashboard,
}) => {
    const [cards, setCards] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [updatingCardId, setUpdatingCardId] = useState(null);
    const [isIssueCardModalOpen, setIsIssueCardModalOpen] = useState(false);

    useEffect(() => {
        loadCards();
    }, []);

    const loadCards = async () => {
        try {
            setLoading(true);
            setError("");

            const cardList = await fetchMyCards();
            setCards(cardList);
        } catch (err) {
            console.error(err);
            setError("Failed to load cards.");
        } finally {
            setLoading(false);
        }
    };

    const handleToggleStatus = async (cardId) => {
        try {
            setUpdatingCardId(cardId);

            const updatedCard = await toggleCardStatus(cardId);

            toast.success(
                updatedCard.cardStatus === "ACTIVE"
                    ? "Card activated successfully."
                    : "Card frozen successfully."
            );

            setCards(prev =>
                prev.map(card =>
                    card.id === updatedCard.id ? updatedCard : card
                )
            );
            await refreshDashboard();

        } catch (err) {
            console.error(err);
            toast.error(
                err.response?.data?.message ?? "Failed to update card status."
            );
        } finally {
            setUpdatingCardId(null);
        }
    };

    const handleUpdateLimit = async (cardId, newLimit) => {
        try {
            const updatedCard = await updateCardLimit(cardId, newLimit);

            toast.success("Daily limit updated successfully.");

            setCards(prev =>
                prev.map(card =>
                    card.id === updatedCard.id
                        ? updatedCard
                        : card
                )
            );
            await refreshDashboard();

        } catch (err) {
            console.error(err);
            toast.error(
                err.response?.data?.message ?? "Failed to update daily limit."
            );
        }
    };



    if (loading) return <p>Loading cards...</p>;

    if (error) return <p>{error}</p>;

    return (
        <div>
            <div
                style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "flex-start",
                    marginBottom: "24px",
                }}
            >
                <CardsHeader />

                <button
                    onClick={() => setIsIssueCardModalOpen(true)}
                    style={{
                        background: "linear-gradient(135deg,#0d6360,#17817d)",
                        color: "#fff",
                        border: "none",
                        borderRadius: "12px",
                        padding: "12px 22px",
                        fontWeight: "700",
                        fontSize: "14px",
                        cursor: "pointer",
                        boxShadow: "0 10px 24px rgba(13,99,96,.25)",
                        transition: "all .25s ease",
                    }}
                    onMouseEnter={(e) => {
                        e.currentTarget.style.transform = "translateY(-2px)";
                    }}
                    onMouseLeave={(e) => {
                        e.currentTarget.style.transform = "translateY(0)";
                    }}
                >
                    ➕ Apply for Card
                </button>
            </div>

            {cards.length === 0 ? (
                <p>No cards issued yet.</p>
            ) : (
                <div
                    style={{
                        display: "grid",
                        gridTemplateColumns: "repeat(auto-fill, minmax(360px, 1fr))",
                        gap: "24px",
                    }}
                >

                    {cards.map((card) => (

                        <div
                            key={card.id}
                            style={{
                                display: "flex",
                                flexDirection: "column",
                                gap: "16px",
                            }}
                        >

                            <BankCard card={card} />
                            <CardManagementPanel
                                card={card}
                                onToggleStatus={handleToggleStatus}
                                onUpdateLimit={handleUpdateLimit}
                                updating={updatingCardId === card.id}
                            />
                        </div>
                    ))}
                </div>
            )}
            <IssueCardModal
                isOpen={isIssueCardModalOpen}
                onClose={() => setIsIssueCardModalOpen(false)}
                onCardIssued={async () => {
                    setIsIssueCardModalOpen(false);

                    await Promise.all([
                        loadCards(),
                        refreshDashboard(),
                    ]);
                }}
            />
        </div>
    );
};

export default CardsView;