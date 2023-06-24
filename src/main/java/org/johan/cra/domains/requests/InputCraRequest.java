package org.johan.cra.domains.requests;

import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Introspected
public class InputCraRequest {

  @NotNull private String project;

  @NotNull
  private LocalDate craDate;

  @NotNull
  private Float value;
}
