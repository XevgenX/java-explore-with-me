package ru.practicum.explore_with_me.ewm.persistence.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.explore_with_me.ewm.persistence.entity.EventEntity;
import ru.practicum.explore_with_me.ewm.persistence.entity.ParticipationRequestEntity;
import ru.practicum.explore_with_me.ewm.persistence.entity.UserEntity;

import java.util.List;

@Repository
public interface ParticipationRequestRepository extends CrudRepository<ParticipationRequestEntity, Long> {
    List<ParticipationRequestEntity> findByRequester(UserEntity requester);

    List<ParticipationRequestEntity> findByEvent(EventEntity event);
}
