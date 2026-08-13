import { useEffect, useState } from "react";
import {
    fetchPendingLoans,
    approveLoan,
    rejectLoan,
    fetchLoanSummary
} from "../api/bankService";
import toast from "react-hot-toast";


const useLoanApprovals = ({
    refreshDashboard,
}) => {
    const [pendingLoans, setPendingLoans] = useState([]);
    const [pageData, setPageData] = useState(null);
    const [currentPage, setCurrentPage] = useState(0);
    const [loading, setLoading] = useState(true);
    const [initialLoading, setInitialLoading] = useState(true);
    const [error, setError] = useState("");
    const [search, setSearch] = useState("");
    const [loanTypeFilter, setLoanTypeFilter] = useState("ALL");
    const [debouncedSearch, setDebouncedSearch] = useState("");
    const [loanSummary, setLoanSummary] = useState(null);

    useEffect(() => {
        const timer = setTimeout(() => {
            setDebouncedSearch(search);
        }, 500);
        return () => clearTimeout(timer);
    }, [search]);

    const loadLoanSummary = async () => {
        try {
            const response = await fetchLoanSummary();
            setLoanSummary(response);
        } catch (err) {
            console.error("Loan summary failed", err);
        }
    };

    const loadPendingLoans = async () => {
        try {
            if (initialLoading) {
                setLoading(true);
            }
            setError("");
            const response = await fetchPendingLoans({
                page: currentPage,
                size: 10,
                search: debouncedSearch,
                loanType: loanTypeFilter
            });
            setPendingLoans(response.content);
            setPageData(response);
        } catch (err) {
            console.error(err);
            setError(
                "Unable to load pending loan applications."
            );
        }
        finally {
            setLoading(false);
            setInitialLoading(false);
        }
    };

    useEffect(() => {
        loadLoanSummary();
    }, []);

    useEffect(() => {
        loadPendingLoans();
    }, [
        currentPage,
        debouncedSearch,
        loanTypeFilter
    ]);

    const handleApprove = async (loanId) => {
        try {
            await approveLoan(loanId);
            if (pendingLoans.length === 1 && currentPage > 0) {
                setCurrentPage(prev => prev - 1);
            }
            else {
                await loadPendingLoans();
            }
            await loadLoanSummary();
            await refreshDashboard?.();
            toast.success("Loan approved successfully.");
        } catch (err) {
            console.error(err);
            toast.error("Failed to approve loan.");
        }
    };

    const handleRejectApi = async (
        loanId,
        remarks
    ) => {
        try {
            await rejectLoan(loanId, remarks);
            if (pendingLoans.length === 1 && currentPage > 0) {
                setCurrentPage(prev => prev - 1);
            }
            else {
                await loadPendingLoans();
            }
            await loadLoanSummary();
            await refreshDashboard?.();
            toast.success("Loan rejected successfully.");
            return true;
        } catch (error) {
            console.error(error);
            toast.error(
                error.response?.data?.message ??
                error.response?.data?.error ?? error.message
            );
            return false;
        }
    };
    return {
        pendingLoans,
        pageData,
        currentPage,
        setCurrentPage,
        loading,
        initialLoading,
        error,
        search,
        setSearch,
        loanTypeFilter,
        setLoanTypeFilter,
        loanSummary,
        handleApprove,
        handleRejectApi,
    };
};
export default useLoanApprovals;