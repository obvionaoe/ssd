package pt.up.fc.dcc.ssd.blockchain.transactions;

import pt.up.fc.dcc.ssd.common.Repository;
import pt.up.fc.dcc.ssd.p2p.node.Id;

import java.util.ArrayList;
import java.util.List;

import static pt.up.fc.dcc.ssd.common.Serializable.toObject;

public class TransactionRepo implements Repository {
    public ArrayList<Transaction> transactionsList = new ArrayList<Transaction>();

    @Override
    public boolean containsKey(Id key) {
        return false;
    }

    @SuppressWarnings("unchecked")
    public Transaction get() {
        return transactionsList.get(0);
    }

    @Override
    public boolean put(Id key, byte[] byteArray) {
        System.out.println("New Transaction!");
        Transaction transaction = (Transaction) toObject(byteArray);
        transactionsList.add(transaction);
        return true;
    }
}
