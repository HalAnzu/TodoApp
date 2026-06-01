package action;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import model.User;
import repository.TaskRepository;

/**
 * タスクの削除処理（POSTのみ受付）を制御するアクション
 */
public class TaskDeleteAction extends BaseAuthAction {

    private final TaskRepository taskRepository = new TaskRepository();

    @Override
    protected String executeAuthenticated(
            HttpServletRequest request, 
            HttpServletResponse response, 
            User loginUser) throws ServletException {
        
        // 【セキュリティ】削除処理はGETでの実行を絶対に禁止し、POSTのみを許可する
        String method = request.getMethod();
        if (!"POST".equalsIgnoreCase(method)) {
            System.out.println("[WARN] TaskDeleteAction: 不正なリクエストメソッドです: " + method);
            return "redirect:/app/task/list";
        }

        // 1. リクエストから削除対象のタスクIDを取得
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isEmpty()) {
            System.out.println("[WARN] TaskDeleteAction: 削除対象のタスクIDが指定されていません。");
            return "redirect:/app/task/list";
        }

        int taskId;
        try {
            taskId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            System.out.println("[WARN] TaskDeleteAction: 不正なタスクID形式です: " + idParam);
            return "redirect:/app/task/list";
        }

        System.out.println("[DEBUG] TaskDeleteAction: 削除を実行します。TaskID = " + taskId + ", UserID = " + loginUser.getId());

        // 2. リポジトリを呼び出して安全に削除を実行
        // メソッド内部で「WHERE task_id = ? AND user_id = ?」が走り、他人のタスクであれば自動的に弾かれます
        boolean isDeleted = taskRepository.deleteByIdAndUserId(taskId, loginUser.getId());

        if (isDeleted) {
            System.out.println("[INFO] TaskDeleteAction: タスクID " + taskId + " の削除に成功しました。");
            
            // PRGパターン：削除成功のメッセージをセッションに格納してリダイレクト
            HttpSession session = request.getSession();
            session.setAttribute("flash_success", "タスクを削除しました。");
        } else {
            // 所有者が異なる、または既に存在しないIDの場合は、安全のため何事もなかったかのように一覧に戻す
            // （悪意ある攻撃者に「そのIDのタスクが実在するかどうか」を推測させないためのセキュリティ対策）
            System.out.println("[SECURITY WARN] TaskDeleteAction: 削除が実行されませんでした。他人のタスクか、存在しないIDです。");
        }

        // 成功・失敗に関わらず、処理が終わったら即座に一覧画面へリダイレクト
        return "redirect:/app/task/list";
    }
}
