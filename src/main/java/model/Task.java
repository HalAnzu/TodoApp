package model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * tasksテーブルに対応するDTOクラス
 */
public class Task implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int userId;
    private String title;
    private String description;
    private String status;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // 引数なしコンストラクタ
    public Task() {}

    // 全フィールドコンストラクタ
    public Task(int id, int userId, String title, String description, String status, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // getter / setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    // ユーティリティメソッド
    @Override
    public String toString() {
        return "Task{id=" + id + ", userId=" + userId + ", title='" + title + "', status='" + status + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id && userId == task.userId && Objects.equals(title, task.title) && Objects.equals(status, task.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, title, status);
    }
}