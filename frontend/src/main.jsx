import React, { useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import './styles.css';

const transactions = [
  { name: 'Salary credit', date: '24 Jul 2026', type: 'Credit', amount: 84500, icon: '↓' },
  { name: 'Green Grocers', date: '23 Jul 2026', type: 'Debit', amount: 1240, icon: '↗' },
  { name: 'Metro recharge', date: '22 Jul 2026', type: 'Debit', amount: 500, icon: '↗' },
  { name: 'Interest payout', date: '18 Jul 2026', type: 'Credit', amount: 893, icon: '↓' },
];
const money = (value) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(value);

function App() {
  const [tab, setTab] = useState('Overview');
  const [hideBalance, setHideBalance] = useState(false);
  const [fdAmount, setFdAmount] = useState(25000);
  const [years, setYears] = useState(3);
  const maturity = useMemo(() => Math.round(fdAmount * Math.pow(1.071, years)), [fdAmount, years]);
  return <main className="shell">
    <aside className="sidebar"><div className="brand"><span>◇</span> bankflow</div><p className="eyebrow">PERSONAL BANKING</p>
      {['Overview', 'Accounts', 'Transactions', 'Fixed deposits'].map(item => <button key={item} className={tab === item ? 'nav active' : 'nav'} onClick={() => setTab(item)}><i>{item === 'Overview' ? '◫' : item === 'Accounts' ? '▣' : item === 'Transactions' ? '↕' : '◌'}</i>{item}</button>)}
      <div className="side-bottom"><button className="nav"><i>?</i>Help & support</button><div className="profile"><div className="avatar">MC</div><div><strong>Maya Choudhary</strong><small>Customer account</small></div><b>⌄</b></div></div>
    </aside>
    <section className="content"><header><div><p className="eyebrow">SATURDAY, 26 JULY</p><h1>Good morning, Maya <span>✦</span></h1><p className="sub">Here’s a clear view of your money today.</p></div><button className="bell">♢<em></em></button></header>
      <section className="hero"><div><p className="eyebrow">TOTAL AVAILABLE BALANCE <button aria-label="toggle balance" onClick={() => setHideBalance(!hideBalance)}>◉</button></p><h2>{hideBalance ? '₹ •••••••' : money(268450)}</h2><p>Across 2 active accounts</p></div><div className="hero-actions"><button className="light">＋ Add money</button><button className="dark">↗ Transfer</button></div></section>
      <div className="grid"><section className="panel accounts"><div className="panel-title"><div><p className="eyebrow">YOUR ACCOUNTS</p><h3>Everyday banking</h3></div><button className="text">View all →</button></div>
        <article className="account-card"><div className="account-icon blue">▣</div><div><strong>Primary savings</strong><p>•••• 4821 · Bengaluru</p></div><div className="account-value"><strong>{money(186450)}</strong><small>Available balance</small></div></article>
        <article className="account-card"><div className="account-icon sand">▤</div><div><strong>Travel fund</strong><p>•••• 9204 · Bengaluru</p></div><div className="account-value"><strong>{money(82000)}</strong><small>Available balance</small></div></article>
      </section>
      <section className="panel activity"><div className="panel-title"><div><p className="eyebrow">RECENT ACTIVITY</p><h3>Latest transactions</h3></div><button className="text">See activity →</button></div>
        {transactions.map(t => <article className="transaction" key={t.name}><div className={t.type === 'Credit' ? 'txn-icon credit' : 'txn-icon debit'}>{t.icon}</div><div><strong>{t.name}</strong><p>{t.date} · {t.type}</p></div><strong className={t.type === 'Credit' ? 'credit-text' : ''}>{t.type === 'Credit' ? '+' : '−'}{money(t.amount)}</strong></article>)}
      </section></div>
      <section className="fd-callout"><div className="fd-mark">%</div><div><p className="eyebrow">GROW YOUR SAVINGS</p><h3>Fixed deposit calculator</h3><p>See what your savings could become with a guaranteed return.</p></div><label>Deposit amount<input type="number" min="10001" value={fdAmount} onChange={e => setFdAmount(Number(e.target.value))}/></label><label>Tenure<select value={years} onChange={e => setYears(Number(e.target.value))}><option value="1">1 year</option><option value="3">3 years</option><option value="5">5 years</option></select></label><div className="maturity"><small>AT MATURITY</small><strong>{money(maturity)}</strong><button>Open an FD →</button></div></section>
    </section></main>
}
createRoot(document.getElementById('root')).render(<App />);
