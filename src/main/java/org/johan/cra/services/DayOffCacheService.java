package org.johan.cra.services;

import io.micronaut.cache.annotation.CacheConfig;
import io.micronaut.cache.annotation.Cacheable;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import org.johan.cra.clients.DayOffApi;
import org.johan.cra.domains.clients.DayOffApiResponse;

@Singleton
@CacheConfig("day-off")
public class DayOffCacheService {

  private final DayOffApi api;

  public DayOffCacheService(DayOffApi api) {
    this.api = api;
  }

  @Cacheable
  public List<DayOffApiResponse> fetchDayOffForYear(int year) {
    var apiResponse = api.getDayOffForYear(String.valueOf(year));
    List<DayOffApiResponse> response = new ArrayList<>();
    apiResponse.forEach((key, value) -> response.add(new DayOffApiResponse(key, value)));
    return response;
  }
}
