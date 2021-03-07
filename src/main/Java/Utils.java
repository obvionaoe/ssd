import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class Utils {

    //porque Sha-256
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
}
