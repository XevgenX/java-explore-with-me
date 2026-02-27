package ru.practicum.explore_with_me.ewm.persistence.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.explore_with_me.ewm.persistence.entity.CompilationEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompilationRepository extends CrudRepository<CompilationEntity, Long> {

    @Query("SELECT DISTINCT c FROM CompilationEntity c " +
            "LEFT JOIN FETCH c.events " +
            "WHERE (:pinned IS NULL OR c.pined = :pinned)")
    List<CompilationEntity> findAll(@Param("pinned") Boolean pinned,
                                    Pageable page);

    @Query("SELECT c FROM CompilationEntity c " +
            "LEFT JOIN FETCH c.events " +
            "WHERE c.id = :id")
    Optional<CompilationEntity> findByIdWithEvents(@Param("id") Long id);
}
