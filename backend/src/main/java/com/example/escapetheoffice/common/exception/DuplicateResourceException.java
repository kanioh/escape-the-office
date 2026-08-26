package com.example.escapetheoffice.common.exception;

/**
 * 登録しようとしたデータが既に存在する場合の例外。
 * GlobalExceptionHandler が 409 Conflict に変換する。
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
