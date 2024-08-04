import { useState, useEffect } from 'react';
import headerStyle from '../styles/header.module.css';
import footerStyle from '../styles/footer.module.css';
import pageStyle from '../styles/Dashboard.module.css';

function Dashboard() {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [address, setAddress] = useState('');
  const [computers, setComputers] = useState([]);

  // Fetch computers from the database
  const fetchComputers = async () => {
    try {
      const response = await fetch('http://localhost:4000/api/endpoint/getAll', {
        method: 'GET',
        headers: {
          "Authorization": `Bearer ${localStorage.getItem('token')}`,
          "Content-Type": "application/json"
        }
      });
      const data = await response.json();
      setComputers(data);
    } catch (error) {
      console.log(error);
    }
  };

  useEffect(() => {
    fetchComputers();
  }, []);

  // Handle adding a new computer
  const handleAdd = async (event) => {
    event.preventDefault();
    try {
      const response = await fetch('http://localhost:4000/api/endpoint/add', {
        method: 'POST',
        headers: {
          "Authorization": `Bearer ${localStorage.getItem('token')}`,
          "Content-Type": "application/json"
        },
        body: JSON.stringify({ name, description, address })
      });
      if (response.ok) {
        fetchComputers();
      }
    } catch (error) {
      console.log(error);
    }
  };

  // Handle connecting to a computer
  const handleConnect = async (computer) => {
    try {
      const response = await fetch('http://localhost:4000/api/endpoint/connect', {
        method: 'POST',
        headers: {
          "Authorization": `Bearer ${localStorage.getItem('token')}`,
          "Content-Type": "application/json"
        },
        body: JSON.stringify(computer)
      });
      const data = await response.json();
      console.log(data);
    } catch (error) {
      console.log(error);
    }
  };

  return (
    <div className={pageStyle.page}>
      <div className={headerStyle.header}>
        <div className={headerStyle.title}>Cockpit</div>
      </div>

      <div className={pageStyle.content}>
        <div className={pageStyle.formContainer}>
          <h3>Computer Panel</h3>
          <form onSubmit={handleAdd}>
            <div className={pageStyle['form-group']}>
              <label>Name</label>
              <input type='text' name='name' value={name} onChange={(e) => setName(e.target.value)} required></input>
            </div>
            <div className={pageStyle['form-group']}>
              <label>Description</label>
              <input type='text' name='description' value={description} onChange={(e) => setDescription(e.target.value)} required></input>
            </div>
            <div className={pageStyle['form-group']}>
              <label>IP Address</label>
              <input
                type='text'
                name='address'
                pattern="^(?:[0-9]{1,3}\.){3}[0-9]{1,3}$"
                title="Please enter a valid IP address (e.g., 192.168.0.1)"
                value={address}
                onChange={(e) => setAddress(e.target.value)}
                required
              ></input>
            </div>
            <button type='submit'>Add</button>
          </form>
        </div>
        <div className={pageStyle.computers}>
          {computers.map((computer) => (
            <div key={computer.id} className={pageStyle['computer-container']}>
              <div className={pageStyle['computer-container-name']}>{computer.name}</div>
              <div className={pageStyle['computer-container-description']}>{computer.description}</div>
              <div className={pageStyle['computer-container-IpAddress']}>{computer.address}</div>
              <button onClick={() => handleConnect(computer)}>Connect</button>
            </div>
          ))}
        </div>
      </div>

      <div className={footerStyle.footer}>
        <div className={footerStyle['footer-content']}>
          All rights reserved to Similachi Andrei!
        </div>
      </div>
    </div>
  );
}

export default Dashboard;
