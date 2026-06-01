package repository;

import java.sql.Connection;
import java.sql.SQLException;

import database.ConnectionFactory;

/**
 * すべてのRepositoryクラスの基底となる抽象クラス
 */
public abstract class BaseRepository {

    /**
     * ConnectionFactory経由でデータベース接続を取得する共通メソッド
     * @return Connection オブジェクト
     * @throws SQLException 接続失敗時にスロー
     */
    protected Connection getConnection() throws SQLException {
        return ConnectionFactory.getConnection();
    }

    /**
     * SQLExceptionが発生した際の共通ハンドリングメソッド
     * @param e 発生した例外
     * @param methodName メソッド名
     */
    protected void handleSQLException(SQLException e, String methodName) {
        System.err.println("[DATABASE ERROR] Exception occurred in method: " + methodName);
        System.err.println("SQLState: " + e.getSQLState());
        System.err.println("ErrorCode: " + e.getErrorCode());
        System.err.println("Message: " + e.getMessage());
        e.printStackTrace();
    }
}