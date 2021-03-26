import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class Utils {

    public static String applySecureHash(String input){
        try {
            MessageDigest digest = MessageDigest.getInstance("Sha256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuffer hexString = new StringBuffer(); //hexadecimal hash
            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getMerkleRoot(ArrayList<Transaction> transactions) {
        int count = transactions.size();

        List<String> previousTreeLayer = new ArrayList<String>();
        for(Transaction transaction : transactions) {
            previousTreeLayer.add(transaction.transactionId);
        }
        List<String> treeLayer = previousTreeLayer;

        while(count > 1) {
            treeLayer = new ArrayList<String>();
            for(int i=1; i < previousTreeLayer.size(); i+=2) {
                treeLayer.add(applySecureHash(previousTreeLayer.get(i-1) + previousTreeLayer.get(i)));
            }
            count = treeLayer.size();
            previousTreeLayer = treeLayer;
        }

        String merkleRoot = (treeLayer.size() == 1) ? treeLayer.get(0) : "";
        return merkleRoot;
    }

    public static String getDifficultyString(int difficulty) {
        return new String(new char[difficulty]).replace('\0', '0');
    }

}
