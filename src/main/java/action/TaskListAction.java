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
 * タスク一覧 兼 検索・並べ替え・ページング制御アクション
 */
public class TaskListAction extends BaseAuthAction {

    // 1ページあたりに表示するタスクの件数（ここでは仕様に合わせ5件に設定。10件等に変更も可能）
    private static final int PAGE_SIZE = 5;

    @Override
    protected String executeAuthenticated(HttpServletRequest request, HttpServletResponse response, User loginUser)
            throws ServletException, IOException {

        // 1. 画面からのパラメータ（検索・ソート・ページ番号）の取得
        String keyword = request.getParameter("keyword");
        String sort = request.getParameter("sort");
        String pageParam = request.getParameter("page");

        // 2. 検索キーワードのトリミングと初期値設定
        if (keyword != null) {
            keyword = keyword.trim();
        } else {
            keyword = "";
        }

        // 3. ソート順の安全チェック（デフォルトはDESC: 新しい順）
        if (sort == null || (!sort.equals("ASC") && !sort.equals("DESC"))) {
            sort = "DESC";
        }

        // 4. ページ番号の安全な数値変換（NumberFormatExceptionの回避とガードルール）
        int currentPage = 1; // デフォルトは1ページ目
        if (pageParam != null && pageParam.matches("\\d+")) {
            currentPage = Integer.parseInt(pageParam);
            if (currentPage < 1) {
                currentPage = 1; // 1未満の不正な数値は1ページ目に補正
            }
        }

        TaskRepository taskRepository = new TaskRepository();

        try {
            // 5. ページング計算に必要な「条件に一致する総件数」をDBから取得
            int totalTasks = taskRepository.countTasks(loginUser.getId(), keyword);

            // 6. 総件数とPAGE_SIZEから「最大ページ数（総ページ数）」を計算
            // 例: 13件で5件ずつの場合、(13 + 5 - 1) / 5 = 3 ページ となる計算式
            int totalPages = (totalTasks + PAGE_SIZE - 1) / PAGE_SIZE;
            if (totalPages < 1) {
                totalPages = 1; // データが0件の場合でも総ページ数は最低1にする
            }

            // 現在のページ番号が最大ページ数を超えていた場合の安全補正
            //if (currentPage > totalPages) {
            //    currentPage = totalPages;
            //}
            
            // 現在のページ番号が最大ページ数を超えていた場合は、1ページ目にリセットする（仕様書のルールに合わせる）
            if (currentPage > totalPages) {
                currentPage = 1;
            }

            // 7. データベースから取得する開始位置（OFFSET）の計算
            // 1ページ目 -> (1-1)*5 = 0,  2ページ目 -> (2-1)*5 = 5
            int offset = (currentPage - 1) * PAGE_SIZE;

            // 8. ページング対応メソッドで、現在のページに表示すべきタスクのみを取得
            List<Task> tasks = taskRepository.searchWithPaging(loginUser.getId(), keyword, sort, PAGE_SIZE, offset);

            // 9. 画面（JSP）へ、ページングの描画に必要な情報をすべて引き渡す
            request.setAttribute("tasks", tasks);
            request.setAttribute("keyword", keyword);
            request.setAttribute("sort", sort);
            request.setAttribute("currentPage", currentPage);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalTasks", totalTasks);

            System.out.println("[DEBUG] ページング実行: 現在ページ=" + currentPage + "/" + totalPages 
                    + " (総件数=" + totalTasks + "件, OFFSET=" + offset + ")");

            // 10. 一覧画面のパスを戻り値として返す（親クラス・FrontControllerの共通設計ルール）
            return "/WEB-INF/views/task/list.jsp";

        } catch (SQLException e) {
            System.err.println("[CRITICAL ERROR] ページング・一覧処理中にデータベースエラーが発生しました。");
            e.printStackTrace();
            throw new ServletException("データベース処理中にエラーが発生しました。", e);
        }
    }
}