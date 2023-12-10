import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class FServerAuth {

  private Map<String, String> userCredentials;

  public FServerAuth() {
    this.userCredentials =
      loadUserCredentialsFromFile("path/to/user_credentials.txt");
  }

  public boolean authenticateUser(String username, String password) {
    if (userCredentials.containsKey(username)) {
      String hashedPassword = userCredentials.get(username);
      String hashedInputPassword = hashPassword(password);
      return hashedInputPassword.equals(hashedPassword);
    }
    return false;
  }

  private Map<String, String> loadUserCredentialsFromFile(String filePath) {
    Map<String, String> credentials = new HashMap<>();
    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] parts = line.split(":");
        if (parts.length == 2) {
          String username = parts[0];
          String hashedPassword = parts[1];
          credentials.put(username, hashedPassword);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return credentials;
  }

  private String hashPassword(String password) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] passwordBytes = password.getBytes();
      byte[] hashedBytes = md.digest(passwordBytes);
      StringBuilder stringBuilder = new StringBuilder();
      for (byte b : hashedBytes) {
        stringBuilder.append(String.format("%02x", b));
      }

      return stringBuilder.toString();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static void main(String[] args) {
    FServerAuth serverAuth = new FServerAuth();
    String username = "teste";
    String password = "teste123";
    if (serverAuth.authenticateUser(username, password)) {
      System.out.println("Authentication successful for user: " + username);
    } else {
      System.out.println("Authentication failed for user: " + username);
    }
  }
}
