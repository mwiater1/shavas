package csc445.shavas.client;

import csc445.shavas.core.Canvas;
import csc445.shavas.core.Colors;
import csc445.shavas.core.Constants;
import csc445.shavas.core.GetQuery;
import csc445.shavas.core.Pixel;
import csc445.shavas.core.UpdateCommand;

import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.NettyTransport;
import io.atomix.copycat.client.ConnectionStrategies;
import io.atomix.copycat.client.CopycatClient;
import io.atomix.copycat.session.Session;
import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static spark.Spark.*;

public final class Client
{

    public static void main(String[] args)
    {
        // Set the port for the web server
        port(80);

        // Create a route for the main page
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", "HELLO CLIENT!");
        get("/", (rq, rs) -> new ModelAndView(map, "index.mustache"), new MustacheTemplateEngine());

        String[] addresses = {""};

        Client client = Client.create(addresses);

        client.commitChanges(Client.TEST_PIXELS);
        System.err.println("committed changes!");
    }

    private Session session;
    private final CopycatClient client;

    public static Client create(String... hostNames)
    {
        if (hostNames.length == 0)
        {
            throw new IllegalArgumentException("Must supply at least one valid server address");
        }

        List<Address> hosts = new ArrayList<>();

        for (String hostName : hostNames)
        {
            hosts.add(new Address(hostName, Constants.CLIENT_PORT));
        }

        return new Client(hosts);
    }

    private Client(List<Address> cluster)
    {
        client = CopycatClient.builder()
                .withTransport(
                        NettyTransport.builder()
                                .withThreads(2)
                                .build())

                .withConnectionStrategy(ConnectionStrategies.EXPONENTIAL_BACKOFF)

                .build();

        client.serializer()
                .register(csc445.shavas.core.UpdateCommand.class)
                .register(csc445.shavas.core.GetQuery.class)
                .register(csc445.shavas.core.Canvas.class)
                .register(csc445.shavas.core.Pixel.class);

        client.onStateChange((state) ->
                System.err.println("Client::Client - onStateChangeListener - new state " + state.name()));

        client.connect(cluster).join();
        System.err.println("Client::Client - connected to cluster");

        Pixel pixel = new Pixel(0, 0, Colors.BLACK);
        System.err.println("Client::Client - made new pixel");
    }

    private static final List<Pixel> TEST_PIXELS = Arrays.asList(new Pixel(0, 0, Colors.BLACK),
            new Pixel(0, 1, Colors.BLACK),
            new Pixel(1, 0, Colors.BLACK),
            new Pixel(1, 1, Colors.BLACK));

    public void commitChanges(List<Pixel> pixelDiffs)
    {
        client.submit(new UpdateCommand(pixelDiffs)).join();
    }
}
