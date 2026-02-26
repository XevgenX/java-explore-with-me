package ru.practicum.explore_with_me.ewm.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.explore_with_me.common.domain.exception.ForbiddenException;
import ru.practicum.explore_with_me.common.domain.validator.ObjectValidatable;
import ru.practicum.explore_with_me.ewm.domain.model.event.Event;
import ru.practicum.explore_with_me.ewm.domain.model.event.EventState;
import ru.practicum.explore_with_me.ewm.domain.model.request.NewParticipationRequest;
import ru.practicum.explore_with_me.ewm.domain.model.request.ParticipationRequest;
import ru.practicum.explore_with_me.ewm.domain.model.request.RequestStatus;
import ru.practicum.explore_with_me.ewm.domain.model.user.User;
import ru.practicum.explore_with_me.ewm.domain.repo.ParticipationRepo;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ParticipationService implements ObjectValidatable  {
    private final ParticipationRepo repo;

    public List<ParticipationRequest> findByUser(User user) {
        validate(user, "user не должен быть null");
        return repo.findByUser(user);
    }

    public List<ParticipationRequest> findByEvent(Event event) {
        validate(event, "event не должен быть null");
        return repo.findByEvent(event);
    }

    public ParticipationRequest create(NewParticipationRequest request) {
        validate(request, "request не должен быть null");
        if (request.getRequester().equals(request.getEvent().getInitiator())) {
            throw new ForbiddenException("Нельзя записаться на свое же событие");
        }
        if (!request.getEvent().getState().equals(EventState.PUBLISHED)) {
            throw new ForbiddenException("Можно записаться только на опубликованное событие");
        }
        if (request.getEvent().getParticipantLimit() > 0 && request.getEvent().getParticipantLimit() <= findByEvent(request.getEvent()).size()) {
            throw new ForbiddenException("На этой событие уже не осталось свободных мест");
        }
        if (request.getEvent().getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
        }
        return repo.create(request);
    }

    public ParticipationRequest update(ParticipationRequest request) {
        validate(request, "request не должен быть null");
        return repo.update(request);
    }

    public ParticipationRequest changeStatus(User participant, Long requestId, RequestStatus status) {
        ParticipationRequest request = findById(requestId, participant);
        if (request.getStatus().equals(RequestStatus.CONFIRMED)) {
            throw new ForbiddenException("Запрос уже принят");
        }
        request.setStatus(status);
        validate(request, "request не должен быть null");
        return repo.update(request);
    }

    public ParticipationRequest changeStatus(Long requestId, RequestStatus status) {
        validate(requestId, "request не должен быть null");
        validate(status, "status не должен быть null");
        ParticipationRequest request = findById(requestId);
        validate(request, "request не должен быть null");
        if (request.getStatus().equals(RequestStatus.CONFIRMED)) {
            throw new ForbiddenException("Запрос уже принят");
        }
        request.setStatus(status);
        return repo.update(request);
    }

    public ParticipationRequest findById(Long requestId, User requester) {
        ParticipationRequest request = repo.findById(requestId);
        if (!request.getRequester().equals(requester)) {
            throw new ForbiddenException("Найденный запрос не принадлежит этому пользователю");
        }
        return request;
    }

    public ParticipationRequest findById(Long requestId) {
        return repo.findById(requestId);
    }

    public void validateThatUserInitiateEvent(User initiator, Event event) {
        if (!event.getInitiator().equals(initiator)) {
            throw new ForbiddenException("Событие не инициированно пользователем");
        }
    }

    public void validateThatRequestForNeededEvent(ParticipationRequest request, Event event) {
        if (!request.getEvent().equals(event)) {
            throw new ForbiddenException("Запрос не соответствует событию");
        }
    }
}
