import React, { useEffect, useState } from "react";
import { getMyTransactions, getTransactionDetails } from "../../api/bankService";
import styles from "./transactionStyles";
import TransactionDetailsDrawer from "./TransactionDetailsDrawer";
import TransactionsTable from "./TransactionsTable";
import TransactionFilters from "./TransactionFilters";
import { getTodayDate } from "../../utils/formatUtils";

const TransactionsView = ({
    accounts = [],
}) => {
    const [transactions, setTransactions] = useState([]);
    const [selectedTransactionId, setSelectedTransactionId] = useState(null);
    const [transactionDetails, setTransactionDetails] = useState(null);
    const [drawerOpen, setDrawerOpen] = useState(false);
    const [detailsLoading, setDetailsLoading] = useState(false);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [page, setPage] = useState(0);
    const [pageData, setPageData] = useState(null);

    const [filters, setFilters] = useState({
        accountNumber: "",
        transactionType: "",
        fromDate: "",
        toDate: "",
        search: "",
    });

    const [appliedFilters, setAppliedFilters] = useState({
        accountNumber: "",
        transactionType: "",
        fromDate: "",
        toDate: "",
        search: "",
    });

    const today = getTodayDate();

    const loadTransactions = async (page = 0) => {
        try {
            setError("");
            setLoading(true);

            if (
                appliedFilters.fromDate &&
                appliedFilters.toDate &&
                appliedFilters.fromDate > appliedFilters.toDate
            ) {
                setError("From date cannot be after To date.");
                setTransactions([]);
                setLoading(false);
                return;
            }
            const transactionList = await getMyTransactions({
                page,
                size: 20,

                ...(appliedFilters.accountNumber && {
                    accountNumber: appliedFilters.accountNumber,
                }),

                ...(appliedFilters.transactionType && {
                    type: appliedFilters.transactionType,
                }),

                ...(appliedFilters.fromDate && {
                    startDate: appliedFilters.fromDate,
                }),

                ...(appliedFilters.toDate && {
                    endDate: appliedFilters.toDate,
                }),

                ...(appliedFilters.search && {
                    search: appliedFilters.search,
                }),
            });

            setTransactions(transactionList.content);
            setPageData(transactionList);
        } catch (err) {
            console.error(err);
            setError("Unable to load transactions.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadTransactions(page);
    }, [
        page,
        appliedFilters,
    ]);

    useEffect(() => {
        closeDrawer();
    }, [page, appliedFilters]);

    const resetFilters = () => {
        const defaultFilters = {
            accountNumber: "",
            transactionType: "",
            fromDate: "",
            toDate: "",
            search: "",
        };

        setFilters(defaultFilters);
        setAppliedFilters(defaultFilters);
        closeDrawer();

        setPage(0);
    };

    const openTransactionDetails = async (transactionId) => {
        try {

            if (detailsLoading) return;

            setDrawerOpen(true);
            setTransactionDetails(null);
            setSelectedTransactionId(transactionId);
            setDetailsLoading(true);
            setError("");
            const transaction = await getTransactionDetails(transactionId);

            setTransactionDetails(transaction);
        } catch (err) {
            console.error(err);
            setError("Unable to load transaction details.");
        } finally {
            setDetailsLoading(false);
        }
    };

    const closeDrawer = () => {
        setDrawerOpen(false);
        setSelectedTransactionId(null);
        setTransactionDetails(null);
    };

    return (
        <div style={styles.card}>

            <div style={styles.header}>

                <div style={styles.headerText}>
                    <h3 style={styles.title}>
                        📜 Transaction History
                    </h3>

                    <p style={styles.subtitle}>
                        Browse and filter transactions across your accounts
                    </p>
                </div>

            </div>


            <TransactionFilters
                accounts={accounts}
                filters={filters}
                setFilters={setFilters}
                setAppliedFilters={setAppliedFilters}
                setPage={setPage}
                resetFilters={resetFilters}
                today={today}
            />

            {error && (
                <div style={styles.errorBanner}>
                    {error}
                </div>
            )}

            {loading ? (
                <div style={styles.emptyState}>
                    Loading transactions...
                </div>
            ) : transactions.length === 0 ? (
                <div style={styles.emptyState}>
                    <div style={{ fontSize: "42px" }}>📜</div>
                    <h4>No transactions matched your filters.</h4>
                    <p>
                        Try changing the filters or make your first transaction.
                    </p>
                </div>
            ) : (
                <>
                    <TransactionsTable
                        transactions={transactions}
                        selectedTransactionId={selectedTransactionId}
                        openTransactionDetails={openTransactionDetails}
                    />

                    {pageData && pageData.totalPages > 1 && (
                        <div style={styles.pagination}>

                            <button
                                disabled={pageData.first}
                                onClick={() => setPage(prev => prev - 1)}
                                style={{
                                    ...styles.pageButton,
                                    opacity: pageData.first ? 0.5 : 1,
                                    cursor: pageData.first ? "not-allowed" : "pointer",
                                }}
                            >
                                ← Previous
                            </button>

                            <span style={styles.pageInfo}>
                                Page {pageData.number + 1} of {pageData.totalPages}
                            </span>

                            <button
                                disabled={pageData.last}
                                onClick={() => setPage(prev => prev + 1)}
                                style={{
                                    ...styles.pageButton,
                                    opacity: pageData.last ? 0.5 : 1,
                                    cursor: pageData.last ? "not-allowed" : "pointer",
                                }}
                            >
                                Next →
                            </button>

                        </div>
                    )}
                </>
            )}
            <TransactionDetailsDrawer
                drawerOpen={drawerOpen}
                transactionDetails={transactionDetails}
                detailsLoading={detailsLoading}
                onClose={closeDrawer}
            />
        </div>
    );
};

export default TransactionsView;