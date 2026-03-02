package ru.practicum.explore_with_me.ewm.persistence.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.explore_with_me.ewm.persistence.entity.UserEntity;

import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, Long> {
    @Query("SELECT u FROM UserEntity u " +
            "WHERE (:ids IS NULL OR u.id IN :ids)")
    List<UserEntity> findAll(@Param("ids") List<Long> ids, Pageable page);
}
