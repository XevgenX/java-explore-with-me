package ru.practicum.explore_with_me.ewm.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore_with_me.common.domain.exception.NotFoundException;
import ru.practicum.explore_with_me.ewm.persistence.mapper.ParticipationRequestPersistenceMapper;
import ru.practicum.explore_with_me.ewm.domain.model.event.Event;
import ru.practicum.explore_with_me.ewm.domain.model.request.NewParticipationRequest;
import ru.practicum.explore_with_me.ewm.domain.model.request.ParticipationRequest;
import ru.practicum.explore_with_me.ewm.domain.model.user.User;
import ru.practicum.explore_with_me.ewm.domain.repo.ParticipationRepo;
import ru.practicum.explore_with_me.ewm.persistence.entity.ParticipationRequestEntity;
import ru.practicum.explore_with_me.ewm.persistence.mapper.EventPersistenceMapper;
import ru.practicum.explore_with_me.ewm.persistence.mapper.UserPersistenceMapper;
import ru.practicum.explore_with_me.ewm.persistence.repository.ParticipationRequestRepository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class ParticipationRequestDao implements ParticipationRepo {
    private final ParticipationRequestRepository repository;
    private final ParticipationRequestPersistenceMapper participationRequestMapper;
    private final EventPersistenceMapper eventMapper;
    private final UserPersistenceMapper userMapper;

    @Transactional(readOnly = true)
    @Override
    public List<ParticipationRequest> findByUser(User user) {
        return participationRequestMapper.toDomainList(repository.findByRequester(userMapper.toEntity(user)));
    }

    @Transactional(readOnly = true)
    @Override
    public List<ParticipationRequest> findByEvent(Event event) {
        return participationRequestMapper.toDomainList(repository.findByEvent(eventMapper.toEntity(event)));
    }

    @Transactional
    @Override
    public ParticipationRequest create(NewParticipationRequest request) {
        ParticipationRequestEntity entity = participationRequestMapper.toEntity(request);
        return participationRequestMapper.toDomain(repository.save(entity));
    }

    @Transactional
    @Override
    public ParticipationRequest update(ParticipationRequest request) {
        return participationRequestMapper.toDomain(repository
                .save(participationRequestMapper.toEntityForUpdate(request)));
    }

    @Transactional
    @Override
    public ParticipationRequest findById(Long requestId) {
        return participationRequestMapper.toDomain(repository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден")));
    }
}
