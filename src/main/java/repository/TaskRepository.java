package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Task;
import model.TaskStatistics;

/**
 * tasksテーブルへのデータアクセスを担うクラス
 */
public class TaskRepository extends BaseRepository {

    /**
     * ResultSetからTaskオブジェクトへの詰め替えを行う共通補助メソッド
     * (DRY原則に基づき、各メソッド内のマッピングを共通化・category対応)
     */
    private Task mapResultSetToTask(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setId(rs.getInt("task_id"));
        task.setUserId(rs.getInt("user_id"));
        task.setTitle(rs.getString("title"));
        task.setDescription(rs.getString("description"));
        task.setStatus(rs.getString("status"));
        task.setPriority(rs.getString("priority"));
        task.setCreatedAt(rs.getTimestamp("created_at"));
        task.setUpdatedAt(rs.getTimestamp("updated_at"));
        task.setFavorite(rs.getBoolean("is_favorite"));
        // ★追加：簡易カテゴリ機能仕様に準拠
        task.setCategory(rs.getString("category"));
        return task;
    }

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
                tasks.add(mapResultSetToTask(rs));
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
                    tasks.add(mapResultSetToTask(rs));
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
                    return mapResultSetToTask(rs);
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
     * 所有者確認付きのタスク削除
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
        // ★修正：新規保存時に category カラムをインサート対象に含める
        String sql = "INSERT INTO tasks (user_id, title, description, status, priority, is_favorite, category, created_at, updated_at) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, task.getUserId());
            stmt.setString(2, task.getTitle());
            stmt.setString(3, task.getDescription());
            stmt.setString(4, task.getStatus());
            stmt.setString(5, task.getPriority());
            stmt.setBoolean(6, task.isFavorite());
            stmt.setString(7, task.getCategory()); // ★追加：カテゴリの保存
            
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
     * ★お気に入り状態（is_favorite）は専用メソッド（toggleFavorite）で管理するため、
     * 通常のタスク編集処理のSQL更新対象からは安全のために除外します。
     */
    public boolean update(Task task) {
        // ★修正：is_favorite = ? をSQLから完全に除外
        String sql = "UPDATE tasks SET title = ?, description = ?, status = ?, priority = ?, category = ?, updated_at = NOW() "
                   + "WHERE task_id = ? AND user_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setString(3, task.getStatus());
            ps.setString(4, task.getPriority());
            ps.setString(5, task.getCategory()); // ★インデックスを5に変更（カテゴリの更新）
            ps.setInt(6, task.getId());          // ★インデックスを6に変更（WHERE句のtask_id）
            ps.setInt(7, task.getUserId());      // ★インデックスを7に変更（WHERE句のuser_id）
            
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
                    tasks.add(mapResultSetToTask(rs));
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
                    tasks.add(mapResultSetToTask(rs));
                }
            }
        }
        return tasks;
    }
    
    /**
     * ★拡張：検索条件・お気に入りフィルター・カテゴリフィルタに該当するタスクの総件数を取得する（ページング用）
     */
    public int countTasks(int userId, String keyword, boolean favoriteOnly, String category) throws SQLException {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM tasks WHERE user_id = ?";
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql += " AND title LIKE ?";
        }
        if (favoriteOnly) {
            sql += " AND is_favorite = true";
        }
        // ★追加：カテゴリフィルタ用の条件
        if (category != null && !category.trim().isEmpty()) {
            sql += " AND category = ?";
        }

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            int paramIndex = 1;
            pstmt.setInt(paramIndex++, userId);
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                pstmt.setString(paramIndex++, "%" + keyword + "%");
            }
            if (category != null && !category.trim().isEmpty()) {
                pstmt.setString(paramIndex++, category.trim());
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
     * ★拡張：ページング・検索・ソート・お気に入り・カテゴリ抽出に対応した包括的なタスク取得
     */
    public List<Task> searchWithPaging(int userId, String keyword, String sort, boolean favoriteOnly, String category, int pageSize, int offset) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        
        String sql = "SELECT * FROM tasks WHERE user_id = ?";
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql += " AND title LIKE ?";
        }
        if (favoriteOnly) {
            sql += " AND is_favorite = true";
        }
        // ★追加：カテゴリでの絞り込み
        if (category != null && !category.trim().isEmpty()) {
            sql += " AND category = ?";
        }
        
        // 並び替え：お気に入りを最上部に優先表示
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
            if (category != null && !category.trim().isEmpty()) {
                pstmt.setString(paramIndex++, category.trim());
            }
            
            pstmt.setInt(paramIndex++, pageSize);
            pstmt.setInt(paramIndex++, offset);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapResultSetToTask(rs));
                }
            }
        }
        return tasks;
    }

    /**
     * 指定されたタスクのお気に入り状態（true/false）を更新する
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
     * 指定されたタスクを同じユーザー用として複製（コピー）する（★ category カラムもそのまま引き継ぐよう修正）
     */
    public boolean copyTask(int originalTaskId, int userId) throws SQLException {
        Task original = findById(originalTaskId);
        
        if (original == null || original.getUserId() != userId) {
            System.out.println("[WARN] Copy failed: Task not found or unauthorized.");
            return false;
        }
        
        String copyTitle = "コピー - " + original.getTitle();
        
        // ★修正：category列もインサート対象に含め、元タスクのカテゴリ値を引き継ぐ
        String sql = "INSERT INTO tasks (user_id, title, description, status, priority, is_favorite, category, created_at, updated_at) "
                   + "VALUES (?, ?, ?, 'pending', 'medium', false, ?, NOW(), NOW())";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            ps.setString(2, copyTitle);
            ps.setString(3, original.getDescription());
            ps.setString(4, original.getCategory()); // 元タスクのカテゴリをコピー
            
            int insertedRows = ps.executeUpdate();
            System.out.println("[DEBUG] TaskRepository.copyTask: 影響した行数 = " + insertedRows);
            
            return insertedRows > 0;
        } catch (SQLException e) {
            handleSQLException(e, "copyTask");
            return false;
        }
    }

    /**
     * ★修正版：画面上部のチップ表示用。ログインユーザーが保有するカテゴリ別のタスク件数を集計する
     * データベース内の NULL, 空文字、"未分類" をすべて「未分類」に統一して正確にマージします。
     */
    public Map<String, Integer> getCategoryStats(int userId) throws SQLException {
        Map<String, Integer> stats = new HashMap<>();
        String sql = "SELECT category, COUNT(*) as cnt FROM tasks WHERE user_id = ? GROUP BY category ORDER BY category ASC";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String category = rs.getString("category");
                    int count = rs.getInt("cnt");
                    
                    // NULL、空文字、または文字列としての"未分類"、JSPから戻ってくる可能性のある値すべてを「未分類」として統一
                    if (category == null || category.trim().isEmpty() || "未分類".equals(category.trim()) || "_UNCLASSIFIED_".equals(category.trim())) {
                        category = "未分類";
                    } else {
                        category = category.trim();
                    }
                    
                    stats.put(category, stats.getOrDefault(category, 0) + count);
                }
            }
        }
        return stats;
    }
    
    /**
     * 指定されたユーザーのタスク統計情報をまとめて取得する（簡易統計機能用）
     */
    public TaskStatistics getTaskStatistics(int userId) throws SQLException {
        TaskStatistics stats = new TaskStatistics();
        
        // 1行でステータス別、優先度別、本日作成数をすべて集計するSQL
        // データベース内の status/priority が大文字小文字混在していても安全なように LOWER() を適用
        String sql = "SELECT "
                   + "  COUNT(*) as total, "
                   + "  COUNT(CASE WHEN LOWER(status) = 'completed' THEN 1 END) as completed, "
                   + "  COUNT(CASE WHEN LOWER(status) = 'pending' THEN 1 END) as pending, "
                   + "  COUNT(CASE WHEN LOWER(status) = 'in_progress' THEN 1 END) as in_progress, "
                   + "  COUNT(CASE WHEN LOWER(priority) = 'low' THEN 1 END) as p_low, "
                   + "  COUNT(CASE WHEN LOWER(priority) = 'medium' THEN 1 END) as p_medium, "
                   + "  COUNT(CASE WHEN LOWER(priority) = 'high' THEN 1 END) as p_high, "
                   + "  COUNT(CASE WHEN DATE(created_at) = CURRENT_DATE THEN 1 END) as today_created "
                   + "FROM tasks "
                   + "WHERE user_id = ?";

        // ★修正：DBUtilではなく、クラス共通の getConnection() を使用するように変更
        try (Connection conn = getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    int completed = rs.getInt("completed");
                    
                    stats.setTotalTasks(total);
                    stats.setCompletedTasks(completed);
                    stats.setPendingTasks(rs.getInt("pending"));
                    stats.setInProgressTasks(rs.getInt("in_progress"));
                    stats.setLowPriorityTasks(rs.getInt("p_low"));
                    stats.setMediumPriorityTasks(rs.getInt("p_medium"));
                    stats.setHighPriorityTasks(rs.getInt("p_high"));
                    stats.setTodayCreatedTasks(rs.getInt("today_created"));
                    
                    // 0除算対策：タスク総数が0件の場合は完了率を0.0%にする
                    if (total > 0) {
                        double rate = ((double) completed / total) * 100.0;
                        rate = Math.round(rate * 10.0) / 10.0; // 小数点第1位までに四捨五入
                        stats.setCompletionRate(rate);
                    } else {
                        stats.setCompletionRate(0.0);
                    }
                }
            }
        } // ★閉じカッコの不整合を修正
        return stats;
    }
} // クラスの閉じカッコ