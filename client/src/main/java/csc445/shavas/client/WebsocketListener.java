package csc445.shavas.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import csc445.shavas.core.*;
import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

@WebSocket
public class WebsocketListener {
    private static Client client = null;
    private static ConcurrentLinkedQueue<Session> sessions = new ConcurrentLinkedQueue<>();

    public static void setClient(Client c) {
        client = c;
    }

    public static void sendPixels(List<Pixel> pixels) {
        Gson gson = new Gson();
        for (Session session : sessions) {
            try {
                session.getRemote().sendString(gson.toJson(pixels));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    @OnWebSocketConnect
    public void connected(Session session) {
        sessions.add(session);
        System.out.println("Connected!");
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        sessions.remove(session);
        System.out.println("Disconnected!");
    }

    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {
        Gson gson = new Gson();
        List<Pixel> pixels = gson.fromJson(message, new TypeToken<List<Pixel>>(){}.getType());
        client.commitChanges(pixels);
    }
}
