package util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * セキュリティを考慮したエラーハンドリングを一元管理する共通クラス
 */
public class ErrorUtil {

    /**
     * 認可（権限）エラーの統一処理
     * 内部ログには詳細を出し、画面には技術的な情報を一切伏せる
     */
    public static String handleAuthorizationError(
            HttpServletRequest request, 
            HttpServletResponse response, 
            String operation) {
        
        // 1. サーバーログ出力（管理者・技術者向けの詳細情報。セキュリティのために標準エラーに出す）
        System.err.println("[SECURITY WARN] 認可エラーが発生しました。");
        System.err.println("  操作: " + operation);
        System.err.println("  対象URI: " + request.getRequestURI());
        System.err.println("  アクセスIP: " + request.getRemoteAddr());
        
        // 2. ユーザー向けに非技術的な優しいメッセージをセット
        request.setAttribute("error", "この操作を実行する権限がありません。");
        
        // 403 Forbidden のステータスコードをレスポンスに明示的に設定
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        
        // 3. 共通の403エラー画面のビューパスを返す
        return "/WEB-INF/views/error/403.jsp";
    }

    /**
     * 認証（ログイン）エラーの統一処理
     * 未認証アクセスを検知した際、元のURLを退避させてログイン画面へリダイレクト
     */
    public static String handleAuthenticationError(
            HttpServletRequest request, 
            HttpServletResponse response) {
        
        // 元の要求URI（リターンURL）を取得して保持する
        String returnUrl = request.getRequestURI();
        if (request.getQueryString() != null) {
            returnUrl += "?" + request.getQueryString();
        }
        
        System.out.println("[AUTH INFO] 未認証アクセスを検知。リターンURLを保存: " + returnUrl);
        
        HttpSession session = request.getSession(true);
        session.setAttribute("returnUrl", returnUrl);
        
        // フロントコントローラー経由でのログイン画面リダイレクト
        return "redirect:/app/login";
    }
}