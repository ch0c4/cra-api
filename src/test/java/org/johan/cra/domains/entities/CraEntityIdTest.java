package org.johan.cra.domains.entities;

import static org.junit.jupiter.api.Assertions.*;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class CraEntityIdTest {

  @Test
  void test_equals() {
    EqualsVerifier.simple().forClass(CraEntityId.class).verify();
  }
}
