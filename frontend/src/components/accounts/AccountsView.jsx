import React from 'react';
import BalanceSummaryCard from '../BalanceSummaryCard';
import { toggleAccountStatus } from "../../api/bankService";
import { formatDate, formatCurrency } from "../../utils/formatUtils";
import { getAccountStatusStyle } from '../../utils/accountStatusUtils';
import toast from "react-hot-toast";
import ConfirmModal from "../ConfirmModal";

const AccountsView = ({
  accounts,
  loading,
  error,
  refreshAccounts,
}) => {
  const [confirmAccount, setConfirmAccount] = React.useState(null);

  const handleToggleStatus = (account) => {
    setConfirmAccount(account);
  };

  const confirmToggleStatus = async () => {

    const account = confirmAccount;

    setConfirmAccount(null);

    try {

      await toggleAccountStatus(
        account.accountNumber
      );


      toast.success(
        account.accountStatus === "ACTIVE"
          ? "✅ Account frozen successfully."
          : "✅ Account activated successfully."
      );


      await refreshAccounts();


    } catch (err) {

      console.error(err);


      toast.error(
        err.response?.data?.message ??
        "Failed to update account status."
      );

    }

  };

  const totalBalance = accounts.reduce((sum, acc) => sum + (Number(acc.currentBalance) || 0), 0);
  const savingsAccounts = accounts.filter((acc) => acc.accountType === 'SAVINGS');
  const currentAccounts = accounts.filter((acc) => acc.accountType === 'CURRENT');
  const activeSavingsCount = savingsAccounts.filter(acc => acc.accountStatus === "ACTIVE").length;
  const activeCurrentCount = currentAccounts.filter(acc => acc.accountStatus === "ACTIVE").length;

  return (
    <div style={styles.container}>
      {/* Summary & Quick Actions Header */}
      <BalanceSummaryCard
        accounts={accounts}
        refreshAccounts={refreshAccounts}
      />

      {error && <div style={styles.errorBox}>⚠️ {error}</div>}

      {loading ? (
        <div style={{ color: '#6b7280' }}>Loading accounts...</div>
      ) : accounts.length === 0 ? (
        <div style={styles.emptyCard}>
          <div style={{ fontSize: '40px' }}>🏦</div>
          <h3 style={{ margin: '8px 0 4px 0' }}>No Active Accounts Found</h3>
          <p style={{ color: '#6b7280', fontSize: '14px' }}>
            Use the <strong>"+ Open New Bank Account"</strong> button above to open an account.
          </p>
        </div>
      ) : (
        <div style={styles.sectionsGrid}>
          {/* SAVINGS */}
          <div style={styles.section}>
            <div style={styles.sectionHeader}>
              <h3 style={styles.sectionTitle}>💰 Savings Accounts</h3>
              <span style={styles.countBadge}>
                {savingsAccounts.length} Accounts
              </span>
            </div>
            {savingsAccounts.length === 0 ? (
              <p style={styles.noneText}>No Savings accounts found.</p>
            ) : (
              savingsAccounts.map((acc) => (
                <div key={acc.accountNumber} style={styles.accountCard}>
                  <div style={styles.cardTop}>

                    <div>
                      <div style={styles.accNumber}>
                        {acc.accountNumber}
                      </div>

                      <span style={styles.badgeSavings}>
                        SAVINGS
                      </span>
                    </div>

                    <div
                      style={{
                        ...styles.statusBadge,
                        ...getAccountStatusStyle(acc.accountStatus),
                      }}
                    >
                      {acc.accountStatus === "ACTIVE"
                        ? "🟢 ACTIVE"
                        : "🟠 FROZEN"}
                    </div>

                  </div>
                  <div style={styles.cardBottom}>
                    <span style={styles.balanceLabel}>
                      Holder: {acc.userName || localStorage.getItem('fullName') || 'Customer'}
                    </span>

                    <span style={styles.balanceAmount}>
                      {formatCurrency(acc.currentBalance)}
                    </span>

                    <button
                      style={{
                        ...styles.accountActionButton,
                        backgroundColor:
                          acc.accountStatus === "ACTIVE"
                            ? "#ea580c"
                            : "#15803d",
                      }}
                      onClick={() => handleToggleStatus(acc)}
                    >
                      {acc.accountStatus === "ACTIVE"
                        ? "Freeze Account"
                        : "Activate Account"}
                    </button>
                  </div>
                </div>
              ))
            )}
          </div>

          {/* CURRENT */}
          <div style={styles.section}>
            <div style={styles.sectionHeader}>
              <h3 style={styles.sectionTitle}>🏢 Current Accounts</h3>
              <span style={styles.countBadge}>
                {currentAccounts.length} Accounts
              </span>
            </div>
            {currentAccounts.length === 0 ? (
              <p style={styles.noneText}>No Current accounts found.</p>
            ) : (
              currentAccounts.map((acc) => (
                <div key={acc.accountNumber} style={styles.accountCard}>
                  <div style={styles.cardTop}>

                    <div>
                      <div style={styles.accNumber}>
                        {acc.accountNumber}
                      </div>

                      <span style={styles.badgeCurrent}>
                        CURRENT
                      </span>
                    </div>

                    <div
                      style={{
                        ...styles.statusBadge,
                        ...getAccountStatusStyle(acc.accountStatus),
                      }}
                    >
                      {acc.accountStatus === "ACTIVE"
                        ? "🟢 ACTIVE"
                        : "🟠 FROZEN"}
                    </div>

                  </div>

                  <div style={styles.cardBottom}>
                    <span style={styles.balanceLabel}>
                      Holder: {acc.userName || localStorage.getItem('fullName') || 'Customer'}
                    </span>

                    <span style={styles.balanceAmount}>
                      {formatCurrency(acc.currentBalance)}
                    </span>

                    <button
                      style={{
                        ...styles.accountActionButton,
                        backgroundColor:
                          acc.accountStatus === "ACTIVE"
                            ? "#ea580c"
                            : "#15803d",
                      }}
                      onClick={() => handleToggleStatus(acc)}
                    >
                      {acc.accountStatus === "ACTIVE"
                        ? "Freeze Account"
                        : "Activate Account"}
                    </button>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      )}
      <ConfirmModal

        open={!!confirmAccount}


        title={
          confirmAccount?.accountStatus === "ACTIVE"
            ? "Freeze Account?"
            : "Activate Account?"
        }


        message={
          confirmAccount?.accountStatus === "ACTIVE"
            ? "Outgoing transactions will not be allowed until this account is activated again."
            : "This account will become active again."
        }


        confirmText={
          confirmAccount?.accountStatus === "ACTIVE"
            ? "Freeze Account"
            : "Activate Account"
        }


        danger={
          confirmAccount?.accountStatus === "ACTIVE"
        }


        onCancel={() =>
          setConfirmAccount(null)
        }


        onConfirm={confirmToggleStatus}

      />
    </div>
  );
};

const styles = {
  container: { display: 'flex', flexDirection: 'column', gap: '20px' },
  summaryCard: { backgroundColor: '#0d6360', color: '#ffffff', borderRadius: '16px', padding: '24px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' },
  summaryLabel: { fontSize: '13px', opacity: 0.9, textTransform: 'uppercase', letterSpacing: '0.5px' },
  summaryAmount: { margin: '4px 0 0 0', fontSize: '32px', fontWeight: '800' },
  quickActions: { display: 'flex', gap: '10px' },
  actionBtnPrimary: { backgroundColor: '#ffffff', color: '#0d6360', border: 'none', padding: '10px 16px', borderRadius: '8px', fontWeight: '700', cursor: 'pointer' },
  actionBtnSecondary: { backgroundColor: 'transparent', color: '#ffffff', border: '1px solid rgba(255,255,255,0.4)', padding: '10px 16px', borderRadius: '8px', fontWeight: '700', cursor: 'pointer' },
  sectionsGrid: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px' },
  section: { backgroundColor: '#ffffff', borderRadius: '16px', padding: '20px', border: '1px solid #eef0ec', display: 'flex', flexDirection: 'column', gap: '16px' },
  sectionHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'center' },
  sectionTitle: { margin: 0, fontSize: '16px', color: '#111827' },
  countBadge: { fontSize: '12px', backgroundColor: '#f3f4f6', padding: '2px 8px', borderRadius: '12px', color: '#4b5563' },
  accountCard: { backgroundColor: '#f9fafb', borderRadius: '12px', padding: '16px', border: '1px solid #e5e7eb', display: 'flex', flexDirection: 'column', gap: '12px' },
  cardTop: { display: 'flex', justifyContent: 'space-between', alignItems: 'center' },
  accNumber: { fontFamily: 'monospace', fontWeight: '700', fontSize: '14px' },
  badgeSavings: {
    backgroundColor: '#f3f4f6',
    color: '#374151',
    padding: '3px 8px',
    borderRadius: '6px',
    fontSize: '11px',
    fontWeight: '700',
    marginTop: '6px',
    display: 'inline-block'
  },
  badgeCurrent: {
    backgroundColor: '#f3f4f6',
    color: '#374151',
    padding: '3px 8px',
    borderRadius: '6px',
    fontSize: '11px',
    fontWeight: '700',
    marginTop: '6px',
    display: 'inline-block'
  },
  cardBottom: { display: 'flex', flexDirection: 'column', gap: '4px' },
  balanceLabel: { fontSize: '12px', color: '#6b7280' },
  balanceAmount: { fontSize: '20px', fontWeight: '800', color: '#0d6360' },
  noneText: { fontSize: '13px', color: '#9ca3af', fontStyle: 'italic', margin: 0 },
  errorBox: { backgroundColor: '#fee2e2', color: '#991b1b', padding: '12px', borderRadius: '8px', fontSize: '13px' },
  emptyCard: { backgroundColor: '#ffffff', borderRadius: '16px', padding: '40px', textAlign: 'center', border: '1px solid #eef0ec' },
  accountActionButton: { marginTop: '12px', width: '100%', border: 'none', borderRadius: '8px', padding: '10px', color: '#ffffff', fontWeight: '700', cursor: 'pointer' },
  statusBadge: { display: "inline-block", marginTop: "6px", padding: "3px 8px", borderRadius: "6px", fontSize: "11px", fontWeight: "700" }
};

export default AccountsView;
