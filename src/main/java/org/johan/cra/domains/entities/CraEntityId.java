package org.johan.cra.domains.entities;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Embeddable
public class CraEntityId implements Serializable {

  @Serial private static final long serialVersionUID = 6065670419986083150L;

  @Column(nullable = false)
  private String project;

  @Column(nullable = false)
  private LocalDate craDate;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CraEntityId that = (CraEntityId) o;
    return Objects.equals(project, that.project)
        && Objects.equals(craDate, that.craDate)
        && Objects.equals(userId, that.userId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(project, craDate, userId);
  }
}
