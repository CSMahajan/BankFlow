import React, { useEffect, useState } from 'react';
import axios from 'axios';

const Dashboard = () => {
  const [dashboardData, setDashboardData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        const token = localStorage.getItem('token'); 

        const response = await axios.get('/api/v1/dashboard/summary', {
          headers: {
            Authorization: token ? `Bearer ${token}` : '',
            'Content-Type': 'application/json',
          },
        });

        console.log('API Response Data:', response.data); // Log data to console for inspection
        setDashboardData(response.data);
        setLoading(false);
      } catch (err) {
        console.error('Failed to fetch dashboard data:', err);
        setError('Unable to load dashboard data.');
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  if (loading) {
    return <div style={{ padding: '20px' }}>Loading Dashboard...</div>;
  }

  if (error) {
    return <div style={{ padding: '20px', color: 'red' }}>{error}</div>;
  }

  return (
    <div style={{ padding: '20px', fontFamily: 'sans-serif' }}>
      <h1>Dashboard Summary</h1>
      
      {/* Safely renders the API JSON response on the screen */}
      {dashboardData ? (
        <pre style={{ 
          backgroundColor: '#f4f4f4', 
          padding: '15px', 
          borderRadius: '5px',
          overflowX: 'auto' 
        }}>
          {JSON.stringify(dashboardData, null, 2)}
        </pre>
      ) : (
        <p>No data returned from backend.</p>
      )}
    </div>
  );
};

export default Dashboard;
