<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>新規タスク登録 - TodoApp</title>
    <style>
        body { font-family: 'Helvetica Neue', Arial, sans-serif; background-color: #f8f9fa; color: #333; margin: 0; padding: 20px; }
        .container { max-width: 600px; margin: 0 auto; background: #fff; padding: 30px; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
        h1 { margin-top: 0; margin-bottom: 20px; font-size: 22px; color: #495057; border-bottom: 2px solid #e9ecef; padding-bottom: 10px; }
        
        .form-group { margin-bottom: 20px; }
        label { display: block; font-weight: bold; margin-bottom: 8px; color: #495057; }
        .required { color: #dc3545; margin-left: 4px; }
        
        input[type="text"], textarea, select { 
            width: 100%; padding: 10px; font-size: 14px; border: 1px solid #ced4da; border-radius: 4px; box-sizing: border-box; transition: border-color 0.2s; background-color: #fff;
        }
        input[type="text"]:focus, textarea:focus, select:focus { border-color: #80bdff; outline: none; }
        
        /* ★追加：カテゴリ用ハイブリッドUIのコンテナ＆テキスト入力欄スタイル */
        .category-select-wrapper { display: flex; flex-direction: column; gap: 10px; }
        .new-category-input-group { display: none; margin-top: 5px; animation: fadeIn 0.3s ease; }
        @keyframes fadeIn { from { opacity: 0; transform: translateY(-5px); } to { opacity: 1; transform: translateY(0); } }

        /* エラー表示スタイリング */
        .input-error { border-color: #dc3545 !important; background-color: #fff8f8; }
        .error-message { color: #dc3545; font-size: 13px; margin-top: 4px; font-weight: bold; display: block; }
        
        .btn-group { display: flex; justify-content: flex-end; gap: 10px; margin-top: 30px; }
        .btn { display: inline-block; padding: 10px 20px; font-size: 14px; font-weight: bold; text-decoration: none; border-radius: 4px; cursor: pointer; border: none; transition: background-color 0.2s; }
        .btn-primary { background-color: #007bff; color: #fff; }
        .btn-primary:hover { background-color: #0056b3; }
        .btn-secondary { background-color: #6c757d; color: #fff; }
        .btn-secondary:hover { background-color: #5a6268; }
    </style>
</head>
<body>

<div class="container">
    <h1>新規タスク登録</h1>

    <form action="${pageContext.request.contextPath}/app/task/new" method="POST">
        
        <div class="form-group">
            <label for="title">タイトル<span class="required">（必須）</span></label>
            <input type="text" id="title" name="title" 
                   class="${not empty fieldErrors.title ? 'input-error' : ''}" 
                   value="<c:out value='${title}'/>" 
                   placeholder="例：買い物に行く" autofocus>
            <c:forEach var="err" items="${fieldErrors.title}">
                <div class="error-message">⚠️ <c:out value="${err}"/></div>
            </c:forEach>
        </div>

        <div class="form-group">
            <label for="categorySelect">カテゴリ<span class="text-muted" style="font-size:12px; font-weight:normal; color:#6c757d;">（任意：50文字以内）</span></label>
            <div class="category-select-wrapper">
                
                <c:set var="isExistingCategory" value="false" />
                <c:if var="hasCategoryValue" test="${not empty category}">
                    <c:forEach var="stat" items="${categoryStats}">
                        <c:if test="${stat.key == category}"><c:set var="isExistingCategory" value="true" /></c:if>
                    </c:forEach>
                </c:if>

                <select id="categorySelect" name="category" class="${not empty fieldErrors.category ? 'input-error' : ''}">
                    <option value="" <c:if test="${empty category}">selected</c:if>>-- 未分類 --</option>
                    
                    <c:forEach var="stat" items="${categoryStats}">
                        <c:if test="${stat.key != '未分類'}">
                            <option value="<c:out value='${stat.key}'/>" <c:if test="${category == stat.key}">selected</c:if>>
                                <c:out value="${stat.key}"/>
                            </option>
                        </c:if>
                    </c:forEach>
                    
                    <option value="__NEW_CATEGORY__" <c:if test="${hasCategoryValue && !isExistingCategory}">selected</c:if>>（＋新しく入力する）</option>
                </select>

                <div id="newCategoryGroup" class="new-category-input-group">
                    <input type="text" id="newCategoryInput" 
                           class="${not empty fieldErrors.category ? 'input-error' : ''}"
                           placeholder="新しいカテゴリ名を入力してください" 
                           value="<c:out value='${!isExistingCategory ? category : \"\"}'/>">
                </div>
            </div>
            
            <c:forEach var="err" items="${fieldErrors.category}">
                <div class="error-message">⚠️ <c:out value="${err}"/></div>
            </c:forEach>
        </div>

        <div class="form-group">
            <label for="description">説明<span class="text-muted" style="font-size:12px; font-weight:normal; color:#6c757d;">（任意）</span></label>
            <textarea id="description" name="description" rows="5" 
                      class="${not empty fieldErrors.description ? 'input-error' : ''}" 
                      placeholder="例：牛乳と卵、パンを駅前のスーパーで買う"><c:out value="${description}"/></textarea>
            <c:forEach var="err" items="${fieldErrors.description}">
                <div class="error-message">⚠️ <c:out value="${err}"/></div>
            </c:forEach>
        </div>

        <div class="form-group">
            <label for="status">ステータス<span class="required">（必須）</span></label>
            <select id="status" name="status" class="${not empty fieldErrors.status ? 'input-error' : ''}">
                <option value="pending" <c:if test="${empty status || status == 'pending'}">selected</c:if>>未着手</option>
                <option value="in_progress" <c:if test="${status == 'in_progress'}">selected</c:if>>着手中</option>
                <option value="completed" <c:if test="${status == 'completed'}">selected</c:if>>完了</option>
            </select>
            <c:forEach var="err" items="${fieldErrors.status}">
                <div class="error-message">⚠️ <c:out value="${err}"/></div>
            </c:forEach>
        </div>

        <div class="form-group">
            <label for="priority">優先度<span class="required">（必須）</span></label>
            <select id="priority" name="priority" class="${not empty fieldErrors.priority ? 'input-error' : ''}">
                <option value="low" <c:if test="${status != null && priority == 'low'}">selected</c:if>>低</option>
                <option value="medium" <c:if test="${empty priority || priority == 'medium'}">selected</c:if>>中</option>
                <option value="high" <c:if test="${priority == 'high'}">selected</c:if>>高</option>
            </select>
            <c:forEach var="err" items="${fieldErrors.priority}">
                <div class="error-message">⚠️ <c:out value="${err}"/></div>
            </c:forEach>
        </div>

        <div class="btn-group">
            <a href="${pageContext.request.contextPath}/app/task/list" class="btn btn-secondary">キャンセル</a>
            <button type="submit" class="btn btn-primary">登録する</button>
        </div>
    </form>
</div>

<script>
document.addEventListener("DOMContentLoaded", function() {
    const categorySelect = document.getElementById("categorySelect");
    const newCategoryGroup = document.getElementById("newCategoryGroup");
    const newCategoryInput = document.getElementById("newCategoryInput");

    function toggleCategoryMode() {
        if (categorySelect.value === "__NEW_CATEGORY__") {
            // 「新しく入力する」が選ばれたらテキストボックスを表示
            newCategoryGroup.style.display = "block";
            // サーバーへテキストボックスの値を送信するため、name属性をこちらに付与
            newCategoryInput.name = "category";
            // 代わりにセレクトボックスのnameを一時的に外す（多重送信の防止）
            categorySelect.removeAttribute("name");
        } else {
            // 既存カテゴリ選択、または未分類の場合はテキストボックスを隠す
            newCategoryGroup.style.display = "none";
            // セレクトボックスの値を送信するため、name属性を戻す
            categorySelect.name = "category";
            newCategoryInput.removeAttribute("name");
        }
    }

    // イベントリスナーの登録（変更時に毎回切り替える）
    categorySelect.addEventListener("change", toggleCategoryMode);

    // 初期化処理：エラーによる画面戻り時の状態を正しく再現する
    toggleCategoryMode();
});
</script>

</body>
</html>