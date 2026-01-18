package de.manu3lll.hydash;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import de.manu3lll.hydash.HttpHandler.CommandHandler;
import de.manu3lll.hydash.HttpHandler.DashboardPlayerCountHelper;
import de.manu3lll.hydash.HttpHandler.LogStreamHandler;

import java.io.*;
import java.net.InetSocketAddress;

public class SimpleWebServer {

    protected final JavaPlugin plugin;
    protected final WebConfig config;
    private HttpServer server;

    public SimpleWebServer(JavaPlugin plugin, WebConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void start() {
        try {
            InetSocketAddress address = new InetSocketAddress(config.bindAddress, config.port);
            server = HttpServer.create(address, 0);
            server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());

            server.createContext("/", new DashboardHandler());
            server.createContext("/dashboard", new DashboardHandler());
            server.createContext("/stream", new LogStreamHandler(plugin, config));
            server.createContext("/cmd", new CommandHandler(plugin, config));
            server.createContext("/playercount", new DashboardPlayerCountHelper());

            server.start();
            plugin.getLogger().atInfo().log("HyDash running on " + config.bindAddress + ":" + config.port);
        } catch (IOException e) {
            plugin.getLogger().atSevere().log("Failed to start HyDash server", e);
        }
    }

    public void stop() {
        if (server != null) server.stop(0);
    }

    class DashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>HyDash Console</title>
                    <style>
                        html, body { height: 100%; margin: 0; padding: 0; background: #121212; color: #e0e0e0; font-family: 'Consolas', monospace; overflow: hidden; }
                        body { display: flex; flex-direction: column; padding: 20px; box-sizing: border-box; }
                        h2 { margin: 0 0 10px 0; border-bottom: 1px solid #333; padding-bottom: 10px; flex-shrink: 0; }
                        
                        #main-content { display: flex; flex-direction: column; height: 100%; min-height: 0; }
                        
                        #logs { 
                            flex-grow: 1; min-height: 0; background: #1e1e1e; border: 1px solid #333; 
                            padding: 10px; overflow-y: auto; display: flex; flex-direction: column; 
                            font-size: 14px; border-radius: 4px; scroll-behavior: auto;
                        }
                        
                        .line { border-bottom: 1px solid #2a2a2a; padding: 2px 0; word-wrap: break-word; white-space: pre-wrap; flex-shrink: 0; }
                        .input-area { margin-top: 15px; display: flex; gap: 10px; flex-shrink: 0; }
                        
                        input[type="text"], input[type="password"] { flex-grow: 1; padding: 12px; background: #252526; color: white; border: 1px solid #444; border-radius: 4px; font-family: inherit; }
                        button { padding: 0 20px; cursor: pointer; background: #007acc; color: white; border: none; border-radius: 4px; font-weight: bold; }
                        
                        #login-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.9); z-index: 1000; display: flex; justify-content: center; align-items: center; }
                        .login-box { background: #1e1e1e; padding: 30px; border-radius: 8px; border: 1px solid #007acc; text-align: center; width: 300px; }
                        #loginError { color: #ff5555; margin-top: 10px; font-size: 13px; display: none; }
                        .hidden { display: none !important; }
                    </style>
                </head>
                <body>
                    <div id="login-overlay">
                        <div class="login-box">
                            <h3>HyDash Login</h3>
                            <input type="password" id="tokenInput" placeholder="Enter Token...">
                            <button id="loginBtn" onclick="doLogin()" style="width:100%; margin-top:10px;">Connect</button>
                            <div id="loginError">⚠️ Invalid Token</div>
                        </div>
                    </div>
                    <div id="main-content" class="hidden">
                         <div style="display:flex; justify-content:space-between; align-items:center; flex-shrink:0;">
                    <h2>HyDash - Hytale Server Dashboard <span style="font-size: 0.6em; color: #888;">v0.1</span></h2>
                    <div style="display: flex; align-items: center; gap: 15px;">
                        <div id="stats">👤 Players: <span id="pCount" class="stat-val">0 / 0</span></div>
                        <div id="status">Connecting...</div>
                        <div style="font-size: 20px; color: #888; margin-top: 5px; cursor: pointer;" onclick="logout()">[Logout]</div>
                    </div>
                </div>
                        <div id="logs"></div>
                        <div class="input-area">
                            <input type="text" id="cmd" placeholder="Command..." autocomplete="off">
                            <button onclick="send()">Send</button>
                        </div>
                    </div>

                    <script>
                        const out = document.getElementById('logs');
                        
                        function setCookie(n, v, d) { const ex = new Date(); ex.setTime(ex.getTime()+(d*24*60*60*1000)); document.cookie = n+"="+v+";expires="+ex.toUTCString()+";path=/"; }
                        function getCookie(n) { let b = document.cookie.match('(^|;)\\\\s*' + n + '\\\\s*=\\\\s*([^;]+)'); return b ? b.pop() : ""; }
                        function logout() { setCookie("auth_token", "", -1); location.reload(); }

                        const urlT = new URLSearchParams(window.location.search).get('token');
                        if(urlT) { setCookie("auth_token", urlT, 30); window.history.replaceState({},"", "/dashboard"); }

                        const currentT = getCookie("auth_token");
                        if(currentT) { document.getElementById('login-overlay').classList.add('hidden'); document.getElementById('main-content').classList.remove('hidden'); start(currentT); }

                        async function doLogin() {
                            const v = document.getElementById('tokenInput').value;
                            const res = await fetch("/cmd?token=" + encodeURIComponent(v), { method: 'POST', body: "ping" });
                            if(res.status === 200) { setCookie("auth_token", v, 30); location.reload(); }
                            else { document.getElementById('loginError').style.display='block'; }
                        }

                        function start(token) {
                        const esStats = new EventSource("/playercount?token=" + encodeURIComponent(token));
                                    esStats.onmessage = (e) => {
                                        document.getElementById('pCount').innerText = e.data;
                                    };
                        
                        
                            const es = new EventSource("/stream?token=" + encodeURIComponent(token));
                            es.onopen = () => { document.getElementById('status').innerText = "🟢 ONLINE"; };
                            es.onmessage = (e) => {
                                let msg = e.data.startsWith("LOG:") ? e.data.substring(4) : e.data;
                                
                                const isAtBottom = (out.scrollHeight - out.scrollTop - out.clientHeight) < 50;

                                const d = document.createElement("div");
                                d.className = "line";
                                d.textContent = msg;
                                out.appendChild(d);
                                
                                if (isAtBottom) {
                                    requestAnimationFrame(() => { out.scrollTop = out.scrollHeight; });
                                }
                                if(out.children.length > 500) out.firstChild.remove();
                            };
                            es.onerror = () => { document.getElementById('status').innerText = "🔴 DISCONNECTED"; };
                        }

                        async function send() {
                            const input = document.getElementById('cmd');
                            const val = input.value; if(!val) return; input.value = "";
                            const res = await fetch("/cmd?token=" + encodeURIComponent(getCookie("auth_token")), { method: 'POST', body: val });
                            const text = await res.text();
                            const d = document.createElement("div");
                            d.className = "line"; d.style.color = "#8be9fd"; d.textContent = "➥ " + text;
                            out.appendChild(d);
                            requestAnimationFrame(() => { out.scrollTop = out.scrollHeight; });
                        }

                        document.getElementById('cmd').onkeypress = (e) => { if(e.key === "Enter") send(); };
                        document.getElementById('tokenInput').onkeypress = (e) => { if(e.key === "Enter") doLogin(); };
                    </script>
                </body>
                </html>
                """;
            HelperMethods.sendResponse(t, html, 200);
        }
    }

}