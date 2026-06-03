package action;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import controller.Action;
import model.User;
import repository.TaskRepository; // ★共通処理で利用するためインポートを追加
import util.AuthUtil;
import util.ErrorUtil;

/**
 * ログイン認可が必要なActionのための共通親クラス (Template Method パターン)
 */
public abstract class BaseAuthAction implements Action {

    @Override
    public final String execute(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("[SECURITY CHECK] Intercepting request in BaseAuthAction...");

        // 1. 認証チェック：ログインしているかどうかをチェック
        if (!AuthUtil.isLoggedIn(request)) {
            // 未ログインの場合はErrorUtilを呼び出してログイン画面へ案内（リターンURL保存含む）
            return ErrorUtil.handleAuthenticationError(request, response);
        }

        // 2. ログインユーザー情報の取得
        // ★ここで「loginUser」という変数を正しく定義しています
        User loginUser = AuthUtil.getLoginUser(request);

        // 万が一、オブジェクトが取得できなかった場合の安全弁
        if (loginUser == null) {
            return ErrorUtil.handleAuthenticationError(request, response);
        }

        // 3. 認可チェックを通過したため、子クラス（業務Action）の個別処理へ委譲する
        // ★定義した loginUser を第3引数として子クラスに引き渡します
        return executeAuthenticated(request, response, loginUser);
    }

    /**
     * 認証・認可が成功した後に実行される、各画面固有の業務処理
     * 子クラス（HelloWorldActionなど）でオーバーライドして実装します
     */
    protected abstract String executeAuthenticated(
            HttpServletRequest request, 
            HttpServletResponse response, 
            User loginUser) throws ServletException, IOException;

    /**
     * ★共通化メソッド：画面のドロップダウンやチップ表示に必要なカテゴリ統計情報をリクエストにセットする
     * 子クラス（TaskListAction, TaskNewAction, TaskEditActionなど）から1行で呼び出せます。
     */
    protected void setupCategoryDropdown(HttpServletRequest request, TaskRepository taskRepository, User loginUser) {
        try {
            java.util.Map<String, Integer> categoryStats = taskRepository.getCategoryStats(loginUser.getId());
            request.setAttribute("categoryStats", categoryStats);
            System.out.println("[DEBUG] [共通処理] カテゴリ統計数をJSPに渡しました。件数 = " + (categoryStats != null ? categoryStats.size() : 0));
        } catch (java.sql.SQLException e) {
            System.err.println("[ERROR] [共通処理] カテゴリ統計の取得に失敗しました。");
            e.printStackTrace();
        }
    }
}
