import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FServerAccessControl {
    private Map<String, String> accessControlMap;

    public FServerAccessControl(String accessConfigFilePath) {
        this.accessControlMap = loadAccessControlConfig(accessConfigFilePath);
    }

    private Map<String, String> loadAccessControlConfig(String filePath) {
        Map<String, String> accessMap = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    accessMap.put(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return accessMap;
    }

    public boolean checkAccessPermission(String username, String permission) {
        String userPermission = accessControlMap.get(username);
        return userPermission != null && userPermission.contains(permission);
    }
}
