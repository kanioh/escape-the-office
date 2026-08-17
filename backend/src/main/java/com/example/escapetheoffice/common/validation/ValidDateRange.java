package com.example.escapetheoffice.common.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * 終了日が開始日以降であることを検証する。
 * 2項目を突き合わせるため、項目ではなく DTO 全体に付ける。
 */
// TYPE = クラスや record の宣言に付けられる
@Target(ElementType.TYPE)
// 既定ではコンパイル後に消えてしまうため、実行時まで残すよう指定する
@Retention(RetentionPolicy.RUNTIME)
// 判定を担当するクラスを紐付ける
@Constraint(validatedBy = DateRangeValidator.class)
public @interface ValidDateRange {

    String message() default "終了日は開始日以降にしてください";

    // groups と payload は使わないが、Bean Validation の仕様で宣言が必須
    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
