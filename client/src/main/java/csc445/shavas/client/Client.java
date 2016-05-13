package csc445.shavas.client;

import csc445.shavas.core.Colors;
import csc445.shavas.core.Pixel;
import csc445.shavas.core.UpdateCommand;
import csc445.shavas.core.JoinCommand;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.NettyTransport;
import io.atomix.catalyst.util.Listener;
import io.atomix.copycat.client.ConnectionStrategies;
import io.atomix.copycat.client.CopycatClient;
import io.atomix.copycat.session.Session;
import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;
import io.atomix.copycat.session.Event;

import javax.swing.event.ChangeEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public final class Client
{

    public static void main(String[] args)
    {
        // Set the port for the web server
        //port(80);

        // Create a route for the main page
        //HashMap<String, Object> map = new HashMap<>();
        //map.put("name", "HELLO CLIENT!");
        //get("/", (rq, rs) -> new ModelAndView(map, "index.mustache"), new MustacheTemplateEngine());

        Scanner keyboardScanner = new Scanner(System.in);

        int port = -1;

        while (port < 0)
        {
            System.err.println("Please enter a valid port to create a client");
            try
            {
                port = Integer.parseInt(keyboardScanner.next());
            }
            catch (NumberFormatException nfe)
            {
            }
        }

        String[] addresses = {"pi.cs.oswego.edu", "rho.cs.oswego.edu", "wolf.cs.oswego.edu"};

        Client client = Client.create(port, addresses);

        String input = "";
        while (!input.equals("quit"))
        {
            System.err.println("Enter \'pixel\' to send a random pixel or \'quit\' to quit");
            input = keyboardScanner.next();

            if (input.equals("pixel"))
            {
                Pixel randomPixel = Pixel.randomPixel();
                System.err.println("Commiting pixel " + randomPixel);
                client.commitChanges(Collections.singletonList(randomPixel));
            }
        }

        client.close();
        System.err.println("Exiting");
    }

    private final CopycatClient client;

    public static Client create(int port, String... hostNames)
    {
        if (hostNames.length == 0)
        {
            throw new IllegalArgumentException("Must supply at least one valid server address");
        }

        List<Address> hosts = new ArrayList<>();

        for (String hostName : hostNames)
        {
            hosts.add(new Address(hostName, port));
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
                .register(csc445.shavas.core.Pixel.class)
                .register(csc445.shavas.core.JoinCommand.class);

        client.onStateChange((state) -> {
                System.err.println("Client::Client - onStateChangeListener - new state " + state.name());
                if (state == CopycatClient.State.CONNECTED) {

                    client.submit(new JoinCommand()).join();
                }
            });

        client.connect(cluster).join();
        System.err.println("Client::Client - connected to cluster");
        client.session().onStateChange((s) -> System.err.println("Client::Client - new state " + s));

        client.onEvent("change", (e) -> System.err.println("Client::Client - e: " + e));

        client.<List<Pixel>>onEvent("change", (pixels) -> {
            pixels.forEach((p) -> System.out.println(p.toString()));
        });

        client.onEvent("Event", (e) -> System.err.println("Client::Client - e: " + e));
        client.onEvent("UpdateCommand", (u) -> System.err.println("Client::Client - u: " + u));
    }

    private static final List<Pixel> TEST_PIXELS = Arrays.asList(
            new Pixel(0, 0, Colors.BLACK),
            new Pixel(0, 1, Colors.BLACK),
            new Pixel(1, 0, Colors.BLACK),
            new Pixel(1, 1, Colors.BLACK));

    public void commitChanges(List<Pixel> pixelDiffs)
    {
        client.submit(new UpdateCommand(pixelDiffs)).join();
    }

    public void close()
    {
        client.close();
    }
}
