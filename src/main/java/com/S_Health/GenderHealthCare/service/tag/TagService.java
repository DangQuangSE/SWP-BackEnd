package com.S_Health.GenderHealthCare.service.tag;

import com.S_Health.GenderHealthCare.dto.TagDTO;
import com.S_Health.GenderHealthCare.dto.request.tag.TagRequest;
import com.S_Health.GenderHealthCare.entity.Tag;
import com.S_Health.GenderHealthCare.exception.exceptions.AppException;
import com.S_Health.GenderHealthCare.repository.TagRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TagService {
    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private ModelMapper modelMapper;

    public TagDTO createTag(TagRequest request) {
        if (tagRepository.existsByName(request.getName())) {
            throw new AppException("Tag đã tồn tại");
        }

        Tag tag = new Tag();
        tag.setName(request.getName().trim().toLowerCase());
        tag.setDescription(request.getDescription());

        return modelMapper.map(tagRepository.save(tag), TagDTO.class);
    }

    public List<TagDTO> getAllTags() {
        return tagRepository.findAll().stream()
                .map(tag -> modelMapper.map(tag, TagDTO.class))
                .collect(Collectors.toList());
    }

    public TagDTO getTagById(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy tag"));
        return modelMapper.map(tag, TagDTO.class);
    }

    public TagDTO updateTag(Long id, TagRequest request) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy tag"));

        // Check if new name conflicts with existing tag
        if (!tag.getName().equals(request.getName()) && 
            tagRepository.existsByName(request.getName())) {
            throw new AppException("Tag đã tồn tại");
        }

        tag.setName(request.getName().trim().toLowerCase());
        tag.setDescription(request.getDescription());

        return modelMapper.map(tagRepository.save(tag), TagDTO.class);
    }

    public void deleteTag(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy tag"));
        tag.setIsActive(false);
        tagRepository.save(tag);
    }

    // Method mới: chỉ lấy tag đã tồn tại, không tạo mới
    public Optional<Tag> getExistingTag(String name) {
        String normalizedName = name.trim().toLowerCase();
        return tagRepository.findByNameAndIsActiveTrue(normalizedName);
    }

    // Method mới: lấy nhiều tag đã tồn tại
    public List<Tag> getExistingTags(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new ArrayList<>();
        }

        List<Tag> existingTags = new ArrayList<>();
        for (String tagName : tagNames) {
            String normalizedName = tagName.trim().toLowerCase();
            Optional<Tag> tag = tagRepository.findByNameAndIsActiveTrue(normalizedName);
            if (tag.isPresent()) {
                existingTags.add(tag.get());
            }
        }
        return existingTags;
    }

    // Method mới: kiểm tra danh sách tag có tồn tại không
    public List<String> validateTagNames(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> invalidTags = new ArrayList<>();
        for (String tagName : tagNames) {
            String normalizedName = tagName.trim().toLowerCase();
            if (!tagRepository.findByNameAndIsActiveTrue(normalizedName).isPresent()) {
                invalidTags.add(tagName);
            }
        }
        return invalidTags;
    }

}
