package org.johan.cra.domains.responses;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DayElement {

    private String day;
    private Float value;
    private Boolean isDayOff;
    private Boolean isWeekEndDay;

}
