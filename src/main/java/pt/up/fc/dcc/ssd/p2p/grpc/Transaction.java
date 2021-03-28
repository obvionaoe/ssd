package pt.up.fc.dcc.ssd.p2p.grpc;

public class Transaction{
    public String transactionId;
    public String SenderId;
    public String ReceiverId;
    public long timestamp;
    public float bid;

    public Transaction(String transactionId, String SenderId, String ReceiverId, long timestamp, float bid){
        this.transactionId = transactionId;
        this.SenderId = SenderId;
        this ReceiverId = ReceiverId;
        this.timestamp = timestamp;
        this.bid = bid;
    }

    public class Request{
        public float bid;
        public String SenderId;
        public String pKey;
    }

    public class Response{
        public String SenderId;
        public String ReceiverId;
        public String pKey;
    }
}