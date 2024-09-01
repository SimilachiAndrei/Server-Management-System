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
    const inputBuffer = useRef(''); 

    useEffect(() => {
        const sessionId = sessionIdRef.current;

        // Initialize the xterm.js terminal
        const terminal = new Terminal({
            cursorBlink: true,
            rows: 24,
            cols: 80,
            convertEol: true,  // Convert EOL characters to the correct format
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
                    terminal.write(output);  // Write received output to the xterm terminal
                });
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
                            input: inputBuffer.current + '\n'
                        }),
                    });
                }
                inputBuffer.current = '';  // Clear buffer
            } else if (data === '\u0003') {  // Handle Ctrl+C
                if (stompClientRef.current && stompClientRef.current.connected) {
                    stompClientRef.current.publish({
                        destination: '/app/sendInput',
                        body: JSON.stringify({
                            sessionId: sessionId,
                            input: '\u0003'
                        }),
                    });
                }
                terminal.write('^C\n');
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