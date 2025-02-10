import React from 'react';
import { BrowserRouter as Router, Route, Routes, Navigate } from 'react-router-dom';
import Login from './components/Login';
import Dashboard from './components/Dashboard';
import Signup from './components/Signup';
import Cockpit from './components/Cockpit';


const isTokenValid = () => {
    const token = localStorage.getItem('token');
    const expiresIn = localStorage.getItem('expiresIn');

    if (token && expiresIn) {
        const currentTime = Date.now();
        return currentTime < parseInt(expiresIn, 10);
    }
    localStorage.clear();
    sessionStorage.clear();
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
                <Route
                    path="/cockpit"
                    element={isTokenValid() ? <Cockpit /> : <Navigate to="/" />}
                />
            </Routes>
        </Router>
    );
}

export default App;
