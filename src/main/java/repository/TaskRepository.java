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
}