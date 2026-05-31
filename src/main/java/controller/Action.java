package controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 統一的なAction実行インターフェース
 */
public interface Action {
    /**
     * リクエストに応じた処理を実行し、遷移先のパスを返却する
     * @param request サーブレットリクエスト
     * @param response サーブレットレスポンス
     * @return 遷移先JSPパス（例: "/WEB-INF/views/hello.jsp"）または リダイレクトURL
     * @throws ServletException サーブレット例外
     * @throws IOException 入出力例外
     */
    String execute(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException; // [cite: 13, 14, 15, 16]
}
