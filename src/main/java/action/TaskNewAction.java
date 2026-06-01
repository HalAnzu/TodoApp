package action;

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
 * 新規タスクの表示（GET）および登録処理（POST）を制御するアクション（バリデーション強化版）
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
            // ① 画面表示処理 (GET)
            // ==========================================
            return "/WEB-INF/views/task/new.jsp";
            
        } else if ("POST".equalsIgnoreCase(method)) {
            // ==========================================
            // ② データベース登録処理 (POST)
            // ==========================================
            
            // 1. フォームからの入力値を受け取る（status と priority を追加）
            String title = request.getParameter("title");
            String description = request.getParameter("description");
            String status = request.getParameter("status");
            String priority = request.getParameter("priority");
            
            // 前後の余白（空白）を除去
            if (title != null) title = title.trim();
            if (description != null) description = description.trim();
            if (status != null) status = status.trim();
            if (priority != null) priority = priority.trim();

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

            // 3. エラーがあった場合は、入力値をすべて保持してフォーム画面に戻す
            if (!fieldErrors.isEmpty()) {
                System.out.println("[WARN] TaskNewAction: バリデーションエラーを検知しました。エラー項目数: " + fieldErrors.size());
                
                // フィールド別のエラーマップをJSPに引き渡す
                request.setAttribute("fieldErrors", fieldErrors);
                
                // 入力された値をすべてJSPに送り返す（選択状態・入力値保持機能）
                request.setAttribute("title", title);
                request.setAttribute("description", description);
                request.setAttribute("status", status);
                request.setAttribute("priority", priority);
                
                return "/WEB-INF/views/task/new.jsp";
            }

            // 4. エラーがなければ、Taskオブジェクトを組み立ててDBへ保存
            Task task = new Task();
            task.setUserId(loginUser.getId()); // セキュリティ担保
            task.setTitle(title);
            task.setDescription(description);
            task.setStatus(status);       // 拡張したステータスをセット
            task.setPriority(priority);   // 第8回で拡張する優先度をセット

            boolean isSaved = taskRepository.save(task);

            if (isSaved) {
                System.out.println("[INFO] TaskNewAction: 新規タスク登録成功。");
                
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
