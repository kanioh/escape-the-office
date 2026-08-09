package com.example.escapetheoffice.common.exception;

/**
 * 要求されたデータが存在しない場合の例外。
 * GlobalExceptionHandler が 404 Not Found に変換する。
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
