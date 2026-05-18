package com.pulsefit.service;

import com.pulsefit.domain.ClassSessionEntity;
import com.pulsefit.domain.ClassTemplateEntity;
import com.pulsefit.domain.StudioEntity;
import com.pulsefit.exception.NotFoundException;
import com.pulsefit.repository.ClassSessionRepository;
import com.pulsefit.repository.ClassTemplateRepository;
import com.pulsefit.repository.StudioRepository;
import com.pulsefit.web.dto.ApiDtos.LandingResponse;
import com.pulsefit.web.dto.ApiDtos.SessionSummary;
import com.pulsefit.web.dto.ApiDtos.StudioDetailResponse;
import com.pulsefit.web.dto.ApiDtos.StudioSummary;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CatalogService {
  private final StudioRepository studios;
  private final ClassSessionRepository sessions;
  private final ClassTemplateRepository templates;

  @Transactional(readOnly = true)
  public LandingResponse landing() {
    return new LandingResponse(
        studios.findByApprovedTrueAndFeaturedTrueOrderByRatingDesc().stream().map(this::studioSummary).toList(),
        sessions.findAll().stream().limit(6).map(this::sessionSummary).toList());
  }

  @Transactional(readOnly = true)
  public List<SessionSummary> discover(String q) {
    String needle = q == null ? "" : q.toLowerCase(Locale.ROOT);
    return sessions.findAll().stream()
        .map(this::sessionSummary)
        .filter(session -> (session.title() + session.studioName() + session.discipline() + session.neighborhood()).toLowerCase(Locale.ROOT).contains(needle))
        .toList();
  }

  @Transactional(readOnly = true)
  public StudioDetailResponse studioDetail(String slug) {
    StudioEntity studio = studios.findBySlug(slug).orElseThrow(() -> new NotFoundException("Studio not found"));
    ClassTemplateEntity template = templates.findByStudioSlug(slug).stream().findFirst().orElseThrow(() -> new NotFoundException("Studio has no classes yet"));
    return new StudioDetailResponse(
        studio.getId(),
        studio.getSlug(),
        studio.getName(),
        studio.getNeighborhood(),
        studio.getDiscipline(),
        studio.getDescription(),
        studio.getImageUrl(),
        template.getInstructor().getName(),
        studio.getRating(),
        studio.getBasePrice(),
        sessions.findByTemplateStudioSlugOrderByStartsAtAsc(slug).stream().map(this::sessionSummary).toList());
  }

  private StudioSummary studioSummary(StudioEntity studio) {
    return new StudioSummary(studio.getId(), studio.getSlug(), studio.getName(), studio.getNeighborhood(), studio.getDiscipline(), studio.getImageUrl(), studio.getRating(), studio.getBasePrice(), studio.isFeatured());
  }

  private SessionSummary sessionSummary(ClassSessionEntity session) {
    return new SessionSummary(
        session.getId(),
        session.getTemplate().getStudio().getName(),
        session.getTemplate().getTitle(),
        session.getStartsAt(),
        session.getTemplate().getDiscipline(),
        session.getTemplate().getStudio().getNeighborhood(),
        session.getPrice(),
        session.getCapacity(),
        session.getBookedCount(),
        session.getStatus().name());
  }
}
