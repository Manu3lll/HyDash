# 🚀 HyDash - Hytale Server Web Dashboard

**HyDash** is a lightweight, high-performance web interface for Hytale servers. It allows administrators to monitor their server via a browser, track logs in real-time, and execute commands remotely—from anywhere.

<p align="center">
    <img src="https://img.shields.io/badge/Java-21%2B-orange">
    <a href="https://hytale.com/">
        <img src="https://img.shields.io/badge/Hytale-Server-green"></a>
    <img src="https://img.shields.io/badge/License-MIT-blue" alt="sdf">
    <a href="https://www.paypal.com/paypalme/hmanuel02" alt="Backers on Open Collective">
        <img src="https://shields.io/badge/paypal-donate-blue?logo=paypal&style=for-the-badgePayPal" /></a>
    <a href="https://buymeacoffee.com/manu.hiller?">
    <img src="https://img.shields.io/badge/-buy_me_a%C2%A0coffee-gray?logo=buy-me-a-coffee">    
</a>
</p><img width="1920" height="911" alt="2026-01-18 21_55_29-HyDash – config json  HyDash" src="https://github.com/user-attachments/assets/4f0155d7-cd9b-4d96-a018-2a5efb2a37f2" />


## ✨ Features

* **🖥️ Web-based UI:** Modern dark-mode interface, runs directly in the browser.
* **🔴 Live Log Stream:** View the server console in real-time (via Server-Sent Events).
* **⚡ Remote Commands:** Execute commands (like `/kick`, `/stop`) without being in-game.
* **📊 Live Stats:** Monitor current player count.
* **🔒 Token Auth:** Simple protection via access tokens.
* **📱 Responsive:** Works on mobile devices.

## 📥 Installation

1.  Download the latest `.jar` file from `releases` (or compile it yourself).
2.  Stop your Hytale server.
3.  Place the `HyDash.jar` into your server's **`mods`** folder.
4.  Start the server.
5.  The dashboard is now accessible on port `8888`.

## ⚙️ Configuration

Currently, the port and token are configured in the source code (hardcoded).

* **Default port:** `8888`
* **Default token:** `ChangeMe123`
* **Default listing to:** `0.0.0.0`

To change these, edit the `config.json` file inside `mods/Manu3lll_HyDash/config.json`:

```json
{
  "bindAddress": "0.0.0.0", //change listing to here
  "port": 8888, //change port, where webserver should run here
  "token": "ChangeMe123" //change token for login here
}
```
## 🔨 Contributing
Community contributions are welcome and encouraged. If you are a plugin developer and this plugin does not fulfill your needs, please consider contributing to this repository before building your own web dashboard implementation.

## 🔒 Security
If you believe to have found a security vulnerability, please report your findings via https://github.com/Manu3lll/HyDash/issues.

Thank you for your help und your support! ❤️
