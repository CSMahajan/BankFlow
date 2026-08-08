import React from 'react';
import BalanceSummaryCard from './BalanceSummaryCard';
import DashboardSummaryCard from './DashboardSummaryCard';
import FinancialSummaryCard from './FinancialSummaryCard';

const DashboardOverview = ({
    accounts,
    refreshDashboard,
    summary,
    loadingSummary,
    summaryError,
    analytics,
    loadingAnalytics,
    analyticsError,
}) => {
    return (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            <BalanceSummaryCard
                accounts={accounts}
                refreshDashboard={refreshDashboard}
            />

            <DashboardSummaryCard
                summary={summary}
                loading={loadingSummary}
                error={summaryError}
            />

            <FinancialSummaryCard
                analytics={analytics}
                loading={loadingAnalytics}
                error={analyticsError}
            />
        </div>
    );
};

export default DashboardOverview;