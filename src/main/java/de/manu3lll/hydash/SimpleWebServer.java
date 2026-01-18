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
                    <link rel="icon" type="image/png" href="data:image/png;base64,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">
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
                    <h2>HyDash - Hytale Server Dashboard <span style="font-size: 0.6em; color: #888;">v0.0.1</span></h2>
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