package com.pulsefit.repository;

import com.pulsefit.domain.ClassSessionEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassSessionRepository extends JpaRepository<ClassSessionEntity, Long> {
  List<ClassSessionEntity> findByTemplateStudioSlugOrderByStartsAtAsc(String slug);
  Optional<ClassSessionEntity> findByIdAndTemplateStudioSlug(Long id, String slug);
}
