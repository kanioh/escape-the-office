package com.example.escapetheoffice.study;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.escapetheoffice.study.dto.StudyProgressResponse;

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
}
