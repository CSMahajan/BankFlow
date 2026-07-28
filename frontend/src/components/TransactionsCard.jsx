import React from 'react';

const TransactionsCard = ({ transactions }) => {
  return (
    <div style={styles.card}>
      <div style={styles.cardHeader}>
        <div>
          <span style={styles.sectionTag}>RECENT ACTIVITY</span>
          <h2 style={styles.sectionTitle}>Latest transactions</h2>
        </div>
        <a href="#activity" style={styles.linkText}>See activity →</a>
      </div>

      <div style={styles.transactionList}>
        {Array.isArray(transactions) && transactions.length > 0 ? (
          transactions.map((tx, idx) => {
            const isDebit = tx.amount < 0 || tx.description?.toLowerCase().includes('payment') || tx.description?.toLowerCase().includes('transfer to');
            const amount = Math.abs(tx.amount || 0);

            return (
              <div key={tx.id || idx} style={styles.txRow}>
                <div style={isDebit ? styles.txCircleDebit : styles.txCircleCredit}>
                  {isDebit ? '↗' : '↙'}
                </div>
                <div style={styles.txDetails}>
                  <div style={styles.txTitle}>{tx.description || tx.merchantName || 'Transaction'}</div>
                  <div style={styles.txSub}>{tx.date || 'Recent'} · {isDebit ? 'Debit' : 'Credit'}</div>
                </div>
                <div style={isDebit ? styles.txAmountDebit : styles.txAmountCredit}>
                  {isDebit ? '-₹' : '+₹'}{amount.toLocaleString('en-IN')}
                </div>
              </div>
            );
          })
        ) : (
          <div style={{ color: '#9ca3af', fontSize: '13px' }}>No recent activity.</div>
        )}
      </div>
    </div>
  );
};

const styles = {
  card: { backgroundColor: '#ffffff', borderRadius: '16px', padding: '28px', border: '1px solid #eef0ec' },
  cardHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '24px' },
  sectionTag: { fontSize: '11px', fontWeight: '700', color: '#6b7280' },
  sectionTitle: { fontSize: '22px', fontFamily: 'Georgia, serif', fontWeight: '700', margin: 0 },
  linkText: { fontSize: '13px', color: '#0d6360', fontWeight: '600', textDecoration: 'none' },
  transactionList: { display: 'flex', flexDirection: 'column', gap: '18px' },
  txRow: { display: 'flex', alignItems: 'center', justifyContent: 'space-between' },
  txCircleDebit: { width: '36px', height: '36px', borderRadius: '50%', backgroundColor: '#fef2f2', color: '#dc2626', display: 'flex', alignItems: 'center', justifyContent: 'center' },
  txCircleCredit: { width: '36px', height: '36px', borderRadius: '50%', backgroundColor: '#dcfce7', color: '#16a34a', display: 'flex', alignItems: 'center', justifyContent: 'center' },
  txDetails: { flex: 1, marginLeft: '14px' },
  txTitle: { fontWeight: '600', fontSize: '14px' },
  txSub: { fontSize: '12px', color: '#6b7280' },
  txAmountCredit: { fontWeight: '700', fontSize: '14px', color: '#16a34a' },
  txAmountDebit: { fontWeight: '700', fontSize: '14px', color: '#111827' },
};

export default TransactionsCard;
