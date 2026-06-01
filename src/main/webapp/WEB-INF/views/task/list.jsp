<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>タスク一覧 - TodoApp</title>
    <style>
        body { font-family: 'Helvetica Neue', Arial, sans-serif; background-color: #f8f9fa; color: #333; margin: 0; padding: 20px; }
        .container { max-width: 1000px; margin: 0 auto; background: #fff; padding: 30px; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
        
        .header-area { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; border-bottom: 2px solid #e9ecef; padding-bottom: 15px; }
        h1 { margin: 0; font-size: 24px; color: #495057; }
        .user-info { font-size: 14px; color: #6c757d; }
        
        /* 検索・ソート操作バーのスタイリング */
        .search-sort-bar { display: flex; justify-content: space-between; align-items: center; background-color: #f1f3f5; padding: 12px 20px; border-radius: 6px; margin-bottom: 20px; gap: 15px; }
        .search-form { display: flex; align-items: center; gap: 10px; flex-grow: 1; }
        .search-input { padding: 8px 12px; font-size: 14px; border: 1px solid #ced4da; border-radius: 4px; width: 280px; box-sizing: border-box; }
        .search-input:focus { border-color: #80bdff; outline: none; }
        
        .sort-links { font-size: 14px; color: #495057; font-weight: bold; white-space: nowrap; }
        .sort-links a { text-decoration: none; color: #007bff; margin-left: 5px; padding: 4px 8px; border-radius: 4px; transition: background-color 0.2s; }
        .sort-links a:hover { background-color: #e9ecef; text-decoration: underline; }
        .sort-active { background-color: #007bff !important; color: #fff !important; text-decoration: none !important; cursor: default; }

        .result-count { font-size: 14px; color: #6c757d; margin-bottom: 10px; font-weight: bold; }

        /* フラッシュメッセージ */
        .alert-success { background-color: #d4edda; color: #155724; border: 1px solid #c3e6cb; padding: 12px; border-radius: 4px; margin-bottom: 20px; font-weight: bold; }
        
        .btn { display: inline-block; padding: 8px 16px; font-size: 14px; font-weight: bold; text-decoration: none; border-radius: 4px; cursor: pointer; transition: background-color 0.2s; border: none; }
        .btn-primary { background-color: #007bff; color: #fff; }
        .btn-primary:hover { background-color: #0056b3; }
        .btn-secondary { background-color: #6c757d; color: #fff; padding: 8px 14px; text-decoration: none; font-size: 14px; }
        .btn-secondary:hover { background-color: #5a6268; }
        
        .btn-edit { background-color: #ffc107; color: #212529; padding: 5px 10px; font-size: 12px; border-radius: 4px; text-decoration: none; font-weight: bold; }
        .btn-edit:hover { background-color: #e0a800; }
        .btn-delete { background-color: #dc3545; color: #fff; padding: 5px 10px; font-size: 12px; border-radius: 4px; font-weight: bold; cursor: pointer; }
        .btn-delete:hover { background-color: #bd2130; }
        .btn-logout { background-color: #6c757d; color: #fff; font-size: 12px; padding: 6px 12px; margin-left: 10px; }
        .btn-logout:hover { background-color: #5a6268; }

        .task-table { width: 100%; border-collapse: collapse; margin-top: 5px; }
        .task-table th, .task-table td { padding: 12px; text-align: left; border-bottom: 1px solid #dee2e6; }
        .task-table th { background-color: #f8f9fa; color: #495057; font-weight: bold; }
        
        /* ステータスバッジ (第8回の実装値 pending/in_progress/completed に同期) */
        .badge { display: inline-block; padding: 4px 8px; font-size: 12px; font-weight: bold; border-radius: 12px; text-align: center; }
        .badge-status-pending { background-color: #e9ecef; color: #495057; }
        .badge-status-in_progress { background-color: #cce5ff; color: #004085; }
        .badge-status-completed { background-color: #d4edda; color: #155724; text-decoration: line-through; }
        
        .empty-message { text-align: center; color: #868e96; padding: 40px; font-style: italic; font-size: 15px; background: #f8f9fa; border-radius: 6px; border: 1px dashed #dee2e6; }
        .action-cell { display: flex; gap: 8px; align-items: center; }
        .inline-form { margin: 0; padding: 0; display: inline; }
    </style>
    <script>
        function confirmDelete(taskTitle) {
            return confirm("タスク「" + taskTitle + "」を削除してもよろしいですか？\nこの操作は取り消せません。");
        }
    </script>
</head>
<body>

<div class="container">
    <div class="header-area">
        <div>
            <h1>マイタスク一覧</h1>
            <div class="user-info">
                ログインユーザー: <strong><c:out value="${loginUser.username}"/></strong> さん
                <a href="${pageContext.request.contextPath}/app/logout" class="btn btn-logout">ログアウト</a>
            </div>
        </div>
        <a href="${pageContext.request.contextPath}/app/task/new" class="btn btn-primary">+ 新規タスク追加</a>
    </div>

    <c:if test="${not empty successMessage}">
        <div class="alert-success">
            <c:out value="${successMessage}"/>
        </div>
    </c:if>

    <div class="search-sort-bar">
        <form action="${pageContext.request.contextPath}/app/task/list" method="GET" class="search-form">
            <input type="hidden" name="sort" value="<c:out value='${sort}'/>">
            
            <input type="text" name="keyword" class="search-input" placeholder="タスク名で検索..." value="<c:out value='${keyword}'/>">
            <button type="submit" class="btn btn-primary">検索</button>
            
            <c:if test="${not empty keyword}">
                <a href="${pageContext.request.contextPath}/app/task/list?sort=<c:out value='${sort}'/>" class="btn btn-secondary">クリア</a>
            </c:if>
        </form>
        
        <div class="sort-links">
            作成日時:
            <a href="${pageContext.request.contextPath}/app/task/list?keyword=<c:out value='${keyword}'/>&sort=DESC" 
               class="${sort == 'DESC' ? 'sort-active' : ''}">新しい順</a>
            <a href="${pageContext.request.contextPath}/app/task/list?keyword=<c:out value='${keyword}'/>&sort=ASC" 
               class="${sort == 'ASC' ? 'sort-active' : ''}">古い順</a>
        </div>
    </div>

    <c:choose>
        <c:when test="${not empty tasks}">
            <div class="result-count">
                該当件数: <c:out value="${tasks.size()}"/> 件
            </div>
            
            <table class="task-table">
                <thead>
                    <tr>
                        <th style="width: 25%;">タイトル</th>
                        <th style="width: 40%;">説明</th>
                        <th style="width: 15%;">ステータス</th>
                        <th style="width: 20%;">操作</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="task" items="${tasks}">
                        <tr>
                            <td><strong><c:out value="${task.title}"/></strong></td>
                            <td><c:out value="${task.description}"/></td>
                            <td>
                                <c:choose>
                                    <c:when test="${task.status == 'pending'}">
                                        <span class="badge badge-status-pending">未着手</span>
                                    </c:when>
                                    <c:when test="${task.status == 'in_progress'}">
                                        <span class="badge badge-status-in_progress">着手中</span>
                                    </c:when>
                                    <c:when test="${task.status == 'completed'}">
                                        <span class="badge badge-status-completed">完了</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge badge-status-pending"><c:out value="${task.status}"/></span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td class="action-cell">
                                <a href="${pageContext.request.contextPath}/app/task/edit?id=${task.id}" class="btn btn-edit">編集</a>
                                
                                <form action="${pageContext.request.contextPath}/app/task/delete" method="POST" class="inline-form" 
                                      onsubmit="return confirmDelete('<c:out value="${task.title}"/>');">
                                    <input type="hidden" name="id" value="${task.id}">
                                    <button type="submit" class="btn btn-delete">削除</button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </c:when>
        
        <c:otherwise>
            <c:choose>
                <c:when test="${not empty keyword}">
                    <div class="empty-message">
                        キーワード「<strong><c:out value="${keyword}"/></strong>」に一致するタスクが見つかりませんでした。
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="empty-message">
                        登録されているタスクはまだありません。上のボタンから新しいタスクを追加してみましょう！
                    </div>
                </c:otherwise>
            </c:choose>
        </c:otherwise>
    </c:choose>
</div>

</body>
</html>