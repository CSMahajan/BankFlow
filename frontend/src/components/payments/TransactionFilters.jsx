import React from "react";
import styles from "./transactionStyles";

const TransactionFilters = ({
    accounts,
    filters,
    setFilters,
    setAppliedFilters,
    setPage,
    resetFilters,
    today,
}) => {

    return (
        <div style={styles.filterBar}>
            <input
                type="text"
                placeholder="Search transaction ID or description"
                value={filters.search}
                onChange={(e) =>
                    setFilters(prev => ({
                        ...prev,
                        search: e.target.value,
                    }))
                }
                onKeyDown={(e) => {
                    if (e.key === "Enter") {
                        setPage(0);
                        setAppliedFilters(filters);
                    }
                }}
                style={styles.searchInput}
            />
            <select
                value={filters.accountNumber}
                onChange={(e) =>
                    setFilters(prev => ({
                        ...prev,
                        accountNumber: e.target.value,
                    }))
                }
                style={styles.filterSelect}
            >
                <option value="">All Accounts</option>

                {accounts.map(account => (
                    <option
                        key={account.accountNumber}
                        value={account.accountNumber}
                    >
                        {account.accountType} • {account.accountNumber}
                    </option>
                ))}
            </select>

            <select
                value={filters.transactionType}
                onChange={(e) =>
                    setFilters(prev => ({
                        ...prev,
                        transactionType: e.target.value,
                    }))
                }
                style={styles.filterSelect}
            >
                <option value="">All Transactions</option>
                <option value="CREDIT">Credits</option>
                <option value="DEBIT">Debits</option>
            </select>

            <div style={styles.dateToolbar}>
                <div style={styles.dateField}>
                    <span style={styles.dateLabel}>Start Date</span>
                    <input
                        type="date"
                        value={filters.fromDate}
                        max={today}
                        onChange={(e) =>
                            setFilters(prev => ({
                                ...prev,
                                fromDate: e.target.value,
                            }))
                        }
                        style={styles.dateInput}
                    />
                </div>
                <div style={styles.dateField}>
                    <span style={styles.dateLabel}>End Date</span>
                    <input
                        type="date"
                        value={filters.toDate}
                        min={filters.fromDate}
                        max={today}
                        onChange={(e) =>
                            setFilters(prev => ({
                                ...prev,
                                toDate: e.target.value,
                            }))
                        }
                        style={styles.dateInput}
                    />
                </div>

                <button
                    style={styles.applyButton}
                    onClick={() => {
                        setPage(0);
                        setAppliedFilters(filters);
                    }}
                >
                    Apply Filters
                </button>

                <button
                    style={styles.resetButton}
                    onClick={resetFilters}
                >
                    Reset
                </button>
            </div>
        </div>
    );
};

export default TransactionFilters;