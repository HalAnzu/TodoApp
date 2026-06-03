package action;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.TaskStatistics;
import model.User;
import repository.TaskRepository;

/**
 * ダッシュボード（簡易統計画面）の表示要求（GET）を処理するコントローラー
 */
public class DashboardAction extends BaseAuthAction {

    private final TaskRepository taskRepository = new TaskRepository();

    @Override
    protected String executeAuthenticated(
            HttpServletRequest request, 
            HttpServletResponse response, 
            User loginUser) throws ServletException, IOException {
        
        System.out.println("[DEBUG] DashboardAction: 表示処理を開始します。ユーザーID = " + loginUser.getId());

        try {
            // 1. Repositoryからログインユーザー固有の統計データを取得
            TaskStatistics statistics = taskRepository.getTaskStatistics(loginUser.getId());
            
            // 2. 統計DTOオブジェクトをリクエストスコープに格納
            request.setAttribute("statistics", statistics);
            
            // 3. ★リファクタリングの成果：親クラスの共通メソッドを1行呼ぶだけでカテゴリ統計も自動セット！
            setupCategoryDropdown(request, taskRepository, loginUser);
            
            System.out.println("[DEBUG] DashboardAction: 統計データの取得に成功しました。総数 = " + statistics.getTotalTasks());
            
        } catch (SQLException e) {
            System.err.println("[CRITICAL ERROR] DashboardAction: 統計データの取得中にDB例外が発生しました。");
            e.printStackTrace();
            throw new ServletException("ダッシュボード情報の取得に失敗しました。", e);
        }

        // 4. ダッシュボード専用のJSP画面へフォワード
        return "/WEB-INF/views/dashboard/main.jsp"; // 手順書の仕様に合わせたパス
    }
}
