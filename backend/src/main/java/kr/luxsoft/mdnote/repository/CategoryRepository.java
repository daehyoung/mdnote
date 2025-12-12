package kr.luxsoft.mdnote.repository;

import kr.luxsoft.mdnote.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByParentIsNull(); // All Roots (Legacy)
    List<Category> findByParentIsNullAndOwnerIsNull(); // System Roots
    List<Category> findByParentIsNullAndOwner(kr.luxsoft.mdnote.model.User owner); // User Roots
    
    List<Category> findByOwnerIsNull(); // All System Categories
    List<Category> findByOwner(kr.luxsoft.mdnote.model.User owner); // All User Categories
    
    List<Category> findAll();
}
