<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%> 
<!DOCTYPE html> 
<html lang="ja"> 
<head>
    <meta charset="UTF-8"> 
    <meta name="viewport" content="width=device-width, initial-scale=1.0"> 
    <title>TodoApp - 基盤動作確認</title>
    <style>
        /* 簡易スタイリング（見やすいレイアウトと中央配置） */
        body {
            font-family: Arial, sans-serif; 
            background-color: #f4f6f9;
            color: #333;
            margin: 0;
            padding: 0;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
        }
        .container {
            width: 100%;
            max-width: 600px; 
            background: #ffffff;
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        }
        h1 {
            color: #007bff;
            border-bottom: 2px solid #007bff;
            padding-bottom: 10px;
            margin-top: 0;
        }
        .info-box {
            background-color: #e9ecef; 
            padding: 15px;
            border-radius: 5px;
            margin-bottom: 20px;
            font-size: 1.1em;
            font-weight: bold;
        }
        .details {
            border-collapse: collapse;
            width: 100%;
            margin-bottom: 25px;
        }
        .details th, .details td {
            border: 1px solid #dee2e6;
            padding: 10px;
            text-align: left;
        }
        .details th {
            background-color: #f8f9fa;
            width: 35%;
        }
        .btn-list {
            display: flex;
            flex-direction: column;
            gap: 10px;
        }
        .btn {
            display: inline-block;
            background-color: #6c757d; 
            color: white;
            padding: 10px 15px;
            text-decoration: none;
            border-radius: 4px;
            text-align: center;
            transition: background 0.2s;
        }
        .btn:hover {
            background-color: #5a6268;
        }
        .btn-primary {
            background-color: #007bff;
        }
        .btn-primary:hover {
            background-color: #0069d9;
        }
        /* ★ タスク一覧ボタン用の緑色スタイルを追加 */
        .btn-success {
            background-color: #28a745;
        }
        .btn-success:hover {
            background-color: #218838;
        }
    </style>
</head>
<body>

<div class="container">
    <h1>TodoApp 基盤動作確認</h1> 
    <div class="info-box">
        ${message} 
    </div>
    
    <table class="details">
        <tr>
            <th>サーバー時刻</th>
            <td>${currentTime}</td> 
        </tr>
        <tr>
            <th>アクセスパス (pathInfo)</th>
            <td>${pathInfo}</td> 
        </tr>
        <tr>
            <th>リモートIPアドレス</th>
            <td>${remoteAddr}</td> 
        </tr>
        <tr>
            <th>ブラウザ情報 (User-Agent)</th>
            <td><small>${userAgent}</small></td> 
        </tr>
    </table>
    
    <h3>動作確認用テストリンク</h3>
    <div class="btn-list">
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/app/hello">① 通常アクセス (/app/hello)</a> 
        <a class="btn" href="${pageContext.request.contextPath}/app/">② デフォルト動作テスト (/app/)</a> 
        <a class="btn" href="${pageContext.request.contextPath}/app/hello?name=田中">③ パラメータ付きテスト (?name=田中)</a> 
        <a class="btn" style="background-color: #dc3545;" href="${pageContext.request.contextPath}/app/unknown_test_path">④ 404エラーテスト</a> 
        
        <a class="btn btn-success" href="${pageContext.request.contextPath}/app/task/list">📋 タスク一覧画面へ (/app/task/list)</a>
        <a href="${pageContext.request.contextPath}/app/dashboard" class="btn" 
		   style="background: #2ecc71; color: #fff; padding: 10px 20px; text-decoration: none; border-radius: 4px; font-weight: bold; margin-right: 10px; display: inline-block;">
		   📊 ダッシュボード（統計画面）
</a>
    </div>
</div>

</body>
</html>