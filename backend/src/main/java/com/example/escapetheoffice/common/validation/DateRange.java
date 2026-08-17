package com.example.escapetheoffice.common.validation;

import java.time.LocalDate;

/**
 * 開始日と終了日を持つリクエストであることを表す。
 * 検証クラスを特定の DTO 型に縛らないよう、間にこの約束を挟む。
 *
 * <p>メソッド名を record の項目名と揃えてあるため、
 * startDate / endDate を持つ record は implements するだけで条件を満たす。
 */
public interface DateRange {

    LocalDate startDate();

    /** 終了未定を表すため null を返すことがある */
    LocalDate endDate();
}
