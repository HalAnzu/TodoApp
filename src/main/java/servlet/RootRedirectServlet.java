package servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ルートURL（/）へのアクセスを、アプリケーションのメイン入り口（/app/hello）へリダイレクトするサーブレット
 */
@WebServlet("") // 空文字を指定することで、コンテキストルート直下（/TaskManager/）へのアクセスをキャッチします
public class RootRedirectServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // 「/TaskManager/app/hello」という絶対パスを動的に生成してリダイレクト
        String targetPath = request.getContextPath() + "/app/hello";
        
        System.out.println("[ROUTE LOG] ルートURLへのアクセスを検知しました。 " + targetPath + " へリダイレクトします。");
        response.sendRedirect(targetPath);
    }
}