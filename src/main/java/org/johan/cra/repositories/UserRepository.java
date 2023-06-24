package org.johan.cra.repositories;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;
import org.johan.cra.domains.entities.UserEntity;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, Long> {

  Optional<UserEntity> findByEmail(@NotNull String email);

  List<UserEntity> findByRole(String role);
}
