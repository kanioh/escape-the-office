package com.example.escapetheoffice.study;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.escapetheoffice.study.dto.StudyItemResponse;

@RestController
@RequestMapping("/api/study-items")
public class StudyItemController {

    private final StudyItemService studyItemService;

    public StudyItemController(StudyItemService studyItemService) {
        this.studyItemService = studyItemService;
    }

    @GetMapping
    public List<StudyItemResponse> findAll() {
        return studyItemService.findAll();
    }
}
