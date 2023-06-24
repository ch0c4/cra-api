package org.johan.cra.controllers;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import org.johan.cra.domains.requests.RegisterRequest;
import org.johan.cra.domains.responses.UserResponse;
import org.johan.cra.services.UserService;

import javax.validation.Valid;
import java.util.List;

@Controller("/users")
@Secured(SecurityRule.IS_ANONYMOUS)
public class UserController {

  private final UserService service;

  public UserController(UserService service) {
    this.service = service;
  }

  @Post("/register")
  @Secured(SecurityRule.IS_ANONYMOUS)
  @Status(HttpStatus.NO_CONTENT)
  public void register(@Valid @Body RegisterRequest request) {
    service.register(request);
  }

  @Get
  @Secured({"Admin"})
  public List<UserResponse> all() {
    return service.all();
  }
}
