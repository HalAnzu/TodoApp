package model;

/**
 * ダッシュボード用の統計情報を保持するDTOクラス
 */
public class TaskStatistics {
    // 基本統計
    private int totalTasks;        // 総タスク数
    private int completedTasks;    // 完了タスク数
    private int pendingTasks;      // 未着手タスク数
    private int inProgressTasks;   // 着手中タスク数
    private double completionRate; // 完了率 (0.0 〜 100.0)
    private int todayCreatedTasks; // 今日作成されたタスク数

    // 優先度統計
    private int lowPriorityTasks;
    private int mediumPriorityTasks;
    private int highPriorityTasks;

    // ゲッターとセッター
    public int getTotalTasks() { return totalTasks; }
    public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }

    public int getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }

    public int getPendingTasks() { return pendingTasks; }
    public void setPendingTasks(int pendingTasks) { this.pendingTasks = pendingTasks; }

    public int getInProgressTasks() { return inProgressTasks; }
    public void setInProgressTasks(int inProgressTasks) { this.inProgressTasks = inProgressTasks; }

    public double getCompletionRate() { return completionRate; }
    public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }

    public int getTodayCreatedTasks() { return todayCreatedTasks; }
    public void setTodayCreatedTasks(int todayCreatedTasks) { this.todayCreatedTasks = todayCreatedTasks; }

    public int getLowPriorityTasks() { return lowPriorityTasks; }
    public void setLowPriorityTasks(int lowPriorityTasks) { this.lowPriorityTasks = lowPriorityTasks; }

    public int getMediumPriorityTasks() { return mediumPriorityTasks; }
    public void setMediumPriorityTasks(int mediumPriorityTasks) { this.mediumPriorityTasks = mediumPriorityTasks; }

    public int getHighPriorityTasks() { return highPriorityTasks; }
    public void setHighPriorityTasks(int highPriorityTasks) { this.highPriorityTasks = highPriorityTasks; }
}