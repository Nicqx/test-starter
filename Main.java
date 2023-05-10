import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.Properties;
import java.util.concurrent.TimeUnit;


public class Main {

    private static SecretKeySpec secretKey;
    private static final String ALGORITHM = "AES";
    // this key is only used to hide the visible and easily readable password in the user.config file
    static final String secretKeyy = "#H@Ew<QPmD%jknUne>YrhQHz^ja*6PBwJ79D@=^67kxcxTA.TiF,q5XRb6LZ~Ha4w";
    static String account = "";
    static String pass = "";

    public static void main(String[] args) throws IOException {
        long startTime = System.currentTimeMillis();
        readUserConfig();

        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
        // configure options parameter to Chrome driver
        ChromeOptions o = new ChromeOptions();
        // add Incognito parameter
        o.addArguments("--incognito");
        // DesiredCapabilities object
        DesiredCapabilities c = new DesiredCapabilities();
        //set capability to browser
        c.setCapability(ChromeOptions.CAPABILITY, o);
        WebDriver driver = new ChromeDriver(o);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));


        // Launch Website
        driver.get("https://epgweb.sero.wh.rnd.internal.ericsson.com/testjobs");
        loginToEPGTestPortal(driver, wait, account, pass);


        driver.close();
        long endTime = System.currentTimeMillis();
        System.out.println("Runtime: " + (endTime - startTime) / 1000 + " seconds");
    }

    static void loginToEPGTestPortal(WebDriver driver, WebDriverWait wait, String account, String pass) {
        // adding the email address
        wait.until(ExpectedConditions.elementToBeClickable(By.id("edit-name")));
        driver.findElement(By.id("edit-name")).sendKeys(account);
        // adding the password
        wait.until(ExpectedConditions.elementToBeClickable(By.id("edit-pass")));
        driver.findElement(By.id("edit-pass")).sendKeys(pass);
        // click to the login button
        wait.until(ExpectedConditions.elementToBeClickable(By.id("edit-submit")));
        driver.findElement(By.id("edit-submit")).click();
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

        Main.account = prop.getProperty("account");
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
