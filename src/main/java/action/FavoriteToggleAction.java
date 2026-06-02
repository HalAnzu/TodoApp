package action;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.User;
import repository.TaskRepository;

/**
 * 非同期でお気に入り状態（ON/OFF）を切り替えるAPI用Action
 */
public class FavoriteToggleAction extends BaseAuthAction {

    private final TaskRepository taskRepository = new TaskRepository();

    // ★修正：親クラスの throws 節（ServletException, IOException）と完全に一致させます
    @Override
    protected String executeAuthenticated(HttpServletRequest request, HttpServletResponse response, User loginUser)
            throws ServletException, IOException {
        
        // 1. 親クラスから引数で渡された「loginUser」から安全にユーザーIDを取得
        int userId = loginUser.getId();

        // 2. リクエストパラメータの取得
        String taskIdParam = request.getParameter("taskId");
        String isFavoriteParam = request.getParameter("isFavorite");

        // 安全チェック：不正なパラメータの場合はエラーレスポンスを返す
        if (taskIdParam == null || !taskIdParam.matches("\\d+") || isFavoriteParam == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null; // JSPへのフォワードは行わない
        }

        int taskId = Integer.parseInt(taskIdParam);
        boolean isFavorite = "true".equals(isFavoriteParam);

        // 3. レスポンスの設定（軽量なプレーンテキストとして結果を返す）
        response.setContentType("text/plain;charset=UTF-8");
        
        try (PrintWriter out = response.getWriter()) {
            // 4. データベースの更新を実行（内部で他人のタスクを操作させない所有者チェックが走ります）
            boolean success = taskRepository.toggleFavorite(taskId, userId, isFavorite);
            
            if (success) {
                out.print("SUCCESS");
                System.out.println("[DEBUG] お気に入り切り替え成功: タスクID=" + taskId + " (ユーザーID=" + userId + " -> " + isFavorite + ")");
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("ERROR: Unauthorized or not found");
                System.out.println("[SECURITY WARNING] 不正な、または存在しないタスクへのお気に入り操作をブロックしました。タスクID=" + taskId);
            }
        } catch (SQLException e) {
            // ★修正：SQLExceptionが発生した場合は、ログを出力した上でServletExceptionにラップして投げます
            System.err.println("[CRITICAL ERROR] お気に入り非同期更新中にデータベースエラーが発生しました。");
            e.printStackTrace();
            throw new ServletException("データベース処理中にエラーが発生しました。", e);
        }

        // ★ポイント：非同期通信用のレスポンスを独自に書き出したため、コントローラーにはnullを返してJSPへの遷移をスキップさせる
        return null; 
    }
}