import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import pageStyle from '../styles/Login.module.css';
import headerStyle from '../styles/header.module.css';
import footerStyle from '../styles/footer.module.css';

function Login() {
    const navigate = useNavigate();
    const [identifier, setIdentifier] = useState('');
    const [password, setPassword] = useState('');

    const handleSubmit = async (event) => {
        event.preventDefault();
        try {
            const response = await fetch('http://localhost:4000/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ identifier, password })
            });

            if (response.ok) {
                const { token, expiresIn } = await response.json(); 
                const expirationTime = Date.now() + expiresIn;
                localStorage.setItem('token', token);
                localStorage.setItem('expiresIn', expirationTime);
                
                // Wait for the state to be updated before navigating
                await new Promise(resolve => setTimeout(resolve, 0));
                navigate('/dashboard');
            } else {
                const errorData = await response.json();
                console.log(errorData.message);
            }
        } catch (err) {
            console.log(err);
        }
    };


    return (
        <div className={pageStyle.page}>
            <div className={headerStyle.header}>
                <div className={headerStyle.title}>Cockpit</div>
            </div>
            <div className={pageStyle.content}>
                <div className={pageStyle.formContainer}>
                    <h2>Login</h2>
                    <form onSubmit={handleSubmit}>
                        <div className='form-group'>
                            <label>Username</label>
                            <input
                                type='text'
                                name='username'
                                value={identifier}
                                onChange={(e) => setIdentifier(e.target.value)}
                                required></input>
                        </div>
                        <div className='form-group'>
                            <label>Password</label>
                            <input
                                type='password'
                                name='password'
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                required></input>
                        </div>
                        <button type='submit'>Login</button>
                    </form>
                </div>
            </div>
            <div className={footerStyle.footer}>
                <div className='footer-content'>
                    All rights reserved to Similachi Andrei!
                </div>
            </div>
        </div>
    );
}

export default Login;
