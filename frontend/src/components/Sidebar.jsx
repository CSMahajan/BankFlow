import React from 'react';

const Sidebar = ({ user, activeTab, onSelectTab, onLogout }) => {
  const getInitials = (name) => {
    if (!name) return 'U';
    const parts = name.split(' ');
    if (parts.length >= 2) {
      return `${parts[0][0]}${parts[1][0]}`.toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  };

  const displayName = user?.fullName || user?.email || 'User';
  const displayRole = user?.role
    ? `${user.role.charAt(0).toUpperCase() + user.role.slice(1).toLowerCase()} account`
    : 'Customer account';

  const navItems = [
    { id: 'overview', label: 'Overview', icon: '📊' },
    { id: 'accounts', label: 'Accounts', icon: '💳' },
    { id: 'transactions', label: 'Transactions', icon: '⇅' },
    { id: 'fd', label: 'Fixed deposits', icon: '🪙' },
  ];

  return (
    <aside style={styles.sidebar}>
      <div style={styles.navSection}>
        {navItems.map((item) => {
          const isActive = activeTab === item.id;
          return (
            <div
              key={item.id}
              onClick={() => onSelectTab(item.id)}
              style={{
                ...styles.navItem,
                ...(isActive ? styles.navItemActive : {}),
              }}
            >
              <span>{item.icon}</span> {item.label}
            </div>
          );
        })}
      </div>

      <div style={styles.sidebarFooter}>
        <div style={styles.navItem}><span>❓</span> Help & support</div>
        <div style={styles.userProfile}>
          <div style={styles.avatar}>{getInitials(displayName)}</div>
          <div style={{ flex: 1, overflow: 'hidden' }}>
            <div style={styles.userName} title={displayName}>{displayName}</div>
            <div style={styles.userRole}>{displayRole}</div>
          </div>
          <button onClick={onLogout} style={styles.logoutBtn} title="Logout">
            🚪 <span style={styles.logoutText}>Logout</span>
          </button>
        </div>
      </div>
    </aside>
  );
};

const styles = {
  sidebar: {
    width: '240px',
    backgroundColor: '#f5f6f4',
    borderRight: '1px solid #e5e7eb',
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'space-between',
    padding: '32px 16px',
  },
  navSection: { display: 'flex', flexDirection: 'column', gap: '8px' },
  navItem: { display: 'flex', alignItems: 'center', gap: '12px', padding: '10px 14px', borderRadius: '8px', fontSize: '14px', color: '#4b5563', cursor: 'pointer', transition: 'all 0.15s' },
  navItemActive: { backgroundColor: '#e2ece9', color: '#0f4c42', fontWeight: '600' },
  sidebarFooter: { display: 'flex', flexDirection: 'column', gap: '16px' },
  userProfile: { display: 'flex', alignItems: 'center', gap: '10px', paddingTop: '16px', borderTop: '1px solid #e5e7eb' },
  avatar: { width: '36px', height: '36px', borderRadius: '50%', backgroundColor: '#e6d8c3', color: '#785b37', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: '700', flexShrink: 0 },
  userName: { fontSize: '13px', fontWeight: '700', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' },
  userRole: { fontSize: '11px', color: '#6b7280' },
  logoutBtn: { border: '1px solid #fca5a5', backgroundColor: '#fef2f2', color: '#991b1b', padding: '6px 10px', borderRadius: '6px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '4px', fontSize: '12px', fontWeight: '600', flexShrink: 0 },
  logoutText: { fontSize: '11px' },
};

export default Sidebar;
