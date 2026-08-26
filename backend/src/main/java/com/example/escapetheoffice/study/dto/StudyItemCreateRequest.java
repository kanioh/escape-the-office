package com.example.escapetheoffice.study.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// id は DB が採番するため受け取らない。
// 100 は DB の VARCHAR(100) と同条件
public record StudyItemCreateRequest(

        @NotBlank @Size(max = 100) String name) {
}
