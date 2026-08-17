package com.example.escapetheoffice.common.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.escapetheoffice.roadmap.dto.RoadmapEventCreateRequest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * Bean Validation は Spring から独立した仕様のため、
 * コンテキストを起動せず検証エンジンだけを組み立てて確かめる。
 */
class DateRangeValidatorTest {

    // 日付の検証だけを見たいので、title は常に正しい値にして他の違反を混ぜない
    private static final String TITLE = "Spring Boot学習";
    private static final LocalDate START_DATE = LocalDate.of(2026, 10, 1);

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    @DisplayName("終了日が開始日より後なら違反なし")
    void noViolationWhenEndDateIsAfterStartDate() {
        RoadmapEventCreateRequest request = new RoadmapEventCreateRequest(
                TITLE, START_DATE, LocalDate.of(2026, 12, 31));

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    @DisplayName("終了日と開始日が同じ日なら違反なし")
    void noViolationWhenEndDateEqualsStartDate() {
        // 1日だけの予定。isAfter で判定すると弾かれてしまう境界
        RoadmapEventCreateRequest request =
                new RoadmapEventCreateRequest(TITLE, START_DATE, START_DATE);

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    @DisplayName("終了日が開始日より前なら endDate の違反になる")
    void violationOnEndDateWhenEndDateIsBeforeStartDate() {
        RoadmapEventCreateRequest request = new RoadmapEventCreateRequest(
                TITLE, START_DATE, START_DATE.minusDays(1));

        Set<ConstraintViolation<RoadmapEventCreateRequest>> violations =
                validator.validate(request);

        assertThat(violations).hasSize(1);
        ConstraintViolation<RoadmapEventCreateRequest> violation = violations.iterator().next();
        // addPropertyNode("endDate") の結果。ここが空だと項目ごとのエラーとして返せない
        assertThat(violation.getPropertyPath().toString()).isEqualTo("endDate");
        assertThat(violation.getMessage()).isEqualTo("終了日は開始日以降にしてください");
    }

    @Test
    @DisplayName("終了日が未定なら違反なし")
    void noViolationWhenEndDateIsNull() {
        RoadmapEventCreateRequest request =
                new RoadmapEventCreateRequest(TITLE, START_DATE, null);

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    @DisplayName("開始日が未入力なら @NotNull の違反だけを返す")
    void onlyNotNullViolationWhenStartDateIsNull() {
        RoadmapEventCreateRequest request = new RoadmapEventCreateRequest(
                TITLE, null, LocalDate.of(2026, 12, 31));

        Set<ConstraintViolation<RoadmapEventCreateRequest>> violations =
                validator.validate(request);

        // 日付比較側でも弾くと、開始日未入力の人に終了日のエラーまで出て2件になる
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString())
                .isEqualTo("startDate");
    }
}
