package org.johan.cra.domains.requests;

import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Introspected
public class RegisterRequest {

  @Email private String email;

  private String password;
}
