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
    public static Client client = null;

    @OnWebSocketConnect
    public void connected(Session session) {
        System.out.println("Connected!");
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        System.out.println("Disconnected!");
        System.out.println("REASON: " + reason);
    }

    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {
        Gson gson = new Gson();
        List<Pixel> pixels = gson.fromJson(message, new TypeToken<List<Pixel>>(){}.getType());
        pixels.forEach(System.out::println);
    }
}
