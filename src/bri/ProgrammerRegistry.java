package bri;

import java.util.HashMap;
import java.util.Map;
public class ProgrammerRegistry {
    private static Map<String, UserData> users = new HashMap<>();

    static class UserData {
        String password;
        String ftpUrl;
        UserData(String p, String f) { this.password = p; this.ftpUrl = f; }
    }

    public static boolean exists(String login) {
        return users.containsKey(login);
    }

    public static boolean authenticate(String login, String pass) {
        return users.containsKey(login) && users.get(login).password.equals(pass);
    }

    public static void register(String login, String pass, String ftpUrl) {
        users.put(login, new UserData(pass, ftpUrl));
    }

    public static String getFtpUrl(String login) {
        return users.get(login).ftpUrl;
    }

    public static void updateFtpUrl(String login, String newFtpUrl) {
        users.get(login).ftpUrl = newFtpUrl;
    }
}
