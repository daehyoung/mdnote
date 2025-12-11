package kr.luxsoft.mdnote.service;

import kr.luxsoft.mdnote.dto.DepartmentDTO;
import kr.luxsoft.mdnote.model.Department;
import kr.luxsoft.mdnote.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    public List<DepartmentDTO> getDepartmentTree() {
        List<Department> allDepts = departmentRepository.findAll();
        Map<Long, DepartmentDTO> dtoMap = new HashMap<>();
        List<DepartmentDTO> roots = new ArrayList<>();

        // Create DTOs
        for (Department dept : allDepts) {
            Long parentId = dept.getParent() != null ? dept.getParent().getId() : null;
            dtoMap.put(dept.getId(), new DepartmentDTO(dept.getId(), dept.getName(), parentId));
        }

        // Build Tree
        for (Department dept : allDepts) {
            DepartmentDTO dto = dtoMap.get(dept.getId());
            if (dept.getParent() == null) {
                roots.add(dto);
            } else {
                DepartmentDTO parentDTO = dtoMap.get(dept.getParent().getId());
                if (parentDTO != null) {
                    parentDTO.getChildren().add(dto);
                }
            }
        }
        return roots;
    }

    public Department createDepartment(String name, Long parentId) {
        Department dept = new Department();
        dept.setName(name);
        if (parentId != null) {
            Department parent = departmentRepository.findById(parentId).orElse(null);
            dept.setParent(parent);
        }
        return departmentRepository.save(dept);
    }
    
    public Department updateDepartment(Long id, String name) {
        Department dept = departmentRepository.findById(id).orElseThrow();
        dept.setName(name);
        return departmentRepository.save(dept);
    }

    public void deleteDepartment(Long id) {
        departmentRepository.deleteById(id);
    }
}
