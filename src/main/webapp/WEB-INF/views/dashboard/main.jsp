<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <title>ダッシュボード - タスク統計</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .dashboard-container {
            max-width: 1000px;
            margin: 20px auto;
            padding: 0 20px;
        }
        .dashboard-actions {
            margin-bottom: 20px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        /* 概要カードグリッド */
        .summary-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, min-max(220px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }
        @media (max-width: 600px) {
            .summary-grid {
                grid-template-columns: 1fr; /* スマホ時は1列に */
            }
        }
        .card {
            background: #fff;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 2px 5px rgba(0,0,0,0.1);
            text-align: center;
        }
        .card h3 {
            margin: 0 0 10px 0;
            font-size: 14px;
            color: #666;
        }
        .card .value {
            font-size: 32px;
            font-weight: bold;
            color: #333;
        }
        .card .value.rate {
            color: #2ecc71;
        }
        /* グラフセクション */
        .chart-section {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
            margin-bottom: 30px;
        }
        @media (max-width: 768px) {
            .chart-section {
                grid-template-columns: 1fr; /* タブレット・スマホ時は縦並び */
            }
        }
        .chart-card {
            background: #fff;
            padding: 25px;
            border-radius: 8px;
            box-shadow: 0 2px 5px rgba(0,0,0,0.1);
        }
        .chart-card h2 {
            font-size: 18px;
            margin-bottom: 20px;
            border-left: 4px solid #3498db;
            padding-left: 10px;
            color: #333;
        }
        /* 純CSS 棒グラフ構造 */
        .status-item {
            margin-bottom: 18px;
        }
        .status-label-row {
            display: flex;
            justify-content: space-between;
            margin-bottom: 5px;
            font-size: 14px;
            color: #555;
        }
        .status-bar-container {
            background: #eceff1;
            border-radius: 6px;
            overflow: hidden;
            height: 16px;
            width: 100%;
        }
        .status-bar {
            height: 100%;
            border-radius: 6px;
            transition: width 0.6s ease-in-out;
            width: 0%; /* 初期値 */
        }
        /* ステータス・優先度別のカラーバリエーション */
        .bar-pending { background-color: #3498db; }    /* 青 */
        .bar-progress { background-color: #f1c40f; }   /* 黄 */
        .bar-completed { background-color: #2ecc71; }  /* 緑 */
        .bar-low { background-color: #95a5a6; }        /* 灰 */
        .bar-medium { background-color: #e67e22; }     /* 橙 */
        .bar-high { background-color: #e74c3c; }       /* 赤 */

        /* カテゴリ統計用 */
        .category-list {
            list-style: none;
            padding: 0;
            margin: 0;
        }
        .category-item {
            display: flex;
            justify-content: space-between;
            padding: 10px 0;
            border-bottom: 1px solid #eee;
            font-size: 14px;
        }
        .category-name {
            font-weight: 500;
            color: #333;
        }
        .category-count {
            background: #f0f2f5;
            padding: 2px 8px;
            border-radius: 12px;
            font-size: 12px;
            color: #666;
        }
    </style>
</head>
<body>

    <header style="background: #333; color: #fff; padding: 15px 20px;">
        <span style="font-size: 20px; font-weight: bold;">タスク管理システム</span>
        <span style="float: right;">ようこそ、<c:out value="${loginUser.username}"/> さん</span>
    </header>

    <div class="dashboard-container">
        
        <div class="dashboard-actions">
            <h1>ダッシュボード（簡易統計）</h1>
            <a href="${pageContext.request.contextPath}/app/task/list" class="btn" style="background: #34495e; color: #fff; padding: 8px 15px; text-decoration: none; border-radius: 4px;">タスク一覧に戻る</a>
        </div>

        <c:set var="total" value="${statistics.totalTasks}" />
        <c:set var="pendingRate" value="${total > 0 ? (statistics.pendingTasks * 100.0 / total) : 0}" />
        <c:set var="progressRate" value="${total > 0 ? (statistics.inProgressTasks * 100.0 / total) : 0}" />
        <c:set var="completedRate" value="${total > 0 ? (statistics.completedTasks * 100.0 / total) : 0}" />
        
        <c:set var="lowRate" value="${total > 0 ? (statistics.lowPriorityTasks * 100.0 / total) : 0}" />
        <c:set var="mediumRate" value="${total > 0 ? (statistics.mediumPriorityTasks * 100.0 / total) : 0}" />
        <c:set var="highRate" value="${total > 0 ? (statistics.highPriorityTasks * 100.0 / total) : 0}" />

        <div class="summary-grid">
            <div class="card">
                <h3>総タスク数</h3>
                <div class="value"><c:out value="${statistics.totalTasks}"/> <span style="font-size:16px;">件</span></div>
            </div>
            <div class="card">
                <h3>完了タスク数</h3>
                <div class="value"><c:out value="${statistics.completedTasks}"/> <span style="font-size:16px;">件</span></div>
            </div>
            <div class="card">
                <h3>タスク完了率</h3>
                <div class="value rate"><c:out value="${statistics.completionRate}"/> <span style="font-size:16px;">%</span></div>
            </div>
            <div class="card">
                <h3>今日作成されたタスク</h3>
                <div class="value" style="color: #e74c3c;"><c:out value="${statistics.todayCreatedTasks}"/> <span style="font-size:16px;">件</span></div>
            </div>
        </div>

        <div class="chart-section">
            
            <div class="chart-card">
                <h2>ステータス別進捗</h2>
                
                <div class="status-item">
                    <div class="status-label-row">
                        <span>未着手</span>
                        <span><c:out value="${statistics.pendingTasks}"/> 件 (<fmt:formatNumber value="${pendingRate}" maxFractionDigits="1"/>%)</span>
                    </div>
                    <div class="status-bar-container">
                        <div class="status-bar bar-pending" style="width: ${pendingRate}%"></div>
                    </div>
                </div>

                <div class="status-item">
                    <div class="status-label-row">
                        <span>着手中</span>
                        <span><c:out value="${statistics.inProgressTasks}"/> 件 (<fmt:formatNumber value="${progressRate}" maxFractionDigits="1"/>%)</span>
                    </div>
                    <div class="status-bar-container">
                        <div class="status-bar bar-progress" style="width: ${progressRate}%"></div>
                    </div>
                </div>

                <div class="status-item">
                    <div class="status-label-row">
                        <span>完了</span>
                        <span><c:out value="${statistics.completedTasks}"/> 件 (<fmt:formatNumber value="${completedRate}" maxFractionDigits="1"/>%)</span>
                    </div>
                    <div class="status-bar-container">
                        <div class="status-bar bar-completed" style="width: ${completedRate}%"></div>
                    </div>
                </div>
            </div>

            <div class="chart-card">
                <h2>優先度別内訳</h2>
                
                <div class="status-item">
                    <div class="status-label-row">
                        <span>低 (Low)</span>
                        <span><c:out value="${statistics.lowPriorityTasks}"/> 件 (<fmt:formatNumber value="${lowRate}" maxFractionDigits="1"/>%)</span>
                    </div>
                    <div class="status-bar-container">
                        <div class="status-bar bar-low" style="width: ${lowRate}%"></div>
                    </div>
                </div>

                <div class="status-item">
                    <div class="status-label-row">
                        <span>中 (Medium)</span>
                        <span><c:out value="${statistics.mediumPriorityTasks}"/> 件 (<fmt:formatNumber value="${mediumRate}" maxFractionDigits="1"/>%)</span>
                    </div>
                    <div class="status-bar-container">
                        <div class="status-bar bar-medium" style="width: ${mediumRate}%"></div>
                    </div>
                </div>

                <div class="status-item">
                    <div class="status-label-row">
                        <span>高 (High)</span>
                        <span><c:out value="${statistics.highPriorityTasks}"/> 件 (<fmt:formatNumber value="${highRate}" maxFractionDigits="1"/>%)</span>
                    </div>
                    <div class="status-bar-container">
                        <div class="status-bar bar-high" style="width: ${highRate}%"></div>
                    </div>
                </div>
            </div>
        </div>

        <div class="chart-card" style="margin-bottom: 50px;">
            <h2>カテゴリ別タスク数</h2>
            <c:choose>
                <c:when test="${not empty categoryStats}">
                    <ul class="category-list">
                        <c:forEach var="entry" items="${categoryStats}">
                            <li class="category-item">
                                <span class="category-name"><c:out value="${entry.key}"/></span>
                                <span class="category-count"><c:out value="${entry.value}"/> 件</span>
                            </li>
                        </c:forEach>
                    </ul>
                </c:when>
                <c:otherwise>
                    <p style="color: #777; font-size: 14px;">登録されているカテゴリはありません。</p>
                </c:otherwise>
            </c:choose>
        </div>

    </div>
</body>
</html>