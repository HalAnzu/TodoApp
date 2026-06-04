package test;

import java.util.List;

import model.Task;
import model.User;
import repository.TaskRepository;
import repository.UserRepository;

/**
 * Repository層のデータマッピングテストクラス
 */
public class RepositoryTest {

    public static void main(String[] args) {
        System.out.println("=== TaskManager Repository層 動作確認テスト開始 ===");

        // 1. UserRepository のテスト
        System.out.println("\n--- [TEST] UserRepository.findAll() ---");
        UserRepository userRepo = new UserRepository();
        List<User> userList = userRepo.findAll();
        
        System.out.println("取得件数: " + userList.size() + " 件");
        for (User u : userList) {
            // カプセル化されたDTOからgetterで安全に取り出せているか確認
            System.out.println("[DTO検証] " + u.toString());
        }

        // 2. UserRepository.findById のテスト
        System.out.println("\n--- [TEST] UserRepository.findById(1) ---");
        User singleUser = userRepo.findById(1);
        if (singleUser != null) {
            System.out.println("単一ユーザー取得成功: " + singleUser.getUsername() + " (" + singleUser.getEmail() + ")");
        } else {
            System.out.println("[FAILED] ユーザーが見つかりませんでした。");
        }

        // 3. TaskRepository のテスト
        System.out.println("\n--- [TEST] TaskRepository.findAll() ---");
        TaskRepository taskRepo = new TaskRepository();
        List<Task> taskList = taskRepo.findAll();
        
        System.out.println("取得件数: " + taskList.size() + " 件");
        for (Task t : taskList) {
            System.out.println("[DTO検証] タスク名: " + t.getTitle() + " | ステータス: " + t.getStatus());
        }

        System.out.println("\n=== TaskManager Repository層 動作確認テスト終了 ===");
    }
}