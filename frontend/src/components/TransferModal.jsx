import React from "react";
import TransferForm from "./payments/TransferForm";

const TransferModal = ({ isOpen, onClose, onTransferSuccess, accounts = [] }) => {

  if (!isOpen) return null;

  return (
    <div style={modalStyles.overlay}>
      <div style={modalStyles.modal}>

        <div style={modalStyles.header}>
          <h3 style={modalStyles.title}>
            Transfer Funds
          </h3>

          <button
            style={modalStyles.closeBtn}
            onClick={onClose}
          >
            ✕
          </button>
        </div>

        <TransferForm
          accounts={accounts}
          onSuccess={() => {
            onTransferSuccess?.();
            onClose();
          }}
          onCancel={onClose}
        />

      </div>
    </div>
  );
};

const modalStyles = {
  overlay: { position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0, 0, 0, 0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 },
  modal: { backgroundColor: '#ffffff', borderRadius: '16px', padding: '24px', width: '100%', maxWidth: '440px', boxShadow: '0 10px 25px rgba(0,0,0,0.1)' },
  header: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' },
  title: { margin: 0, fontSize: '18px', fontFamily: 'Georgia, serif' },
  closeBtn: { border: 'none', background: 'none', fontSize: '18px', cursor: 'pointer' }
};

export default TransferModal;
