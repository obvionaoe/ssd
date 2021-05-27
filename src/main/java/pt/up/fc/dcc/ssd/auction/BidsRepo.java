package pt.up.fc.dcc.ssd.auction;

import pt.up.fc.dcc.ssd.common.Repository;
import pt.up.fc.dcc.ssd.p2p.node.Id;

import java.io.Serializable;

public class BidsRepo implements Repository, Serializable {

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
