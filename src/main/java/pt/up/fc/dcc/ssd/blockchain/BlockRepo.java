package pt.up.fc.dcc.ssd.blockchain;

import pt.up.fc.dcc.ssd.common.Repository;
import pt.up.fc.dcc.ssd.p2p.node.Id;
import pt.up.fc.dcc.ssd.p2p.node.KademliaNode;

import java.util.ArrayList;
import java.util.List;

import static pt.up.fc.dcc.ssd.blockchain.Utils.isChainValid;
import static pt.up.fc.dcc.ssd.common.Serializable.toByteArray;
import static pt.up.fc.dcc.ssd.common.Serializable.toObject;

public class BlockRepo implements Repository {
    KademliaNode self;

    public BlockRepo(KademliaNode self){
        this.self = self;
    }
    @Override
    public boolean containsKey(Id key) {
        return false;
    }

    @Override
    public byte[] get(Id key) {
        return toByteArray(self.getBlockchain());
    }

    @Override
    public boolean put(Id key, byte[] byteArray) {
        Block newBlock = (Block) toObject(byteArray);
        self.getBlockchain().addBlock(newBlock);
        //isChainValid(self.getBlockchain());
        return false;
    }
}
