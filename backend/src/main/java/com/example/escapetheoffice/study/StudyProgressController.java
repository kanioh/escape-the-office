package com.example.escapetheoffice.study;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.escapetheoffice.study.dto.StudyProgressResponse;
import com.example.escapetheoffice.study.dto.StudyProgressUpdateRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/study-progress")
public class StudyProgressController {

    private final StudyProgressService studyProgressService;

    public StudyProgressController(StudyProgressService studyProgressService) {
        this.studyProgressService = studyProgressService;
    }

    @GetMapping
    public List<StudyProgressResponse> findAll() {
        return studyProgressService.findAll();
    }

    // 進捗の id ではなく学習項目の id で指定する。
    // 未登録の項目には進捗の id が存在せず、画面から指定しようがないため。
    // 新規作成でも更新でも呼び出し側から見た結果は同じなので、201 と 200 を区別しない
    @PutMapping("/{studyItemId}")
    public StudyProgressResponse update(
            @PathVariable Long studyItemId,
            @Valid @RequestBody StudyProgressUpdateRequest request) {
        return studyProgressService.update(studyItemId, request);
    }
}
