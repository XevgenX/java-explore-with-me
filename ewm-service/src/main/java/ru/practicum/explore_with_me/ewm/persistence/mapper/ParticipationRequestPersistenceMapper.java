package ru.practicum.explore_with_me.ewm.persistence.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.explore_with_me.ewm.domain.model.request.NewParticipationRequest;
import ru.practicum.explore_with_me.ewm.domain.model.request.ParticipationRequest;
import ru.practicum.explore_with_me.ewm.domain.model.request.RequestStatus;
import ru.practicum.explore_with_me.ewm.persistence.entity.EventEntity;
import ru.practicum.explore_with_me.ewm.persistence.entity.ParticipationRequestEntity;
import ru.practicum.explore_with_me.ewm.persistence.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class ParticipationRequestPersistenceMapper {
    private final EventPersistenceMapper eventMapper;
    private final UserPersistenceMapper userMapper;

    public ParticipationRequestEntity toEntity(NewParticipationRequest request) {
        if (request == null) {
            return null;
        }

        ParticipationRequestEntity entity = new ParticipationRequestEntity();
        entity.setEvent(eventMapper.toEntity(request.getEvent()));
        entity.setRequester(userMapper.toEntity(request.getRequester()));
        entity.setCreatedOn(request.getCreated() != null ? request.getCreated() : LocalDateTime.now());
        entity.setStatus(request.getStatus());
        return entity;
    }

    public ParticipationRequestEntity toEntityForUpdate(ParticipationRequest request) {
        if (request == null) {
            return null;
        }

        ParticipationRequestEntity entity = new ParticipationRequestEntity();
        entity.setId(request.getId());
        entity.setEvent(eventMapper.toEntity(request.getEvent()));
        entity.setRequester(userMapper.toEntity(request.getRequester()));
        entity.setCreatedOn(request.getCreated());
        entity.setStatus(request.getStatus());
        return entity;
    }

    public ParticipationRequest toDomain(ParticipationRequestEntity entity) {
        if (entity == null) {
            return null;
        }

        return new ParticipationRequest(
                entity.getId(),
                entity.getEvent() != null ? eventMapper.toDomain(entity.getEvent()) : null,
                entity.getRequester() != null ? userMapper.toDomain(entity.getRequester()) : null,
                entity.getStatus(),
                entity.getCreatedOn()
        );
    }

    public List<ParticipationRequest> toDomainList(List<ParticipationRequestEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    public List<ParticipationRequestEntity> toEntityList(List<ParticipationRequest> domains,
                                                         Map<Long, EventEntity> eventMap,
                                                         Map<Long, UserEntity> userMap) {
        if (domains == null) {
            return null;
        }

        return domains.stream()
                .map(request -> {
                    ParticipationRequestEntity entity = new ParticipationRequestEntity();
                    entity.setId(request.getId());
                    entity.setEvent(eventMap.get(request.getEvent()));
                    entity.setRequester(userMap.get(request.getRequester()));
                    entity.setCreatedOn(request.getCreated());
                    return entity;
                })
                .collect(Collectors.toList());
    }

    public void updateEntityFromDomain(ParticipationRequest request,
                                       ParticipationRequestEntity entity,
                                       EventEntity eventEntity,
                                       UserEntity userEntity) {
        if (request == null || entity == null) {
            return;
        }

        entity.setEvent(eventEntity);
        entity.setRequester(userEntity);
        entity.setCreatedOn(request.getCreated());
    }

    private RequestStatus mapRequestStatus(String status) {
        if (status == null) {
            return null;
        }

        try {
            return RequestStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new MappingException("Unknown status value: " + status);
        }
    }

    private String mapStatusToString(RequestStatus status) {
        return status != null ? status.name() : null;
    }

    // Вспомогательный класс для обработки ошибок маппинга
    public static class MappingException extends RuntimeException {
        public MappingException(String message) {
            super(message);
        }
    }
}
