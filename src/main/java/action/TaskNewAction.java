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
 * 新規タスクの表示（GET）および登録処理（POST）を制御するアクション（カテゴリ機能拡張・共通リファクタリング版）
 */
public class TaskNewAction extends BaseAuthAction {

    private final TaskRepository taskRepository = new TaskRepository();

    @Override
    protected String executeAuthenticated(
            HttpServletRequest request, 
            HttpServletResponse response, 
            User loginUser) throws ServletException, IOException { // ★IOExceptionを追加
        
        String method = request.getMethod();
        System.out.println("[DEBUG] TaskNewAction: Method = " + method);

        if ("GET".equalsIgnoreCase(method)) {
            // ==========================================
            // ① 新規登録画面の初期表示処理 (GET)
            // ==========================================
            
            // ★共通化：親クラスのメソッドを1行呼び出すだけでドロップダウン一覧をセット可能に
            setupCategoryDropdown(request, taskRepository, loginUser);

            return "/WEB-INF/views/task/new.jsp";
            
        } else if ("POST".equalsIgnoreCase(method)) {
            // ==========================================
            // ② データベース登録処理 (POST)
            // ==========================================
            
            // 1. フォームからの入力値を受け取る
            String title = request.getParameter("title");
            String description = request.getParameter("description");
            String status = request.getParameter("status");
            String priority = request.getParameter("priority");
            String category = request.getParameter("category");
            
            // 前後の余白（空白）を除去
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

            // 2. 高度なバリデーション（フィールドごとにリストでエラーを管理）
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

            // 3. エラーがあった場合は、入力値をすべて保持してフォーム画面に戻す
            if (!fieldErrors.isEmpty()) {
                request.setAttribute("fieldErrors", fieldErrors);
                
                // エラー時に入力内容がフォームから消えないようオブジェクトを作成してJSPに渡す
                Task dummyTask = new Task();
                dummyTask.setTitle(title);
                dummyTask.setDescription(description);
                dummyTask.setStatus(status);
                dummyTask.setPriority(priority);
                dummyTask.setCategory(category);
                request.setAttribute("task", dummyTask);
                
                // ★共通化：バリデーションエラーによる押し戻し時も親クラスの共通処理で選択肢を再セット
                setupCategoryDropdown(request, taskRepository, loginUser);
                
                return "/WEB-INF/views/task/new.jsp";
            }

            // 4. エラーがなければ、Taskオブジェクトを組み立ててDBへ保存
            Task task = new Task();
            task.setUserId(loginUser.getId());
            task.setTitle(title);
            task.setDescription(description);
            task.setStatus(status);       
            task.setPriority(priority);   
            task.setCategory(category);   

            boolean isSaved = taskRepository.save(task);

            if (isSaved) {
                System.out.println("[INFO] TaskNewAction: 新規タスク登録成功。カテゴリ: " + category);
                
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