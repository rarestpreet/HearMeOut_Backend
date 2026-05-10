package com.project.hearmeout_backend.service.implementation;

import com.project.hearmeout_backend.dto.request.tag_request.TagCreationRequestDTO;
import com.project.hearmeout_backend.dto.response.tag_response.TagResponseDTO;
import com.project.hearmeout_backend.mapper.TagMapper;
import com.project.hearmeout_backend.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagServiceImpl {

    private final TagRepository tagRepository;

    @Transactional(readOnly = true)
    public List<TagResponseDTO> getAllTags(int pageNum) {
        Pageable pageable = PageRequest.of(pageNum, 10);

        return tagRepository.findAllTagsDTO(pageable);
    }

    @Transactional
    public void createNewTag(TagCreationRequestDTO tag) {
        tagRepository.save(TagMapper.toTagEntity(tag));
    }

    // method to update info of a tag and remove tag
}
