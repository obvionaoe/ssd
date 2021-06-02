package pt.up.fc.dcc.ssd.blockchain;

import pt.up.fc.dcc.ssd.common.Repository;
import pt.up.fc.dcc.ssd.p2p.node.Id;

public class BlockchainRepo implements Repository {
    @Override
    public boolean containsKey(Id key) {
        return false;
    }

    @Override
    public byte[] get(Id key) {
        return new byte[0];
    }

    @Override
    public boolean put(Id key, byte[] byteArray) {
        return false;
    }
}
