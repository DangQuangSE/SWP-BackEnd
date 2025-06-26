package com.S_Health.GenderHealthCare.api;

import com.S_Health.GenderHealthCare.dto.TagDTO;
import com.S_Health.GenderHealthCare.dto.request.tag.TagRequest;
import com.S_Health.GenderHealthCare.service.tag.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@SecurityRequirement(name = "api")
public class TagAPI {
    @Autowired
    private TagService tagService;

    @PostMapping
    @Operation(summary = "Tạo mới tag", description = "Tạo mới tag cho blog")
    public ResponseEntity<TagDTO> createTag(@Valid @RequestBody TagRequest request) {
        return ResponseEntity.ok(tagService.createTag(request));
    }

    @GetMapping
    @Operation(summary = "Lấy tất cả tag", description = "Lấy danh sách tất cả các tag")
    public ResponseEntity<List<TagDTO>> getAllTags() {
        return ResponseEntity.ok(tagService.getAllTags());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy tag theo id", description = "Lấy thông tin tag theo id")
    public ResponseEntity<TagDTO> getTagById(@PathVariable Long id) {
        return ResponseEntity.ok(tagService.getTagById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật tag", description = "Cập nhật thông tin tag")
    public ResponseEntity<TagDTO> updateTag(
            @PathVariable Long id,
            @Valid @RequestBody TagRequest request) {
        return ResponseEntity.ok(tagService.updateTag(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa tag", description = "Xóa tag theo id")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}
