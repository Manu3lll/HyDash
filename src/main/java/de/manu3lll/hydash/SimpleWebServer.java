package de.manu3lll.hydash;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import de.manu3lll.hydash.HttpHandler.*;

import java.io.*;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import java.util.Collection;

public class SimpleWebServer {

    protected final JavaPlugin plugin;
    private HttpServer server;
    private final int PORT = 8888;
    protected final String AUTH_TOKEN = "Geheim123";

    public SimpleWebServer(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());

            server.createContext("/", new DashboardHandler());
            server.createContext("/stream", new LogStreamHandler(plugin));
            server.createContext("/cmd", new CommandHandler(plugin));
            server.createContext("/playercount", new DashboardPlayerCountHelper());

            server.start();
            plugin.getLogger().atInfo().log("Admin dashboard running on port " + PORT);

        } catch (IOException e) {
            plugin.getLogger().atInfo().log("Error during webserver startup!", e);
        }
    }

    public void stop() {
        if (server != null) server.stop(0);
    }

    class DashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            try {
                if (!HelperMethods.checkAuth(t,AUTH_TOKEN)) return;

                String html = """
                    <!DOCTYPE html>
                    <html lang="de">
                    <head>
                        <meta charset="UTF-8">
                        <title>Hytale Console</title>
                        <style>
                            body { background-color: #121212; color: #e0e0e0; font-family: 'Consolas', monospace; padding: 20px; display: flex; flex-direction: column; height: 90vh; }
                            h2 { margin-top: 0; border-bottom: 1px solid #333; padding-bottom: 10px; color: #fff; }
                            #logs { 
                                flex-grow: 1;
                                display: flex; 
                                flex-direction: column-reverse;
                                background: #1e1e1e; 
                                border: 1px solid #333; 
                                padding: 10px; 
                                overflow-y: auto; 
                                font-size: 14px;
                                border-radius: 4px;
                            }
                            
                            .line { border-bottom: 1px solid #2a2a2a; padding: 2px 0; word-wrap: break-word; white-space: pre-wrap; }
                            .line:hover { background-color: #252526; }
                            
                            .input-area { margin-top: 15px; display: flex; gap: 10px; }
                            
                            input[type="text"] { 
                                flex-grow: 1;
                                padding: 12px; 
                                background: #252526; 
                                color: white; 
                                border: 1px solid #444; 
                                border-radius: 4px;
                                font-family: inherit;
                            }
                            input:focus { outline: 1px solid #007acc; }
                            
                            button { 
                                padding: 0 20px; 
                                cursor: pointer; 
                                background: #007acc; 
                                color: white; 
                                border: none; 
                                border-radius: 4px; 
                                font-weight: bold;
                            }
                            button:hover { background: #005f9e; }
                            
                            #status { font-size: 12px; margin-bottom: 5px; color: #666; font-weight: bold;}
                        </style>
                    </head>
                    <body>
                        <div style="display:flex; justify-content:space-between; align-items:center;">
                            <h2>Hytale Server Dashboard</h2>
                            <h3>Player connected: <div id="playerCount">none</div></h3>
                            <div id="status">Connecting...</div>
                        </div>
                        
                        <div id="logs">
                            <div class="line" style="color: #666">Waiting for logs...</div>
                        </div>
                        
                        <div class="input-area">
                            <input type="text" id="cmd" placeholder="Enter command..." autocomplete="off">
                            <button onclick="send()">Submit</button>
                        </div>

                        <script>
                            const TOKEN = "%s";
                            const out = document.getElementById('logs');
                            const stat = document.getElementById('status');
                            const playerCount = document.getElementById('playerCount');
                            
                                                        // --- 1. LIVE STREAM (SSE) ---
                            // Hier verbindet sich das Frontend mit dem LogStreamHandler
                            const es1 = new EventSource("/playercount");
                                     
                            es1.onopen = () => {
                            };
                                                        
                            es1.onmessage = (a) => {
                              var letter = a.data.charAt(0);
                              playerCount.innerText = a.data;
                              if(letter == 0){
                                playerCount.style.color="#ff5555";
                              }else{
                                playerCount.style.color="#50fa7b";
                              }
                              
                            };
                            
                            es1.onerror = () => { 
                                playerCount.innerText = "0/100"; 
                                playerCount.style.color="#ff5555"; 
                            };
                            
                            
                            // --- 1. LIVE STREAM (SSE) ---
                            // Hier verbindet sich das Frontend mit dem LogStreamHandler
                            const es = new EventSource("/stream?token=" + TOKEN);
                            
                            es.onopen = () => { 
                                stat.innerText = "🟢 CONNECTED"; 
                                stat.style.color="#50fa7b"; 
                                out.innerHTML = ""; // Logs leeren bei Start
                            };
                            
                            es.onmessage = (e) => {
                                const d = document.createElement("div");
                                d.className = "line";
                                d.textContent = e.data; // Hier kommt der Text aus Java an
                                out.prepend(d);
                                // Maximal 200 Zeilen behalten, damit Browser nicht laggt
                                if(out.children.length > 200) out.lastChild.remove();
                            };
                            
                            es.onerror = () => { 
                                stat.innerText = "🔴 DISCONNECTED (Reconnecting...)"; 
                                stat.style.color="#ff5555"; 
                            };

                            // --- 2. COMMANDS SENDEN ---
                            async function send() {
                                const i = document.getElementById('cmd');
                                if(!i.value) return;
                                
                                const cmdText = i.value;
                                i.value = ""; // Sofort leeren
                                
                                try {
                                    const response = await fetch("/cmd?token=" + TOKEN, {
                                        method: 'POST', 
                                        body: cmdText
                                    });
                                    const text = await response.text();
                                    
                                    // Antwort blau einfärben und anzeigen
                                    const d = document.createElement("div");
                                    d.className = "line";
                                    d.style.color = "#8be9fd"; 
                                    d.textContent = "➥ " + text;
                                    out.prepend(d);
                                } catch (err) {
                                    alert("Error during transmission: " + err);
                                }
                            }
                            
                            document.getElementById('cmd').addEventListener("keypress", (e) => {
                                if(e.key === "Enter") send();
                            });
                        </script>
                    </body>
                    </html>
                    """.formatted(AUTH_TOKEN);

                HelperMethods.sendResponse(t, html, 200);

            } catch (Exception e) {
                plugin.getLogger().atInfo().log("Dashboard Error:", e);
                HelperMethods.sendResponse(t, "Server Error: " + e, 500);
            }
        }
    }

}