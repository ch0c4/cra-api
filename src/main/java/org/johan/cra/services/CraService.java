package org.johan.cra.services;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.authentication.Authentication;
import jakarta.inject.Singleton;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.johan.cra.domains.clients.DayOffApiResponse;
import org.johan.cra.domains.entities.CraEntity;
import org.johan.cra.domains.entities.CraEntityId;
import org.johan.cra.domains.entities.UserEntity;
import org.johan.cra.domains.requests.InputCraRequest;
import org.johan.cra.domains.responses.CraResponse;
import org.johan.cra.domains.responses.DayElement;
import org.johan.cra.helpers.TimeHelper;
import org.johan.cra.repositories.CraRepository;
import org.johan.cra.repositories.UserRepository;

@Singleton
public class CraService {

  private final DayOffCacheService dayOffCacheService;
  private final CraRepository craRepository;
  private final UserRepository userRepository;
  private final TimeHelper timeHelper;

  public CraService(
      DayOffCacheService dayOffCacheService,
      CraRepository craRepository,
      UserRepository userRepository,
      TimeHelper timeHelper) {
    this.dayOffCacheService = dayOffCacheService;
    this.craRepository = craRepository;
    this.userRepository = userRepository;
    this.timeHelper = timeHelper;
  }

  public List<CraResponse> getSpecificMonthForMe(Authentication authentication, int month) {
    var user = getUser(authentication.getName());
    var now = timeHelper.now();
    var dateToGenerate = LocalDate.of(now.getYear(), month, 1);
    var craList = craRepository.fetchCraForUserAndMonthAndYear(user.getId(), month, now.getYear());

    return generate(craList, dateToGenerate);
  }

  public List<CraResponse> getCurrentMonthForUserId(Long userId) {
    var user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, "User not found"));
    var now = timeHelper.now();

    var craList =
        craRepository.fetchCraForUserAndMonthAndYear(
            user.getId(), now.getMonth().getValue(), now.getYear());

    return generate(craList, now);
  }

  public List<CraResponse> getSpecificMonthForUser(Long userId, int month) {
    var user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, "User not found"));
    var now = timeHelper.now();
    var dateToGenerate = LocalDate.of(now.getYear(), month, 1);

    var craList = craRepository.fetchCraForUserAndMonthAndYear(user.getId(), month, now.getYear());

    return generate(craList, dateToGenerate);
  }

  public List<CraResponse> getCurrentMonth(Authentication authentication) {
    var user = getUser(authentication.getName());
    var now = timeHelper.now();

    var craList =
        craRepository.fetchCraForUserAndMonthAndYear(
            user.getId(), now.getMonth().getValue(), now.getYear());

    return generate(craList, now);
  }

  public void upsertDay(Authentication authentication, InputCraRequest request) {
    var user = getUser(authentication.getName());

    if (request.getValue() > 1.0) {
      throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Value can't be greater than 1.0");
    }

    if (checkIsDayOff(request.getCraDate())
        || checkIsWeekEndDay(request.getCraDate().getDayOfWeek())) {
      throw new HttpStatusException(
          HttpStatus.BAD_REQUEST, "CraDay can't be on day off or week end");
    }

    CraEntity craEntity;

    var optionalCraEntity =
        craRepository.findById(
            new CraEntityId(request.getProject(), request.getCraDate(), user.getId()));

    if (optionalCraEntity.isPresent()) {
      craEntity = optionalCraEntity.get();
      craEntity.setValue(request.getValue());
    } else {
      craEntity =
          CraEntity.builder()
              .project(request.getProject())
              .craDate(request.getCraDate())
              .userId(user.getId())
              .value(request.getValue())
              .build();
    }

    craRepository.update(craEntity);
  }

  private List<CraResponse> generate(List<CraEntity> craList, LocalDate dateToGenerate) {
    var year = dateToGenerate.getYear();
    var month = dateToGenerate.getMonth();

    var startDate = LocalDate.of(year, month, 1);
    var daysInMonth = month.length(startDate.isLeapYear());

    if (craList.isEmpty()) {
      List<DayElement> dayElements = new ArrayList<>();
      for (int day = 1; day <= daysInMonth; day++) {
        var date = LocalDate.of(year, month, day);
        dayElements.add(
            DayElement.builder()
                .day(date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .isDayOff(checkIsDayOff(date))
                .isWeekEndDay(checkIsWeekEndDay(date.getDayOfWeek()))
                .value(0f)
                .build());
      }
      return List.of(CraResponse.builder().dayElements(dayElements).build());
    }

    List<CraResponse> craResponses = new ArrayList<>();
    var projects = craList.stream().map(CraEntity::getProject).toList();
    for (var project : projects) {
      List<DayElement> dayElements = new ArrayList<>();
      for (int day = 1; day <= daysInMonth; day++) {
        var date = LocalDate.of(year, month, day);
        dayElements.add(
            DayElement.builder()
                .day(date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .isDayOff(checkIsDayOff(date))
                .isWeekEndDay(checkIsWeekEndDay(date.getDayOfWeek()))
                .value(getCraValueForDateAndProject(date, craList, project))
                .build());
      }
      craResponses.add(CraResponse.builder().project(project).dayElements(dayElements).build());
    }
    return craResponses;
  }

  private Float getCraValueForDateAndProject(
      LocalDate date, List<CraEntity> craList, String project) {
    var optionalCra =
        craList.stream()
            .filter(
                craEntity ->
                    craEntity.getCraDate().equals(date) && craEntity.getProject().equals(project))
            .findFirst();
    if (optionalCra.isPresent()) {
      return optionalCra.get().getValue();
    }
    return 0f;
  }

  private Boolean checkIsWeekEndDay(DayOfWeek dayOfWeek) {
    return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
  }

  private Boolean checkIsDayOff(LocalDate dateToCheck) {
    var omg =
        dayOffCacheService.fetchDayOffForYear(dateToCheck.getYear()).stream()
            .map(DayOffApiResponse::getDay)
            .toList();
    return omg.contains(dateToCheck);
  }

  private UserEntity getUser(String email) {
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, "User not found"));
  }
}
