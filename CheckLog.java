import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class CheckLog {

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/NovelSyncERP?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "admin";

        try (Connection conn = DriverManager.getConnection(url, user, password); Statement stmt = conn.createStatement()) {
            ResultSet rs2 = stmt.executeQuery("SELECT count(*) FROM notification");
            if (rs2.next()) {
                System.out.println("Notification table exists with " + rs2.getInt(1) + " rows.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
