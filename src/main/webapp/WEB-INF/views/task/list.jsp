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
        
        /* ★追加：カテゴリ統計チップのコンテナとスタイル */
        .category-stats-container { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 20px; }
        .category-chip { display: inline-flex; align-items: center; padding: 6px 12px; border-radius: 16px; font-size: 13px; font-weight: bold; text-decoration: none; background-color: #e9ecef; color: #495057; transition: all 0.2s; border: 1px solid #dee2e6; }
        .category-chip:hover { background-color: #dee2e6; color: #212529; }
        .category-chip.active { background-color: #28a745; color: #fff; border-color: #28a745; }
        .category-chip .badge-count { margin-left: 6px; background-color: rgba(0,0,0,0.1); padding: 2px 6px; border-radius: 10px; font-size: 11px; }
        .category-chip.active .badge-count { background-color: rgba(255,255,255,0.2); }

        /* 検索・ソート操作バー */
        .search-sort-bar { display: flex; justify-content: space-between; align-items: center; background-color: #f1f3f5; padding: 12px 20px; border-radius: 6px; margin-bottom: 20px; gap: 15px; }
        .search-form { display: flex; align-items: center; gap: 15px; flex-grow: 1; }
        .search-input { padding: 8px 12px; font-size: 14px; border: 1px solid #ced4da; border-radius: 4px; width: 240px; box-sizing: border-box; }
        .search-input:focus { border-color: #80bdff; outline: none; }
        
        /* お気に入りフィルターのスタイル */
        .favorite-filter-label { font-size: 14px; color: #495057; font-weight: bold; cursor: pointer; display: flex; align-items: center; gap: 5px; white-space: nowrap; }
        .favorite-filter-label input { width: 16px; height: 16px; cursor: pointer; }

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

        .task-table { width: 100%; border-collapse: collapse; margin-top: 5px; margin-bottom: 25px; }
        .task-table th, .task-table td { padding: 12px; text-align: left; border-bottom: 1px solid #dee2e6; }
        .task-table th { background-color: #f8f9fa; color: #495057; font-weight: bold; }
        
        /* お気に入り星ボタンのスタイル */
        .title-cell { display: flex; align-items: center; gap: 8px; }
        .fav-btn { background: none; border: none; font-size: 18px; cursor: pointer; padding: 0; margin: 0; line-height: 1; transition: transform 0.1s ease; outline: none; }
        .fav-btn:active { transform: scale(1.3); }
        .fav-active { color: #ffc107; }
        .fav-inactive { color: #ccc; }
        .fav-inactive:hover { color: #ffda6a; }
        
        /* タスクコピー機能 */
        .btn-copy { background-color: #17a2b8; color: #fff; padding: 5px 10px; font-size: 12px; border-radius: 4px; text-decoration: none; font-weight: bold; }
        .btn-copy:hover { background-color: #138496; color: #fff; }

        /* ステータスバッジ */
        .badge { display: inline-block; padding: 4px 8px; font-size: 12px; font-weight: bold; border-radius: 12px; text-align: center; }
        .badge-status-pending { background-color: #e9ecef; color: #495057; }
        .badge-status-in_progress { background-color: #cce5ff; color: #004085; }
        .badge-status-completed { background-color: #d4edda; color: #155724; text-decoration: line-through; }
        
        /* ★追加：一覧内でのカテゴリタグのスタイリング */
        .tag-category { background-color: #e2f0d9; color: #385723; border: 1px solid #c5e0b4; }
        .tag-uncategorized { background-color: #f2f2f2; color: #7f7f7f; border: 1px solid #d9d9d9; font-style: italic; }

        .empty-message { text-align: center; color: #868e96; padding: 40px; font-style: italic; font-size: 15px; background: #f8f9fa; border-radius: 6px; border: 1px dashed #dee2e6; }
        .action-cell { display: flex; gap: 8px; align-items: center; }
        .inline-form { margin: 0; padding: 0; display: inline; }

        /* ページングのスタイリング */
        .pagination-container { display: flex; justify-content: center; align-items: center; margin-top: 10px; }
        .pagination { display: flex; list-style: none; padding: 0; margin: 0; gap: 5px; }
        .page-item { display: inline; }
        .page-link { display: block; padding: 8px 14px; text-decoration: none; color: #007bff; background-color: #fff; border: 1px solid #dee2e6; border-radius: 4px; font-weight: bold; font-size: 14px; transition: all 0.2s; }
        .page-link:hover { background-color: #e9ecef; border-color: #dee2e6; }
        .page-item.active .page-link { background-color: #007bff; border-color: #007bff; color: #fff; cursor: default; }
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
                <a href="${pageContext.request.contextPath}/app/dashboard" class="btn btn-dashboard" 
       				style="background: #2ecc71; color: #fff; padding: 6px 12px; text-decoration: none; border-radius: 4px; font-size: 14px; font-weight: bold; display: inline-flex; align-items: center; gap: 4px;">
       				📊 統計
    			</a>
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

    <div class="category-stats-container">
        <a href="${pageContext.request.contextPath}/app/task/list?keyword=<c:out value='${keyword}'/>&sort=<c:out value='${sort}'/>&favoriteOnly=${favoriteOnly}" 
           class="category-chip ${empty selectedCategory ? 'active' : ''}">
            すべて
        </a>
        
        <c:forEach var="stat" items="${categoryStats}">
            <c:set var="isUnclassified" value="${stat.key == '未分類'}" />
            <c:set var="paramValue" value="${isUnclassified ? '_UNCLASSIFIED_' : stat.key}" />
            
            <a href="${pageContext.request.contextPath}/app/task/list?keyword=<c:out value='${keyword}'/>&sort=<c:out value='${sort}'/>&favoriteOnly=${favoriteOnly}&category=<c:out value='${paramValue}'/>" 
               class="category-chip ${selectedCategory == paramValue ? 'active' : ''}">
                <c:out value="${stat.key}"/>
                <span class="badge-count"><c:out value="${stat.value}"/></span>
            </a>
        </c:forEach>
    </div>

    <div class="search-sort-bar">
        <form action="${pageContext.request.contextPath}/app/task/list" method="GET" class="search-form" id="searchForm">
            <input type="hidden" name="sort" value="<c:out value='${sort}'/>">
            <input type="hidden" name="category" value="<c:out value='${selectedCategory}'/>">
            
            <input type="text" name="keyword" class="search-input" placeholder="タスク名で検索..." value="<c:out value='${keyword}'/>">
            
            <label class="favorite-filter-label">
                <input type="checkbox" name="favoriteOnly" value="true" onchange="document.getElementById('searchForm').submit();" ${favoriteOnly ? 'checked' : ''}>
                ★ お気に入りのみ表示
            </label>

            <button type="submit" class="btn btn-primary">検索</button>
            
            <c:if test="${not empty keyword || favoriteOnly || not empty selectedCategory}">
                <a href="${pageContext.request.contextPath}/app/task/list" class="btn btn-secondary">クリア</a>
            </c:if>
        </form>
        
        <div class="sort-links">
            作成日時:
            <a href="${pageContext.request.contextPath}/app/task/list?keyword=<c:out value='${keyword}'/>&sort=DESC&favoriteOnly=${favoriteOnly}&category=<c:out value='${selectedCategory}'/>" 
               class="${sort == 'DESC' ? 'sort-active' : ''}">新しい順</a>
            <a href="${pageContext.request.contextPath}/app/task/list?keyword=<c:out value='${keyword}'/>&sort=ASC&favoriteOnly=${favoriteOnly}&category=<c:out value='${selectedCategory}'/>" 
               class="${sort == 'ASC' ? 'sort-active' : ''}">古い順</a>
        </div>
    </div>

    <c:choose>
        <c:when test="${not empty tasks}">
            <div class="result-count">
                該当件数: <c:out value="${totalTasks}"/> 件 （<c:out value="${currentPage}"/> / <c:out value="${totalPages}"/> ページ）
            </div>
            
            <table class="task-table">
                <thead>
                    <tr>
                        <th style="width: 25%;">タイトル</th>
                        <th style="width: 15%;">カテゴリ</th>
                        <th style="width: 25%;">説明</th>
                        <th style="width: 15%;">ステータス</th>
                        <th style="width: 20%;">操作</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="task" items="${tasks}">
                        <tr>
                            <td>
                                <div class="title-cell">
                                    <button type="button" 
                                            class="fav-btn ${task.favorite ? 'fav-active' : 'fav-inactive'}" 
                                            data-task-id="${task.id}" 
                                            data-favorite="${task.favorite}"
                                            title="${task.favorite ? 'お気に入り解除' : 'お気に入り登録'}">
                                        <c:choose>
                                            <c:when test="${task.favorite}">★</c:when>
                                            <c:otherwise>☆</c:otherwise>
                                        </c:choose>
                                    </button>
                                    <strong><c:out value="${task.title}"/></strong>
                                </div>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${not empty task.category}">
                                        <span class="badge tag-category"><c:out value="${task.category}"/></span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge tag-uncategorized">未分類</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
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
                                <a href="${pageContext.request.contextPath}/app/task/copy?id=${task.id}" 
                                   class="btn btn-copy"
                                   onclick="return confirm('「<c:out value="${task.title}"/>」を複製しますか？');">複製</a>
                            
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

            <div class="pagination-container">
                <ul class="pagination">
                    
                    <c:if test="${currentPage > 1}">
                        <li class="page-item">
                            <a class="page-link" href="${pageContext.request.contextPath}/app/task/list?keyword=<c:out value='${keyword}'/>&sort=<c:out value='${sort}'/>&favoriteOnly=${favoriteOnly}&category=<c:out value='${selectedCategory}'/>&page=${currentPage - 1}">前へ</a>
                        </li>
                    </c:if>

                    <c:forEach var="i" begin="1" end="${totalPages}">
                        <li class="page-item ${currentPage == i ? 'active' : ''}">
                            <a class="page-link" href="${pageContext.request.contextPath}/app/task/list?keyword=<c:out value='${keyword}'/>&sort=<c:out value='${sort}'/>&favoriteOnly=${favoriteOnly}&category=<c:out value='${selectedCategory}'/>&page=${i}">${i}</a>
                        </li>
                    </c:forEach>

                    <c:if test="${currentPage < totalPages}">
                        <li class="page-item">
                            <a class="page-link" href="${pageContext.request.contextPath}/app/task/list?keyword=<c:out value='${keyword}'/>&sort=<c:out value='${sort}'/>&favoriteOnly=${favoriteOnly}&category=<c:out value='${selectedCategory}'/>&page=${currentPage + 1}">次へ</a>
                        </li>
                    </c:if>
                    
                </ul>
            </div>
        </c:when>
        
        <c:otherwise>
            <c:choose>
                <c:when test="${favoriteOnly && not empty keyword}">
                    <div class="empty-message">
                        キーワード「<strong><c:out value="${keyword}"/></strong>」に一致するお気に入りタスクが見つかりませんでした。
                    </div>
                </c:when>
                <c:when test="${favoriteOnly}">
                    <div class="empty-message">
                        お気に入り登録されているタスクがありません。
                    </div>
                </c:when>
                <c:when test="${not empty keyword}">
                    <div class="empty-message">
                        キーワード「<strong><c:out value="${keyword}"/></strong>」に一致するタスクが見つかりませんでした。
                    </div>
                </c:when>
                <c:when test="${not empty selectedCategory}">
                    <div class="empty-message">
                        選択されたカテゴリに該当するタスクはありません。
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

<script>
document.addEventListener("DOMContentLoaded", function() {
    const favButtons = document.querySelectorAll(".fav-btn");
    
    favButtons.forEach(button => {
        button.addEventListener("click", function() {
            const taskId = this.getAttribute("data-task-id");
            const currentFav = this.getAttribute("data-favorite") === "true";
            const nextFav = !currentFav;
            
            const contextPath = "${pageContext.request.contextPath}";
            const url = contextPath + "/app/task/toggleFavorite?taskId=" + taskId + "&isFavorite=" + nextFav;
            
            button.disabled = true;
            
            fetch(url, { method: "POST" })
                .then(response => {
                    if (response.ok) {
                        return response.text();
                    } else if (response.status === 403) {
                        throw new Error("このタスクを操作する権限がありません。");
                    } else {
                        throw new Error("サーバー通信エラーが発生しました。(Status: " + response.status + ")");
                    }
                })
                .then(text => {
                    if (text.trim() === "SUCCESS") {
                        this.setAttribute("data-favorite", nextFav ? "true" : "false");
                        
                        if (nextFav) {
                            this.innerHTML = "★";
                            this.classList.remove("fav-inactive");
                            this.classList.add("fav-active");
                            this.title = "お気に入り解除";
                        } else {
                            this.innerHTML = "☆";
                            this.classList.remove("fav-active");
                            this.classList.add("fav-inactive");
                            this.title = "お気に入り登録";
                        }
                    } else {
                        alert("処理に失敗しました。詳細: " + text);
                    }
                })
                .catch(error => {
                    console.error("Error:", error);
                    alert("エラーが発生しました。\n" + error.message);
                })
                .finally(() => {
                    button.disabled = false;
                });
        });
    });
});
</script>

</body>
</html>