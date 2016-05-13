package csc445.shavas.client;

import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.HashMap;

import static spark.Spark.*;

public class Test {

    public static void main(String[] args) {
        //Set the port for the web server
        port(80);

        // Set the websocket
        webSocket("/ws", WebsocketListener.class);

        //Create a route for the main page
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", "HELLO CLIENT!");
        get("/", (rq, rs) -> new ModelAndView(map, "index.mustache"), new MustacheTemplateEngine());
    }

}
