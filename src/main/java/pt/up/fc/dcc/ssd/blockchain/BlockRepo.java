package pt.up.fc.dcc.ssd.blockchain;

import pt.up.fc.dcc.ssd.common.Repository;
import pt.up.fc.dcc.ssd.p2p.node.Id;

import java.util.ArrayList;
import java.util.List;

import static pt.up.fc.dcc.ssd.common.Serializable.toByteArray;
import static pt.up.fc.dcc.ssd.common.Serializable.toObject;

public class BlockRepo implements Repository {
    List<Block> blockchain = new ArrayList<>();
    @Override
    public boolean containsKey(Id key) {
        return false;
    }

    @Override
    public byte[] get(Id key) {
        return toByteArray(blockchain);
    }

    @Override
    public boolean put(Id key, byte[] byteArray) {
        // TODO: validate block
        Block newBlock = (Block) toObject(byteArray);
        blockchain.add(newBlock);
        return false;
    }
}
