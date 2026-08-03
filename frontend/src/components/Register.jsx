import React, { useState } from 'react';
import API from '../api/axios';
import { Eye, EyeOff } from "lucide-react";
import { EyeIcon, EyeSlashIcon } from "@heroicons/react/24/outline";

const Register = ({ onRegisterSuccess, onSwitchToLogin }) => {
  const [role, setRole] = useState('CUSTOMER'); // 'CUSTOMER' or 'ADMIN'
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [adminToken, setAdminToken] = useState(''); // Required for admin creation
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);
  const [showPassword, setShowPassword] = useState(false);
  const [showAdminToken, setShowAdminToken] = useState(false);

  const handleRegister = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setSuccessMsg(null);

    try {
      if (role === 'CUSTOMER') {
        // Customer Register Endpoint
        await API.post('/auth/register', {
          email,
          fullName,
          password,
        });
      } else {
        // Admin Register Endpoint (requires Super Admin Bearer token)
        await API.post(
          '/admin/users/create-admin',
          {
            email,
            fullName,
            password,
          },
          {
            headers: {
              Authorization: `Bearer ${adminToken.trim()}`,
            },
          }
        );
      }

      setLoading(false);
      setSuccessMsg('Registration successful! Redirecting to login...');
      setTimeout(() => {
        onRegisterSuccess();
      }, 1500);
    } catch (err) {
      console.error('Registration error:', err);
      setError(
        err.response?.data?.message ||
        'Failed to register. Please check your inputs or authorization token.'
      );
      setLoading(false);
    }
  };

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <div style={styles.header}>
          <h2 style={styles.brand}>🏦 BankFlow</h2>
          <p style={styles.subtitle}>Create a new account</p>
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
            👤 Customer Register
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
            🛡️ Admin Register
          </button>
        </div>

        {error && <div style={styles.errorBox}>⚠️ {error}</div>}
        {successMsg && <div style={styles.successBox}>✅ {successMsg}</div>}

        <form onSubmit={handleRegister} style={styles.form}>
          <div style={styles.field}>
            <label style={styles.label}>Full Name</label>
            <input
              type="text"
              placeholder="e.g. Firstname Lastname"
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              required
              style={styles.input}
            />
          </div>

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

            <div
              style={{
                position: "relative",
                width: "100%",
              }}
            >
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

          {/* Super Admin Token Field (Only shown for Admin Registration) */}
          {role === 'ADMIN' && (
            <div style={styles.field}>
              <label style={styles.label}>Admin Token</label>

              <div
                style={{
                  position: "relative",
                  width: "100%",
                }}
              >
                <input
                  type={showAdminToken ? "text" : "password"}
                  placeholder="super-admin-token"
                  value={adminToken}
                  onChange={(e) => setAdminToken(e.target.value)}
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
                  onClick={() => setShowAdminToken(!showAdminToken)}
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
                  {showAdminToken ? (
                    <EyeSlashIcon style={{ width: 22, height: 22 }} />
                  ) : (
                    <EyeIcon style={{ width: 22, height: 22 }} />
                  )}
                </button>
              </div>
            </div>
          )}

          <button type="submit" disabled={loading} style={styles.submitBtn}>
            {loading ? 'Creating Account...' : `Register as ${role === 'ADMIN' ? 'Admin' : 'Customer'}`}
          </button>
        </form>

        {/* Login Prompt */}
        <div style={styles.footerPrompt}>
          <span>Already have an account?</span>
          <button type="button" onClick={onSwitchToLogin} style={styles.linkBtn}>
            Sign in here
          </button>
        </div>
      </div>
    </div>
  );
};

const styles = {
  container: { display: 'flex', minHeight: '100vh', alignItems: 'center', justifyContent: 'center', backgroundColor: '#f9fafb', padding: '16px' },
  card: { backgroundColor: '#ffffff', borderRadius: '16px', padding: '32px', width: '100%', maxWidth: '440px', border: '1px solid #eef0ec', boxShadow: '0 4px 12px rgba(0,0,0,0.03)' },
  header: { textAlign: 'center', marginBottom: '24px' },
  brand: { fontSize: '24px', fontWeight: '800', fontFamily: 'Georgia, serif', color: '#0d6360', margin: '0 0 6px 0' },
  subtitle: { fontSize: '14px', color: '#6b7280', margin: 0 },
  roleTabs: { display: 'flex', gap: '8px', marginBottom: '20px', backgroundColor: '#f3f4f6', padding: '4px', borderRadius: '10px' },
  roleTabBtn: { flex: 1, border: 'none', padding: '10px', borderRadius: '8px', fontWeight: '700', fontSize: '13px', cursor: 'pointer', transition: 'all 0.2s' },
  form: { display: 'flex', flexDirection: 'column', gap: '16px' },
  field: { display: 'flex', flexDirection: 'column', gap: '6px' },
  label: { fontSize: '13px', fontWeight: '700', color: '#374151' },
  input: { padding: '11px 14px', borderRadius: '8px', border: '1px solid #d1d5db', fontSize: '14px', outline: 'none' },
  helperText: { fontSize: '11px', color: '#6b7280' },
  submitBtn: { backgroundColor: '#0d6360', color: '#ffffff', border: 'none', padding: '12px', borderRadius: '8px', fontWeight: '700', fontSize: '14px', cursor: 'pointer', marginTop: '4px' },
  errorBox: { backgroundColor: '#fee2e2', color: '#991b1b', padding: '10px 14px', borderRadius: '8px', fontSize: '13px', marginBottom: '16px' },
  successBox: { backgroundColor: '#dcfce7', color: '#15803d', padding: '10px 14px', borderRadius: '8px', fontSize: '13px', marginBottom: '16px' },
  footerPrompt: { display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '6px', marginTop: '20px', fontSize: '13px', color: '#6b7280' },
  linkBtn: { background: 'none', border: 'none', color: '#0d6360', fontWeight: '700', cursor: 'pointer', padding: 0, fontSize: '13px' },
};

export default Register;
