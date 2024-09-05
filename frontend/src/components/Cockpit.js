import React, { useEffect, useRef } from 'react';
import { Terminal } from 'xterm';
import 'xterm/css/xterm.css';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const Cockpit = () => {
    const terminalRef = useRef(null);
    const sessionId = `session-${Math.random().toString(36).substr(2, 9)}`;
    const inputBuffer = useRef('');
    const stompClientRef = useRef(null);

    useEffect(() => {
        if (terminalRef.current.children.length > 0) {
            console.log("Terminal already initialized");
            return;
        }

        const terminal = new Terminal({
            cursorBlink: true,
            rows: 24,
            cols: 80,
            convertEol: true,
        });
        terminal.open(terminalRef.current);

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
                    terminal.write(output);
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
                terminal.write('\r\n');  // Move to the next line
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
                terminal.write('^C\r\n');
                inputBuffer.current = '';  // Clear buffer
            } else if (data === '\u007F') {  // Handle backspace
                if (inputBuffer.current.length > 0) {
                    inputBuffer.current = inputBuffer.current.slice(0, -1);
                    terminal.write('\b \b');  // Erase character on the terminal
                }
            } else {
                inputBuffer.current += data;
                terminal.write(data);
            }
        });

        return () => {
            if (stompClient.connected) {
                stompClient.publish({
                    destination: '/app/terminateTerminal',
                    body: sessionId
                });
                stompClient.deactivate();
            }
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