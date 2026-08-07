package com.example.escapetheoffice.study;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.escapetheoffice.study.dto.StudyItemResponse;

@Service
public class StudyItemService {

    private final StudyItemRepository studyItemRepository;

    public StudyItemService(StudyItemRepository studyItemRepository) {
        this.studyItemRepository = studyItemRepository;
    }

    @Transactional(readOnly = true)
    public List<StudyItemResponse> findAll() {
        // SQL は ORDER BY がない限り順序を保証しないため、表示順を明示する
        return studyItemRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(StudyItemResponse::from)
                .toList();
    }
}
