package action;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import model.User;
import repository.TaskRepository;

/**
 * 既存タスクを複製（コピー）するアクション
 */
public class TaskCopyAction extends BaseAuthAction {

    @Override
    protected String executeAuthenticated(HttpServletRequest request, HttpServletResponse response, User loginUser)
            throws ServletException, IOException {

        // 1. リクエストパラメータ（複製元のタスクID）の取得
        String idParam = request.getParameter("id");

        // 2. パラメータの安全チェック（未指定や不正な文字列のガード）
        if (idParam == null || !idParam.matches("\\d+")) {
            setSessionMessage(request, "不正なタスクIDが指定されたため、複製できませんでした。");
            // ★重複していた部分を整理し、正しいリダイレクト先のみにしました
            return "redirect:/app/task/list";
        }

        int originalTaskId = Integer.parseInt(idParam);
        TaskRepository taskRepository = new TaskRepository();

        try {
            // 3. Repositoryのコピー処理を実行（内部で所有者認可チェックが走ります）
            boolean success = taskRepository.copyTask(originalTaskId, loginUser.getId());

            if (success) {
                // 複製成功時：セッションにサクセスメッセージを格納
                setSessionMessage(request, "タスクを複製しました。");
                System.out.println("[DEBUG] タスク複製成功: 元タスクID=" + originalTaskId + " (ユーザーID=" + loginUser.getId() + ")");
            } else {
                // 複製失敗時（他人のタスクや存在しないIDの場合）
                setSessionMessage(request, "指定されたタスクが見つからないか、複製する権限がありません。");
                System.out.println("[SECURITY WARNING] 不正な、または存在しないタスクへの複製操作をブロックしました。タスクID=" + originalTaskId);
            }

        } catch (SQLException e) {
            System.err.println("[CRITICAL ERROR] タスクの複製処理中にデータベースエラーが発生しました。");
            e.printStackTrace();
            setSessionMessage(request, "システムエラーにより、タスクの複製に失敗しました。");
        }

        // 4. 仕様通り、処理後は必ず一覧画面へリダイレクト
        // ★末尾の重複もスッキリ解消しました
        return "redirect:/app/task/list";
    }

    /**
     * リダイレクト後もメッセージが消失しないよう、セッションにメッセージを一時退避する補助メソッド
     */
    private void setSessionMessage(HttpServletRequest request, String message) {
        HttpSession session = request.getSession();
        session.setAttribute("successMessage", message);
    }
}