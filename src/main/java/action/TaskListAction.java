package action;

import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.Task;
import model.User;
import repository.TaskRepository;

/**
 * ログインユーザー固有のタスク一覧を取得して画面に渡すアクション
 */
public class TaskListAction extends BaseAuthAction {

    // データアクセスを行うリポジトリのインスタンスを用意
    private final TaskRepository taskRepository = new TaskRepository();

    @Override
    protected String executeAuthenticated(
            HttpServletRequest request, 
            HttpServletResponse response, 
            User loginUser) throws ServletException {
        
        try {
            // 1. ログインユーザーのIDを使って、その人のタスク一覧のみをDBから取得
            int userId = loginUser.getId();
            List<Task> taskList = taskRepository.findByUserId(userId);
            
            // 2. 開発・デバッグ用にログを出力（正しく件数が取れているか確認するため）
            System.out.println("[INFO] TaskListAction: ユーザー「" + loginUser.getUsername() 
                    + "」(ID:" + userId + ") のタスクを " + taskList.size() + " 件取得しました。");
            
            // 3. JSP（画面）で使えるように、リクエストスコープにデータを格納
            request.setAttribute("tasks", taskList);
            request.setAttribute("loginUser", loginUser); // 画面で「こんにちは、〇〇さん」と出す用
            
            // 4. 次に表示するJSPのビューパスを返す
            return "/WEB-INF/views/task/list.jsp";
            
        } catch (Exception e) {
            System.err.println("[ERROR] TaskListActionでエラーが発生しました: " + e.getMessage());
            e.printStackTrace();
            
            // 予期せぬ例外はFrontControllerの共通500エラーへ委譲するために、ServletExceptionで包んで投げる
            throw new ServletException("タスク一覧の取得中にシステムエラーが発生しました。", e);
        }
    }
}