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
@WebServlet("/app/*") // [cite: 33]
public class FrontController extends HttpServlet { // [cite: 30]
    private static final long serialVersionUID = 100L;

    // パスとActionの対応を管理するマップ
    private Map<String, Action> actionMap; // [cite: 36]

    @Override
    public void init() throws ServletException { // [cite: 37]
        try {
            actionMap = new HashMap<>(); // [cite: 198]
            
            // 各種Actionクラスの登録（キー名はパス正規化後の文字列）
            actionMap.put("hello", new HelloWorldAction()); // [cite: 84, 199]
            
            // 今後、Todo機能を追加する際はここに追記します
            // actionMap.put("todoList", new TodoListAction());
            // ★第4回のActionを新しく登録！
            actionMap.put("login", new action.LoginAction());   // "/app/login" で呼び出し
            actionMap.put("logout", new action.LogoutAction()); // "/app/logout" で呼び出し
            
            
            System.out.println("[INFO] FrontController initialized with " + actionMap.size() + " actions."); // [cite: 200]
        } catch (Exception e) {
            System.err.println("[ERROR] Initialization failed: " + e.getMessage()); // [cite: 202]
            throw new ServletException("Failed to initialize FrontController", e); // [cite: 41, 203]
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        processRequest(request, response); // [cite: 43]
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        processRequest(request, response); // [cite: 43]
    }

    /**
     * GET/POSTの共通リクエスト処理
     */
    private void processRequest(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException { // [cite: 45]
        
        // 文字化け防止（リクエストのエンコーディング設定）
        request.setCharacterEncoding("UTF-8"); // [cite: 188]
        
        // パス情報の取得と正規化
        String pathInfo = request.getPathInfo(); // [cite: 46]
        String actionKey = normalizePathInfo(pathInfo); // [cite: 52, 208]
        
        System.out.println("[DEBUG] Processing path: " + pathInfo + " -> ActionKey: " + actionKey); // [cite: 209]

        // actionMapから該当するActionを取得
        Action action = actionMap.get(actionKey); // [cite: 48, 211]
        
        if (action == null) {
            // 該当するActionがない場合は404エラー処理へ
            System.out.println("[WARN] No action found for path key: " + actionKey); // [cite: 213]
            handleNotFound(request, response); // [cite: 53, 214]
            return;
        }

        // Actionの実行と画面遷移処理
        try {
            String resultPage = action.execute(request, response); // [cite: 49]
            if (resultPage != null) {
                handleResult(resultPage, request, response); // [cite: 55]
            } else {
                throw new ServletException("Action returned null path."); // [cite: 175]
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Exception during action execution: " + e.getMessage());
            e.printStackTrace();
            throw new ServletException("Action execution failed", e); // [cite: 54]
        }
    }

    /**
     * パス情報の正規化（先頭・末尾の"/"を除去し、空ならデフォルト値を返す）
     */
    private String normalizePathInfo(String pathInfo) { // [cite: 52]
        if (pathInfo == null || pathInfo.equals("/")) { // [cite: 47]
            return "hello"; // デフォルトのアクションキー 
        }
        // 先頭の"/"を削除
        if (pathInfo.startsWith("/")) {
            pathInfo = pathInfo.substring(1); // [cite: 47]
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
            throws ServletException, IOException { // [cite: 55, 237]
        if (result.startsWith("redirect:")) {
            // "redirect:/app/hello" などの場合はリダイレクト処理
            String targetUrl = request.getContextPath() + result.substring("redirect:".length());
            response.sendRedirect(targetUrl);
        } else {
            // 通常のJSPパスの場合はフォワード処理
            System.out.println("[DEBUG] Forwarding to: " + result); // [cite: 241]
            RequestDispatcher dispatcher = request.getRequestDispatcher(result); // [cite: 242]
            dispatcher.forward(request, response); // [cite: 50, 243]
        }
    }

    /**
     * 404 Not Found のエラーハンドリング
     */
    private void handleNotFound(HttpServletRequest request, HttpServletResponse response) 
            throws IOException { // [cite: 53]
        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Requested resource not found in TodoApp."); // [cite: 125, 140]
    }
}