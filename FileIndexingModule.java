import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class FileIndexingModule {

  private String userRootDirectory;
  private static final String INDEX_FILE_PATH = "data.txt";

  public FileIndexingModule(String userRootDirectory) {
    this.userRootDirectory = userRootDirectory;
  }

  public void indexFile(
    String username,
    String filePath,
    String privateKeyFilePath
  ) {
    try {
      byte[] fileContent = Files.readAllBytes(
        Paths.get(userRootDirectory, username, filePath)
      );
      byte[] encryptedContent = encrypt(fileContent);
      byte[] signature = sign(encryptedContent, privateKeyFilePath);
      String hash = calculateHash(encryptedContent);
      saveIndexData(username, filePath, encryptedContent, signature, hash);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private byte[] encrypt(byte[] data) throws NoSuchAlgorithmException {
    try {
      KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
      keyGenerator.init(256);
      SecretKey secretKey = keyGenerator.generateKey();
      Cipher cipher = Cipher.getInstance("AES");
      cipher.init(Cipher.ENCRYPT_MODE, secretKey);
      byte[] encryptedData = cipher.doFinal(data);
      return encryptedData;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  private byte[] sign(byte[] data, String privateKeyFilePath)
    throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    try {
      byte[] privateKeyBytes = Files.readAllBytes(
        Paths.get(privateKeyFilePath)
      );
      PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

      Signature signature = Signature.getInstance("SHA256withRSA");
      signature.initSign(privateKey);
      signature.update(data);
      return signature.sign();
    } catch (IOException | InvalidKeySpecException e) {
      e.printStackTrace();
      return null;
    }
  }

  private String calculateHash(byte[] data) throws NoSuchAlgorithmException {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] hashBytes = md.digest(data);
      StringBuilder result = new StringBuilder();
      for (byte hashByte : hashBytes) {
        result.append(String.format("%02x", hashByte));
      }
      return result.toString();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      throw e;
    }
  }

  private void saveIndexData(
    String username,
    String filePath,
    byte[] encryptedContent,
    byte[] signature,
    String hash
  ) {
    try {
      String indexedData = String.format(
        "Username: %s\nFile Path: %s\nEncrypted Content: %s\nSignature: %s\nHash: %s\n\n",
        username,
        filePath,
        bytesToHex(encryptedContent),
        bytesToHex(signature),
        hash
      );
      try (FileOutputStream fos = new FileOutputStream(INDEX_FILE_PATH, true)) {
        fos.write(indexedData.getBytes());
      }

      System.out.println("Index data saved successfully.");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private String bytesToHex(byte[] bytes) {
    StringBuilder result = new StringBuilder();
    for (byte b : bytes) {
      result.append(String.format("%02x", b));
    }
    return result.toString();
  }

  public static void main(String[] args) {
    FileIndexingModule fileIndexingModule = new FileIndexingModule(
      "/root"
    );
    // watch video again to solve issues and to clarify privateKeyFilePath doubts
    fileIndexingModule.indexFile("test", "document.txt", "test/chave_privada.pem");
  }
}
