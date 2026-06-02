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
        
        // ★追加：お気に入り絞り込みフィルターパラメータの取得
        String favoriteParam = request.getParameter("favoriteOnly");
        boolean favoriteOnly = "true".equals(favoriteParam);

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
            // 5. 総件数の取得（★修正：favoriteOnly フラグを引数に追加）
            int totalTasks = taskRepository.countTasks(loginUser.getId(), keyword, favoriteOnly);

            // 6. 総件数とPAGE_SIZEから「最大ページ数（総ページ数）」を計算
            int totalPages = (totalTasks + PAGE_SIZE - 1) / PAGE_SIZE;
            if (totalPages < 1) {
                totalPages = 1; // データが0件の場合でも総ページ数は最低1にする
            }

            // 現在のページ番号が最大ページ数を超えていた場合は、1ページ目にリセットする（仕様書のルールに合わせる）
            if (currentPage > totalPages) {
                currentPage = 1;
            }

            // 7. データベースから取得する開始位置（OFFSET）の計算
            int offset = (currentPage - 1) * PAGE_SIZE;

            // 8. ページング・条件に合致するタスク一覧の取得（★修正：変数名を「tasks」に統一して下のエラーを防止）
            List<Task> tasks = taskRepository.searchWithPaging(loginUser.getId(), keyword, sort, favoriteOnly, PAGE_SIZE, offset);

            // 9. 画面（JSP）へ、ページングの描画に必要な情報をすべて引き渡す
            request.setAttribute("tasks", tasks); // 変数名が一致したため、ここでエラーは出なくなります！
            request.setAttribute("keyword", keyword);
            request.setAttribute("sort", sort);
            request.setAttribute("favoriteOnly", favoriteOnly); // ★追加：現在のフィルター状態を保持
            request.setAttribute("currentPage", currentPage);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalTasks", totalTasks);

            System.out.println("[DEBUG] ページング・お気に入り実行: 現在ページ=" + currentPage + "/" + totalPages 
                    + " (総件数=" + totalTasks + "件, OFFSET=" + offset + ", お気に入り絞り込み=" + favoriteOnly + ")");

            // 10. 一覧画面のパスを戻り値として返す
            return "/WEB-INF/views/task/list.jsp";

        } catch (SQLException e) {
            System.err.println("[CRITICAL ERROR] ページング・一覧処理中にデータベースエラーが発生しました。");
            e.printStackTrace();
            throw new ServletException("データベース処理中にエラーが発生しました。", e);
        }
    }
}