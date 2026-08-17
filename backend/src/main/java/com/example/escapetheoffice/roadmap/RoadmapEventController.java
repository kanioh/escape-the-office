package com.example.escapetheoffice.roadmap;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.escapetheoffice.roadmap.dto.RoadmapEventCreateRequest;
import com.example.escapetheoffice.roadmap.dto.RoadmapEventResponse;
import com.example.escapetheoffice.roadmap.dto.RoadmapEventUpdateRequest;

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

    // 更新後の内容を返す。画面が再取得せずに表示を更新できる
    @PutMapping("/{id}")
    public RoadmapEventResponse update(
            @PathVariable Long id,
            @Valid @RequestBody RoadmapEventUpdateRequest request) {
        return roadmapEventService.update(id, request);
    }

    // 削除は返す本文が無いため 204。void のままだと 200 になり、意図が伝わらない
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        roadmapEventService.delete(id);
    }
}
