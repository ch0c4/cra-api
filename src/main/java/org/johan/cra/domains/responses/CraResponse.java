package org.johan.cra.domains.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.ALWAYS)
public class CraResponse {

  private String project;
  private List<DayElement> dayElements;
}
