import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Properties;

public class Main {

    private static SecretKeySpec secretKey;
    private static final String ALGORITHM = "AES";
    // this key is only used to hide the visible and easily readable password in the user.config file
    static final String secretKeyy = "#H@Ew<QPmD%jknUne>YrhQHz^ja*6PBwJ79D@=^67kxcxTA.TiF,q5XRb6LZ~Ha4w";
    static String account = "";
    static String pass = "";

    public static void main(String[] args) throws InterruptedException, IOException {
        long startTime = System.currentTimeMillis();
        readUserConfig();

        long endTime = System.currentTimeMillis();
        System.out.println("Runtime: " + (endTime - startTime) / 1000 + " seconds");
    }

    static void readUserConfig() throws IOException {
        FileReader input = new FileReader("user.properties");
        Properties prop = new Properties();

        prop.load(input);
        input.close();
        if (!prop.getProperty("pass").startsWith("encrypted")) {
            String encryptedString = encrypt(prop.getProperty("pass"), secretKeyy);
            FileWriter output = new FileWriter("user.properties");
            prop.setProperty("pass", "encrypted" + encryptedString);
            prop.store(output, null);
            output.close();
        }

        Main.account = prop.getProperty("email");
        Main.pass = decrypt(prop.getProperty("pass").split("encrypted")[1], secretKeyy);
    }

    public static void prepareSecreteKey(String myKey) {
        MessageDigest sha = null;
        try {
            byte[] key = myKey.getBytes(StandardCharsets.UTF_8);
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static String encrypt(String strToEncrypt, String secret) {
        try {
            prepareSecreteKey(secret);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }

    public static String decrypt(String strToDecrypt, String secret) {
        try {
            prepareSecreteKey(secret);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } catch (Exception e) {
            System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
    }
}