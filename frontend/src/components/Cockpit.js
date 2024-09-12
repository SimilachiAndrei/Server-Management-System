import React, { useEffect, useRef, useState } from "react";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";
import { Terminal } from "xterm";
import 'xterm/css/xterm.css';
import pageStyle from '../styles/Cockpit.module.css';



function CircularChart({ value, max, label }) {
    const radius = 50; // Radius of the circle
    const strokeWidth = 10; // Stroke width for the circle
    const circumference = 2 * Math.PI * radius; // Circumference of the circle
    const progress = (value / max) * circumference; // Calculate progress

    return (
        <div className={pageStyle.section}>
            <div className={pageStyle.label}>{label}</div>
            <svg width="120" height="120" viewBox="0 0 120 120" xmlns="http://www.w3.org/2000/svg" fill="none">
                <circle
                    cx="60"
                    cy="60"
                    r={radius}
                    stroke="#e6e6e6"
                    strokeWidth={strokeWidth}
                />
                <circle
                    cx="60"
                    cy="60"
                    r={radius}
                    stroke="green"
                    strokeWidth={strokeWidth}
                    strokeDasharray={circumference}
                    strokeDashoffset={circumference - progress}
                    strokeLinecap="round"
                    transform="rotate(-90 60 60)"
                />

                <text
                    x="50%"
                    y="50%"
                    dominantBaseline="middle"
                    textAnchor="middle"
                    fontSize="20"
                    fill="#000"
                >
                    {value}%
                </text>
            </svg>
        </div>
    );
}


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

                            const parseRamString = (ramString) => {
                                const value = parseFloat(ramString);
                                if (ramString.toLowerCase().includes('gb')) {
                                    return value * 1024;
                                }
                                return value;
                            };
                            
                            setCpuLoad(output['CPU Load'].toFixed(2));
                            setRamUsage(output['RAM usage'].toFixed(2));
                            setTotalRam(parseRamString(output['Total RAM']));
                            setUsedRam(parseRamString(output['Used RAM']));
                            setFreeRam(parseRamString(output['Free RAM']));
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
                <div className={pageStyle.stats}>
                    <CircularChart value={cpuLoad} max={100} label="CPU Load" />
                    <CircularChart value={ramUsage} max={100} label="RAM Usage" />
                    {/* <CircularChart value={((usedRam / totalRam) * 100).toFixed(2)} max={100} label="Used RAM" />
                    <CircularChart value={((freeRam / totalRam) * 100).toFixed(2)} max={100} label="Free RAM" /> */}
                </div>
            </div>
            <div className={pageStyle['terminal-group']}>
                <h1>Remote Terminal Access</h1>
                <div ref={terminalRef} className={pageStyle.terminal} />
            </div>
        </div>
    );
}

export default Cockpit;