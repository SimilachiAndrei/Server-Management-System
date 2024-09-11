import React, { useEffect, useRef, useState } from "react";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";
import { Terminal } from "xterm";
import 'xterm/css/xterm.css';
import pageStyle from '../styles/Cockpit.module.css';

function Cockpit() {
    const terminalRef = useRef(null);
    const terminalInstance = useRef(null);
    const inputBuffer = useRef('');
    const stompClientRef = useRef(null);
    // const [stats, setStats] = useState('');

    const [cpuLoad, setCpuLoad] = useState('');
    const [ramUsage, setRamUsage] = useState('');
    const [totalRam, setTotalRam] = useState('');
    const [usedRam, setUsedRam] = useState('');
    const [freeRam, setFreeRam] = useState('');




    useEffect(() => {
        // Initialize terminal with a delay
        const timer = setTimeout(() => {
            if (terminalRef.current && !terminalInstance.current) {
                terminalInstance.current = new Terminal({
                    cursorBlink: true,
                    rows: 24,
                    cols: 80,
                    convertEol: true,
                    theme: {
                        background: '#59253A'
                    }
                });

                terminalInstance.current.open(terminalRef.current);

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


                            terminalInstance.current.write(output);
                        });

                        // Subscribe to CPU usage stats topic
                        stompClient.subscribe('/topic/cpuUsage', (message) => {
                            const output = JSON.parse(message.body);
                            console.log(output);
                            setCpuLoad(output['CPU Load']);
                            setRamUsage(output['RAM usage']);
                            setTotalRam(output['Total RAM']);
                            setUsedRam(output['Used RAM']);
                            setFreeRam(output['Free RAM']);
                            // setStats(output);
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
                terminalInstance.current.onData(data => {
                    if (data === '\r') {  // Enter key pressed
                        if (stompClientRef.current && stompClientRef.current.connected) {
                            stompClientRef.current.publish({
                                destination: '/app/sendInput',
                                body: JSON.stringify({
                                    jwt: localStorage.getItem('token'),
                                    input: inputBuffer.current + '\n'
                                }),
                            });
                        }
                        terminalInstance.current.write('\r\n');  // Move to the next line
                        inputBuffer.current = '';  // Clear buffer
                    } else if (data === '\u0003') {  // Handle Ctrl+C
                        if (stompClientRef.current && stompClientRef.current.connected) {
                            stompClientRef.current.publish({
                                destination: '/app/sendInput',
                                body: JSON.stringify({
                                    jwt: localStorage.getItem('token'),
                                    input: '\u0003'
                                }),
                            });
                        }
                        terminalInstance.current.write('^C\r\n');
                        inputBuffer.current = '';  // Clear buffer
                    } else if (data === '\u007F') {  // Handle backspace
                        if (inputBuffer.current.length > 0) {
                            inputBuffer.current = inputBuffer.current.slice(0, -1);
                            terminalInstance.current.write('\b \b');  // Erase character on the terminal
                        }
                    } else {
                        inputBuffer.current += data;
                        terminalInstance.current.write(data);
                    }
                });
            }
        }, 100);

        return () => {
            clearTimeout(timer);
            if (terminalInstance.current) {
                terminalInstance.current.dispose();
                terminalInstance.current = null;
            }
            if (stompClientRef.current) {
                if (stompClientRef.current.connected) {
                    stompClientRef.current.publish({
                        destination: '/app/terminateTerminal',
                        body: localStorage.getItem('token')
                    });
                }
                stompClientRef.current.deactivate();
            }
        };
    }, []);

    return (
        <div className={pageStyle.page}>
            <div className={pageStyle['stat-group']}>
                <h2>System Stats:</h2>
                <pre className={pageStyle.stats}>
                    <div>{cpuLoad}</div>
                    <div>{ramUsage}</div>
                    <div>{totalRam}</div>
                    <div>{usedRam}</div>
                    <div>{freeRam}</div>
                </pre>
            </div>
            <div className={pageStyle['terminal-group']}>
                <h1>Remote Terminal Access</h1>
                <div ref={terminalRef} className={pageStyle.terminal} />
            </div>
        </div>
    );
}

export default Cockpit;