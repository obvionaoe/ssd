package pt.up.fc.dcc.ssd.p2p.grpc;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import p2p.*;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {
    private static final Logger logger = Logger.getLogger(Client.class.getName());

    private final ClientGrpc.ClientBlockingStub cBlockingStub;

    public Client(Channel channel) {
        // 'channel' here is a Channel, not a ManagedChannel, so it is not this code's responsibility to
        // shut it down.

        // Passing Channels to code makes code easier to test and makes it easier to reuse Channels.
        cBlockingStub = ClientGrpc.newBlockingStub(channel);
    }

    /**
     * Greet server. If provided, the first element of {@code args} is the name to use in the
     * greeting. The second argument is the target server.
     */
    public static void main(String[] args) throws Exception {
        String server = "world";
        // Access a service running on the local machine on port 50050
        String target = "localhost:50050";

        if (args.length != 1) {
            System.err.println("ERROR: Please provide a topic");
            return;
        }


        // Create a communication channel to the server, known as a Channel. Channels are thread-safe
        // and reusable. It is common to create channels at the beginning of your application and reuse
        // them until the application shuts down.
        ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                // needing certificates.
                .usePlaintext()
                .keepAliveWithoutCalls(true)
                .build();
        Client client = new Client(channel);

        // Buy or sell
        String operation = args[0];

        // TODO topic is a hash
        String topic = args[1];

        String bid = args[2];

        ClientNode clientNode = new ClientNode(50001, new ClientImpl(), operation, topic, bid);

        // TODO make transaction for who has the topic in the network
        clientNode.start();
        clientNode.makeTransaction();

    }
}
