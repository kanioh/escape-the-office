package com.example.escapetheoffice.roadmap;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.escapetheoffice.roadmap.dto.RoadmapEventCreateRequest;
import com.example.escapetheoffice.roadmap.dto.RoadmapEventResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/roadmap-events")
public class RoadmapEventController {

    private final RoadmapEventService roadmapEventService;

    public RoadmapEventController(RoadmapEventService roadmapEventService) {
        this.roadmapEventService = roadmapEventService;
    }

    // 新規作成が確定しているため 201。UPSERT の PUT と違い、作成と更新が分かれない
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoadmapEventResponse create(@Valid @RequestBody RoadmapEventCreateRequest request) {
        return roadmapEventService.create(request);
    }

    @GetMapping
    public List<RoadmapEventResponse> findAll() {
        return roadmapEventService.findAll();
    }
}
