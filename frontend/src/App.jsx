import React, { useState, useEffect } from 'react';
import Login from './components/Login';
import Register from './components/Register';
import Dashboard from './pages/Dashboard';
import { Toaster } from "react-hot-toast";

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
    const storedToken = localStorage.getItem('token');
    const storedRole = localStorage.getItem('userRole') || 'CUSTOMER';
    const storedName = localStorage.getItem('fullName') || '';

    setToken(storedToken);
    setUserRole(storedRole);
    setUserName(storedName);
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
    <>
      <Dashboard
        userRole={userRole}
        userName={userName}
        onLogout={handleLogout}
      />

      <Toaster
        position="top-right"
        toastOptions={{
          success: {
            duration: 4000,
            style: {
              borderLeft: "5px solid #22c55e",
            },
          },
          error: {
            duration: 5000,
            style: {
              borderLeft: "5px solid #ef4444",
            },
          },
        }}
      />
    </>
  );
}

export default App;
