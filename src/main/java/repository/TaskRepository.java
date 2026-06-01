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
     * 全てのタスク情報を取得する
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
     * 指定されたユーザーIDに紐づくタスク一覧のみを、作成日時の降順で全件取得する
     */
    public List<Task> findByUserId(int userId) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE user_id = ? ORDER BY created_at DESC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            System.out.println("[DEBUG] Executing findByUserId SQL with userId: " + userId);
            
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
            System.out.println("[DEBUG] Retrieved " + tasks.size() + " tasks for userId: " + userId);
        } catch (SQLException e) {
            handleSQLException(e, "findByUserId");
        }
        return tasks;
    }

    /**
     * 主キー（task_id）で特定のタスクを1件取得する（編集画面の初期表示用）
     */
    public Task findById(int taskId) {
        String sql = "SELECT * FROM tasks WHERE task_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, taskId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Task task = new Task();
                    task.setId(rs.getInt("task_id"));
                    task.setUserId(rs.getInt("user_id"));
                    task.setTitle(rs.getString("title"));
                    task.setDescription(rs.getString("description"));
                    task.setStatus(rs.getString("status"));
                    task.setCreatedAt(rs.getTimestamp("created_at"));
                    task.setUpdatedAt(rs.getTimestamp("updated_at"));
                    return task;
                }
            }
        } catch (SQLException e) {
            handleSQLException(e, "findById");
        }
        return null; // 見つからなかった場合
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
            System.out.println("[DEBUG] TaskRepository.deleteByIdAndUserId: 影響した行数 = " + affectedRows);
            return affectedRows > 0; // 削除成功ならtrue
        } catch (SQLException e) {
            handleSQLException(e, "deleteByIdAndUserId");
            return false;
        }
    }

    /**
     * 新規タスクをデータベースに保存し、自動生成されたIDをオブジェクトにセットする
     */
    public boolean save(Task task) {
        String sql = "INSERT INTO tasks (user_id, title, description, status, created_at, updated_at) "
                   + "VALUES (?, ?, ?, 'NOT_STARTED', NOW(), NOW())";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, task.getUserId());
            stmt.setString(2, task.getTitle());
            stmt.setString(3, task.getDescription());
            
            int insertedRows = stmt.executeUpdate();
            boolean success = insertedRows > 0;
            
            if (success) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        task.setId(generatedKeys.getInt(1));
                        System.out.println("[DEBUG] Task saved successfully. Generated ID: " + task.getId());
                    }
                }
            }
            return success;
        } catch (SQLException e) {
            handleSQLException(e, "save");
            return false;
        }
    }
    
    /**
     * 既存のタスク情報を更新する（所有者チェック付き・あなたのテーブル定義に完全最適化）
     */
    public boolean update(Task task) {
        // WHERE句に user_id を含めることで、不正なID書き換え更新を完全に防御
        String sql = "UPDATE tasks SET title = ?, description = ?, status = ?, updated_at = NOW() "
                   + "WHERE task_id = ? AND user_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setString(3, task.getStatus());
            ps.setInt(4, task.getId());
            ps.setInt(5, task.getUserId());
            
            int updatedRows = ps.executeUpdate();
            System.out.println("[DEBUG] TaskRepository.update: 影響した行数 = " + updatedRows);
            
            return updatedRows > 0; // 1行以上更新されていれば成功
            
        } catch (SQLException e) {
            handleSQLException(e, "update");
            return false;
        }
    }
}