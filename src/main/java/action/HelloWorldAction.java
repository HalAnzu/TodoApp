package action;

import java.io.IOException;
import java.util.Date; // ★Dateクラスのインポートを追加

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.User;

/**
 * ログインが必要なメイン画面 (BaseAuthActionを継承することで自動的に保護されます)
 */
public class HelloWorldAction extends BaseAuthAction {

    @Override
    protected String executeAuthenticated(HttpServletRequest request, HttpServletResponse response, User loginUser)
            throws ServletException, IOException {
        
        System.out.println("[BUSINESS LOGIC] Executing HelloWorldAction for user: " + loginUser.getUsername());
        
        // 1. パラメータ処理 (name属性の取得とヌルチェック)
        String name = request.getParameter("name");
        String message = "Welcome to TaskManager!";
        
        // もしリクエストパラメータにnameがなければ、ログインしたユーザーの名前をデフォルトにする仕様に拡張
        if (name == null || name.trim().isEmpty()) {
            name = loginUser.getUsername();
        }
        
        message = "こんにちは、" + name + " さん！TaskManagerへようこそ！";
        
        // 2. 表示用データの準備とリクエストスコープへのバインド
        request.setAttribute("message", message);
        request.setAttribute("currentTime", new Date());
        request.setAttribute("pathInfo", request.getPathInfo());
        request.setAttribute("userAgent", request.getHeader("User-Agent"));
        request.setAttribute("remoteAddr", request.getRemoteAddr());
        
        // 第4回・5回用にログインユーザーオブジェクト自体も渡しておくとJSP側で便利です
        request.setAttribute("loginUser", loginUser);
        
        // 3. 遷移先JSPのパスを返却
        return "/WEB-INF/views/hello.jsp";
    }
}