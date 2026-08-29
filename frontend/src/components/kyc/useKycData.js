import { useEffect, useState } from "react";
import toast from "react-hot-toast";

import {
    fetchAdminKycDocuments,
    fetchAdminKycSummary
} from "../../api/bankService";

const useKycData = () => {
    const [documents, setDocuments] = useState([]);
    const [summary, setSummary] = useState(null);
    const [loading, setLoading] = useState(true);
    const [initialLoading, setInitialLoading] = useState(true);

    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    const [search, setSearch] = useState("");
    const [status, setStatus] = useState("ALL");

    const loadData = async () => {
        try {
            setLoading(true);

            const [docs, summaryData] = await Promise.all([
                fetchAdminKycDocuments({
                    page,
                    size: 10,
                    search,
                    status
                }),
                fetchAdminKycSummary()
            ]);

            setDocuments(docs.content);
            setTotalPages(docs.totalPages);
            setSummary(summaryData);

        } catch (err) {
            console.error(err);
            toast.error("Unable to load KYC data");
        } finally {
            setLoading(false);
            setInitialLoading(false);
        }
    };

    useEffect(() => {
        setPage(0);
    }, [search, status]);

    useEffect(() => {
        const timer = setTimeout(() => {
            loadData();
        }, 500);

        return () => clearTimeout(timer);
    }, [page, status, search]);

    return {
        documents,
        summary,
        loading,
        initialLoading,

        page,
        setPage,
        totalPages,

        search,
        setSearch,
        status,
        setStatus,

        loadData
    };
};

export default useKycData;