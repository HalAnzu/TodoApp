package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.Task;

/**
 * tasksテーブルへのデータアクセスを担うクラス
 */
public class TaskRepository extends BaseRepository {

    /**
     * 全てのタスク情報を取得する（次回の一覧画面で大活躍します）
     */
    public List<Task> findAll() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Task task = new Task();
                task.setId(rs.getInt("task_id"));
                task.setUserId(rs.getInt("user_id"));
                task.setTitle(rs.getString("title"));
                task.setDescription(rs.getString("description"));
                task.setStatus(rs.getString("status"));
                task.setCreatedAt(rs.getTimestamp("created_at"));
                task.setUpdatedAt(rs.getTimestamp("updated_at"));
                tasks.add(task);
            }
        } catch (SQLException e) {
            handleSQLException(e, "findAll");
        }
        return tasks;
    }
    
    /**
     * 指定されたユーザーIDに紐づくタスク一覧のみを全件取得する
     */
    public List<Task> findByUserId(int userId) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE user_id = ? ORDER BY created_at DESC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Task task = new Task();
                    task.setId(rs.getInt("task_id"));
                    task.setUserId(rs.getInt("user_id"));
                    task.setTitle(rs.getString("title"));
                    task.setDescription(rs.getString("description"));
                    task.setStatus(rs.getString("status"));
                    task.setCreatedAt(rs.getTimestamp("created_at"));
                    task.setUpdatedAt(rs.getTimestamp("updated_at"));
                    tasks.add(task);
                }
            }
        } catch (SQLException e) {
            handleSQLException(e, "findByUserId");
        }
        return tasks;
    }

    /**
     * 操作対象のタスクが、本当にそのユーザーのものか検証する（認可チェック）
     */
    public boolean isOwner(int taskId, int userId) {
        String sql = "SELECT COUNT(*) FROM tasks WHERE task_id = ? AND user_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, taskId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0; // 1件以上存在すれば所有者である
                }
            }
        } catch (SQLException e) {
            handleSQLException(e, "isOwner");
        }
        return false;
    }

    /**
     * 特定のユーザーに紐づくタスクの総件数を取得する
     */
    public int countByUserId(int userId) {
        String sql = "SELECT COUNT(*) FROM tasks WHERE user_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            handleSQLException(e, "countByUserId");
        }
        return 0;
    }

    /**
     * 所有者確認付きのタスク削除（不正なパラメータ書き換えによる他者タスク削除を完全防御）
     */
    public boolean deleteByIdAndUserId(int taskId, int userId) {
        String sql = "DELETE FROM tasks WHERE task_id = ? AND user_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, taskId);
            ps.setInt(2, userId);
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0; // 削除成功ならtrue
        } catch (SQLException e) {
            handleSQLException(e, "deleteByIdAndUserId");
            return false;
        }
    }
}