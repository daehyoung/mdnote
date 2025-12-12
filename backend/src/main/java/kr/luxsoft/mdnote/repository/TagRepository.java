package kr.luxsoft.mdnote.repository;

import kr.luxsoft.mdnote.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);
    
    @org.springframework.data.jpa.repository.Query("SELECT new kr.luxsoft.mdnote.dto.TagDTO(t.name, (SELECT COUNT(d) FROM Document d JOIN d.tags dt WHERE dt.id = t.id AND (:status IS NULL OR d.status = :status))) FROM Tag t ORDER BY t.name")
    java.util.List<kr.luxsoft.mdnote.dto.TagDTO> findAllTagsWithCount(@org.springframework.data.repository.query.Param("status") String status);
}
