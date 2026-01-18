package de.manu3lll.hydash.HttpHandler;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import de.manu3lll.hydash.HelperMethods;
import de.manu3lll.hydash.SimpleWebServer;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

public class CommandHandler extends SimpleWebServer implements HttpHandler {

    public CommandHandler(JavaPlugin plugin) {
        super(plugin);
    }

    private String stripAnsi(String text) {
        if (text == null) return "";
        return text.replaceAll("\u001B\\[[;\\d]*m", "");
    }

    private String extractFullText(Object msgObj) {
        if (msgObj == null) return "";
        if (msgObj instanceof String) return (String) msgObj;

        StringBuilder sb = new StringBuilder();
        try {
            Class<?> clazz = msgObj.getClass();
            String myText = null;

            // 1. Eigener Text
            try {
                Method mAnsi = clazz.getMethod("getAnsiMessage");
                Object res = mAnsi.invoke(msgObj);
                if (res != null) myText = res.toString();
            } catch (Exception e) {}

            if (myText == null) {
                try {
                    Method mRaw = clazz.getMethod("getRawText");
                    Object res = mRaw.invoke(msgObj);
                    if (res != null) myText = res.toString();
                } catch (Exception e) {}
            }

            if (myText != null && !myText.isBlank()) {
                sb.append(myText);
                if (myText.trim().endsWith(":")) sb.append("\n");
            }

            // 2. Kinder (Arguments/Children) rekursiv
            String[] methods = {"getChildren", "getArgs", "getParams", "getData"};
            for (String mName : methods) {
                try {
                    Method m = clazz.getMethod(mName);
                    Object res = m.invoke(msgObj);
                    if (res instanceof Collection<?>) {
                        for (Object child : (Collection<?>) res) {
                            String t = extractFullText(child);
                            if (!t.isEmpty()) sb.append(t).append(" ");
                        }
                    } else if (res instanceof Object[]) {
                        for (Object child : (Object[]) res) {
                            String t = extractFullText(child);
                            if (!t.isEmpty()) sb.append(t).append(" ");
                        }
                    }
                } catch (Exception e) {}
            }
        } catch (Exception e) { return "Extract Error"; }
        return sb.toString();
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (!HelperMethods.checkAuth(t,this.AUTH_TOKEN)) return;

        if ("POST".equalsIgnoreCase(t.getRequestMethod())) {
            InputStream is = t.getRequestBody();
            String rawCommand = new String(is.readAllBytes(), StandardCharsets.UTF_8).trim();
            final String command = rawCommand.startsWith("/") ? rawCommand.substring(1) : rawCommand;

            //plugin.getLogger().atInfo().log("[Web] Command: " + command);
            StringBuilder responseBuffer = new StringBuilder();

            try {
                Class<?> senderInterface = Class.forName("com.hypixel.hytale.server.core.command.system.CommandSender");
                Object fakeSender = java.lang.reflect.Proxy.newProxyInstance(
                        senderInterface.getClassLoader(),
                        new Class<?>[]{senderInterface},
                        (proxy, method, args) -> {
                            String mName = method.getName();
                            if (mName.equals("getName")) return "WebAdmin";
                            if (mName.equals("hasPermission")) return true;
                            if (mName.contains("send") || mName.contains("msg")) {
                                String clean = stripAnsi(extractFullText(args[0]));
                                plugin.getLogger().atInfo().log(">>> " + clean);
                                responseBuffer.append(clean).append("\n");
                                return null;
                            }
                            return null;
                        }
                );

                Object manager = HytaleServer.get().getCommandManager();
                java.lang.reflect.Method handle = manager.getClass().getMethod("handleCommand", senderInterface, String.class);
                handle.invoke(manager, fakeSender, command);

                //String finalRes = responseBuffer.length() > 0 ? responseBuffer.toString() : "OK (Keine Rückgabe)";
                //sendResponse(t, finalRes, 200);

            } catch (Exception e) {
                e.printStackTrace(); // Wichtig für Console Logs
                HelperMethods.sendResponse(t, "Error: " + e.getMessage(), 500);
            }
        } else {
            HelperMethods.sendResponse(t, "Only POST allowed", 405);
        }
    }

}
