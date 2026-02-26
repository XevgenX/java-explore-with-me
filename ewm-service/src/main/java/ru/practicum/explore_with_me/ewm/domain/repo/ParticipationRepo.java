package ru.practicum.explore_with_me.ewm.domain.repo;

import ru.practicum.explore_with_me.ewm.domain.model.event.Event;
import ru.practicum.explore_with_me.ewm.domain.model.request.NewParticipationRequest;
import ru.practicum.explore_with_me.ewm.domain.model.request.ParticipationRequest;
import ru.practicum.explore_with_me.ewm.domain.model.user.User;

import java.util.List;

public interface ParticipationRepo {
    List<ParticipationRequest> findByUser(User user);

    List<ParticipationRequest> findByEvent(Event event);

    ParticipationRequest create(NewParticipationRequest request);

    ParticipationRequest update(ParticipationRequest request);

    ParticipationRequest findById(Long requestId);
}
