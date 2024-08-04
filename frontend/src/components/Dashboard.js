import React from 'react';
import headerStyle from '../styles/header.module.css';
import footerStyle from '../styles/footer.module.css';
import pageStyle from '../styles/Dashboard.module.css';

function Dashboard() {
  return (
    <div className={pageStyle.page}>

      <div className={headerStyle.header}>
        <div className={headerStyle.title}>Cockpit</div>
      </div>

      <div className={pageStyle.content}>
        <div className={pageStyle.formContainer}>
          <h3>Computer Panel</h3>
          <form>
            <div className={pageStyle['form-group']}>
              <label>Name</label>
              <input type='text' name='name'></input>
            </div>
            <div className={pageStyle['form-group']}>
              <label>Description</label>
              <input type='text' name='description'></input>
            </div>
            <div className={pageStyle['form-group']}>
              <label>IP Address</label>
              <input
                type='text'
                name='ipAddress'
                pattern="^(?:[0-9]{1,3}\.){3}[0-9]{1,3}$"
                title="Please enter a valid IP address (e.g., 192.168.0.1)"
                required
              ></input>
            </div>
            <button type='submit'>Add</button>
          </form>
        </div>
        <div className={pageStyle.computers}>
          <div className={pageStyle['computer-container']}>
            <div className={pageStyle['computer-container-name']}>AndreiPC</div>
            <div className={pageStyle['computer-container-description']}>Chestie</div>
            <div className={pageStyle['computer-container-Ip Address']}>127.0.0.1</div>
            <button>Connect</button>
          </div>
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
