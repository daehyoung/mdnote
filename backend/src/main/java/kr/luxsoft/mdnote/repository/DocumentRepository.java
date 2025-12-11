package kr.luxsoft.mdnote.repository;

import kr.luxsoft.mdnote.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    
    // Consolidated Search with Permissions
    @Query("SELECT DISTINCT d FROM Document d LEFT JOIN d.tags t WHERE " +
           "(:categoryId IS NULL OR d.category.id = :categoryId) AND " +
           "(:status IS NULL OR d.status = :status) AND " +
           "(:query IS NULL OR LOWER(d.title) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%')) OR LOWER(t.name) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%'))) AND " +
           "(" +
           " :isAdmin = true OR " +
           " d.author.username = :username OR " +
           " (d.publicRead = true) OR " +
           " (d.groupRead = true AND d.author.department.id = :deptId)" +
           ")")
    Page<Document> findAllWithPermissions(
            @Param("categoryId") Long categoryId, 
            @Param("status") String status, 
            @Param("query") String query,
            @Param("username") String username,
            @Param("deptId") Long deptId,
            @Param("isAdmin") boolean isAdmin, // Kept for interface compatibility or remove? Let's keep signature to avoid break, but ignore logic if used elsewhere. actually usage is only inside service. 
            Pageable pageable);

    @Query("SELECT DISTINCT d FROM Document d LEFT JOIN d.tags t WHERE " +
           "(:categoryId IS NULL OR d.category.id = :categoryId) AND " +
           "(:status IS NULL OR d.status = :status) AND " +
           "(:query IS NULL OR LOWER(d.title) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%')) OR LOWER(t.name) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%')))")
    Page<Document> findAllAdmin(
            @Param("categoryId") Long categoryId, 
            @Param("status") String status, 
            @Param("query") String query,
            Pageable pageable);

    @Query("SELECT DISTINCT d FROM Document d LEFT JOIN d.tags t WHERE " +
           "LOWER(d.title) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%')) OR " +
           "LOWER(t.name) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%'))")
    List<Document> searchDocuments(@Param("query") String query); // Legacy or Admin usage? We should deprecate/update this too.

    // Let's replace the strict 'findAll' and 'findByStatus' logic in Service with the consolidated query above.
    // Keeping this for reference or specialized usage if needed, but safe to omit if Service uses consolidated.
    // We will update Service to use findAllWithPermissions.
}
