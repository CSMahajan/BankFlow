import { useEffect, useState } from 'react';
import useLoanApprovals from "../../hooks/useLoanApprovals";
import PageCard from '../PageCard';
import LoanSummaryCards from "./LoanSummaryCards";
import LoanSearchToolbar from "./LoanSearchToolbar";
import LoanTable from "./LoanTable";
import LoanPagination from './LoanPagination';
import LoanRejectModal from "./LoanRejectModal";
import toast from "react-hot-toast";

const LoanApprovalsView = ({
    refreshDashboard,
}) => {
    const [expandedLoanId, setExpandedLoanId] = useState(null);
    const [showRejectModal, setShowRejectModal] = useState(false);
    const [selectedLoanId, setSelectedLoanId] = useState(null);
    const [selectedLoan, setSelectedLoan] = useState(null);
    const [rejectionRemarks, setRejectionRemarks] = useState("");

    const {
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
        handleRejectLoan

    } = useLoanApprovals({
        refreshDashboard
    });

    useEffect(() => {
        setExpandedLoanId(null);
    }, [
        currentPage,
        search,
        loanTypeFilter
    ]);

    if (loading && initialLoading) {
        return <p>Loading pending loan applications...</p>;
    }

    if (error) {
        return <p>{error}</p>;
    }

    const handleReject = (loan) => {
        setSelectedLoanId(loan.id);
        setSelectedLoan(loan);
        setRejectionRemarks("");
        setShowRejectModal(true);
    };

    const confirmReject = async () => {
        if (!rejectionRemarks.trim()) {
            toast.error("Rejection remarks are required.");
            return;
        }
        const success = await handleRejectLoan(
            selectedLoanId,
            rejectionRemarks.trim()
        );
        if (success) {
            setShowRejectModal(false);
            setSelectedLoanId(null);
            setSelectedLoan(null);
            setRejectionRemarks("");
        }
    };

    const personalCount =
        loanSummary?.personalLoans ?? 0;

    const homeCount =
        loanSummary?.homeLoans ?? 0;

    const vehicleCount =
        loanSummary?.vehicleLoans ?? 0;

    return (
        <PageCard title="🏦 Loan Approvals">

            <div style={styles.pageHeader}>

                <div>
                    <p style={styles.subtitle}>
                        Review and approve customer loan applications.
                    </p>
                </div>

                <div style={styles.pendingBadge}>
                    {pageData?.totalElements ?? 0} Pending
                </div>

            </div>

            <LoanSummaryCards
                personalCount={personalCount}
                homeCount={homeCount}
                vehicleCount={vehicleCount}
            />

            {pendingLoans.length === 0 ? (

                <div style={styles.emptyState}>

                    {search || loanTypeFilter !== "ALL"
                        ? "No loans found matching your criteria."
                        : "No pending loan applications."
                    }

                </div>

            ) : (
                <>
                    <LoanSearchToolbar
                        search={search}
                        setSearch={setSearch}
                        loanTypeFilter={loanTypeFilter}
                        setLoanTypeFilter={setLoanTypeFilter}
                        setCurrentPage={setCurrentPage}
                    />
                    <hr
                        style={{
                            border: "none",
                            borderTop: "1px solid #e5e7eb",
                            margin: "28px 0",
                        }}
                    />
                    <LoanTable
                        loans={pendingLoans}
                        expandedLoanId={expandedLoanId}
                        setExpandedLoanId={setExpandedLoanId}
                        handleApprove={handleApprove}
                        handleReject={handleReject}
                    />
                    <LoanPagination
                        pageData={pageData}
                        setCurrentPage={setCurrentPage}
                    />
                </>
            )}

            <LoanRejectModal
                showRejectModal={showRejectModal}
                selectedLoan={selectedLoan}
                rejectionRemarks={rejectionRemarks}
                setRejectionRemarks={setRejectionRemarks}
                closeModal={() => {
                    setShowRejectModal(false);
                    setSelectedLoanId(null);
                    setSelectedLoan(null);
                    setRejectionRemarks("");
                }}
                confirmReject={confirmReject}
            />
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

    emptyState: {
        textAlign: "center",
        padding: "40px",
        color: "#64748b",
        fontSize: "15px",
    }
};

export default LoanApprovalsView;