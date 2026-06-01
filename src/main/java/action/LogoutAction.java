package action;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import controller.Action;
import util.SessionUtil;

/**
 * ログアウト処理を実行するActionクラス
 */
public class LogoutAction implements Action { //

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException { //
        
        // SessionUtil を使ってセッションを破棄
        SessionUtil.logout(request); //
        
        // ログアウト後はログイン画面へ自動的にリダイレクトさせる
        return "redirect:/app/login"; //
    }
}