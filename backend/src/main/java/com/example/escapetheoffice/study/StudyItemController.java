package com.example.escapetheoffice.study;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.escapetheoffice.study.dto.StudyItemCreateRequest;
import com.example.escapetheoffice.study.dto.StudyItemResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/study-items")
public class StudyItemController {

    private final StudyItemService studyItemService;

    public StudyItemController(StudyItemService studyItemService) {
        this.studyItemService = studyItemService;
    }

    // 新規作成が確定しているため 201
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudyItemResponse create(@Valid @RequestBody StudyItemCreateRequest request) {
        return studyItemService.create(request);
    }

    @GetMapping
    public List<StudyItemResponse> findAll() {
        return studyItemService.findAll();
    }
}
