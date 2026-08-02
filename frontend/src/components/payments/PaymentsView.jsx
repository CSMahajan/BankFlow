import React from "react";

const PaymentsView = ({ activeTab }) => {
    return (
        <div>
            <h2 style={{ marginBottom: "24px" }}>💸 Payments</h2>

            {activeTab === "transfer" && (
                <div>Transfer Money (Coming Next)</div>
            )}

            {activeTab === "scheduled" && (
                <div>Scheduled Transfers (Coming Soon)</div>
            )}

            {activeTab === "transactions" && (
                <div>Transactions (Coming Soon)</div>
            )}
        </div>
    );
};

export default PaymentsView;