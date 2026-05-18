package com.pulsefit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "studio")
public class StudioEntity extends AuditedEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String slug;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String neighborhood;

  @Column(nullable = false)
  private String discipline;

  @Column(nullable = false, length = 1000)
  private String description;

  @Column(name = "image_url", nullable = false)
  private String imageUrl;

  @Column(nullable = false)
  private boolean featured;

  @Column(nullable = false)
  private boolean approved;

  @Column(nullable = false, precision = 3, scale = 2)
  private BigDecimal rating;

  @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
  private BigDecimal basePrice;
}
