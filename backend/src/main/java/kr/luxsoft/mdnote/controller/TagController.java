package kr.luxsoft.mdnote.controller;

import kr.luxsoft.mdnote.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@Tag(name = "Tags", description = "Document tagging and metadata APIs")
public class TagController {

    @Autowired
    private TagRepository tagRepository;

    @GetMapping
    @Operation(summary = "Get All Tags", description = "Fetches a list of all unique tags used across documents, with usage counts.")
    public List<kr.luxsoft.mdnote.dto.TagDTO> getAllTags(@org.springframework.web.bind.annotation.RequestParam(required = false) String status) {
        return tagRepository.findAllTagsWithCount(status);
    }
}
