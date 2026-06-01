package action;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import model.Task;
import model.User;
import repository.TaskRepository;

/**
 * タスクの編集画面表示（GET）および更新処理（POST）を制御するアクション
 */
public class TaskEditAction extends BaseAuthAction {

    private final TaskRepository taskRepository = new TaskRepository();

    @Override
    protected String executeAuthenticated(
            HttpServletRequest request, 
            HttpServletResponse response, 
            User loginUser) throws ServletException {
        
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
            // 他人のタスクを編集しようとした場合は、安全のため一覧へリダイレクト（実務では403エラー画面等への遷移も一般的）
            return "redirect:/app/task/list";
        }

        if ("GET".equalsIgnoreCase(method)) {
            // ==========================================
            // ① 編集画面の初期表示処理 (GET)
            // ==========================================
            // データベースから既存のタスク情報を取得
            Task task = taskRepository.findById(taskId);
            if (task == null) {
                return "redirect:/app/task/list";
            }

            // JSPにタスクデータを渡す
            request.setAttribute("task", task);
            return "/WEB-INF/views/task/edit.jsp";
            
        } else if ("POST".equalsIgnoreCase(method)) {
            // ==========================================
            // ② データベース更新処理 (POST)
            // ==========================================
            
            // フォームからの入力値を取得
            String title = request.getParameter("title");
            String description = request.getParameter("description");
            String status = request.getParameter("status");
            
            if (title != null) title = title.trim();
            if (description != null) description = description.trim();

            // 入力バリデーション（チェック）
            Map<String, String> errors = new HashMap<>();
            
            if (title == null || title.isEmpty()) {
                errors.put("title", "タイトルは必須入力です。");
            } else if (title.length() > 50) {
                errors.put("title", "タイトルは50文字以内で入力してください。");
            }
            
            if (description != null && description.length() > 200) {
                errors.put("description", "説明は200文字以内で入力してください。");
            }
            
            // ステータスの不正値チェック（NOT_STARTED, IN_PROGRESS, COMPLETED のいずれか）
            if (status == null || (!status.equals("NOT_STARTED") && !status.equals("IN_PROGRESS") && !status.equals("COMPLETED"))) {
                errors.put("status", "不正なステータスが選択されました。");
            }

            // エラーがあった場合は、入力値を保持して編集画面（edit.jsp）に戻す
            if (!errors.isEmpty()) {
                System.out.println("[WARN] TaskEditAction: バリデーションエラーを検知しました。");
                request.setAttribute("errors", errors);
                
                // エラー時に入力内容が消えないよう、一時的なタスクオブジェクトを組み立ててJSPに送る
                Task dummyTask = new Task();
                dummyTask.setId(taskId);
                dummyTask.setTitle(title);
                dummyTask.setDescription(description);
                dummyTask.setStatus(status);
                
                request.setAttribute("task", dummyTask);
                return "/WEB-INF/views/task/edit.jsp";
            }

            // エラーがなければ、Taskオブジェクトを更新用に組み立てる
            Task updatedTask = new Task();
            updatedTask.setId(taskId);
            updatedTask.setUserId(loginUser.getId()); // 所有者IDを設定
            updatedTask.setTitle(title);
            updatedTask.setDescription(description);
            updatedTask.setStatus(status);

            // リポジトリを呼び出してUPDATEを実行
            boolean isUpdated = taskRepository.update(updatedTask);

            if (isUpdated) {
                System.out.println("[INFO] TaskEditAction: タスクID " + taskId + " の更新に成功しました。");
                
                // PRGパターン：セッションにメッセージを詰めてリダイレクト
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