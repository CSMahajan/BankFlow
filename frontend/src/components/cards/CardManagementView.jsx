import React, { useEffect, useState } from 'react';
import { blockCard, unblockCard } from '../../api/bankService';
import toast from "react-hot-toast";
import PageCard from '../PageCard';
import useCards from "../../hooks/useCards";
import CardSearchToolbar from "./CardSearchToolbar";
import CardSummaryCards from "./CardSummaryCards";
import CardStatusModal from './CardStatusModal';
import CardTable from './CardTable';
import CardPagination from "./CardPagination";
import CardHeader from "./CardHeader";
import CardEmptyState from "./CardEmptyState";

const CardManagementView = ({
    refreshDashboard,
}) => {
    const [currentPage, setCurrentPage] = useState(0);
    const [search, setSearch] = useState("");
    const [searchInput, setSearchInput] = useState("");
    const [statusFilter, setStatusFilter] = useState("ALL");
    const [searchLoading, setSearchLoading] = useState(false);
    const [expandedCardId, setExpandedCardId] = useState(null);
    const [showBlockModal, setShowBlockModal] = useState(false);
    const [selectedCard, setSelectedCard] = useState(null);
    const [actionLoading, setActionLoading] = useState(false);
    const {
        cards,
        pageData,
        cardSummary,
        loading,
        error,
        reloadCards,
        reloadSummary
    } = useCards({
        currentPage,
        search,
        statusFilter
    });

    const handleToggleStatus = async () => {

        try {

            setActionLoading(true);

            if (selectedCard.cardStatus === "BLOCKED") {
                await unblockCard(selectedCard.id);
            } else {
                await blockCard(selectedCard.id);
            }

            await reloadCards();
            await reloadSummary();
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

    return (
        <>
            <PageCard title="🏦 Card Management">

                <CardHeader
                    totalCards={totalCards}
                />
                <CardSummaryCards
                    activeCount={activeCount}
                    blockedCount={blockedCount}
                    frozenCount={frozenCount}
                />

                <CardSearchToolbar
                    searchInput={searchInput}
                    setSearchInput={setSearchInput}
                    searchLoading={searchLoading}
                    statusFilter={statusFilter}
                    setStatusFilter={setStatusFilter}
                    setCurrentPage={setCurrentPage}
                />

                <div style={styles.resultInfo}>
                    Showing {filteredCount} cards
                    {search && ` matching "${search}"`}
                </div>

                {cards.length === 0 ? (
                    <CardEmptyState />
                ) : (
                    <>
                        <hr
                            style={{
                                border: "none",
                                borderTop: "1px solid #e5e7eb",
                                margin: "28px 0",
                            }}
                        />

                        <CardTable
                            cards={cards}
                            expandedCardId={expandedCardId}
                            setExpandedCardId={setExpandedCardId}
                            handleStatusClick={handleStatusClick}
                        />

                        <CardPagination
                            pageData={pageData}
                            setCurrentPage={setCurrentPage}
                        />
                    </>
                )}
            </PageCard>
            <CardStatusModal
                selectedCard={selectedCard}
                showBlockModal={showBlockModal}
                setShowBlockModal={setShowBlockModal}
                actionLoading={actionLoading}
                handleToggleStatus={handleToggleStatus}
            />
        </>
    );
};

const styles = {

    resultInfo: {
        color: "#64748b",
        fontSize: "14px",
        marginBottom: "16px",
    },
};

export default CardManagementView;