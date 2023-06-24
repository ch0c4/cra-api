package org.johan.cra.repositories;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;
import java.util.List;
import org.johan.cra.domains.entities.CraEntity;
import org.johan.cra.domains.entities.CraEntityId;

@Repository
public interface CraRepository extends CrudRepository<CraEntity, CraEntityId> {

  @Query(
      "FROM cra c WHERE c.userId = :userId AND MONTH(c.craDate) = :month and YEAR(c.craDate) = :year")
  List<CraEntity> fetchCraForUserAndMonthAndYear(Long userId, int month, int year);
}
