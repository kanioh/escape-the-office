package com.example.escapetheoffice.common.validation;

import java.time.LocalDate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 終了日が開始日以降であることを検証する。
 * DateRange を実装した DTO であれば型を問わず使える。
 */
public class DateRangeValidator implements ConstraintValidator<ValidDateRange, DateRange> {

    private String message;

    // 検証の前に1度だけ呼ばれる。アノテーションに書かれたメッセージを控えておく
    @Override
    public void initialize(ValidDateRange annotation) {
        this.message = annotation.message();
    }

    @Override
    public boolean isValid(DateRange value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        LocalDate start = value.startDate();
        LocalDate end = value.endDate();

        // start の未入力は @NotNull の担当。ここでも弾くとエラーが二重になる。
        // end の未入力は終了未定を表す正常な状態
        if (start == null || end == null) {
            return true;
        }

        // 1日だけの予定を弾かないよう、同じ日は許可する(isAfterは使わない)
        if (!end.isBefore(start)) {
            return true;
        }

        // 既定では項目に紐づかないエラーになり、getFieldErrors() で拾う
        // GlobalExceptionHandler の対象から外れてしまうため、endDate のエラーとして作り直す
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode("endDate")
                .addConstraintViolation();

        return false;
    }
}
