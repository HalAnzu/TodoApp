package test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import database.ConnectionFactory;

/**
 * DB接続および初期データ取得テスト用実行クラス
 */
public class ConnectionTest {

    public static void main(String[] args) {
        System.out.println("=== TodoApp データベース接続テスト開始 ===");

        // try-with-resources により、Connection、PreparedStatement、ResultSet は自動クローズされます
        try (Connection conn = ConnectionFactory.getConnection()) { // [cite: 1154]
            
            // 1. 基本接続状態の確認
            if (conn != null && !conn.isClosed()) { // [cite: 1155]
                System.out.println("[SUCCESS] データベース接続の取得に成功しました。状態: Open");
            }

            // 2. メタデータを使用したテーブル存在確認
            DatabaseMetaData metaData = conn.getMetaData(); // [cite: 1001]
            try (ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                System.out.println("\n--- 存在するテーブル一覧 ---");
                while (tables.next()) {
                    System.out.println("テーブル名: " + tables.getString("TABLE_NAME"));
                }
            }

            // 3. usersテーブルの件数・内容確認テスト
            System.out.println("\n--- users テーブルのデータ確認 ---");
            String userSql = "SELECT user_id, username, email, created_at FROM users";
            try (PreparedStatement psUser = conn.prepareStatement(userSql);
                 ResultSet rsUser = psUser.executeQuery()) {
                
                while (rsUser.next()) {
                    System.out.printf("ID: %d | ユーザー名: %s | メール: %s | 作成日: %s%n",
                            rsUser.getInt("user_id"),
                            rsUser.getString("username"),
                            rsUser.getString("email"),
                            rsUser.getTimestamp("created_at"));
                }
            }

            // 4. tasksテーブルの件数・内容確認テスト（日本語文字化けチェック含む）
            System.out.println("\n--- tasks テーブルのデータ確認（日本語チェック） ---");
            String taskSql = "SELECT task_id, title, status, priority FROM tasks";
            try (PreparedStatement psTask = conn.prepareStatement(taskSql);
                 ResultSet rsTask = psTask.executeQuery()) {
                
                while (rsTask.next()) {
                    System.out.printf("タスクID: %d | タイトル: %s | ステータス: %s | 優先度: %s%n",
                            rsTask.getInt("task_id"),
                            rsTask.getString("title"), // ここで日本語が化けないかチェック
                            rsTask.getString("status"),
                            rsTask.getString("priority"));
                }
            }

        } catch (SQLException e) {
            System.err.println("\n[FAILURE] データベーステスト中にエラーが発生しました。");
            System.err.println("エラーメッセージ: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n=== TodoApp データベース接続テスト終了 ===");
    }
}
