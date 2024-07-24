import React from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/Login.css';

function Login({ setIsAuthenticated }) {
    const navigate = useNavigate();

    const handleSubmit = (event) => {
        event.preventDefault();
        // Add your login logic here (e.g., authentication)
        // If authentication is successful, set authentication state and navigate to /dashboard
        setIsAuthenticated(true);
        navigate('/dashboard');
    };

    return (
        <div className='page'>
            <div className='header'>
                <div className='title'>Cockpit</div>
            </div>
            <div className='content'>
                <div className='formContainer'>
                    <h2>Login</h2>
                    <form onSubmit={handleSubmit}>
                        <div className='form-group'>
                            <label>Username</label>
                            <input type='text' name='username' required></input>
                        </div>
                        <div className='form-group'>
                            <label>Password</label>
                            <input type='password' name='password' required></input>
                        </div>
                        <button type='submit'>Login</button>
                    </form>
                </div>
            </div>
            <div className='footer'>
                <div className='footer-content'>
                    All rights reserved to Similachi Andrei!
                </div>
            </div>
        </div>
    );
}

export default Login;
