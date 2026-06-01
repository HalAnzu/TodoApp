package util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import model.User;

/**
 * セッション管理およびセッション固定化攻撃対策を行う共通ユーティリティークラス
 */
public class SessionUtil {

    // セッション属性名の定数定義
    private static final String LOGIN_USER_KEY = "loginUser"; //

    /**
     * セッションからログイン中のユーザー情報を取得する
     */
    public static User getLoginUser(HttpServletRequest request) { //
        HttpSession session = request.getSession(false); // 存在しない場合は新規作成しない
        if (session == null) {
            return null;
        }
        try {
            return (User) session.getAttribute(LOGIN_USER_KEY);
        } catch (ClassCastException e) {
            // セッション情報が破損している場合は安全のために削除
            session.removeAttribute(LOGIN_USER_KEY);
            return null;
        }
    }

    /**
     * 現在ログイン状態であるかをチェックする
     */
    public static boolean isLoggedIn(HttpServletRequest request) { //
        return getLoginUser(request) != null;
    }

    /**
     * ログイン成功時の処理（セッション固定化攻撃対策を含む）
     */
    public static void login(HttpServletRequest request, User user) { //
        // 1. 既存のセッションがあれば一度完全に破棄する（Session IDの偽装を防ぐ）
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate(); // 破棄
        }

        // 2. 新しいセッションを完全に新規作成し、ユーザー情報を紐付ける
        HttpSession newSession = request.getSession(true);
        newSession.setAttribute(LOGIN_USER_KEY, user);
        
        System.out.println("[SECURITY INFO] Session recreated for user: " + user.getUsername());
    }

    /**
     * ログアウト処理（セッションの完全破棄）
     */
    public static void logout(HttpServletRequest request) { //
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate(); // セッションの無効化
            System.out.println("[INFO] Session invalidated successfully.");
        }
    }
}