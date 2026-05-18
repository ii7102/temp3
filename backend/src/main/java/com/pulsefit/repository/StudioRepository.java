package com.pulsefit.repository;

import com.pulsefit.domain.StudioEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudioRepository extends JpaRepository<StudioEntity, Long> {
  Optional<StudioEntity> findBySlug(String slug);
  List<StudioEntity> findByApprovedTrueAndFeaturedTrueOrderByRatingDesc();
}
