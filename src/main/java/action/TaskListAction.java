package action;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.Task;
import model.User;
import repository.TaskRepository;

/**
 * タスク一覧 兼 検索・並べ替え制御アクション
 */
public class TaskListAction extends BaseAuthAction {

    @Override
    protected String executeAuthenticated(HttpServletRequest request, HttpServletResponse response, User loginUser)
            throws ServletException, IOException {

        // 1. 画面からの検索・ソートパラメータの取得
        String keyword = request.getParameter("keyword");
        String sort = request.getParameter("sort");

        // 2. パラメータのトリミングと初期値（デフォルト）設定
        if (keyword != null) {
            keyword = keyword.trim();
        } else {
            keyword = ""; // null の場合は空文字にして扱いやすくする
        }

        // ソート順の安全チェック（ASCでもDESCでもなければ、デフォルトのDESC（新しい順）にする）
        if (sort == null || (!sort.equals("ASC") && !sort.equals("DESC"))) {
            sort = "DESC";
        }

        TaskRepository taskRepository = new TaskRepository();
        List<Task> tasks;

        try {
            // 3. キーワードの有無によってRepositoryの呼び出しを分岐
            if (!keyword.isEmpty()) {
                // キーワードがある場合は部分一致検索
                tasks = taskRepository.search(loginUser.getId(), keyword, sort);
                System.out.println("[DEBUG] TaskListAction: 検索キーワード「" + keyword + "」、ソート「" + sort + "」で検索を実行しました。");
            } else {
                // キーワードがない場合は全件ソート取得
                tasks = taskRepository.findAllByUserIdWithSort(loginUser.getId(), sort);
                System.out.println("[DEBUG] TaskListAction: 全件取得、ソート「" + sort + "」を実行しました。");
            }

            // 4. 画面（JSP）に渡すデータをリクエスト属性にセット
            request.setAttribute("tasks", tasks);
            request.setAttribute("keyword", keyword); // 検索ワードを保持
            request.setAttribute("sort", sort);       // 現在のソート順を保持

            System.out.println("[INFO] TaskListAction: ユーザー「" + loginUser.getUsername() + "」のタスクを " + tasks.size() + " 件表示します。");

            // 5. 遷移先のJSPパスを戻り値として返す（FrontControllerが自動でフォワードします）
            return "/WEB-INF/views/task/list.jsp";

        } catch (SQLException e) {
            System.err.println("[CRITICAL ERROR] 一覧・検索処理中にデータベースエラーが発生しました。");
            e.printStackTrace();
            throw new ServletException("データベース処理中にエラーが発生しました。", e);
        }
    }
}