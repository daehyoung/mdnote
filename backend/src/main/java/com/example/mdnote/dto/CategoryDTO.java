package com.example.mdnote.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class CategoryDTO {
    private Long id;
    private String name;
    private List<CategoryDTO> children = new ArrayList<>();

    public CategoryDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
