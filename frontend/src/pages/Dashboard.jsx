import React from 'react';
import CustomerDashboard from "../components/CustomerDashboard";
import AdminDashboard from "../components/AdminDashboard";

const Dashboard = ({ userRole, userName, onLogout }) => {
  // Normalize the user role check
  const currentRole = (userRole || localStorage.getItem('userRole') || 'CUSTOMER').toUpperCase();

  if (currentRole === 'ADMIN') {
    return <AdminDashboard userRole={currentRole} userName={userName} onLogout={onLogout} />;
  }

  return <CustomerDashboard userRole={currentRole} userName={userName} onLogout={onLogout} />;
};

export default Dashboard;
