package util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import model.User;

/**
 * 認証・認可に関する詳細なチェックを行う共通ユーティリティークラス
 */
public class AuthUtil {

    private static final String LOGIN_USER_KEY = "loginUser";

    /**
     * セッションからログインユーザー情報を取得する
     */
    public static User getLoginUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        try {
            return (User) session.getAttribute(LOGIN_USER_KEY);
        } catch (ClassCastException e) {
            session.removeAttribute(LOGIN_USER_KEY);
            return null;
        }
    }

    /**
     * ログイン状態をチェックする
     */
    public static boolean isLoggedIn(HttpServletRequest request) {
        return getLoginUser(request) != null;
    }

    /**
     * データの所有者チェック（認可の基本判定）
     * @param targetUserId データの所有者ID
     * @param loginUserId 現在ログインしているユーザーのID
     * @return 一致していればtrue
     */
    public static boolean isOwner(int targetUserId, int loginUserId) {
        return targetUserId == loginUserId;
    }

    /**
     * 統合権限チェック：ログイン状態であり、かつデータの所有者であるか
     */
    public static boolean canAccess(HttpServletRequest request, int targetUserId) {
        User loginUser = getLoginUser(request);
        if (loginUser == null) {
            return false;
        }
        return isOwner(targetUserId, loginUser.getId());
    }

    /**
     * 未認証時にログイン画面へリダイレクトさせ、元のURLを保持する処理
     */
    public static String redirectToLogin(HttpServletRequest request) {
        // オプション要件：元の要求URI（リターンURL）を取得してセッション等に保存することも可能
        String requestURI = request.getRequestURI();
        String queryString = request.getQueryString();
        String returnURL = requestURI + (queryString != null ? "?" + queryString : "");
        
        request.getSession(true).setAttribute("returnURL", returnURL);
        System.out.println("[AUTH INFO] Unauthorized access intercepted. ReturnURL saved: " + returnURL);
        
        return "redirect:/app/login";
    }
}