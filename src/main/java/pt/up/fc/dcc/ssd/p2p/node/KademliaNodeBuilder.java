package pt.up.fc.dcc.ssd.p2p.node;

import pt.up.fc.dcc.ssd.p2p.grpc.KademliaImpl;
import pt.up.fc.dcc.ssd.p2p.node.exceptions.KademliaNodeBuilderException;
import pt.up.fc.dcc.ssd.p2p.node.util.NodeType;

import java.util.ArrayList;
import java.util.Arrays;

import static pt.up.fc.dcc.ssd.p2p.Config.*;

public class KademliaNodeBuilder {
    private ID id = new ID();
    private int port = 0;
    private String address = "localhost";
    private NodeType type = NodeType.NODE;
    private final ArrayList<KademliaImpl> implementations = new ArrayList<>();

    public KademliaNodeBuilder id(ID id) {
        this.id = id;
        return this;
    }

    public KademliaNodeBuilder port(int port) {
        this.port = port;
        return this;
    }

    public KademliaNodeBuilder address(String address) {
        this.address = address;
        return this;
    }

    public KademliaNodeBuilder addService(KademliaImpl kademliaImpl) {
        implementations.add(kademliaImpl);
        return this;
    }

    public KademliaNodeBuilder addServices(KademliaImpl... kademliaImpl) {
        implementations.addAll(Arrays.asList(kademliaImpl));
        return this;
    }

    public KademliaNodeBuilder type(NodeType type) {
        this.type = type;
        return this;
    }

    public KademliaNode build() throws KademliaNodeBuilderException {
        switch (type) {
            case BOOTSTRAP_NODE:
                return new KademliaNode(BOOTSTRAP_NODE_ID, BOOTSTRAP_NODE_ADDR, BOOTSTRAP_NODE_PORT, implementations);
            case NODE:
            default:
                return new KademliaNode(id, address, port, implementations);
        }
    }
}
