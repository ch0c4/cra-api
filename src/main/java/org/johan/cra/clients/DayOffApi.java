package org.johan.cra.clients;

import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.annotation.Client;

import java.time.LocalDate;
import java.util.Map;

@Client("https://calendrier.api.gouv.fr/jours-feries")
public interface DayOffApi {

  @Get("/metropole/{year}.json")
  Map<LocalDate, String> getDayOffForYear(String year);
}
