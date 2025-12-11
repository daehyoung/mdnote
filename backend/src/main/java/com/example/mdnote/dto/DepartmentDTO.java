package com.example.mdnote.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class DepartmentDTO {
    private Long id;
    private String name;
    private Long parentId;
    private List<DepartmentDTO> children = new ArrayList<>();

    public DepartmentDTO(Long id, String name, Long parentId) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
    }
}
