<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>ページが見つかりません - TaskManager</title>
    <style>
        body { font-family: 'Helvetica Neue', Arial, sans-serif; background-color: #f8f9fa; color: #333; text-align: center; padding: 50px; }
        .error-container { background: #fff; max-width: 500px; margin: 60px auto; padding: 40px; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
        h1 { color: #f0ad4e; font-size: 28px; margin-bottom: 20px; }
        .error-message { font-size: 16px; color: #666; line-height: 1.6; margin-bottom: 30px; }
        .btn { display: inline-block; background-color: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 4px; font-weight: bold; }
        .btn:hover { background-color: #0056b3; }
    </style>
</head>
<body>
    <div class="error-container">
        <h1>ページが見つかりません (404)</h1>
        <p class="error-message">
            お探しのページは、移動または削除されたか、URLが間違っている可能性があります。
        </p>
        <a href="${pageContext.request.contextPath}/app/hello" class="btn">メイン画面に戻る</a>
    </div>
</body>
</html>