import React, { useEffect, useState } from 'react';
import axios from 'axios';

const App = () => {
    const [greeting, setGreeting] = useState('');

    useEffect(() => {
        axios.get('http://localhost:4000/api/greeting')
            .then(response => {
                setGreeting(response.data);
            });
    }, []);

    return (
        <div>
            <h1>{greeting}</h1>
        </div>
    );
};

export default App;

