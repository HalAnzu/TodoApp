package controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import action.HelloWorldAction;

/**
 * 全体のリクエストを統括するフロントコントローラー
 */
@WebServlet("/app/*")
public class FrontController extends HttpServlet {
    private static final long serialVersionUID = 100L;

    // パスとActionの対応を管理するマップ
    private Map<String, Action> actionMap;

    @Override
    public void init() throws ServletException {
        try {
            actionMap = new HashMap<>();
            
            // 各種Actionクラスの登録（キー名はパス正規化後の文字列）
            actionMap.put("hello", new HelloWorldAction());
            
            // 今後、Todo機能を追加する際はここに追記します
            // actionMap.put("todoList", new TodoListAction());
            
            // 第4回のActionの登録
            actionMap.put("login", new action.LoginAction());   // "/app/login" で呼び出し
            actionMap.put("logout", new action.LogoutAction()); // "/app/logout" で呼び出し
            
            // ★第6回のActionを新しく登録！
            actionMap.put("task/list", new action.TaskListAction()); // "/app/task/list" で呼び出し
            actionMap.put("task/new", new action.TaskNewAction());   // "/app/task/new" で呼び出し  
            
            // ★第7回のActionを新しく登録！
            actionMap.put("task/edit", new action.TaskEditAction());   // "/app/task/edit" で呼び出し
            actionMap.put("task/delete", new action.TaskDeleteAction()); // "/app/task/delete" で呼び出し
            
            actionMap.put("task/toggleFavorite", new action.FavoriteToggleAction()); // ★修正：favoriteOnly フラグを引数に追加）
            
            System.out.println("[INFO] FrontController initialized with " + actionMap.size() + " actions.");
        } catch (Exception e) {
            System.err.println("[ERROR] Initialization failed: " + e.getMessage());
            throw new ServletException("Failed to initialize FrontController", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * GET/POSTの共通リクエスト処理
     */
    private void processRequest(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 文字化け防止（リクエストのエンコーディング設定）
        request.setCharacterEncoding("UTF-8");
        
        // パス情報の取得と正規化
        String pathInfo = request.getPathInfo();
        String actionKey = normalizePathInfo(pathInfo);
        
        System.out.println("[DEBUG] Processing path: " + pathInfo + " -> ActionKey: " + actionKey);

        // actionMapから該当するActionを取得
        Action action = actionMap.get(actionKey);
        
        if (action == null) {
            // 該当するActionがない場合は404エラー処理へ
            System.out.println("[WARN] No action found for path key: " + actionKey);
            handleNotFound(request, response);
            return;
        }
        
        // ★【追加】セッションからフラッシュメッセージを抽出し、リクエストスコープに詰め替える処理
        javax.servlet.http.HttpSession session = request.getSession(false);
        if (session != null) {
            String flashSuccess = (String) session.getAttribute("flash_success");
            if (flashSuccess != null) {
                // JSP（list.jspなど）が読み込めるようにrequestにセット
                request.setAttribute("successMessage", flashSuccess);
                // 1回表示したら消すためにセッションから削除
                session.removeAttribute("flash_success");
                System.out.println("[DEBUG] Flash message transferred to request: " + flashSuccess);
            }
        }        

        // Actionの実行と画面遷移処理
        try {
            String resultPage = action.execute(request, response);
            if (resultPage != null) {
                handleResult(resultPage, request, response);
            } else {
                throw new ServletException("Action returned null path.");
            }
        } catch (Exception e) {
            System.err.println("[CRITICAL ERROR] Exception during action execution: " + e.getMessage());
            e.printStackTrace(); // 開発用にコンソールへ詳細を出力
            
            // 安全なエラー画面へのフォワード処理（無限ループ防止）
            try {
                if (!response.isCommitted()) {
                    // レスポンスステータスを500に設定
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    
                    // web.xmlを介さず、直接500.jspへ安全にフォワードする
                    request.getRequestDispatcher("/WEB-INF/views/error/500.jsp").forward(request, response);
                }
            } catch (Exception ex) {
                System.err.println("[FATAL ERROR] エラー画面への遷移自体に失敗しました: " + ex.getMessage());
            }
            return;
        }
    } // ★←ここ！消失していた processRequest を閉じる波カッコを追加しました

    /**
     * パス情報の正規化（先頭・末尾の"/"を除去し、空ならデフォルト値を返す）
     */
    private String normalizePathInfo(String pathInfo) {
        if (pathInfo == null || pathInfo.equals("/")) {
            return "hello"; // デフォルトのアクションキー 
        }
        // 先頭の"/"を削除
        if (pathInfo.startsWith("/")) {
            pathInfo = pathInfo.substring(1);
        }
        // 末尾の"/"を削除
        if (pathInfo.endsWith("/")) {
            pathInfo = pathInfo.substring(0, pathInfo.length() - 1);
        }
        return pathInfo;
    }

    /**
     * 実行結果に応じた画面遷移（フォワードまたはリダイレクト）
     */
    private void handleResult(String result, HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        if (result.startsWith("redirect:")) {
            // "redirect:/app/hello" などの場合はリダイレクト処理
            String targetUrl = request.getContextPath() + result.substring("redirect:".length());
            response.sendRedirect(targetUrl);
        } else {
            // 通常のJSPパスの場合はフォワード処理
            System.out.println("[DEBUG] Forwarding to: " + result);
            RequestDispatcher dispatcher = request.getRequestDispatcher(result);
            dispatcher.forward(request, response);
        }
    }

    /**
     * 404 Not Found のエラーハンドリング（安全な直接フォワード版）
     */
    private void handleNotFound(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        if (!response.isCommitted()) {
            // ステータスコードを404に設定
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            
            // 直接404.jspへフォワード
            request.getRequestDispatcher("/WEB-INF/views/error/404.jsp").forward(request, response);
        }
    }
}