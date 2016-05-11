package csc445.shavas.server;

import csc445.shavas.core.Canvas;
import csc445.shavas.core.Colors;
import csc445.shavas.core.Constants;
import csc445.shavas.core.GetQuery;
import csc445.shavas.core.Pixel;
import csc445.shavas.core.UpdateCommand;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.NettyTransport;
import io.atomix.catalyst.util.Listener;
import io.atomix.copycat.server.CopycatServer;
import io.atomix.copycat.server.storage.Storage;
import io.atomix.copycat.server.storage.StorageLevel;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public final class Server
{
    public static void main(String[] args) throws UnknownHostException
    {
        String[] cluster = {"127.0.0.1"};

        InetAddress serverAddress = InetAddress.getByName("localhost");

        Server server = Server.create(serverAddress, cluster);
    }

    private final CopycatServer server;

    private Server(Address serverAddress, List<Address> cluster)
    {
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

        Listener<CopycatServer.State> stateChangeListener = server.onStateChange((state) ->
                System.err.println("Server::Server - onStateChange: new state " + state.name()));

        server.bootstrap().join();

        Pixel testPixel = new Pixel(0, 0, Colors.BLACK);
        System.err.println("Server::Server - made test pixel");
    }

    public static Server create(InetAddress serverIp, String... addresses)
    {
        List<Address> cluster = new ArrayList<>();

        for (String hostName : addresses)
        {
            cluster.add(new Address(hostName, Constants.SERVER_PORT));
        }

        Address serverAddress = new Address(serverIp.getHostAddress(), Constants.SERVER_PORT);

        return new Server(serverAddress, cluster);
    }

    static class Builder
    {
        private InetAddress serverIp;
        private List<Address> clusterConfiguration = null;

        private Builder()
        {
        }

        public Builder builder()
        {
            return new Builder();
        }
    }
}
