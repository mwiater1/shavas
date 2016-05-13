package csc445.shavas.server;

import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.NettyTransport;
import io.atomix.catalyst.util.Listener;
import io.atomix.copycat.server.CopycatServer;
import io.atomix.copycat.server.storage.Storage;
import io.atomix.copycat.server.storage.StorageLevel;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public final class Server
{
    public static void main(String[] args) throws IOException
    {
        boolean configExists = false;

        File logDirectory = new File(LOG_PATH);

        if (logDirectory.isDirectory())
        {
            File[] logs = logDirectory.listFiles();

            configExists = logs != null && logs.length > 0;
        }

        System.err.println("Server::main - configExists: " + configExists);

        File clusterConfig = new File(CLUSTER_CONFIG_FILE);

        Scanner clusterScanner = new Scanner(clusterConfig);

        if (!clusterScanner.hasNext())
        {
            throw new AssertionError("cluster.txt must have the server address and the desired port separated by a space as the first entries in the file.");
        }

        String serverAddress = clusterScanner.next();
        int port = Integer.parseInt(clusterScanner.next());

        List<String> hostNames = new ArrayList<>();

        while (clusterScanner.hasNext())
        {
            hostNames.add(clusterScanner.next());
        }

        clusterScanner.close();

        String[] hosts = new String[hostNames.size()];
        hostNames.toArray(hosts);

        Server server = Server.create(serverAddress, port, hosts);
    }

    public static final String LOG_PATH = "logs";
    public static final String CLUSTER_CONFIG_FILE = "cluster.txt";

    private final CopycatServer server;
    private final List<Address> cluster;

    private Server(Address serverAddress, List<Address> cluster)
    {
        this.cluster = cluster;

        server = CopycatServer.builder(serverAddress)
                .withStateMachine(CanvasStateMachine::new)

                .withTransport(
                        NettyTransport.builder()
                                .withThreads(4)
                                .build())

                .withStorage(
                        Storage.builder()
                                .withDirectory(new File("logs"))
                                .withStorageLevel(StorageLevel.DISK)
                                .build())

                .build();

        server.serializer()
                .register(csc445.shavas.core.Canvas.class)
                .register(csc445.shavas.core.Pixel.class)
                .register(csc445.shavas.core.UpdateCommand.class)
                .register(csc445.shavas.core.GetQuery.class)
                .register(csc445.shavas.core.JoinCommand.class);

        Listener<CopycatServer.State> stateChangeListener = server.onStateChange((state) ->
                System.err.println("Server::Server - onStateChange: new state " + state.name()));

        if (cluster == null)
        {
            server.bootstrap().join();
            System.err.println("Server::Server - bootstrapped cluster");
        }
        else
        {
            server.join(cluster).join();
            System.err.println("Server::Server - joined cluster " + cluster);
        }

        Thread thread = new Thread()
        {
            @Override
            public void run()
            {
                Scanner kbScanner = new Scanner(System.in);

                for (; ; )
                {
                    String input = kbScanner.next();
                    System.err.println("Server::Server::kbThread - input: " + input);
                    if (input.equals("quit"))
                    {
                        server.leave();
                    }
                    else if (input.equals("restart") && server.state().equals(CopycatServer.State.INACTIVE))
                    {
                        server.join(cluster).join();
                        System.err.println("Server::run - rejoined cluster");
                    }
                }
            }
        };

        thread.start();
    }

    public static Server create(InetAddress serverIp, int port, String... addresses)
    {
        List<Address> cluster;

        if (addresses.length > 0)
        {
            cluster = new ArrayList<>();

            for (String hostName : addresses)
            {
                cluster.add(new Address(hostName, port));
            }
        }
        else
        {
            cluster = null;
        }

        Address serverAddress = new Address(serverIp.getHostAddress(), port);

        return new Server(serverAddress, cluster);
    }

    public static Server create(String serverIp, int port, String... addresses) throws UnknownHostException
    {
        return Server.create(InetAddress.getByName(serverIp), port, addresses);
    }
}
