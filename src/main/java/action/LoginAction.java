package action;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import controller.Action;
import model.User;
import repository.UserRepository;
import util.SessionUtil;

/**
 * ログイン画面表示および認証処理を統括するActionクラス
 */
public class LoginAction implements Action { //

    private final UserRepository userRepository = new UserRepository();

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException { //
        
        String method = request.getMethod(); // GET か POST かを取得
        System.out.println("[DEBUG] LoginAction invoked via method: " + method);

        if ("POST".equalsIgnoreCase(method)) {
            // 認証の実行 (POST)
            return processLogin(request, response); //
        } else {
            // ログインフォームの表示 (GET)
            return showLoginForm(request, response); //
        }
    }

    /**
     * ログインフォーム表示処理 (GET時)
     */
    private String showLoginForm(HttpServletRequest request, HttpServletResponse response) {
        // すでにログインしている場合はメイン（hello）へリダイレクト
        if (SessionUtil.isLoggedIn(request)) {
            return "redirect:/app/hello";
        }
        return "/WEB-INF/views/login.jsp";
    }

    /**
     * 認証処理実行 (POST時)
     */
    private String processLogin(HttpServletRequest request, HttpServletResponse response) {
        // 1. パラメータの取得
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        System.out.println("ログイン試行 - ユーザー名: " + username); // デバッグ用ログ

        // 2. パラメータのnull / 空文字チェック
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            request.setAttribute("errorMessage", "ユーザー名とパスワードを入力してください。");
            request.setAttribute("enteredUsername", username); // 入力値保持によるUX配慮
            return "/WEB-INF/views/login.jsp";
        }

        // 3. データベース照合
        User user = userRepository.findByUsername(username.trim());
        System.out.println("ユーザー取得結果: " + (user != null ? "成功" : "失敗"));

        // 実際のアプリではハッシュ化パスワードの照合を行いますが、
        // 今回の初期データ（例: secure_pass1）に合わせて簡易文字列比較を行います
        if (user != null && user.getPassword().equals(password)) {
            // 4. 認証成功：SessionUtilを使用して安全にセッション開始
            SessionUtil.login(request, user);
            return "redirect:/app/hello"; // 成功時はFrontController経由でリダイレクト
        } else {
            // 5. 認証失敗：エラーメッセージを格納してフォームを再表示
            request.setAttribute("errorMessage", "ユーザー名またはパスワードが正しくありません。");
            request.setAttribute("enteredUsername", username); // 入力値を保持させる
            return "/WEB-INF/views/login.jsp";
        }
    }
}