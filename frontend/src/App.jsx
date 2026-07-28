import React, { useState, useEffect } from 'react';
import Login from './components/Login';
import Dashboard from './pages/Dashboard';

function App() {
  const [token, setToken] = useState(localStorage.getItem('token') || null);
  const [userRole, setUserRole] = useState(localStorage.getItem('userRole') || 'CUSTOMER');
  const [userName, setUserName] = useState(localStorage.getItem('fullName') || '');

  useEffect(() => {
    if (token) {
      localStorage.setItem('token', token);
      localStorage.setItem('userRole', userRole);
      if (userName) localStorage.setItem('fullName', userName);
    }
  }, [token, userRole, userName]);

  const handleLoginSuccess = (data) => {
    let newToken = null;
    let newRole = 'CUSTOMER';
    let newName = '';

    if (typeof data === 'object' && data !== null) {
      newToken = data.token || data.accessToken || data.jwt;
      newRole = data.role || data.userRole || 'CUSTOMER';
      newName = data.fullName || data.name || data.username || data.email || '';
    } else if (typeof data === 'string') {
      newToken = data;
    }

    // Fallback check in case Login component updated localStorage directly
    if (!newToken) newToken = localStorage.getItem('token');
    if (!newName) newName = localStorage.getItem('fullName') || localStorage.getItem('username') || '';
    if (!newRole) newRole = localStorage.getItem('userRole') || 'CUSTOMER';

    if (newToken) {
      setToken(newToken);
      setUserRole(newRole);
      setUserName(newName);
    }
  };

  const handleLogout = () => {
    setToken(null);
    setUserRole('CUSTOMER');
    setUserName('');
    localStorage.clear();
  };

  if (!token) {
    return (
      <Login
        onLoginSuccess={handleLoginSuccess}
        onLogin={handleLoginSuccess}
        setToken={handleLoginSuccess}
      />
    );
  }

  return (
    <Dashboard
      userRole={userRole}
      userName={userName}
      onLogout={handleLogout}
    />
  );
}

export default App;
