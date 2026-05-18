package com.pulsefit.repository;

import com.pulsefit.domain.ClassTemplateEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassTemplateRepository extends JpaRepository<ClassTemplateEntity, Long> {
  List<ClassTemplateEntity> findByStudioSlug(String slug);
}
