import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class CreateTable {

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/NovelSyncERP?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "admin";

        String createSql =
            "CREATE TABLE notification (" +
            "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
            "title VARCHAR(255) NOT NULL," +
            "message VARCHAR(1000) NOT NULL," +
            "is_read BOOLEAN NOT NULL," +
            "type VARCHAR(255) NOT NULL," +
            "reference_id BIGINT," +
            "created_at DATETIME NOT NULL," +
            "recipient_id BIGINT" +
            ");";

        try (Connection conn = DriverManager.getConnection(url, user, password); Statement stmt = conn.createStatement()) {
            stmt.execute(createSql);
            System.out.println("Notification table created.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
