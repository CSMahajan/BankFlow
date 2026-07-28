import React, { useState } from 'react';
import CreateAccountModal from './CreateAccountModal';

const FloatingCreateAccountButton = ({ onAccountCreated }) => {
  const [isOpen, setIsOpen] = useState(false);

  return (
    <>
      {/* Fixed position guarantees visibility on top of any active screen/tab */}
      <button
        type="button"
        onClick={() => setIsOpen(true)}
        style={styles.floatingBtn}
      >
        <span style={styles.icon}>➕</span> Open New Account
      </button>

      {/* Account Creation Modal */}
      <CreateAccountModal
        isOpen={isOpen}
        onClose={() => setIsOpen(false)}
        onAccountCreated={() => {
          if (onAccountCreated) onAccountCreated();
          // Optional: Refresh page to update account list across views
          window.location.reload();
        }}
      />
    </>
  );
};

const styles = {
  floatingBtn: {
    position: 'fixed',
    bottom: '32px',
    right: '32px',
    backgroundColor: '#0d6360',
    color: '#ffffff',
    border: 'none',
    padding: '14px 24px',
    borderRadius: '50px',
    fontWeight: '700',
    fontSize: '15px',
    boxShadow: '0 8px 24px rgba(13, 99, 96, 0.35)',
    cursor: 'pointer',
    zIndex: 99999, // Guarantees it floats above all navbar/containers
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
    transition: 'transform 0.2s ease, background-color 0.2s ease',
  },
  icon: {
    fontSize: '14px',
  },
};

export default FloatingCreateAccountButton;
