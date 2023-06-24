package org.johan.cra.domains.entities;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity(name = "cra")
@Table(name = "cra")
@IdClass(CraEntityId.class)
public class CraEntity {

  @Id
  @Column(nullable = false)
  private String project;

  @Id
  @Column(nullable = false)
  private LocalDate craDate;

  @Id
  @Column(name = "user_id", nullable = false)
  private Long userId;

  private Float value;
}
