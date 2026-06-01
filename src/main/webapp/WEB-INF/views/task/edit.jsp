<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>タスク編集 - TodoApp</title>
    <style>
        body { font-family: 'Helvetica Neue', Arial, sans-serif; background-color: #f8f9fa; color: #333; margin: 0; padding: 20px; }
        .container { max-width: 600px; margin: 0 auto; background: #fff; padding: 30px; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
        h1 { margin-top: 0; margin-bottom: 20px; font-size: 22px; color: #495057; border-bottom: 2px solid #e9ecef; padding-bottom: 10px; }
        
        .form-group { margin-bottom: 20px; }
        label { display: block; font-weight: bold; margin-bottom: 8px; color: #495057; }
        .required { color: #dc3545; margin-left: 4px; }
        
        input[type="text"], textarea, select { 
            width: 100%; padding: 10px; font-size: 14px; border: 1px solid #ced4da; border-radius: 4px; box-sizing: border-box; transition: border-color 0.2s;
            background-color: #fff;
        }
        input[type="text"]:focus, textarea:focus, select:focus { border-color: #80bdff; outline: none; }
        
        /* エラー表示スタイリング */
        .input-error { border-color: #dc3545 !important; background-color: #fff8f8; }
        .error-message { color: #dc3545; font-size: 13px; margin-top: 6px; font-weight: bold; }
        
        .btn-group { display: flex; justify-content: flex-end; gap: 10px; margin-top: 30px; }
        .btn { display: inline-block; padding: 10px 20px; font-size: 14px; font-weight: bold; text-decoration: none; border-radius: 4px; cursor: pointer; border: none; transition: background-color 0.2s; }
        .btn-primary { background-color: #28a745; color: #fff; } /* 編集は緑色のボタンに */
        .btn-primary:hover { background-color: #218838; }
        .btn-secondary { background-color: #6c757d; color: #fff; }
        .btn-secondary:hover { background-color: #5a6268; }
    </style>
</head>
<body>

<div class="container">
    <h1>タスク編集</h1>

    <form action="${pageContext.request.contextPath}/app/task/edit?id=${task.id}" method="POST">
        
        <div class="form-group">
            <label for="title">タイトル<span class="required">（必須）</span></label>
            <input type="text" id="title" name="title" 
                   class="${not empty errors.title ? 'input-error' : ''}" 
                   value="<c:out value='${task.title}'/>" 
                   placeholder="例：買い物に行く">
            <c:if test="${not empty errors.title}">
                <div class="error-message"><c:out value="${errors.title}"/></div>
            </c:if>
        </div>

        <div class="form-group">
            <label for="status">ステータス</label>
            <select id="status" name="status" class="${not empty errors.status ? 'input-error' : ''}">
                <option value="NOT_STARTED" ${task.status == 'NOT_STARTED' ? 'selected' : ''}>未着手</option>
                <option value="IN_PROGRESS" ${task.status == 'IN_PROGRESS' ? 'selected' : ''}>着手中</option>
                <option value="COMPLETED" ${task.status == 'COMPLETED' ? 'selected' : ''}>完了</option>
            </select>
            <c:if test="${not empty errors.status}">
                <div class="error-message"><c:out value="${errors.status}"/></div>
            </c:if>
        </div>

        <div class="form-group">
            <label for="description">説明<span class="text-muted" style="font-size:12px; font-weight:normal; color:#6c757d;">（任意）</span></label>
            <textarea id="description" name="description" rows="5" 
                      class="${not empty errors.description ? 'input-error' : ''}" 
                      placeholder="詳細なメモを入力してください"><c:out value="${task.description}"/></textarea>
            <c:if test="${not empty errors.description}">
                <div class="error-message"><c:out value="${errors.description}"/></div>
            </c:if>
        </div>

        <div class="btn-group">
            <a href="${pageContext.request.contextPath}/app/task/list" class="btn btn-secondary">キャンセル</a>
            <button type="submit" class="btn btn-primary">変更を保存する</button>
        </div>
    </form>
</div>

</body>
</html>