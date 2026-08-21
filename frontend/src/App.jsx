import React, { useState, useEffect } from 'react';
import Login from './components/Login';
import Register from './components/Register';
import Dashboard from './pages/Dashboard';
import ForgotPassword from "./components/ForgotPassword";
import ResetPasswordPage from "./pages/ResetPasswordPage";
import { Toaster } from "react-hot-toast";
import VerifyEmailPage from '../src/pages/VerifyEmailPage';
import { logout } from "./api/bankService";

function App() {
  const [verificationToken, setVerificationToken] = useState(null);
  const [resetPasswordToken, setResetPasswordToken] = useState(null);
  const [accessToken, setAccessToken] = useState(localStorage.getItem('accessToken'));
  const [userRole, setUserRole] = useState(localStorage.getItem('userRole') || 'CUSTOMER');
  const [userName, setUserName] = useState(localStorage.getItem('fullName') || '');
  const [screen, setScreen] = useState('login');

  useEffect(() => {
    if (accessToken) {
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('userRole', userRole);

      if (userName) {
        localStorage.setItem('fullName', userName);
      }
    }
  }, [accessToken, userRole, userName]);

  useEffect(() => {

    const params = new URLSearchParams(window.location.search);
    const token = params.get("token");

    if (window.location.pathname === "/verify-email" && token) {
      setVerificationToken(token);
    }

    if (window.location.pathname === "/reset-password" && token) {
      setResetPasswordToken(token);
    }

  }, []);

  const handleLoginSuccess = () => {
    const storedToken = localStorage.getItem('accessToken');
    const storedRole = localStorage.getItem('userRole') || 'CUSTOMER';
    const storedName = localStorage.getItem('fullName') || '';

    setAccessToken(storedToken);
    setUserRole(storedRole);
    setUserName(storedName);
  };

  const handleLogout = async () => {

    const refreshToken =
      localStorage.getItem("refreshToken");

    try {

      if (refreshToken) {
        await logout(refreshToken);
      }

    } catch (error) {

      console.error(
        "Logout failed",
        error
      );

    } finally {

      localStorage.clear();

      setAccessToken(null);
      setUserRole('CUSTOMER');
      setUserName('');
      setScreen('login');

    }
  };

  if (resetPasswordToken) {
    return (
      <ResetPasswordPage
        token={resetPasswordToken}
      />
    );
  }

  if (!accessToken) {

    if (screen === "login") {
      return (
        <Login
          onLoginSuccess={handleLoginSuccess}
          onSwitchToRegister={() => setScreen("register")}
          onSwitchToForgotPassword={() => setScreen("forgot-password")}
        />
      );
    }

    if (screen === "register") {
      return (
        <Register
          onRegisterSuccess={() => setScreen("login")}
          onSwitchToLogin={() => setScreen("login")}
        />
      );
    }

    return (
      <ForgotPassword
        onBackToLogin={() => setScreen("login")}
      />
    );
  }

  return (
    <>
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

      <Dashboard
        userRole={userRole}
        userName={userName}
        onLogout={handleLogout}
      />
    </>
  );
}

export default App;
