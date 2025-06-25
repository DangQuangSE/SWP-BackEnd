package com.S_Health.GenderHealthCare.service.tag;

import com.S_Health.GenderHealthCare.dto.TagDTO;
import com.S_Health.GenderHealthCare.dto.request.tag.TagRequest;
import com.S_Health.GenderHealthCare.entity.Tag;
import com.S_Health.GenderHealthCare.exception.exceptions.BadRequestException;
import com.S_Health.GenderHealthCare.repository.TagRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagService {
    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private ModelMapper modelMapper;

    public TagDTO createTag(TagRequest request) {
        if (tagRepository.existsByName(request.getName())) {
            throw new BadRequestException("Tag đã tồn tại");
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
                .orElseThrow(() -> new BadRequestException("Không tìm thấy tag"));
        return modelMapper.map(tag, TagDTO.class);
    }

    public TagDTO updateTag(Long id, TagRequest request) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy tag"));

        // Check if new name conflicts with existing tag
        if (!tag.getName().equals(request.getName()) && 
            tagRepository.existsByName(request.getName())) {
            throw new BadRequestException("Tag đã tồn tại");
        }

        tag.setName(request.getName().trim().toLowerCase());
        tag.setDescription(request.getDescription());

        return modelMapper.map(tagRepository.save(tag), TagDTO.class);
    }

    public void deleteTag(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy tag"));
        tag.setIsActive(false);
        tagRepository.save(tag);
    }

    public Tag getOrCreateTag(String name) {
        String normalizedName = name.trim().toLowerCase();
        return tagRepository.findByNameAndIsActiveTrue(normalizedName)
                .orElseGet(() -> {
                    Tag newTag = new Tag();
                    newTag.setName(normalizedName);
                    return tagRepository.save(newTag);
                });
    }
}
