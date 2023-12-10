import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

public class FServerStorage {

  private String rootDirectory; // The root directory of the file system

  public FServerStorage(String rootDirectory) {
    this.rootDirectory = rootDirectory;
  }

  public void setRootDirectory(String rootDirectory) {
    this.rootDirectory = rootDirectory;
  }

  public boolean putFile(
    String username,
    String sourcePath,
    String destinationPath
  ) {
    try {
      Path sourceFilePath = Path.of(sourcePath);
      Path destinationFilePath = Path.of(
        getUserDirectory(username),
        destinationPath
      );

      Files.createDirectories(destinationFilePath.getParent());
      Files.copy(
        sourceFilePath,
        destinationFilePath,
        StandardCopyOption.REPLACE_EXISTING
      );

      return true;
    } catch (IOException e) {
      logError("Error putting file", e);
      return false;
    }
  }

  public boolean getFile(
    String username,
    String sourcePath,
    String destinationPath
  ) {
    Path sourceFilePath = Path.of(getUserDirectory(username), sourcePath);
    Path destinationFilePath = Path.of(destinationPath);

    try {
      Files.copy(
        sourceFilePath,
        destinationFilePath,
        StandardCopyOption.REPLACE_EXISTING
      );
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  public boolean copyFile(
    String username,
    String sourcePath,
    String destinationPath
  ) {
    Path sourceFilePath = Path.of(getUserDirectory(username), sourcePath);
    Path destinationFilePath = Path.of(
      getUserDirectory(username),
      destinationPath
    );

    try {
      Files.copy(
        sourceFilePath,
        destinationFilePath,
        StandardCopyOption.REPLACE_EXISTING
      );
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  public boolean removeFile(String username, String filePath) {
    Path fileToDelete = Path.of(getUserDirectory(username), filePath);

    try {
      Files.deleteIfExists(fileToDelete);
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  public String getFileInfo(String username, String filePath) {
    Path fileToInspect = Path.of(getUserDirectory(username), filePath);
    File file = fileToInspect.toFile();

    if (file.exists()) {
      return (
        "File Name: " +
        file.getName() +
        "\nFile Size: " +
        file.length() +
        " bytes" +
        "\nLast Modified: " +
        file.lastModified()
      );
    } else {
      return "File not found";
    }
  }

  public String listFiles(String username, String directoryPath) {
    Path userDirectory = Path.of(getUserDirectory(username), directoryPath);

    if (Files.isDirectory(userDirectory)) {
      try {
        List<String> fileList = Files
          .list(userDirectory)
          .map(Path::getFileName)
          .map(Path::toString)
          .collect(Collectors.toList());
        return String.join(", ", fileList);
      } catch (IOException e) {
        e.printStackTrace();
        return "Error listing files.";
      }
    } else {
      return "Invalid directory.";
    }
  }

  public boolean createDirectory(String username, String newDirectoryPath) {
    Path newDirectory = Path.of(getUserDirectory(username), newDirectoryPath);
    try {
        Files.createDirectories(newDirectory);
        return true;
    } catch (IOException e) {
        e.printStackTrace();
        return false;
    }
}


  private String getUserDirectory(String username) {
    return rootDirectory + File.separator + username + File.separator;
  }

  private void logError(String message, Exception e) {
    System.err.println(message);
    e.printStackTrace();
  }
}
