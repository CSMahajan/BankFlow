import React, { useState, useEffect } from 'react';
import Login from './components/Login';
import Register from './components/Register';
import Dashboard from './pages/Dashboard';

function App() {
  const [token, setToken] = useState(localStorage.getItem('token'));
  const [userRole, setUserRole] = useState(localStorage.getItem('userRole') || 'CUSTOMER');
  const [userName, setUserName] = useState(localStorage.getItem('fullName') || '');
  const [screen, setScreen] = useState('login');

  useEffect(() => {
    if (token) {
      localStorage.setItem('token', token);
      localStorage.setItem('userRole', userRole);

      if (userName) {
        localStorage.setItem('fullName', userName);
      }
    }
  }, [token, userRole, userName]);

  const handleLoginSuccess = () => {
    setToken(localStorage.getItem('token'));
    setUserRole(localStorage.getItem('userRole') || 'CUSTOMER');
    setUserName(localStorage.getItem('fullName') || '');
  };

  const handleLogout = () => {
    localStorage.clear();
    setToken(null);
    setUserRole('CUSTOMER');
    setUserName('');
    setScreen('login');
  };

  if (!token) {
    return screen === 'login' ? (
      <Login
        onLoginSuccess={handleLoginSuccess}
        onSwitchToRegister={() => setScreen('register')}
      />
    ) : (
      <Register
        onRegisterSuccess={() => setScreen('login')}
        onSwitchToLogin={() => setScreen('login')}
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