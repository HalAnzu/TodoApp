package action;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import controller.Action;

/**
 * 動作確認用の最初のActionクラス
 */
public class HelloWorldAction implements Action { // [cite: 64]

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException { // [cite: 66]
        
        // 1. パラメータ処理 (name属性の取得とヌルチェック)
        String name = request.getParameter("name"); // [cite: 75, 80]
        String message = "Welcome to TodoApp!"; // 
        
        if (name != null && !name.trim().isEmpty()) { // [cite: 82]
            message = "こんにちは、" + name + " さん！TodoAppへようこそ！"; // [cite: 76, 123]
        }
        
        // 2. 表示用データの準備とリクエストスコープへのバインド
        request.setAttribute("message", message); // [cite: 73, 74]
        request.setAttribute("currentTime", new Date()); // [cite: 69]
        request.setAttribute("pathInfo", request.getPathInfo()); // [cite: 70]
        request.setAttribute("userAgent", request.getHeader("User-Agent")); // [cite: 71]
        request.setAttribute("remoteAddr", request.getRemoteAddr()); // [cite: 72]
        
        // 3. 遷移先JSPのパスを返却
        return "/WEB-INF/views/hello.jsp"; // [cite: 78, 179]
    }
}