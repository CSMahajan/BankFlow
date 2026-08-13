import React from "react";

const AccountTableHeader = () => {

    return (
        <thead>
            <tr>
                <th style={styles.header}>
                    Customer
                </th>

                <th style={styles.header}>
                    Account Number
                </th>

                <th style={styles.header}>
                    Type
                </th>

                <th style={styles.header}>
                    Balance
                </th>

                <th style={styles.header}>
                    Status
                </th>
            </tr>
        </thead>
    );
};


const styles = {

    header: {
        background: "#f8fafc",
        color: "#334155",
        fontWeight: 700,
        fontSize: "14px",
        padding: "14px 16px",
        textAlign: "left",
        borderBottom: "1px solid #e5e7eb",
        whiteSpace: "nowrap",
    }

};


export default AccountTableHeader;