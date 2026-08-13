import React, { useState, useEffect } from "react";
import useAccounts from "../../hooks/useAccounts";
import PageCard from '../PageCard';
import AccountSummaryCards from "./AccountSummaryCards";
import AccountSearchToolbar from "./AccountSearchToolbar";
import AccountStatusModal from "./AccountStatusModal";
import AccountPagination from "./AccountPagination";
import AccountTable from "./AccountTable";

const AccountManagementView = ({
    refreshDashboard,
}) => {
    const [expandedAccountId, setExpandedAccountId] = useState(null);

    const [showStatusModal, setShowStatusModal] = useState(false);
    const [selectedAccount, setSelectedAccount] = useState(null);

    const {
        accounts,
        pageData,
        currentPage,
        setCurrentPage,

        loading,

        search,
        setSearch,

        accountStatusFilter,
        setAccountStatusFilter,

        accountSummary,

        accountTransactions,
        transactionLoading,

        loadAccountTransactions,

        handleToggleStatus

    } = useAccounts({
        refreshDashboard
    });

    useEffect(() => {
        setExpandedAccountId(null);
    }, [
        currentPage,
        search,
        accountStatusFilter
    ]);

    const handleStatusClick = (account) => {
        setSelectedAccount(account);
        setShowStatusModal(true);
    };

    const handleRowClick = (accountId) => {

        setExpandedAccountId(prev => {

            return prev === accountId ? null : accountId;
        });
    };

    return (
        <PageCard title="🏦 Account Management">

            {loading && (
                <p style={{ color: "#64748b" }}>
                    Updating accounts...
                </p>
            )}

            <div style={styles.pageHeader}>

                <div>
                    <p style={styles.subtitle}>
                        View customer accounts and freeze or unfreeze them when required.
                    </p>
                </div>

                <div style={styles.pendingBadge}>
                    {pageData?.totalElements ?? 0} Total Accounts
                </div>

            </div>

            <AccountSummaryCards
                accountSummary={accountSummary}
            />

            <>
                <AccountSearchToolbar
                    search={search}
                    setSearch={setSearch}
                    accountStatusFilter={accountStatusFilter}
                    setAccountStatusFilter={setAccountStatusFilter}
                    setCurrentPage={setCurrentPage}
                />

                <div style={styles.resultInfo}>
                    Showing{" "}
                    <strong>{accounts.length}</strong>{" "}
                    of{" "}
                    <strong>{pageData?.totalElements ?? 0}</strong>{" "}
                    matching accounts
                </div>

                <hr
                    style={{
                        border: "none",
                        borderTop: "1px solid #e5e7eb",
                        margin: "28px 0",
                    }}
                />

                {
                    accounts.length === 0 ? (
                        <p>No accounts found.</p>
                    ) : (
                        <AccountTable
                            accounts={accounts}

                            expandedAccountId={expandedAccountId}

                            handleRowClick={handleRowClick}

                            accountTransactions={accountTransactions}

                            loadAccountTransactions={
                                loadAccountTransactions
                            }

                            transactionLoading={
                                transactionLoading
                            }

                            handleStatusClick={
                                handleStatusClick
                            }
                        />
                    )
                }

                <AccountStatusModal
                    showStatusModal={showStatusModal}
                    selectedAccount={selectedAccount}
                    setShowStatusModal={setShowStatusModal}
                    setSelectedAccount={setSelectedAccount}
                    handleToggleStatus={handleToggleStatus}
                />
            </>
            <AccountPagination
                pageData={pageData}
                setCurrentPage={setCurrentPage}
            />
        </PageCard>
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

    resultInfo: {
        marginTop: "-12px",
        marginBottom: "20px",
        color: "#64748b",
        fontSize: "14px",
    },
};

export default AccountManagementView;