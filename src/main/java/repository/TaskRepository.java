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
    
 // java.sql.SQLException や java.util.List, java.util.ArrayList のインポートがあるか確認してください

    /**
     * キーワード検索（タスク名での部分一致） ＋ ソート機能
     */
    public List<Task> search(int userId, String keyword, String sort) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        String sql;
        
        // ソート条件（ASC / DESC）によってSQLを分岐
        if ("ASC".equals(sort)) {
            sql = "SELECT * FROM tasks WHERE user_id = ? AND title LIKE ? ORDER BY created_at ASC";
        } else {
            sql = "SELECT * FROM tasks WHERE user_id = ? AND title LIKE ? ORDER BY created_at DESC";
        }

        // try-with-resources文でConnection、PreparedStatementを取得
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // パラメータの設定（SQLインジェクション対策）
            pstmt.setInt(1, userId);
            pstmt.setString(2, "%" + keyword + "%"); // 部分一致検索のために前後に % を結合
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Task task = new Task();
                    task.setId(rs.getInt("task_id"));
                    task.setTitle(rs.getString("title"));
                    task.setDescription(rs.getString("description"));
                    task.setStatus(rs.getString("status"));
                    task.setPriority(rs.getString("priority"));
                    task.setCreatedAt(rs.getTimestamp("created_at"));
                    task.setUpdatedAt(rs.getTimestamp("updated_at"));
                    task.setUserId(rs.getInt("user_id"));
                    tasks.add(task);
                }
            }
        }
        return tasks;
    }

    /**
     * 全件検索 ＋ ソート機能（キーワードなしの場合用）
     */
    public List<Task> findAllByUserIdWithSort(int userId, String sort) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        String sql;
        
        if ("ASC".equals(sort)) {
            sql = "SELECT * FROM tasks WHERE user_id = ? ORDER BY created_at ASC";
        } else {
            sql = "SELECT * FROM tasks WHERE user_id = ? ORDER BY created_at DESC";
        }

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Task task = new Task();
                    task.setId(rs.getInt("task_id"));
                    task.setTitle(rs.getString("title"));
                    task.setDescription(rs.getString("description"));
                    task.setStatus(rs.getString("status"));
                    task.setPriority(rs.getString("priority"));
                    task.setCreatedAt(rs.getTimestamp("created_at"));
                    task.setUpdatedAt(rs.getTimestamp("updated_at"));
                    task.setUserId(rs.getInt("user_id"));
                    tasks.add(task);
                }
            }
        }
        return tasks;
    }
    
    /**
     * 検索条件に該当するタスクの総件数を取得する（ページング計算用）
     */
    public int countTasks(int userId, String keyword) throws SQLException {
        int count = 0;
        // ベースとなるSQL。所有者チェックを徹底
        String sql = "SELECT COUNT(*) FROM tasks WHERE user_id = ?";
        
        // キーワードが指定されている場合は、LIKE条件を動的に追加
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql += " AND title LIKE ?";
        }

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            // キーワードがある場合のみ、2番目のパラメータをバインド
            if (keyword != null && !keyword.trim().isEmpty()) {
                pstmt.setString(2, "%" + keyword + "%");
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }
        }
        return count;
    }

    /**
     * ページング・検索・ソートに対応したタスク取得
     */
    public List<Task> searchWithPaging(int userId, String keyword, String sort, int pageSize, int offset) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        
        // 1. ベースSQLの構築
        String sql = "SELECT * FROM tasks WHERE user_id = ?";
        
        // 2. キーワード検索条件の動的追加
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql += " AND title LIKE ?";
        }
        
        // 3. ソート条件の結合（ASC または DESC）
        if ("ASC".equals(sort)) {
            sql += " ORDER BY created_at ASC";
        } else {
            sql += " ORDER BY created_at DESC";
        }
        
        // 4. ページング条件（LIMIT と OFFSET）の結合 ※順番に注意！
        sql += " LIMIT ? OFFSET ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            int paramIndex = 1;
            
            // 所有者IDのセット
            pstmt.setInt(paramIndex++, userId);
            
            // キーワードがある場合はセット
            if (keyword != null && !keyword.trim().isEmpty()) {
                pstmt.setString(paramIndex++, "%" + keyword + "%");
            }
            
            // ページングパラメータのセット（安全な数値バインド）
            pstmt.setInt(paramIndex++, pageSize);
            pstmt.setInt(paramIndex++, offset);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Task task = new Task();
                    task.setId(rs.getInt("task_id"));
                    task.setTitle(rs.getString("title"));
                    task.setDescription(rs.getString("description"));
                    task.setStatus(rs.getString("status"));
                    task.setPriority(rs.getString("priority"));
                    task.setCreatedAt(rs.getTimestamp("created_at"));
                    task.setUpdatedAt(rs.getTimestamp("updated_at"));
                    task.setUserId(rs.getInt("user_id"));
                    tasks.add(task);
                }
            }
        }
        return tasks;
    }
}