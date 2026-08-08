package com.example.escapetheoffice.common.exception;

/**
 * 既に存在するデータを重複して登録しようとした場合の例外。
 * GlobalExceptionHandler が 409 Conflict に変換する。
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
