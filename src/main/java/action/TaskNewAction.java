package action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import model.Task;
import model.User;
import repository.TaskRepository;
import util.ValidationUtil; // ★Step1で作成したUtilをインポート

/**
 * 新規タスクの表示（GET）および登録処理（POST）を制御するアクション（カテゴリ機能拡張版）
 */
public class TaskNewAction extends BaseAuthAction {

    private final TaskRepository taskRepository = new TaskRepository();

    @Override
    protected String executeAuthenticated(
            HttpServletRequest request, 
            HttpServletResponse response, 
            User loginUser) throws ServletException {
        
        String method = request.getMethod();
        System.out.println("[DEBUG] TaskNewAction: Method = " + method);

        if ("GET".equalsIgnoreCase(method)) {
            // ==========================================
            // ① 新規登録画面の初期表示処理 (GET)
            // ==========================================
            
            // ★追加：新規登録画面のドロップダウンに既存のカテゴリ一覧を表示する処理
            try {
                java.util.Map<String, Integer> categoryStats = taskRepository.getCategoryStats(loginUser.getId());
                request.setAttribute("categoryStats", categoryStats);
                System.out.println("[DEBUG] TaskNewAction(GET): 既存カテゴリ数をJSPに渡しました。");
            } catch (java.sql.SQLException e) {
                System.err.println("[ERROR] TaskNewAction(GET): カテゴリ統計の取得に失敗しました。");
                e.printStackTrace();
            }

            return "/WEB-INF/views/task/new.jsp"; // ※環境に合わせてJSPのパスを確認してください
            
        } else if ("POST".equalsIgnoreCase(method)) {
            // ==========================================
            // ② データベース登録処理 (POST)
            // ==========================================
            
            // 1. フォームからの入力値を受け取る（★ category を追加）
            String title = request.getParameter("title");
            String description = request.getParameter("description");
            String status = request.getParameter("status");
            String priority = request.getParameter("priority");
            String category = request.getParameter("category"); // ★追加：カテゴリの取得
            
            // 前後の余白（空白）を除去
            if (title != null) title = title.trim();
            if (description != null) description = description.trim();
            if (status != null) status = status.trim();
            if (priority != null) priority = priority.trim();
            
            // ★追加：カテゴリのトリミングと空文字の null（未分類）化
            if (category != null) {
                category = category.trim();
                if (category.isEmpty()) {
                    category = null;
                }
            }

            // 2. 高度なバリデーション（フィールドごとにリストでエラーを管理）
            Map<String, List<String>> fieldErrors = new HashMap<>();
            
            // 各項目を ValidationUtil で個別にチェック
            List<String> titleErrors = ValidationUtil.validateTitle(title);
            if (!titleErrors.isEmpty()) {
                fieldErrors.put("title", titleErrors);
            }
            
            List<String> descErrors = ValidationUtil.validateDescription(description);
            if (!descErrors.isEmpty()) {
                fieldErrors.put("description", descErrors);
            }
            
            List<String> statusErrors = ValidationUtil.validateStatus(status);
            if (!statusErrors.isEmpty()) {
                fieldErrors.put("status", statusErrors);
            }
            
            List<String> priorityErrors = ValidationUtil.validatePriority(priority);
            if (!priorityErrors.isEmpty()) {
                fieldErrors.put("priority", priorityErrors);
            }

            // ★追加：手順書の仕様に基づくバリデーション（カテゴリ50文字超過チェック）
            if (category != null && category.length() > 50) {
                List<String> categoryErrors = new ArrayList<>();
                categoryErrors.add("カテゴリ名は50文字以内で入力してください。");
                fieldErrors.put("category", categoryErrors);
            }

            // 3. エラーがあった場合は、入力値をすべて保持してフォーム画面に戻す
            if (!fieldErrors.isEmpty()) {
                request.setAttribute("fieldErrors", fieldErrors);
                
                // ★修正：エラー時に入力内容がフォームから消えないよう、dummyTaskオブジェクトを正しく作成してJSPに渡す
                Task dummyTask = new Task();
                dummyTask.setTitle(title);
                dummyTask.setDescription(description);
                dummyTask.setStatus(status);
                dummyTask.setPriority(priority);
                dummyTask.setCategory(category); // エラー時も入力されたカテゴリ（または手入力値）を保持
                request.setAttribute("task", dummyTask);
                
                // ★追加：エラーで新規画面に戻る際にも、既存カテゴリの選択肢を再セット
                try {
                    java.util.Map<String, Integer> categoryStats = taskRepository.getCategoryStats(loginUser.getId());
                    request.setAttribute("categoryStats", categoryStats);
                } catch (java.sql.SQLException e) {
                    e.printStackTrace();
                }
                
                return "/WEB-INF/views/task/new.jsp";
            }

            // 4. エラーがなければ、Taskオブジェクトを組み立ててDBへ保存
            Task task = new Task();
            task.setUserId(loginUser.getId()); // セキュリティ担保
            task.setTitle(title);
            task.setDescription(description);
            task.setStatus(status);       // 拡張したステータスをセット
            task.setPriority(priority);   // 第8回で拡張する優先度をセット
            task.setCategory(category);   // ★追加：DTOにカテゴリをセット

            boolean isSaved = taskRepository.save(task);

            if (isSaved) {
                System.out.println("[INFO] TaskNewAction: 新規タスク登録成功。カテゴリ: " + category);
                
                // PRGパターン用のフラッシュメッセージ
                HttpSession session = request.getSession();
                session.setAttribute("flash_success", "新しいタスク「" + title + "」を登録しました！");
                
                return "redirect:/app/task/list";
            } else {
                throw new ServletException("データベースへのタスク保存処理に失敗しました。");
            }
        }

        return "redirect:/app/task/list";
    }
}