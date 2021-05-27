package pt.up.fc.dcc.ssd.auction;

import pt.up.fc.dcc.ssd.p2p.node.Id;

import java.io.*;

public class ClientItem implements Serializable {
    public Id topic;
    public String bid; // TODO: type Bid?
    public String item;
    public Id clientId;
    public final long serialVersionUID = 1L;


    ClientItem(Id clientId, Id topic, String bid, String item){
        this.clientId = clientId;
        this.topic = topic;
        this.bid = bid;
        this.item = item;
    }

    public byte[] toByteArray() {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(this);
            out.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ClientItem toObject(byte[] arr) {
        ByteArrayInputStream bis = new ByteArrayInputStream(arr);
        try (ObjectInput in = new ObjectInputStream(bis)) {
            return (ClientItem) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}

