package org.johan.cra.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.johan.cra.clients.DayOffApi;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

class DayOffCacheServiceTest {

  @Test
  void test_fetchDayOffForYear() {
    var dayOffApiMocked = mock(DayOffApi.class);
    when(dayOffApiMocked.getDayOffForYear(anyString()))
        .thenReturn(Map.of(LocalDate.of(2023, 5, 1), "1er mai"));
    var service = new DayOffCacheService(dayOffApiMocked);

    var actual = service.fetchDayOffForYear(2023);
    assertNotNull(actual);
    assertEquals(1, actual.size());
    verify(dayOffApiMocked, times(1)).getDayOffForYear(anyString());
  }
}
