package action;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import model.Task;
import model.User;
import repository.TaskRepository;

/**
 * 新規タスクの表示（GET）および登録処理（POST）を制御するアクション
 */
public class TaskNewAction extends BaseAuthAction {

    private final TaskRepository taskRepository = new TaskRepository();

    @Override
    protected String executeAuthenticated(
            HttpServletRequest request, 
            HttpServletResponse response, 
            User loginUser) throws ServletException {
        
        String method = request.getMethod();
        System.out.println("[DEBUG] TaskNewAction: Method = " + method);

        if ("GET".equalsIgnoreCase(method)) {
            // ==========================================
            // ① 画面表示処理 (GET)
            // ==========================================
            return "/WEB-INF/views/task/new.jsp";
            
        } else if ("POST".equalsIgnoreCase(method)) {
            // ==========================================
            // ② データベース登録処理 (POST)
            // ==========================================
            
            // 1. フォームからの入力値を受け取る
            String title = request.getParameter("title");
            String description = request.getParameter("description");
            
            // 前後の余白（空白）を除去
            if (title != null) title = title.trim();
            if (description != null) description = description.trim();

            // 2. バリデーション（入力チェック）
            Map<String, String> errors = new HashMap<>();
            
            if (title == null || title.isEmpty()) {
                errors.put("title", "タイトルは必須入力です。");
            } else if (title.length() > 50) {
                errors.put("title", "タイトルは50文字以内で入力してください。");
            }
            
            if (description != null && description.length() > 200) {
                errors.put("description", "説明は200文字以内で入力してください。");
            }

            // 3. エラーがあった場合は、入力値を保持してフォーム画面に戻す
            if (!errors.isEmpty()) {
                System.out.println("[WARN] TaskNewAction: バリデーションエラーを検知しました。件数: " + errors.size());
                request.setAttribute("errors", errors);
                
                // 入力された値をJSPに送り返す（入力値保持機能）
                request.setAttribute("title", title);
                request.setAttribute("description", description);
                
                return "/WEB-INF/views/task/new.jsp";
            }

            // 4. エラーがなければ、Taskオブジェクトを組み立ててDBへ保存
            Task task = new Task();
            task.setUserId(loginUser.getId()); // 確実にログインしているユーザーのIDをセット（セキュリティ担保）
            task.setTitle(title);
            task.setDescription(description);

            boolean isSaved = taskRepository.save(task);

            if (isSaved) {
                System.out.println("[INFO] TaskNewAction: 新規タスク登録成功。ID: " + task.getId());
                
                // ★PRGパターンの適用★
                // リダイレクトするとリクエストスコープは消えてしまうため、セッションにメッセージを一時保存（フラッシュメッセージ）
                HttpSession session = request.getSession();
                session.setAttribute("flash_success", "新しいタスク「" + title + "」を登録しました！");
                
                // 一覧画面へリダイレクト（FrontControllerのhandleResultへ引き渡す）
                return "redirect:/app/task/list";
            } else {
                // 保存失敗時はエラー画面（500）に飛ばすため、例外をスロー
                throw new ServletException("データベースへのタスク保存処理に失敗しました。");
            }
        }

        // GET/POST以外のメソッドが来た場合は404扱いにする
        return "redirect:/app/task/list";
    }
}
