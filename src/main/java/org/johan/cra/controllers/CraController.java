package org.johan.cra.controllers;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import java.util.List;

import org.johan.cra.domains.requests.InputCraRequest;
import org.johan.cra.domains.responses.CraResponse;
import org.johan.cra.services.CraService;

import javax.validation.Valid;

@Controller("/cras")
public class CraController {

  private final CraService service;

  public CraController(CraService service) {
    this.service = service;
  }

  @Get
  @Secured({"Default"})
  public List<CraResponse> getCurrentMonth(Authentication authentication) {
    return service.getCurrentMonth(authentication);
  }

  @Get("/month/{month}")
  @Secured({"Default"})
  public List<CraResponse> getSpecificMonth(Authentication authentication, Integer month) {
    return service.getSpecificMonthForMe(authentication, month);
  }

  @Get("/user/{userId}")
  @Secured({"Admin"})
  public List<CraResponse> getCurrentMonthForUser(Long userId) {
    return service.getCurrentMonthForUserId(userId);
  }

  @Get("/month/{month}/user/{userId}")
  @Secured({"Admin"})
  public List<CraResponse> getSpecificMonthForUser(Integer month, Long userId) {
    return service.getSpecificMonthForUser(userId, month);
  }

  @Post
  @Secured({"Default"})
  @Status(HttpStatus.NO_CONTENT)
  public void insertDay(Authentication authentication, @Valid @Body InputCraRequest request) {
    service.upsertDay(authentication, request);
  }
}
