import React, { useEffect, useRef } from 'react';
import { Terminal } from 'xterm';
import 'xterm/css/xterm.css';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const Cockpit = () => {
    const terminalRef = useRef(null);
    const xtermRef = useRef(null);
    const stompClientRef = useRef(null);
    const sessionIdRef = useRef(`session-${Math.random().toString(36).substr(2, 9)}`);
    const inputBuffer = useRef('');  // Buffer for storing input

    useEffect(() => {
        const sessionId = sessionIdRef.current;

        // Initialize the xterm.js terminal
        const terminal = new Terminal({
            cursorBlink: true,
            rows: 24,
            cols: 80,
        });
        terminal.open(terminalRef.current);
        xtermRef.current = terminal;

        // Setup WebSocket connection with SockJS and STOMP
        const socket = new SockJS('http://localhost:4000/ws');
        const stompClient = new Client({
            webSocketFactory: () => socket,
            debug: (str) => console.log(str),
            reconnectDelay: 5000,
            connectHeaders: {
                Authorization: `Bearer ${localStorage.getItem('token')}`,
            },
            onConnect: () => {
                console.log('WebSocket connected');

                // Subscribe to terminal output topic
                stompClient.subscribe('/topic/terminalOutput', (message) => {
                    const output = message.body;
                    console.log('Terminal Output:\n', output);  // Debugging
                    terminal.write(output);  // Write received output to the xterm terminal
                });

                // Subscribe to CPU usage topic
                // stompClient.subscribe('/topic/cpuUsage', (message) => {
                //     const cpuUsage = message.body;
                //     console.log('Computer Data:\n', cpuUsage);  // For debugging
                //     // If needed, you can display CPU usage in the UI
                // });
            },
            onDisconnect: () => {
                console.log('WebSocket disconnected');
            },
            onStompError: (frame) => {
                console.error('Broker reported error: ' + frame.headers['message']);
                console.error('Additional details: ' + frame.body);
            }
        });

        stompClient.activate();
        stompClientRef.current = stompClient;

        // Handle terminal input
        terminal.onData(data => {
            if (data === '\r') {  // Enter key pressed
                if (stompClientRef.current && stompClientRef.current.connected) {
                    stompClientRef.current.publish({
                        destination: '/app/sendInput',
                        body: JSON.stringify({
                            sessionId: sessionId,
                            input: inputBuffer.current
                        }),
                    });
                }
                inputBuffer.current = '';  // Clear buffer
            } else if (data === '\u007F') {  // Handle backspace
                if (inputBuffer.current.length > 0) {
                    inputBuffer.current = inputBuffer.current.slice(0, -1);
                    terminal.write('\b \b');  // Erase character on the terminal
                }
            } else {
                inputBuffer.current += data;  // Accumulate input
                terminal.write(data);  // Echo the input
            }
        });

        // Cleanup on component unmount
        return () => {
            if (stompClientRef.current && stompClientRef.current.connected) {
                stompClientRef.current.publish({
                    destination: '/app/terminateTerminal',
                    body: sessionId
                });
            }
            stompClientRef.current.deactivate();
        };
    }, []);

    return (
        <div>
            <h1>Remote Terminal Access</h1>
            <div ref={terminalRef} style={{ height: '400px', width: '800px', border: '1px solid black' }} />
        </div>
    );
};

export default Cockpit;