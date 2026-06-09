import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class ClearDB {

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/NovelSyncERP?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "admin";

        try (Connection conn = DriverManager.getConnection(url, user, password); Statement stmt = conn.createStatement()) {
            int rows = stmt.executeUpdate("UPDATE DATABASECHANGELOG SET MD5SUM = NULL");
            System.out.println("Cleared " + rows + " checksums in DATABASECHANGELOG.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
