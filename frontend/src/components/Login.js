import React, { useState } from 'react';
// import { useNavigate } from 'react-router-dom';
import '../styles/Login.css';


function Login() {
    return (
        <div className='page'>
            <div className='header'>
                <div className='title'>Cockpit</div>
                </div>
            <div className='content'>
                <div className='formContainer'>
                    <h2>Login</h2>
                    <form>
                        <div className='form-group'>
                            <label>Username</label>
                            <input type='text'></input>
                        </div>
                        <div className='form-group'>
                            <label>Password</label>
                            <input type='text'></input>
                        </div>
                    </form>
                </div>
            </div>
            <div className='footer'>
                <div className='footer-content'>
                    All rights reserved to Similachi Andrei !
                </div>
            </div>
        </div>
    );
}

export default Login;
