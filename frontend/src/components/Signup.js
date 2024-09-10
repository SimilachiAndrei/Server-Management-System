import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import pageStyle from '../styles/Login.module.css';
import headerStyle from '../styles/header.module.css';
import footerStyle from '../styles/footer.module.css';

function Signup() {
    const navigate = useNavigate();
    const [username, setUsername] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState(null);

    const handleSubmit = async (event) => {
        event.preventDefault();
        try {
            const response = await fetch('http://localhost:4000/auth/signup', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ username, email, password }),
            });

            if (response.ok) {
                navigate('/');
            } else {
                const errorData = await response.json();
                setError(errorData.message);
            }
        } catch (err) {
            setError('An error occurred. Please try again.');
        }
    };

    return (
        <div className={pageStyle.page}>
            <div className={headerStyle.header}>
                <div className={headerStyle.title}>Cockpit</div>
            </div>
            <div className={pageStyle.content}>
                <div className={pageStyle.formContainer}>
                    <h2>Signup</h2>
                    {error && <div className='error'>{error}</div>}
                    <form onSubmit={handleSubmit}>
                        <div className='form-group'>
                            <label>Username</label>
                            <input
                                type='text'
                                name='username'
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                                required
                            />
                        </div>
                        <div className='form-group'>
                            <label>Email</label>
                            <input
                                type='email'
                                name='email'
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                required
                            />
                        </div>
                        <div className='form-group'>
                            <label>Password</label>
                            <input
                                type='password'
                                name='password'
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                required
                            />
                        </div>
                        <div className={pageStyle.buttonGroup}>
                            <button type='submit'>Sign Up</button>
                            <a href='/'>Login</a>
                        </div>
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

export default Signup;
