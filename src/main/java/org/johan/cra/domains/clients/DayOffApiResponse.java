package org.johan.cra.domains.clients;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@AllArgsConstructor
@Getter
public class DayOffApiResponse {

    private LocalDate day;
    private String name;

}
