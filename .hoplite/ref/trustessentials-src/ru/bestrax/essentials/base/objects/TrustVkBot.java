/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package ru.bestrax.essentials.base.objects;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import ru.bestrax.essentials.api.VkBot;
import ru.bestrax.essentials.api.VkCommand;
import ru.bestrax.essentials.api.vk.longpoll.LongPollTask;
import ru.bestrax.essentials.api.vk.messages.KeyboardMessage;
import ru.bestrax.essentials.api.vk.messages.RequestMessage;
import ru.bestrax.essentials.base.objects.TrustVkCommand;

public class TrustVkBot
implements VkBot {
    private int groupId;
    private String id;
    private String token;
    private LongPollTask longPollTask;
    private Timer timer;
    private Map<String, VkCommand> commands;

    public TrustVkBot(String id, int groupId, String token) {
        this.groupId = groupId;
        this.id = id;
        this.token = token;
        this.commands = new ConcurrentHashMap<String, VkCommand>();
    }

    @Override
    public void start() {
        this.longPollTask = new LongPollTask(this);
        this.longPollTask.connect();
        this.timer = new Timer();
        this.timer.scheduleAtFixedRate((TimerTask)this.longPollTask, 0L, 1000L);
    }

    @Override
    public void stop() {
        this.longPollTask.cancel();
        this.timer.cancel();
    }

    @Override
    public void addCommand(VkCommand vkCommand) {
        this.commands.put(vkCommand.getMainCommand().toLowerCase(), vkCommand);
    }

    @Override
    public void removeCommand(String mainCommand) {
        this.commands.remove(mainCommand.toLowerCase());
    }

    @Override
    public void removeCommand(VkCommand vkCommand) {
        this.commands.remove(vkCommand.getMainCommand().toLowerCase(), vkCommand);
    }

    @Override
    public void addSpeedCommand(String answer, String mainCommand, String ... aliases) {
        this.addCommand(new TrustVkCommand(message -> {
            this.sendMessage(Integer.parseInt(message.getPeerId()), answer);
            return Void.TYPE;
        }, mainCommand, aliases));
    }

    @Override
    public void sendMessage(int id, String ... message) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : message) {
            stringBuilder.append(s).append("\n");
        }
        RequestMessage requestMessage = new RequestMessage(String.valueOf(id), stringBuilder.toString());
        requestMessage.send(this);
    }

    @Override
    public void sendMessage(int id, List<String> messages) {
        StringBuilder stringBuilder = new StringBuilder();
        messages.forEach(x -> stringBuilder.append((String)x).append("\n"));
        RequestMessage requestMessage = new RequestMessage(String.valueOf(id), stringBuilder.toString());
        requestMessage.send(this);
    }

    @Override
    public void sendMessage(final RequestMessage message) {
        new Thread(){

            @Override
            public void run() {
                String id = message.getTo();
                String mess = message.getMessage();
                try {
                    String Url = "https://api.vk.com/method/messages.send?&random_id=0&peer_id=" + id + "&message=" + URLEncoder.encode(mess, "utf-8") + "&access_token=" + TrustVkBot.this.token + "&v=5.103";
                    URL Obj = new URL(Url);
                    HttpURLConnection con = (HttpURLConnection)Obj.openConnection();
                    con.setRequestMethod("GET");
                    BufferedReader inLong = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    while (inLong.ready()) {
                        con.disconnect();
                    }
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
                this.stop();
            }
        }.start();
    }

    @Override
    public void sendCallbackMessage(final KeyboardMessage message) {
        new Thread(){

            @Override
            public void run() {
                String id = message.getTo();
                String mess = message.getText();
                try {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("one_time", Boolean.valueOf(false));
                    jsonObject.addProperty("inline", Boolean.valueOf(true));
                    JsonArray arrayF = new JsonArray();
                    int buttonI = 1;
                    JsonArray array = new JsonArray();
                    for (String button : message.getButtons()) {
                        JsonObject keyBoardButton = new JsonObject();
                        JsonObject keyBoardAction = new JsonObject();
                        keyBoardAction.addProperty("label", button);
                        keyBoardAction.addProperty("type", "text");
                        keyBoardAction.addProperty("payload", "{\"button\": \"" + buttonI + "\"}");
                        keyBoardButton.add("action", (JsonElement)keyBoardAction);
                        keyBoardButton.addProperty("color", "positive");
                        array.add((JsonElement)keyBoardButton);
                        ++buttonI;
                    }
                    arrayF.add((JsonElement)array);
                    jsonObject.add("buttons", (JsonElement)arrayF);
                    String keyboard = URLEncoder.encode(jsonObject.toString(), "utf-8");
                    keyboard = URLDecoder.decode(keyboard, "utf-8");
                    String Url = "https://api.vk.com/method/messages.send?&random_id=0&peer_id=" + id + "&message=" + URLEncoder.encode(mess, "utf-8") + "&keyboard=" + keyboard + "&access_token=" + TrustVkBot.this.token + "&v=5.103";
                    Url = Url.replace(" ", "%20");
                    URL Obj = new URL(Url);
                    HttpURLConnection con = (HttpURLConnection)Obj.openConnection();
                    con.setReadTimeout(10000);
                    con.setRequestMethod("GET");
                    BufferedReader inLong = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    while (inLong.ready()) {
                        con.disconnect();
                    }
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
                this.stop();
            }
        }.start();
    }

    @Override
    public Map<String, VkCommand> getCommands() {
        return this.commands;
    }

    @Override
    public int getGroupId() {
        return this.groupId;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getToken() {
        return this.token;
    }
}
