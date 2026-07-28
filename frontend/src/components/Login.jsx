import React, { useState } from 'react';
import API from '../api/axios';

const Login = ({ onLoginSuccess, onLogin }) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const response = await API.post('/auth/login', { email, password });
      const data = response.data;

      // Persist token and registered user profile details
      if (data.token) localStorage.setItem('token', data.token);
      if (data.role) localStorage.setItem('userRole', data.role);
      
      const fullName = data.fullName || data.name || data.username || email.split('@')[0];
      if (fullName) localStorage.setItem('fullName', fullName);

      // Trigger callback
      const callback = onLoginSuccess || onLogin;
      if (callback) callback(data);
    } catch (err) {
      console.error('Login error:', err);
      setError(
        err.response?.data?.message || 'Invalid email or password. Please try again.'
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <div style={styles.header}>
          <h2 style={styles.brand}>🏦 BankFlow</h2>
          <p style={styles.subtitle}>Sign in to your banking account</p>
        </div>

        {error && <div style={styles.errorBox}>⚠️ {error}</div>}

        <form onSubmit={handleSubmit} style={styles.form}>
          <div style={styles.inputGroup}>
            <label style={styles.label}>Email Address</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="name@example.com"
              required
              style={styles.input}
            />
          </div>

          <div style={styles.inputGroup}>
            <label style={styles.label}>Password</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              required
              style={styles.input}
            />
          </div>

          <button type="submit" disabled={loading} style={styles.button}>
            {loading ? 'Authenticating...' : 'Sign In'}
          </button>
        </form>
      </div>
    </div>
  );
};

const styles = {
  container: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    minHeight: '100vh',
    backgroundColor: '#f9fafb',
  },
  card: {
    backgroundColor: '#ffffff',
    padding: '36px',
    borderRadius: '16px',
    boxShadow: '0 4px 20px rgba(0, 0, 0, 0.08)',
    width: '100%',
    maxWidth: '400px',
    border: '1px solid #eef0ec',
  },
  header: { textAlign: 'center', marginBottom: '24px' },
  brand: { margin: 0, fontSize: '26px', fontFamily: 'Georgia, serif', color: '#0d6360' },
  subtitle: { margin: '6px 0 0 0', fontSize: '13px', color: '#6b7280' },
  form: { display: 'flex', flexDirection: 'column', gap: '16px' },
  inputGroup: { display: 'flex', flexDirection: 'column', gap: '6px' },
  label: { fontSize: '13px', fontWeight: '600', color: '#374151' },
  input: {
    padding: '10px 14px',
    borderRadius: '8px',
    border: '1px solid #d1d5db',
    fontSize: '14px',
    outline: 'none',
  },
  button: {
    backgroundColor: '#0d6360',
    color: '#ffffff',
    padding: '12px',
    borderRadius: '8px',
    border: 'none',
    fontWeight: '700',
    fontSize: '14px',
    cursor: 'pointer',
    marginTop: '8px',
  },
  errorBox: {
    backgroundColor: '#fee2e2',
    color: '#991b1b',
    padding: '10px 14px',
    borderRadius: '8px',
    fontSize: '13px',
    marginBottom: '16px',
    border: '1px solid #fca5a5',
  },
};

export default Login;
