package com.pulsefit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "class_template")
public class ClassTemplateEntity extends AuditedEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "studio_id", nullable = false)
  private StudioEntity studio;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "instructor_id", nullable = false)
  private InstructorEntity instructor;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String discipline;

  @Column(nullable = false, length = 1000)
  private String description;

  @Column(name = "duration_minutes", nullable = false)
  private int durationMinutes;

  @Column(name = "default_price", nullable = false, precision = 10, scale = 2)
  private BigDecimal defaultPrice;

  @Column(name = "cancellation_window_hours", nullable = false)
  private int cancellationWindowHours;

  @Column(nullable = false)
  private int capacity;
}
