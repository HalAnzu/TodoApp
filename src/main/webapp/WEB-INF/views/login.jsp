<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>TaskManager - ログイン</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f4f6f9;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            margin: 0;
        }
        .login-container {
            width: 100%;
            max-width: 400px;
            background: #ffffff;
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 4px 10px rgba(0,0,0,0.1);
        }
        h2 {
            text-align: center;
            color: #007bff;
            margin-bottom: 25px;
        }
        .form-group {
            margin-bottom: 20px;
        }
        .form-group label {
            display: block;
            margin-bottom: 5px;
            font-weight: bold;
            color: #555;
        }
        .form-group input {
            width: 100%;
            padding: 10px;
            box-sizing: border-box;
            border: 1px solid #ccc;
            border-radius: 4px;
            font-size: 1em;
        }
        .error-message {
            background-color: #f8d7da;
            color: #721c24;
            padding: 12px;
            border-radius: 4px;
            margin-bottom: 20px;
            font-size: 0.9em;
            border: 1px solid #f5c6cb;
        }
        .btn-submit {
            width: 100%;
            background-color: #007bff;
            color: white;
            border: none;
            padding: 12px;
            font-size: 1em;
            font-weight: bold;
            border-radius: 4px;
            cursor: pointer;
            transition: background 0.2s;
        }
        .btn-submit:hover {
            background-color: #0056b3;
        }
        .demo-hint {
            margin-top: 25px;
            background-color: #e9ecef;
            padding: 10px;
            border-radius: 4px;
            font-size: 0.85em;
            color: #666;
        }
    </style>
</head>
<body>

<div class="login-container">
    <h2>TaskManager ログイン</h2>
    
    <c:if test="${not empty errorMessage}">
        <div class="error-message">
            ${errorMessage}
        </div>
    </c:if>
    
    <form action="${pageContext.request.contextPath}/app/login" method="POST">
        <div class="form-group">
            <label Lothar for="username">ユーザー名</label>
            <input type="text" id="username" name="username" value="${enteredUsername}" placeholder="ユーザー名を入力" required>
        </div>
        <div class="form-group">
            <label for="password">パスワード</label>
            <input type="password" id="password" name="password" placeholder="パスワードを入力" required>
        </div>
        <button type="submit" class="btn-submit">ログイン</button>
    </form>
    
    <div class="demo-hint">
        <strong>【テスト用アカウント情報】</strong><br>
        ユーザー名: <code>admin_user</code><br>
        パスワード: <code>secure_pass1</code>
    </div>
</div>

</body>
</html>