package pt.up.fc.dcc.ssd.common;

import pt.up.fc.dcc.ssd.p2p.node.Id;

public interface Repository {
    /**
     * Checks if the provided Id has been stored in the Repository
     *
     * @param key the Id, topics: a hashed word; bids: a bid Id; Blockchain: a Block Id
     * @return true if the key exists in the repo, false otherwise
     */
    boolean containsKey(Id key);

    /**
     * Gets the stored item as a byte array
     *
     * @param key the Id of the item
     * @return a byte array containing the item associated with the provided key
     */
    byte[]get(Id key);

    /**
     * Inserts a byte array in the repository with the provided key
     *  @param key       the Id
     * @param byteArray the byte array to insert in the repository
     * @return
     */
    boolean put(Id key, byte[] byteArray);
}
