import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Scanner;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class FServerClient {

  private static final String SERVER_ADDRESS = "localhost";
  private static final int SERVER_PORT = 12345;

  public static void main(String[] args) {
    try {
      System.setProperty("javax.net.ssl.trustStore", "client-truststore.jks");
      System.setProperty("javax.net.ssl.trustStorePassword", "password");
      SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
      SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket(
        SERVER_ADDRESS,
        SERVER_PORT
      );

      BufferedReader reader = new BufferedReader(
        new InputStreamReader(socket.getInputStream())
      );
      PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
      Scanner scanner = new Scanner(System.in);

      System.out.println("Connected to FServer");
      authenticateUser(writer, scanner, socket);
      while (true) {
        System.out.print(
          "Enter command (ls, mkdir, put, get, cp, rm, file, exit): "
        );
        String command = scanner.nextLine();
        writer.println(command);
        String response = reader.readLine();
        System.out.println("Server Response: " + response);
        if ("exit".equalsIgnoreCase(command)) {
          break;
        }
      }
    } catch (IOException e) {
      handleException("Error establishing connection", e);
    }
  }

  private static void authenticateUser(
    PrintWriter writer,
    Scanner scanner,
    SSLSocket socket
  ) {
    try {
      System.out.print("Enter username: ");
      String username = scanner.nextLine().trim();

      if (username.isEmpty()) {
        System.out.println("Username cannot be empty.");
        return;
      }

      if (!isValidUsername(username)) {
        System.out.println("Username can only contain Alphanumeric characters only..");
        return;
      }

      System.out.print("Enter password: ");
      Console console = System.console();
      char[] passwordChars = console.readPassword("Enter password: ");
      String password = new String(passwordChars);

      if (password.isEmpty()) {
        System.out.println("Password cannot be empty.");
        return;
      }

      if (!isStrongPassword(password)) {
        System.out.println("Password needs to be longer (min 8 chars).");
        return;
      }

      String hashedPassword = hashPassword(password);

      writer.println("login " + username + " " + hashedPassword);
      String response = new BufferedReader(
        new InputStreamReader(socket.getInputStream())
      )
        .readLine(); //dont delete may need later

      log("Authentication attempt for user: " + username);
      System.out.println("Authentication attempt for user: " + username);

      Arrays.fill(passwordChars, ' ');
    } catch (IOException e) {
      handleException("Error during authentication", e);
    }
  }

  private static String hashPassword(String password) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] hashedBytes = md.digest(password.getBytes());
      return bytesToHex(hashedBytes);
    } catch (NoSuchAlgorithmException e) {
      handleException("Error hashing password", e);
      return null;
    }
  }

  private static String bytesToHex(byte[] bytes) {
    StringBuilder result = new StringBuilder();
    for (byte b : bytes) {
      result.append(String.format("%02x", b));
    }
    return result.toString();
  }

  private static void log(String message) {
    System.out.println("Log: " + message);
  }

  private static void handleException(String message, Exception e) {
    log("Exception: " + message + " - " + e.getMessage());
    e.printStackTrace();
  }

  private static boolean isStrongPassword(String password) {
    return password.length() >= 8;
  }

  private static boolean isValidUsername(String username) {
    return username.matches("^[a-zA-Z0-9]+$");
}
}
