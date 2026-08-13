import { useEffect, useState } from "react";
import {
    fetchAllCards,
    fetchCardSummary
} from "../api/bankService";


const useCards = ({
    currentPage,
    search,
    statusFilter
}) => {

    const [cards, setCards] = useState([]);
    const [pageData, setPageData] = useState(null);
    const [cardSummary, setCardSummary] = useState(null);

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");


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


    return {

        cards,
        pageData,
        cardSummary,

        loading,
        error,

        reloadCards: loadCards,
        reloadSummary: loadCardSummary
    };

};


export default useCards;