import React, { useState, useEffect } from 'react';
import { login, resendVerificationEmail } from '../api/bankService';
import { EyeIcon, EyeSlashIcon } from "@heroicons/react/24/outline";
import toast from "react-hot-toast";

const Login = ({
  onLoginSuccess,
  onSwitchToRegister,
  onSwitchToForgotPassword
}) => {
  const [role, setRole] = useState('CUSTOMER');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [showPassword, setShowPassword] = useState(false);
  const [showResend, setShowResend] = useState(false);
  const [countdown, setCountdown] = useState(0);
  const [sendingVerification, setSendingVerification] = useState(false);

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const data = await login({
        email,
        password,
      });

      const token = data.token || data.accessToken || data.jwt;
      const userRole = data.role || data.userRole || role;

      // 1. Check if backend sent name directly
      let userFullName = data.fullName || data.name || data.username;

      // 2. If missing, format email intelligently
      if (!userFullName && (data.email || email)) {
        const emailStr = data.email || email;
        const handle = emailStr.split('@')[0]; // e.g. "john.doe" or "adminfirstlastname"

        if (handle.includes('.')) {
          // If format is "first.last@gmail.com"
          userFullName = handle
            .split('.')
            .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
            .join(' '); // Output: "John Doe"
        } else {
          // Format "adminfirstlastname" -> "Adminfirstlastname"
          userFullName = handle.charAt(0).toUpperCase() + handle.slice(1);
        }
      }

      // 3. Save to localStorage immediately
      localStorage.setItem('token', token);
      localStorage.setItem('userRole', userRole);
      localStorage.setItem('fullName', userFullName);

      // 4. Trigger parent state update
      onLoginSuccess();
    } catch (err) {
      console.error('Login error:', err);
      const backendMessage = err.response?.data?.message || "Invalid email or password.";

      setError(backendMessage);

      setShowResend(backendMessage === "Please verify your email address before logging in.");

    }
    finally {
      setLoading(false);
    }
  };

  const handleResendVerification = async () => {

    if (!email) {
      toast.error("Please enter your email address.");
      return;
    }

    try {

      setSendingVerification(true);

      await resendVerificationEmail(email);

      toast.success("Verification email sent successfully.");

      setCountdown(60);

    } catch (err) {

      toast.error(
        err.response?.data?.message ||
        "Unable to resend verification email."
      );

    } finally {

      setSendingVerification(false);

    }

  };

  useEffect(() => {
    if (countdown <= 0) {
      return;
    }
    const timer = setInterval(() => {
      setCountdown(previous => previous - 1);
    }, 1000);
    return () => clearInterval(timer);
  }, [countdown]);

  const formattedCountdown =
    `${String(Math.floor(countdown / 60)).padStart(2, "0")}:${String(countdown % 60).padStart(2, "0")}`;

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <div style={styles.header}>
          <h2 style={styles.brand}>🏦 BankFlow</h2>
          <p style={styles.subtitle}>Sign in to your account</p>
        </div>

        {/* Role Selection Tabs */}
        <div style={styles.roleTabs}>
          <button
            type="button"
            style={{
              ...styles.roleTabBtn,
              backgroundColor: role === 'CUSTOMER' ? '#0d6360' : '#f3f4f6',
              color: role === 'CUSTOMER' ? '#ffffff' : '#4b5563',
            }}
            onClick={() => setRole('CUSTOMER')}
          >
            👤 Customer Login
          </button>
          <button
            type="button"
            style={{
              ...styles.roleTabBtn,
              backgroundColor: role === 'ADMIN' ? '#0d6360' : '#f3f4f6',
              color: role === 'ADMIN' ? '#ffffff' : '#4b5563',
            }}
            onClick={() => setRole('ADMIN')}
          >
            🛡️ Admin Login
          </button>
        </div>

        {error && <div style={styles.errorBox}>⚠️ {error}</div>}
        {showResend && (

          <div style={styles.resendContainer}>

            <p style={styles.resendText}>
              Didn't receive the verification email?
            </p>

            <button

              style={styles.linkBtn}

              disabled={
                sendingVerification ||
                countdown > 0
              }

              onClick={handleResendVerification}

            >

              {sendingVerification
                ? "Sending..."
                : countdown > 0
                  ? `Resend available in ${formattedCountdown}s`
                  : "Resend Verification Email"}

            </button>

          </div>

        )}

        <form onSubmit={handleLogin} style={styles.form}>
          <div style={styles.field}>
            <label style={styles.label}>Email Address</label>
            <input
              type="email"
              placeholder="e.g. name@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              style={styles.input}
            />
          </div>

          <div style={styles.field}>
            <label style={styles.label}>Password</label>

            <div style={{ position: "relative", width: "100%" }}>
              <input
                type={showPassword ? "text" : "password"}
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                style={{
                  ...styles.input,
                  width: "100%",
                  paddingRight: "48px",
                  boxSizing: "border-box",
                }}
              />

              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                style={{
                  position: "absolute",
                  top: "50%",
                  right: "14px",
                  transform: "translateY(-50%)",
                  border: "none",
                  background: "transparent",
                  padding: 0,
                  margin: 0,
                  cursor: "pointer",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  color: "#6b7280",
                }}
              >
                {showPassword ? (
                  <EyeSlashIcon style={{ width: 22, height: 22 }} />
                ) : (
                  <EyeIcon style={{ width: 22, height: 22 }} />
                )}
              </button>
            </div>
          </div>

          <div
            style={{
              display: "flex",
              justifyContent: "flex-end",
              marginTop: "-8px",
              marginBottom: "6px",
            }}
          >
            <button
              type="button"
              onClick={onSwitchToForgotPassword}
              style={styles.linkBtn}
            >
              Forgot Password?
            </button>
          </div>

          <button type="submit" disabled={loading} style={styles.submitBtn}>
            {loading ? 'Signing in...' : `Sign In as ${role === 'ADMIN' ? 'Admin' : 'Customer'}`}
          </button>
        </form>

        <div style={styles.footerPrompt}>
          <span>Don't have an account?</span>
          <button type="button" onClick={onSwitchToRegister} style={styles.linkBtn}>
            Register here
          </button>
        </div>
      </div>
    </div>
  );
};

