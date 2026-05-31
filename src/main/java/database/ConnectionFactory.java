package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * データベース接続（Connection）を一元管理するファクトリークラス
 */
public class ConnectionFactory {

    // 接続情報の設定（文字エンコーディング、タイムゾーンを明示）
    private static final String DB_URL = 
        "jdbc:mysql://localhost:3306/taskmanager?characterEncoding=utf8&useUnicode=true&serverTimezone=Asia/Tokyo"; // [cite: 977, 1140]
    private static final String DB_USER = "root"; // 通常はroot（環境に合わせて変更してください） [cite: 978]
    private static final String DB_PASSWORD = "hinaharu"; // ★ご自身のMySQLのrootパスワードを設定してください [cite: 979]
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver"; // MySQL 8.0以降用ドライバー [cite: 980]

    // クラス読み込み時に一度だけドライバーをロードする
    static {
        try {
            Class.forName(DB_DRIVER); // [cite: 983]
            System.out.println("[INFO] MySQL JDBC Driver loaded successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("[ERROR] MySQL JDBC Driver not found: " + e.getMessage());
            System.err.println("[ERROR] Please ensure mysql-connector-j.jar is in WEB-INF/lib");
        }
    }

    /**
     * データベースへの新しい接続を取得します
     * @return Connection オブジェクト
     * @throws SQLException 接続失敗時にスロー
     */
    public static Connection getConnection() throws SQLException { // [cite: 973]
        try {
            System.out.println("[DEBUG] Attempting to connect to: " + DB_URL); // [cite: 1058]
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); // [cite: 982]
            System.out.println("[INFO] Database connection successful."); // [cite: 1060]
            return conn;
        } catch (SQLException e) {
            System.err.println("[ERROR] Connection failed: " + e.getMessage()); // [cite: 1063]
            throw e; // [cite: 1064]
        }
    }
}