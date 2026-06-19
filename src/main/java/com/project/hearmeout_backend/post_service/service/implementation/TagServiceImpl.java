package com.project.hearmeout_backend.post_service.service.implementation;

import com.project.hearmeout_backend.post_service.dto.request.TagCreationRequestDTO;
import com.project.hearmeout_backend.post_service.dto.response.TagResponseDTO;
import com.project.hearmeout_backend.post_service.mapper.TagMapper;
import com.project.hearmeout_backend.post_service.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagServiceImpl {

    private final TagRepository tagRepo;

    @Transactional(readOnly = true)
    public List<TagResponseDTO> getAllTags(int pageNum) {
        Pageable pageable = PageRequest.of(
                Math.max(pageNum, 0), 10
        );

        return tagRepo.findAllTagsDTO(pageable);
    }

    @Transactional
    public void createNewTag(TagCreationRequestDTO tag) {
        tagRepo.save(TagMapper.toTagEntity(tag));
    }

    // method to update info of a tag and remove tag
}
