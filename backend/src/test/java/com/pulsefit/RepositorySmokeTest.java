package com.pulsefit;

import com.pulsefit.domain.StudioEntity;
import com.pulsefit.repository.StudioRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RepositorySmokeTest {
  @Autowired
  StudioRepository studios;

  @Test
  void savesAndLoadsStudioBySlug() {
    StudioEntity studio = new StudioEntity();
    studio.setSlug("unit-test-studio");
    studio.setName("Unit Test Studio");
    studio.setNeighborhood("Soho");
    studio.setDiscipline("Yoga");
    studio.setDescription("Test description");
    studio.setImageUrl("https://example.com/image.jpg");
    studio.setFeatured(true);
    studio.setApproved(true);
    studio.setRating(new BigDecimal("4.90"));
    studio.setBasePrice(new BigDecimal("25.00"));

    studios.save(studio);

    assertThat(studios.findBySlug("unit-test-studio")).isPresent();
  }
}
