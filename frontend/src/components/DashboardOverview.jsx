import React from 'react';
import BalanceSummaryCard from './BalanceSummaryCard';
import DashboardSummaryCard from './DashboardSummaryCard';
import FinancialSummaryCard from './FinancialSummaryCard';

const DashboardOverview = ({
    accounts,
    refreshAccounts,
    refreshSummary,
    summary,
    loadingSummary,
    summaryError,
}) => {
    return (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            <BalanceSummaryCard
                accounts={accounts}
                refreshAccounts={refreshAccounts}
                refreshSummary={refreshSummary}
            />

            <DashboardSummaryCard
                summary={summary}
                loading={loadingSummary}
                error={summaryError}
            />

            <FinancialSummaryCard />
        </div>
    );
};

export default DashboardOverview;