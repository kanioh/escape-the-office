package com.example.escapetheoffice.study;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.escapetheoffice.common.exception.DuplicateResourceException;
import com.example.escapetheoffice.study.dto.StudyItemCreateRequest;
import com.example.escapetheoffice.study.dto.StudyItemResponse;

@Service
public class StudyItemService {

    private final StudyItemRepository studyItemRepository;

    public StudyItemService(StudyItemRepository studyItemRepository) {
        this.studyItemRepository = studyItemRepository;
    }

    /**
     * 学習項目を登録する。
     * 進捗は登録せず、項目だけを作る（GET /api/study-progress が未登録の項目も 0% で返すため、
     * 追加した項目はそのまま一覧に並ぶ）。
     */
    @Transactional
    public StudyItemResponse create(StudyItemCreateRequest request) {
        // " AWS" と "AWS" が別項目として登録されるのを防ぐ
        String name = request.name().trim();

        // 存在チェックと INSERT の間に別リクエストが割り込むと UNIQUE 制約違反になるが、
        // 単一ユーザーのため許容する
        if (studyItemRepository.existsByName(name)) {
            throw new DuplicateResourceException("学習項目「" + name + "」は既に登録されています");
        }

        StudyItem saved = studyItemRepository.save(new StudyItem(name));

        return StudyItemResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<StudyItemResponse> findAll() {
        // SQL は ORDER BY がない限り順序を保証しないため、表示順を明示する
        return studyItemRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(StudyItemResponse::from)
                .toList();
    }
}
