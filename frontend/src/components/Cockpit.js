import React, { useState, useEffect } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const DashboardPage = () => {
    const [cpuData, setCpuData] = useState('');
    const [command, setCommand] = useState('');
    const [response, setResponse] = useState('');
    const [client, setClient] = useState(null);

    useEffect(() => {
        const socket = new SockJS('http://localhost:4000/ws');
        const stompClient = new Client({
            webSocketFactory: () => socket,
            debug: (str) => {
                console.log(str);
            },
            reconnectDelay: 5000,
            connectHeaders: {
                Authorization: `Bearer ${localStorage.getItem('token')}`,
            },
        });

        stompClient.onConnect = () => {
            console.log('Connected to WebSocket');
            stompClient.subscribe('/topic/commandResponse', (message) => {
                setResponse(message.body);
            });

        };
        
        stompClient.onConnect = () => {
          console.log('Connected to WebSocket');
          stompClient.subscribe('/topic/cpuUsage', (message) => {
              setCpuData(message.body);
          });
      };

        stompClient.onDisconnect = () => {
            console.log('Disconnected from WebSocket');
        };

        stompClient.activate();
        setClient(stompClient);

        return () => {
            if (client && client.connected) {
                client.deactivate();
            }
        };
    }, []);

    const handleCommandSubmit = () => {
        if (client && client.connected) {
            client.publish({ destination: '/app/sendCommand', body: command });
            setCommand('');
        }
    };

    return (
        <div>
            <h1>Cockpit</h1>
            <div>
                <h2>CPU & Memory Usage</h2>
                <pre>{cpuData}</pre>
            </div>
            <div>
                <h2>Send Command</h2>
                <input
                    type="text"
                    value={command}
                    onChange={(e) => setCommand(e.target.value)}
                    placeholder="Enter command"
                />
                <button onClick={handleCommandSubmit}>Send</button>
            </div>
            <div>
                <h2>Command Response</h2>
                <pre>{response}</pre>
            </div>
        </div>
    );
};

export default DashboardPage;
