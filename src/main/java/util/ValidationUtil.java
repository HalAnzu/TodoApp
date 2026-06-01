package util;

import java.util.ArrayList;
import java.util.List;

/**
 * 入力値の妥当性を検証するユーティリティクラス。
 * すべて static メソッドとして提供し、インスタンス化せずに利用します。
 */
public class ValidationUtil {

    /**
     * タイトルのバリデーションを行います。
     * @param title 入力されたタイトル
     * @return エラーメッセージのリスト（エラーがなければ空のリスト）
     */
    public static List<String> validateTitle(String title) {
        List<String> errors = new ArrayList<>();
        
        // 必須チェック（null または 空文字、スペースのみの場合もエラー）
        if (title == null || title.trim().isEmpty()) {
            errors.add("タイトルは必須です。");
            return errors; // 必須エラーの場合は文字数チェックをスキップして返す
        }
        
        // 最小文字数チェック（2文字未満）
        if (title.trim().length() < 2) {
            errors.add("タイトルは2文字以上で入力してください。");
        }
        
        // 最大文字数チェック（200文字超）
        if (title.length() > 200) {
            errors.add("タイトルは200文字以内で入力してください。");
        }
        
        return errors;
    }

    /**
     * 説明のバリデーションを行います。
     * @param description 入力された説明
     * @return エラーメッセージのリスト（エラーがなければ空のリスト）
     */
    public static List<String> validateDescription(String description) {
        List<String> errors = new ArrayList<>();
        
        // 文字数チェック（1000文字超）
        if (description != null && description.length() > 1000) {
            errors.add("説明は1000文字以内で入力してください。");
        }
        
        return errors;
    }

    /**
     * ステータス値の妥当性チェックを行います。
     * @param status 入力されたステータスコード
     * @return エラーメッセージのリスト（エラーがなければ空のリスト）
     */
    public static List<String> validateStatus(String status) {
        List<String> errors = new ArrayList<>();
        
        // 定義外の値が送られてきていないか正規表現でチェック
        if (status == null || !status.matches("pending|in_progress|completed")) {
            errors.add("無効なステータスです。");
        }
        
        return errors;
    }

    /**
     * 優先度値の妥当性チェックを行います。
     * @param priority 入力された優先度コード
     * @return エラーメッセージのリスト（エラーがなければ空のリスト）
     */
    public static List<String> validatePriority(String priority) {
        List<String> errors = new ArrayList<>();
        
        // 定義外の値が送られてきていないか正規表現でチェック
        if (priority == null || !priority.matches("low|medium|high")) {
            errors.add("無効な優先度です。");
        }
        
        return errors;
    }
}