const styles = {
  container: { display: 'flex', minHeight: '100vh', alignItems: 'center', justifyContent: 'center', backgroundColor: '#f9fafb', padding: '16px' },
  card: { backgroundColor: '#ffffff', borderRadius: '16px', padding: '32px', width: '100%', maxWidth: '420px', border: '1px solid #eef0ec', boxShadow: '0 4px 12px rgba(0,0,0,0.03)' },
  header: { textAlign: 'center', marginBottom: '24px' },
  brand: { fontSize: '24px', fontWeight: '800', fontFamily: 'Georgia, serif', color: '#0d6360', margin: '0 0 6px 0' },
  subtitle: { fontSize: '14px', color: '#6b7280', margin: 0 },
  roleTabs: { display: 'flex', gap: '8px', marginBottom: '20px', backgroundColor: '#f3f4f6', padding: '4px', borderRadius: '10px' },
  roleTabBtn: { flex: 1, border: 'none', padding: '10px', borderRadius: '8px', fontWeight: '700', fontSize: '13px', cursor: 'pointer', transition: 'all 0.2s' },
  form: { display: 'flex', flexDirection: 'column', gap: '16px' },
  field: { display: 'flex', flexDirection: 'column', gap: '6px' },
  label: { fontSize: '13px', fontWeight: '700', color: '#374151' },
  input: { padding: '11px 14px', borderRadius: '8px', border: '1px solid #d1d5db', fontSize: '14px', outline: 'none' },
  submitBtn: { backgroundColor: '#0d6360', color: '#ffffff', border: 'none', padding: '12px', borderRadius: '8px', fontWeight: '700', fontSize: '14px', cursor: 'pointer', marginTop: '4px' },
  errorBox: { backgroundColor: '#fee2e2', color: '#991b1b', padding: '10px 14px', borderRadius: '8px', fontSize: '13px', marginBottom: '16px' },
  footerPrompt: { display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '6px', marginTop: '20px', fontSize: '13px', color: '#6b7280' },
  linkBtn: { background: 'none', border: 'none', color: '#0d6360', fontWeight: '700', cursor: 'pointer', padding: 0, fontSize: '13px' },
};

export default Login;
