package kr.luxsoft.mdnote.controller;

import kr.luxsoft.mdnote.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    @Autowired
    private TagRepository tagRepository;

    @GetMapping
    public List<kr.luxsoft.mdnote.dto.TagDTO> getAllTags(@org.springframework.web.bind.annotation.RequestParam(required = false) String status) {
        return tagRepository.findAllTagsWithCount(status);
    }
}
