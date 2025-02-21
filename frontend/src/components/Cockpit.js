import React, { useEffect, useRef, useState } from "react";
import { useNavigate } from 'react-router-dom';
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";
import { Terminal } from "xterm";
import 'xterm/css/xterm.css';
import pageStyle from '../styles/Cockpit.module.css';
import { Doughnut } from 'react-chartjs-2';
import { Box } from '@mui/material';
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js';

ChartJS.register(ArcElement, Tooltip, Legend);

function CircularChart({ value, max, label, pstroke="#0877A1" }) {
    const radius = 50;
    const strokeWidth = 10;
    const circumference = 2 * Math.PI * radius;
    const progress = (value / max) * circumference;

    return (
        <div className={pageStyle.section}>
            <div className={pageStyle.label}>{label}</div>
            <svg width="120" height="120" viewBox="0 0 120 120" xmlns="http://www.w3.org/2000/svg" fill="none">
                <circle
                    cx="60"
                    cy="60"
                    r={radius}
                    stroke="#2D4150"
                    strokeWidth={strokeWidth}
                />
                <circle
                    cx="60"
                    cy="60"
                    r={radius}
                    stroke={pstroke}
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

function RamStatistics({ data }) {
    return (
        <Box>
            <Doughnut data={data} />
        </Box>
    );
}

function Cockpit() {
    const terminalRef = useRef(null);
    const terminalInstance = useRef(null);
    const stompClientRef = useRef(null);
    const navigate = useNavigate();
    

    const [cpuLoad, setCpuLoad] = useState('');
    const [ramUsage, setRamUsage] = useState('');
    const [diskUsage, setDiskUsage] = useState('');
    const [data, setData] = useState({
        labels: ['Total RAM', 'Used RAM', 'Free RAM'],
        datasets: [
            {
                data: [0, 0, 0],
                backgroundColor: ['#59253A', '#78244C', '#895061'],
            },
        ],
    });
    const [diskData, setDiskData] = useState(
        {
            labels: ['Total Disk Space', 'Used Disk Space', 'Free Disk Space'],
            datasets: [
                {
                    data: [0, 0, 0],
                    backgroundColor: ['#59253A', '#78244C', '#895061']
                }
            ]
        }
    );


    useEffect(() => {
        const handleConnect = async (computer) => {
            try {
              const response = await fetch('http://localhost:4000/api/endpoint/connect', {
                method: 'POST',
                headers: {
                  "Authorization": `Bearer ${sessionStorage.getItem('token')}`,
                  "Content-Type": "application/json"
                },
                body: JSON.stringify(computer)
              });
              const data = await response.json();
              if (response.ok) {
                navigate(`/cockpit?name=${computer.name}&ip=${computer.address}&port=${computer.port}`);
              } else {
                console.log('Connection failed');
              }
              console.log(data);
            } catch (error) {
              console.log(error);
            }
          };
        const urlParams = new URLSearchParams(window.location.search);
        const pcName = urlParams.get('name');

        const cleanup = () => {
            if (terminalInstance.current) {
                terminalInstance.current.dispose();
                terminalInstance.current = null;
            }
            if (stompClientRef.current && stompClientRef.current.connected) {
                stompClientRef.current.publish({
                    destination: `/app/terminateTerminal`,
                    body: JSON.stringify({
                        jwt: sessionStorage.getItem('token'),
                        name: pcName
                    })
                });
                stompClientRef.current.deactivate();
                // setTimeout(() => {
                    const urlParams = new URLSearchParams(window.location.search);
                    const name = urlParams.get('name');
                    const ip = urlParams.get('ip');
                    const port = parseInt(urlParams.get('port'));
                    if(name != null && ip != null && port != null)
                    {
                        const computer = {"name":name,"description":"","ip":ip,"port":port}
                        handleConnect(computer);
                    }
                //   }, 1000);
            }
        };
    
        // Add beforeunload listener for refresh
        window.addEventListener('beforeunload', cleanup);

        const timer = setTimeout(() => {
            if (terminalRef.current && !terminalInstance.current) {
                terminalInstance.current = new Terminal({
                    cursorBlink: true,
                    rows: 24,
                    cols: 80,
                    convertEol: false,
                    scrollback: 2000,  // Enable scrollback buffer
                    disableStdin: false,  // Allow input
                    mouseEvents: true,  // Enable mouse events
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
                        Authorization: `Bearer ${sessionStorage.getItem('token')}`,
                    },
                    onConnect: () => {
                        console.log('WebSocket connected');

                        // Subscribe to terminal output topic
                        stompClient.subscribe(`/topic/terminalOutput/${sessionStorage.getItem('token')}/${pcName}`, (message) => {
                            const output = message.body;
                            terminalInstance.current.write(output);
                        });

                        // Subscribe to CPU usage stats topic
                        stompClient.subscribe(`/topic/cpuUsage/${sessionStorage.getItem('token')}/${pcName}`, (message) => {
                            const output = JSON.parse(message.body);

                            const parseRamString = (ramString) => {
                                const value = parseFloat(ramString);
                                if (ramString.toLowerCase().includes('gb')) {
                                    return value * 1024;
                                }
                                return value;
                            };

                            const totalRamValue = parseRamString(output['Total RAM']);
                            const usedRamValue = parseRamString(output['Used RAM']);
                            const freeRamValue = parseRamString(output['Free RAM']);

                            const totalDiskValue = parseRamString(output['Total Disk Space']);
                            const usedDiskValue = parseRamString(output['Used Disk Space']);
                            const freeDiskValue = parseRamString(output['Free Disk Space']);

                            setCpuLoad(output['CPU Load'].toFixed(2));
                            setRamUsage(output['RAM usage'].toFixed(2));
                            setDiskUsage(output['Disk Usage' ].toFixed(2))

                            if (totalRamValue && usedRamValue && freeRamValue) {
                                setData({
                                    labels: ['Total RAM', 'Used RAM', 'Free RAM'],
                                    datasets: [
                                        {
                                            data: [totalRamValue, usedRamValue, freeRamValue],
                                            backgroundColor: ['#59253A', '#78244C', '#895061'],
                                        },
                                    ],
                                });
                            }

                            if(totalDiskValue && usedDiskValue && freeDiskValue)
                            {
                                setDiskData({
                                    labels: ['Total Disk Space', 'Used Disk Space', 'Free Disk Space'],
                                    datasets: [
                                        {
                                            data: [totalDiskValue, usedDiskValue, freeDiskValue],
                                            backgroundColor: ['#088F8F', '#0877A1', '#89CFF0']
                                        }
                                    ]
                                })
                            }
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
                    if (stompClientRef.current && stompClientRef.current.connected) {
                        stompClientRef.current.publish({
                            destination: `/app/sendInput`,
                            body: JSON.stringify({
                                jwt: sessionStorage.getItem('token'),
                                name: pcName,
                                input: data  // Send the input immediately
                            }),
                        });
                    }
                });

                // Enable mouse events for applications like htop
                terminalInstance.current.attachCustomKeyEventHandler(e => {
                    if (e.ctrlKey && e.key === 'c') {
                        return true;  // Let Ctrl+C pass through
                    }
                    if (e.type === 'scroll') {
                        return false;  // Prevent scroll events from becoming input
                    }
                    return true;  // Handle other events normally
                });

                // Handle binary data for mouse events
                terminalInstance.current.onBinary(event => {
                    if (stompClientRef.current && stompClientRef.current.connected) {
                        stompClientRef.current.publish({
                            destination: `/app/sendInput`,
                            body: JSON.stringify({
                                jwt: sessionStorage.getItem('token'),
                                name: pcName,
                                input: event  // Send the binary input directly
                            }),
                        });
                    }
                });
            }
        }, 100);

        // Cleanup on component unmount
        return () => {
            clearTimeout(timer);
            cleanup();
            window.removeEventListener('beforeunload', cleanup);    
        };
    }, [navigate]);

    return (
        <div className={pageStyle.page}>
            <div className={pageStyle['stat-group']}>
                <h2>System Stats:</h2>
                <div className={pageStyle.stats}>
                    <CircularChart value={cpuLoad} max={100} label="CPU Load" />
                    <CircularChart value={ramUsage} max={100} label="RAM Usage" pstroke="#00A36C"/>
                    <CircularChart value={diskUsage} max={100} label="Disk Usage" pstroke="#5D3FD3" />
                    <RamStatistics data={data}></RamStatistics>
                    <RamStatistics data={diskData}></RamStatistics>
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