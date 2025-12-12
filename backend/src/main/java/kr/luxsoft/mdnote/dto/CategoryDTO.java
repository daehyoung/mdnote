package kr.luxsoft.mdnote.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class CategoryDTO {
    private Long id;
    private String name;
    private List<CategoryDTO> children = new ArrayList<>();
    private String type; // "SYSTEM" or "USER"

    public CategoryDTO(Long id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }
    
    // Legacy constructor
    public CategoryDTO(Long id, String name) {
        this(id, name, "SYSTEM");
    }
}
