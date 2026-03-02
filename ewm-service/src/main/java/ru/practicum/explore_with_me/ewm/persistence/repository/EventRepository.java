package ru.practicum.explore_with_me.ewm.persistence.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.explore_with_me.ewm.domain.model.event.EventState;
import ru.practicum.explore_with_me.ewm.persistence.entity.EventEntity;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends CrudRepository<EventEntity, Long> {
    List<EventEntity> findAll(Pageable pageable);

    @Query("SELECT e FROM EventEntity e WHERE " +
            "(:users IS NULL OR e.initiator.id IN :users) AND " +
            "(:states IS NULL OR e.state IN :states) AND " +
            "(:categories IS NULL OR e.category.id IN :categories) AND " +
            "(cast(:rangeStart as timestamp) IS NULL OR e.eventDate >= :rangeStart) AND " +
            "(cast(:rangeEnd as timestamp) IS NULL OR e.eventDate <= :rangeEnd)")
    List<EventEntity> findByFilter(@Param("users") List<Long> users,
                                    @Param("states") List<EventState> states,
                                    @Param("categories") List<Long> categories,
                                    @Param("rangeStart") LocalDateTime rangeStart,
                                    @Param("rangeEnd") LocalDateTime rangeEnd,
                                    Pageable pageable);

    @Query("""
        SELECT c FROM EventEntity  c
        ORDER BY c.id
        LIMIT :limit
    """)
    List<EventEntity> findAllWithLimit(Long limit);

    @Query("""
        SELECT c FROM EventEntity  c
        ORDER BY c.id
        OFFSET :from
    """)
    List<EventEntity> findAllFromLine(Long from);

    @Query("""
        SELECT c FROM EventEntity  c
        ORDER BY c.id
        LIMIT :limit OFFSET :from
    """)
    List<EventEntity> findAllFromLineWithLimit(Long from, Long limit);

    @Query("SELECT e FROM EventEntity e WHERE " +
            "(:text IS NULL OR (LOWER(e.annotation) LIKE LOWER(CONCAT('%', CAST(:text AS string), '%')) OR " +
            "LOWER(e.description) LIKE LOWER(CONCAT('%', CAST(:text AS string), '%')) OR " +
            "LOWER(e.title) LIKE LOWER(CONCAT('%', CAST(:text AS string), '%')))) AND " +
            "(:paid IS NULL OR e.paid = :paid) AND " +
            "(:categories IS NULL OR e.category.id IN :categories) AND " +
            "(cast(:rangeStart as timestamp) IS NULL OR e.eventDate >= :rangeStart) AND " +
            "(cast(:rangeEnd as timestamp) IS NULL OR e.eventDate <= :rangeEnd) AND " +
            "(:onlyAvailable = false OR e.participantLimit = 0 OR e.confirmedRequests < e.participantLimit)")
    List<EventEntity> findByFilter(@Param("text") String text,
                                   @Param("paid") Boolean paid,
                                   @Param("categories") List<Long> categories,
                                   @Param("rangeStart") LocalDateTime rangeStart,
                                   @Param("rangeEnd") LocalDateTime rangeEnd,
                                   @Param("onlyAvailable") Boolean onlyAvailable,
                                   Pageable page);
}
