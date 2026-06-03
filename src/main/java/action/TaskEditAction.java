package action;

import java.io.IOException; // ★追加：BaseAuthActionのシグネチャに合わせるため必須
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
 * タスクの編集画面表示（GET）および更新処理（POST）を制御するアクション（カテゴリ機能拡張・共通リファクタリング版）
 */
public class TaskEditAction extends BaseAuthAction {

    private final TaskRepository taskRepository = new TaskRepository();

    @Override
    protected String executeAuthenticated(
            HttpServletRequest request, 
            HttpServletResponse response, 
            User loginUser) throws ServletException, IOException { // ★IOExceptionを追加
        
        String method = request.getMethod();
        System.out.println("[DEBUG] TaskEditAction: Method = " + method);

        // 1. リクエストからタスクID（id）を取得し、数値に変換する
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isEmpty()) {
            System.out.println("[WARN] TaskEditAction: タスクIDが指定されていません。");
            return "redirect:/app/task/list";
        }

        int taskId;
        try {
            taskId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            System.out.println("[WARN] TaskEditAction: 不正なタスクID形式です: " + idParam);
            return "redirect:/app/task/list";
        }

        // 2. 【最重要セキュリティ】操作対象のタスクが本当にログインユーザーのものか認可チェック
        if (!taskRepository.isOwner(taskId, loginUser.getId())) {
            System.out.println("[SECURITY WARN] TaskEditAction: ユーザー " + loginUser.getUsername() 
                    + " (ID:" + loginUser.getId() + ") が他人のタスク (ID:" + taskId + ") へ不正アクセスを試みました。");
            return "redirect:/app/task/list";
        }

        if ("GET".equalsIgnoreCase(method)) {
            // ==========================================
            // ① 編集画面の初期表示処理 (GET)
            // ==========================================
            Task task = taskRepository.findById(taskId);
            if (task == null) {
                return "redirect:/app/task/list";
            }

            // JSPに既存のタスクデータを渡す
            request.setAttribute("task", task);

            // ★共通化：親クラスのメソッドを呼び出すだけで、JSPのドロップダウン用データをセット完了
            setupCategoryDropdown(request, taskRepository, loginUser);

            return "/WEB-INF/views/task/edit.jsp";
            
        } else if ("POST".equalsIgnoreCase(method)) {
            // ==========================================
            // ② データベース更新処理 (POST)
            // ==========================================
            
            // フォームからの入力値を取得
            String title = request.getParameter("title");
            String description = request.getParameter("description");
            String status = request.getParameter("status");
            String priority = request.getParameter("priority");
            String category = request.getParameter("category");
            
            if (title != null) title = title.trim();
            if (description != null) description = description.trim();
            if (status != null) status = status.trim();
            if (priority != null) priority = priority.trim();
            
            // カテゴリのトリミングと空文字の null（未分類）化
            if (category != null) {
                category = category.trim();
                if (category.isEmpty()) {
                    category = null;
                }
            }

            // 高度なバリデーション（フィールドごとにリストでエラーを管理）
            Map<String, List<String>> fieldErrors = new HashMap<>();
            
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

            // カテゴリ50文字超過チェック
            if (category != null && category.length() > 50) {
                List<String> categoryErrors = new ArrayList<>();
                categoryErrors.add("カテゴリ名は50文字以内で入力してください。");
                fieldErrors.put("category", categoryErrors);
            }

            // エラーがあった場合は、入力値を保持して編集画面（edit.jsp）に戻す
            if (!fieldErrors.isEmpty()) {
                System.out.println("[WARN] TaskEditAction: バリデーションエラーを検知しました。エラー項目数: " + fieldErrors.size());
                
                request.setAttribute("fieldErrors", fieldErrors);
                
                // エラー時に入力内容が消えないよう、入力値を含んだダミーオブジェクトを組み立ててJSPに送る
                Task dummyTask = new Task();
                dummyTask.setId(taskId);
                dummyTask.setTitle(title);
                dummyTask.setDescription(description);
                dummyTask.setStatus(status);
                dummyTask.setPriority(priority);
                dummyTask.setCategory(category);
                request.setAttribute("task", dummyTask);
                
                // ★共通化：バリデーションエラーによる押し戻し時も共通メソッドで選択肢を再セット
                setupCategoryDropdown(request, taskRepository, loginUser);
                
                return "/WEB-INF/views/task/edit.jsp";
            }

            // エラーがなければ、Taskオブジェクトを更新用に組み立てる
            Task updatedTask = new Task();
            updatedTask.setId(taskId);
            updatedTask.setUserId(loginUser.getId());
            updatedTask.setTitle(title);
            updatedTask.setDescription(description);
            updatedTask.setStatus(status);
            updatedTask.setPriority(priority);
            updatedTask.setCategory(category);

            // リポジトリを呼び出してUPDATEを実行
            boolean isUpdated = taskRepository.update(updatedTask);

            if (isUpdated) {
                System.out.println("[INFO] TaskEditAction: タスクID " + taskId + " の更新に成功しました。カテゴリ: " + category);
                
                HttpSession session = request.getSession();
                session.setAttribute("flash_success", "タスク「" + title + "」を更新しました！");
                
                return "redirect:/app/task/list";
            } else {
                throw new ServletException("データベースのタスク更新処理に失敗しました。");
            }
        }

        return "redirect:/app/task/list";
    }
}