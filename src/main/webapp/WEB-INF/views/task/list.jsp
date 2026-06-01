<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>タスク一覧 - TodoApp</title>
    <style>
        body { font-family: 'Helvetica Neue', Arial, sans-serif; background-color: #f8f9fa; color: #333; margin: 0; padding: 20px; }
        .container { max-width: 900px; margin: 0 auto; background: #fff; padding: 30px; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
        .header { display: flex; justify-content: space-between; align-items: center; border-bottom: 2px solid #e9ecef; padding-bottom: 15px; margin-bottom: 20px; }
        h1 { margin: 0; font-size: 24px; color: #495057; }
        .user-info { font-size: 14px; color: #6c757d; }
        .btn { display: inline-block; padding: 8px 16px; font-size: 14px; font-weight: bold; text-decoration: none; border-radius: 4px; transition: background-color 0.2s; }
        .btn-primary { background-color: #007bff; color: #fff; }
        .btn-primary:hover { background-color: #0056b3; }
        .btn-danger { background-color: #dc3545; color: #fff; }
        .btn-danger:hover { background-color: #bd2130; }
        
        /* フラッシュメッセージ */
        .alert-success { background-color: #d4edda; color: #155724; border: 1px solid #c3e6cb; padding: 12px; border-radius: 4px; margin-bottom: 20px; }
        
        /* テーブルスタイリング */
        .task-table { width: 100%; border-collapse: collapse; margin-top: 10px; }
        .task-table th, .task-table td { padding: 12px; text-align: left; border-bottom: 1px solid #dee2e6; }
        .task-table th { background-color: #f1f3f5; color: #495057; font-weight: bold; }
        .task-table tr:hover { background-color: #f8f9fa; }
        
        /* ステータスバッジ */
        .badge { display: inline-block; padding: 4px 8px; font-size: 12px; font-weight: bold; border-radius: 12px; text-align: center; }
        .badge-not-started { background-color: #6c757d; color: white; }
        .badge-in-progress { background-color: #007bff; color: white; }
        .badge-completed { background-color: #28a745; color: white; }
        
        .empty-message { text-align: center; padding: 40px; color: #6c757d; font-size: 16px; }
    </style>
</head>
<body>

<div class="container">
    <div class="header">
        <div>
            <h1>タスク一覧</h1>
            <span class="user-info">ログインユーザー: <strong><c:out value="${loginUser.username}"/></strong> さん</span>
        </div>
        <div>
            <a href="${pageContext.request.contextPath}/app/task/new" class="btn btn-primary">新規タスク追加</a>
            <a href="${pageContext.request.contextPath}/app/logout" class="btn btn-danger" style="margin-left: 8px;">ログアウト</a>
        </div>
    </div>

    <c:if test="${not empty successMessage}">
        <div class="alert-success">
            <c:out value="${successMessage}"/>
        </div>
    </c:if>

    <c:choose>
        <c:when test="${not empty tasks}">
            <table class="task-table">
                <thead>
                    <tr>
                        <th style="width: 25%;">タイトル</th>
                        <th style="width: 40%;">説明</th>
                        <th style="width: 15%;">ステータス</th>
                        <th style="width: 20%;">作成日時</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="task" items="${tasks}">
                        <tr>
                            <td><strong><c:out value="${task.title}"/></strong></td>
                            <td><c:out value="${task.description}"/></td>
                            <td>
                                <c:choose>
                                    <c:when test="${task.status == 'NOT_STARTED'}">
                                        <span class="badge badge-not-started">未着手</span>
                                    </c:when>
                                    <c:when test="${task.status == 'IN_PROGRESS'}">
                                        <span class="badge badge-in-progress">着手中</span>
                                    </c:when>
                                    <c:when test="${task.status == 'COMPLETED'}">
                                        <span class="badge badge-completed">完了</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge badge-not-started"><c:out value="${task.status}"/></span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <fmt:formatDate value="${task.createdAt}" pattern="yyyy/MM/dd HH:mm"/>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </c:when>
        <c:otherwise>
            <div class="empty-message">
                登録されているタスクはまだありません。上のボタンから最初のタスクを追加しましょう！
            </div>
        </c:otherwise>
    </c:choose>
</div>

</body>
</html>