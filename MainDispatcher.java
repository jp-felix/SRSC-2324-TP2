import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.*;
import java.net.Socket;
import java.util.Date;
import javax.net.ssl.SSLSocket;

public class MainDispatcher {

  private FServerAuth fServerAuth;
  private FServerAccessControl fServerAccessControl;
  private FServerStorage fServerStorage;

  public MainDispatcher(
    FServerAuth fServerAuth,
    FServerAccessControl fServerAccessControl,
    FServerStorage fServerStorage
  ) {
    this.fServerAuth = fServerAuth;
    this.fServerAccessControl = fServerAccessControl;
    this.fServerStorage = fServerStorage;
  }

  public void processRequest(SSLSocket clientSocket) {
    try {
      BufferedReader reader = new BufferedReader(
        new InputStreamReader(clientSocket.getInputStream())
      );
      PrintWriter writer = new PrintWriter(
        clientSocket.getOutputStream(),
        true
      );

      String command = reader.readLine();
      String[] parts = command.split(" ");
      String operation = parts[0].toLowerCase();

      switch (operation) {
        case "login":
          handleLogin(parts, writer);
          break;
        case "ls":
          handleListFiles(parts, writer);
          break;
        case "mkdir":
          handleCreateDirectory(parts, writer);
          break;
        case "get":
          handleGetFile(parts, writer);
          break;
        case "cp":
          handleCopyFile(parts, writer);
          break;
        case "rm":
          handleRemoveFile(parts, writer);
          break;
        case "file":
          handleFileInfo(parts, writer);
          break;
        default:
          writer.println("Invalid command");
          break;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void handleLogin(String[] parts, PrintWriter writer) {
    String username = parts[1];
    String hashedPassword = parts[2];
    boolean isAuthenticated = fServerAuth.authenticateUser(
      username,
      hashedPassword
    );

    if (isAuthenticated) {
      //   boolean hasAccess = fServerAccessControl.checkAccessPermission(username, operation);
      boolean hasAccess = true; // deal with this later

      if (hasAccess) {
        String authToken = generateAuthToken(username);
        writer.println("Login successful. AuthToken: " + authToken);
      } else {
        writer.println("Access denied.");
      }
    } else {
      writer.println("Authentication failed.");
    }
  }

  private void handleListFiles(String[] parts, PrintWriter writer) {
    String username = parts[1];
    String directoryPath = parts[2];
    String fileList = fServerStorage.listFiles(username, directoryPath);
    writer.println(fileList);
  }

  private void handleCreateDirectory(String[] parts, PrintWriter writer) {
    String username = parts[1];
    String newDirectoryPath = parts[2];
    boolean isDirectoryCreated = fServerStorage.createDirectory(
      username,
      newDirectoryPath
    );
    if (isDirectoryCreated) {
      writer.println("Directory created successfully.");
    } else {
      writer.println("Failed to create directory.");
    }
  }

  private void handleGetFile(String[] parts, PrintWriter writer) {
    String username = parts[1];
    String sourcePath = parts[2];
    String destinationPath = parts[3];
    boolean isFileGet = fServerStorage.getFile(
      username,
      sourcePath,
      destinationPath
    );
    writer.println("File get: " + isFileGet);
  }

  private void handleCopyFile(String[] parts, PrintWriter writer) {
    String username = parts[1];
    String sourcePath = parts[2];
    String destinationPath = parts[3];
    boolean isFileCopied = fServerStorage.copyFile(
      username,
      sourcePath,
      destinationPath
    );
    writer.println("File copy: " + isFileCopied);
  }

  private void handleRemoveFile(String[] parts, PrintWriter writer) {
    String username = parts[1];
    if (fServerAccessControl.checkAccessPermission(username, "remove")) {
      String filePath = parts[2];
      boolean isFileRemoved = fServerStorage.removeFile(username, filePath);
      if (isFileRemoved) {
        writer.println("File removed successfully.");
      } else {
        writer.println("Failed to remove file.");
      }
    } else {
      writer.println(
        "Access denied. You do not have permission to remove files."
      );
    }
  }

  private void handleFileInfo(String[] parts, PrintWriter writer) {
    String username = parts[1];
    if (fServerAccessControl.checkAccessPermission(username, "read")) {
      String filePath = parts[2];
      String fileInfo = fServerStorage.getFileInfo(username, filePath);
      writer.println(fileInfo);
    } else {
      writer.println(
        "Access denied. You do not have permission to view file information."
      );
    }
  }

  private String generateAuthToken(String username) {
    String secretKey = "testSecret";//change later on
    long expirationTimeMillis = System.currentTimeMillis() + 3600000;
    String authToken = Jwts
      .builder()
      .setSubject(username)
      .setExpiration(new Date(expirationTimeMillis))
      .signWith(SignatureAlgorithm.HS256, secretKey)
      .compact();

    return authToken;
  }
}
