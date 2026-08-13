import React from "react";
import AccountTableHeader from "./AccountTableHeader";
import AccountTableRow from "./AccountTableRow";
import { getAccountStatusStyle } from "../../utils/accountStatusUtils";


const AccountTable = ({
    accounts,

    expandedAccountId,
    handleRowClick,

    accountTransactions,
    loadAccountTransactions,
    transactionLoading,

    handleStatusClick,

}) => {

    return (
        <div
            style={styles.container}
        >

            <table style={styles.table}>

                <AccountTableHeader />

                <tbody>

                    {
                        accounts.map((account, index) => (
                            <AccountTableRow

                                key={account.id}

                                account={account}

                                index={index}

                                expandedAccountId={expandedAccountId}

                                handleRowClick={handleRowClick}

                                statusStyle={
                                    getAccountStatusStyle(
                                        account.accountStatus
                                    )
                                }

                                accountTransactions={
                                    accountTransactions
                                }

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
                        ))
                    }

                </tbody>

            </table>

        </div>
    );
};


const styles = {

    container: {
        overflowX: "auto",
        border: "1px solid #e5e7eb",
        borderRadius: "12px",
    },

    table: {
        width: "100%",
        borderCollapse: "collapse",
    }

};


export default AccountTable;