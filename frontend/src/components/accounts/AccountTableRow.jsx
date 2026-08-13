import React, { Fragment, useState } from "react";
import { formatCurrency } from "../../utils/formatUtils";
import AccountDetails from "./AccountDetails";


const AccountTableRow = ({
    account,
    index,
    expandedAccountId,
    handleRowClick,
    statusStyle,

    accountTransactions,
    loadAccountTransactions,
    transactionLoading,

    handleStatusClick,
}) => {

    const [transactionAccountId, setTransactionAccountId] = useState(null);

    return (
        <Fragment>

            <tr
                onClick={() => handleRowClick(account.id)}
                style={{
                    cursor: "pointer",
                    transition: ".15s",
                    background:
                        expandedAccountId === account.id
                            ? "#eff6ff"
                            : index % 2 === 0
                                ? "#ffffff"
                                : "#fafafa",
                }}

                onMouseEnter={(e) => {
                    if (expandedAccountId !== account.id) {
                        e.currentTarget.style.background = "#eff6ff";
                    }
                }}

                onMouseLeave={(e) => {
                    if (expandedAccountId !== account.id) {
                        e.currentTarget.style.background =
                            index % 2 === 0
                                ? "#ffffff"
                                : "#fafafa";
                    }
                }}
            >

                <td style={styles.cell}>
                    {account.customerName}
                </td>


                <td style={styles.cell}>
                    <span
                        style={{
                            fontFamily: "monospace",
                            fontWeight: 600,
                            color: "#15803d",
                        }}
                    >
                        {account.accountNumber}
                    </span>
                </td>


                <td style={styles.cell}>
                    {account.accountType}
                </td>


                <td style={styles.cell}>
                    {formatCurrency(account.currentBalance)}
                </td>


                <td style={styles.cell}>
                    <span
                        style={{
                            padding: "5px 10px",
                            borderRadius: "999px",
                            fontWeight: 600,
                            fontSize: "12px",
                            ...statusStyle,
                        }}
                    >
                        {account.accountStatus}
                    </span>
                </td>

            </tr>


            {
                expandedAccountId === account.id && (

                    <tr>

                        <td
                            colSpan={5}
                            style={{
                                padding: "18px",
                                background: "#fff",
                                borderBottom: "1px solid #e5e7eb"
                            }}
                        >

                            <AccountDetails
                                account={account}
                                statusStyle={statusStyle}

                                transactionAccountId={transactionAccountId}
                                setTransactionAccountId={setTransactionAccountId}

                                accountTransactions={accountTransactions}
                                loadAccountTransactions={loadAccountTransactions}
                                transactionLoading={transactionLoading}

                                handleStatusClick={handleStatusClick}
                            />

                        </td>

                    </tr>

                )
            }


        </Fragment>
    );
};


const styles = {

    cell: {
        padding: "16px",
        borderBottom: "1px solid #f1f5f9",
        fontSize: "14px",
        color: "#334155",
    }

};


export default AccountTableRow;