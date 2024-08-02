import React from 'react';
import { BrowserRouter as Router, Route, Routes, Navigate } from 'react-router-dom';
import Login from './components/Login';
import Dashboard from './components/Dashboard';
import Signup from './components/Signup';

const isTokenValid = () => {
    const token = localStorage.getItem('token');
    const expiresIn = localStorage.getItem('expiresIn');

    if (token && expiresIn) {
        const currentTime = Date.now();
        return currentTime < parseInt(expiresIn, 10);
    }

    return false;
};

function App() {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<Login />} />
                <Route path="/signup" element={<Signup />} />
                <Route
                    path="/dashboard"
                    element={isTokenValid() ? <Dashboard /> : <Navigate to="/" />}
                />
            </Routes>
        </Router>
    );
}

export default App;
