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
                // ★追加：お気に入り状態のマッピング
                task.setFavorite(rs.getBoolean("is_favorite"));
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
                    // ★追加：お気に入り状態のマッピング
                    task.setFavorite(rs.getBoolean("is_favorite"));
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
                    // ★追加：お気に入り状態のマッピング
                    task.setFavorite(rs.getBoolean("is_favorite"));
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
        // ★修正：新規保存時にも is_favorite カラム（デフォルトfalse）を明示的にインサート対象に含める
        String sql = "INSERT INTO tasks (user_id, title, description, status, priority, is_favorite, created_at, updated_at) "
                   + "VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, task.getUserId());
            stmt.setString(2, task.getTitle());
            stmt.setString(3, task.getDescription());
            stmt.setString(4, task.getStatus());
            stmt.setString(5, task.getPriority());
            stmt.setBoolean(6, task.isFavorite()); // DTOの状態を反映
            
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
     * 既存のタスク情報を更新する（所有者チェック付き）
     */
    public boolean update(Task task) {
        // ★修正：通常の更新処理時にお気に入り状態が上書きされて消えないよう、更新対象に含める
        String sql = "UPDATE tasks SET title = ?, description = ?, status = ?, priority = ?, is_favorite = ?, updated_at = NOW() "
                   + "WHERE task_id = ? AND user_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setString(3, task.getStatus());
            ps.setString(4, task.getPriority());
            ps.setBoolean(5, task.isFavorite());
            ps.setInt(6, task.getId());
            ps.setInt(7, task.getUserId());
            
            int updatedRows = ps.executeUpdate();
            System.out.println("[DEBUG] TaskRepository.update: 影響した行数 = " + updatedRows);
            
            return updatedRows > 0;
            
        } catch (SQLException e) {
            handleSQLException(e, "update");
            return false;
        }
    }
    
    /**
     * キーワード検索（タスク名での部分一致） ＋ ソート機能（第9回互換用）
     */
    public List<Task> search(int userId, String keyword, String sort) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        String sql;
        
        if ("ASC".equals(sort)) {
            sql = "SELECT * FROM tasks WHERE user_id = ? AND title LIKE ? ORDER BY is_favorite DESC, created_at ASC";
        } else {
            sql = "SELECT * FROM tasks WHERE user_id = ? AND title LIKE ? ORDER BY is_favorite DESC, created_at DESC";
        }

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, "%" + keyword + "%");
            
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
                    task.setFavorite(rs.getBoolean("is_favorite"));
                    tasks.add(task);
                }
            }
        }
        return tasks;
    }

    /**
     * 全件検索 ＋ ソート機能（キーワードなしの場合用、第9回互換用）
     */
    public List<Task> findAllByUserIdWithSort(int userId, String sort) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        String sql;
        
        if ("ASC".equals(sort)) {
            sql = "SELECT * FROM tasks WHERE user_id = ? ORDER BY is_favorite DESC, created_at ASC";
        } else {
            sql = "SELECT * FROM tasks WHERE user_id = ? ORDER BY is_favorite DESC, created_at DESC";
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
                    task.setFavorite(rs.getBoolean("is_favorite"));
                    tasks.add(task);
                }
            }
        }
        return tasks;
    }
    
    /**
     * 検索条件・お気に入りフィルターに該当するタスクの総件数を取得する（ページング計算用）
     */
    public int countTasks(int userId, String keyword, boolean favoriteOnly) throws SQLException {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM tasks WHERE user_id = ?";
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql += " AND title LIKE ?";
        }
        
        if (favoriteOnly) {
            sql += " AND is_favorite = true";
        }

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            int paramIndex = 1;
            pstmt.setInt(paramIndex++, userId);
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                pstmt.setString(paramIndex++, "%" + keyword + "%");
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
     * ページング・検索・ソート・お気に入りフィルターに対応したタスク取得（★修正：引数に favoriteOnly を追加）
     */
    public List<Task> searchWithPaging(int userId, String keyword, String sort, boolean favoriteOnly, int pageSize, int offset) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        
        String sql = "SELECT * FROM tasks WHERE user_id = ?";
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql += " AND title LIKE ?";
        }
        
        // ★動的追加：お気に入りフィルターがONの場合の条件
        if (favoriteOnly) {
            sql += " AND is_favorite = true";
        }
        
        // ★並び替え：お気に入りを最上部に優先表示
        sql += " ORDER BY is_favorite DESC";
        
        if ("ASC".equals(sort)) {
            sql += ", created_at ASC";
        } else {
            sql += ", created_at DESC";
        }
        
        sql += " LIMIT ? OFFSET ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            int paramIndex = 1;
            pstmt.setInt(paramIndex++, userId);
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                pstmt.setString(paramIndex++, "%" + keyword + "%");
            }
            
            // ★安全なインデックス管理： LIMIT と OFFSET のバインド
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
                    task.setFavorite(rs.getBoolean("is_favorite")); // データベースの値をマッピング
                    tasks.add(task);
                }
            }
        }
        return tasks;
    }
    
    /**
     * 指定されたタスクのお気に入り状態（true/false）を更新する（セキュリティ用の所有者チェック付き）
     */
    public boolean toggleFavorite(int taskId, int userId, boolean isFavorite) throws SQLException {
        String sql = "UPDATE tasks SET is_favorite = ? WHERE task_id = ? AND user_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setBoolean(1, isFavorite);
            pstmt.setInt(2, taskId);
            pstmt.setInt(3, userId);
            
            int updatedRows = pstmt.executeUpdate();
            return updatedRows == 1;
        }
    }
    
    /**
     * 指定されたタスクを同じユーザー用として複製（コピー）する
     * @param originalTaskId 複製元のタスクID
     * @param userId ログインユーザーのID（所有者チェック用）
     * @return 複製成功ならtrue、失敗ならfalse
     */
    public boolean copyTask(int originalTaskId, int userId) throws SQLException {
        // 1) 元のタスクを取得
        Task original = findById(originalTaskId);
        
        // 2) 存在チェック & 所有者チェック（他人のタスクの複製を完全にブロック）
        if (original == null || original.getUserId() != userId) {
            System.out.println("[WARN] Copy failed: Task not found or unauthorized.");
            return false;
        }
        
        // 3) 仕様に基づく複製データの準備
        // タイトルの先頭に固定の接頭辞「コピー - 」を付与
        String copyTitle = "コピー - " + original.getTitle();
        
        // ステータスは常に 'pending'、優先度は常に 'low'/'medium'/'high' のうち 'medium' で固定
        String sql = "INSERT INTO tasks (user_id, title, description, status, priority, is_favorite, created_at, updated_at) "
                   + "VALUES (?, ?, ?, 'pending', 'medium', false, NOW(), NOW())";
        
        // 4) データベースへのインサート実行
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            ps.setString(2, copyTitle);
            ps.setString(3, original.getDescription()); // 説明文はそのまま引き継ぐ
            
            int insertedRows = ps.executeUpdate();
            System.out.println("[DEBUG] TaskRepository.copyTask: 影響した行数 = " + insertedRows);
            
            return insertedRows > 0;
        } catch (SQLException e) {
            handleSQLException(e, "copyTask");
            return false;
        }
    }
}