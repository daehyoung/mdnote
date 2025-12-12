package kr.luxsoft.mdnote.service;

import kr.luxsoft.mdnote.dto.CategoryDTO;
import kr.luxsoft.mdnote.model.Category;
import kr.luxsoft.mdnote.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map; 

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private kr.luxsoft.mdnote.repository.UserRepository userRepository;

    public Map<String, List<CategoryDTO>> getCategoryTree(String username) {
        // Fetch System Categories
        List<Category> systemCats = categoryRepository.findByOwnerIsNull();
        
        // Fetch User Categories (if logged in)
        List<Category> userCats = new ArrayList<>();
        if (username != null && !username.equals("ANONYMOUS_GUEST")) {
            kr.luxsoft.mdnote.model.User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                userCats = categoryRepository.findByOwner(user);
            }
        }

        List<CategoryDTO> systemRoots = buildTree(systemCats, "SYSTEM");
        List<CategoryDTO> userRoots = buildTree(userCats, "USER");
        
        Map<String, List<CategoryDTO>> result = new HashMap<>();
        result.put("system", systemRoots);
        result.put("user", userRoots);
        return result;
    }

    private List<CategoryDTO> buildTree(List<Category> categories, String type) {
        Map<Long, CategoryDTO> dtoMap = new HashMap<>();
        List<CategoryDTO> roots = new ArrayList<>();

        for (Category cat : categories) {
            dtoMap.put(cat.getId(), new CategoryDTO(cat.getId(), cat.getName(), type));
        }

        for (Category cat : categories) {
            CategoryDTO dto = dtoMap.get(cat.getId());
            if (cat.getParent() == null) {
                roots.add(dto);
            } else {
                // Parent must be in the same set for valid tree in this context
                // If parent is system and child is system, ok.
                // If parent is user and child is user, ok.
                // What if User category is child of System? Not supporting mixed hierarchy for now based on requirement "My Documents" root.
                CategoryDTO parentDTO = dtoMap.get(cat.getParent().getId());
                if (parentDTO != null) {
                    parentDTO.getChildren().add(dto);
                } else if (dto != null) { 
                    // Fallback: if parent not found in this set (e.g. cross reference), treat as root or orphan? 
                    // For "My Docs" vs "System", they are disjoint trees.
                    roots.add(dto); 
                }
            }
        }
        return roots;
    }

    public Category createCategory(String name, Long parentId, String username, String scope) {
        Category category = new Category();
        category.setName(name);
        
        kr.luxsoft.mdnote.model.User user = null;
        if (username != null) {
            user = userRepository.findByUsername(username).orElse(null);
        }

        if (parentId != null) {
            Category parent = categoryRepository.findById(parentId).orElseThrow(() -> new RuntimeException("Parent not found"));
            category.setParent(parent);
            // Inherit owner from parent
            category.setOwner(parent.getOwner());
        } else {
            // Root Category
            if ("USER".equalsIgnoreCase(scope) && user != null) {
                category.setOwner(user);
            } else {
                // SYSTEM scope
                if (user == null || !"ADMIN".equals(user.getRole())) {
                    // Only Admin can create System Roots? Or allow for now and refine later?
                    // Let's enforce owner=null for SYSTEM
                    category.setOwner(null);
                } else {
                    category.setOwner(null);
                }
            }
        }
        return categoryRepository.save(category);
    }
    
    public Category updateCategory(Long id, String name) {
        Category category = categoryRepository.findById(id).orElseThrow();
        category.setName(name);
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        // Handle constraint violation if has children? 
        // For now, let JPA/DB handle it or cascade.
        categoryRepository.deleteById(id);
    }
}
