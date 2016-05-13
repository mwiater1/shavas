package csc445.shavas.server;

import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.NettyTransport;
import io.atomix.catalyst.util.Listener;
import io.atomix.copycat.server.CopycatServer;
import io.atomix.copycat.server.cluster.Cluster;
import io.atomix.copycat.server.storage.Storage;
import io.atomix.copycat.server.storage.StorageLevel;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

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
            throw new AssertionError(CLUSTER_CONFIG_FILE + " must have the server address and the desired port separated by a space as the first entries in the file.");
        }

        String serverAddress = clusterScanner.next();
        int port = Integer.parseInt(clusterScanner.next());

        List<String> hostNames = new ArrayList<>();

        while (clusterScanner.hasNext())
        {
            hostNames.add(clusterScanner.next());
        }

        clusterScanner.close();

        String[] hosts = (String[]) hostNames.toArray();

        Server server = Server.create(serverAddress, port, hosts);
    }

    public static final String LOG_PATH = "logs";
    public static final String CLUSTER_CONFIG_FILE = "cluster.txt";

    private final CopycatServer server;
    private final Address[] cluster;

    private Server(Address serverAddress, Address[] cluster)
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
                .register(csc445.shavas.core.GetQuery.class);


        server.onStateChange((state) ->
                System.err.println(System.currentTimeMillis() + "|| Server::Server - onStateChange: new state " + state.name()));

        if (cluster.length == 0)
        {
            server.bootstrap().join();
            System.err.println(System.currentTimeMillis() + "|| Server::Server - bootstrapped cluster");
        }
        else
        {
            server.join(cluster).join();
            System.err.println(System.currentTimeMillis() + "|| Server::Server - joined cluster " + cluster);
        }

        Thread inputThread = new Thread()
        {
            @Override
            public void run()
            {
                Scanner kbScanner = new Scanner(System.in);

                for (; ; )
                {
                    String input = kbScanner.nextLine();
                    System.err.println("Server::Server::kbThread - input: " + input);

                    switch (input)
                    {
                        case "leave":
                            System.err.println("Server::run - leaving cluster");
                            server.leave().join();
                            break;
                        case "shutdown":
                            System.err.println("Server::run - inactivating server");
                            server.shutdown().join();
                            break;
                        case "hard quit":
                            System.err.println("Server::run - hard quitting");
                            server.shutdown().join();
                            System.exit(1);
                            break;
                        case "restart":
                            System.err.println("Server::run - restart: server.state: " + server.state().name());

                            if (server.state().equals(CopycatServer.State.INACTIVE))
                            {
                                server.join(cluster).join();
                                System.err.println("Server::run - rejoined cluster");
                            }
                            break;
                        case "state":
                            System.err.println("Server::run - state: " + server.state().name());
                            break;
                        case "running":
                            System.err.println("Server::run - isRunning: " + server.isRunning());
                            break;
                        case "cluster":
                            Cluster cluster = server.cluster();
                            System.err.println("Server::run - cluster on term " + cluster.term() + " with leader " + cluster.leader() + " and members " + cluster.members());
                            break;
                        default:
                            System.err.println("Server::run - unrecognized command " + input);
                    }
                }
            }
        };

        inputThread.start();
    }

    public static Server create(InetAddress serverIp, int port, String... addresses)
    {
        Address[] cluster = new Address[addresses.length];

        if (addresses.length > 0)
        {
            int index = 0;

            for (String hostName : addresses)
            {
                cluster[index++] = new Address(hostName, port);
            }
        }

        Address serverAddress = new Address(serverIp.getHostAddress(), port);

        return new Server(serverAddress, cluster);
    }

    public static Server create(String serverIp, int port, String... addresses) throws UnknownHostException
    {
        return Server.create(InetAddress.getByName(serverIp), port, addresses);
    }
}
