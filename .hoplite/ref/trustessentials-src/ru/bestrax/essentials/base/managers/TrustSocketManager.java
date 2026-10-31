/*
 * Decompiled with CFR 0.152.
 */
package ru.bestrax.essentials.base.managers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import ru.bestrax.essentials.api.manager.SocketManager;

public class TrustSocketManager
implements SocketManager {
    private Timer timer;
    private TimerTask serverSocketHandler;

    @Override
    public void setServerSocketHandler(TimerTask serverSocketHandler) {
        this.serverSocketHandler = serverSocketHandler;
    }

    @Override
    public void startServerSocketHandler() {
        if (this.timer != null) {
            throw new UnsupportedOperationException("Timer already running");
        }
        this.timer = new Timer();
        this.timer.schedule(this.serverSocketHandler, 1000L, 1000L);
    }

    @Override
    public void stopServerSocketHandler() {
        if (this.timer == null) {
            throw new UnsupportedOperationException("Timer not running");
        }
        this.timer.cancel();
        this.timer = null;
    }

    @Override
    public void sendMessage(String host, int port, String ... messages) {
        this.sendMessage(host, port, Arrays.asList(messages));
    }

    @Override
    public void sendMessage(int port, String ... messages) {
        this.sendMessage("127.0.0.1", port, messages);
    }

    @Override
    public void sendMessage(String host, int port, List<String> messages) {
        try {
            Socket socket = new Socket(host, port);
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            for (String message : messages) {
                String newMessage = URLEncoder.encode(message, "utf-8");
                bufferedWriter.write(newMessage + "\n");
            }
            bufferedWriter.close();
            socket.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendMessage(int port, List<String> messages) {
        this.sendMessage("127.0.0.1", port, messages);
    }
}
