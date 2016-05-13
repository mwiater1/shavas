package csc445.shavas.client;

import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

@WebSocket
public class WebsocketListener {

    @OnWebSocketConnect
    public void connected(Session session) {
        System.out.println("Connected!");
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        System.out.println("Disconnected!");
    }

    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {
        System.out.println("Got: " + message);   // Print message
    }
}
