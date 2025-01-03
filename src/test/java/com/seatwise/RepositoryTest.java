package com.seatwise;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
public abstract class RepositoryTest {

  @Autowired protected TestEntityManager em;

  protected void persistAndFlush(Object... entities) {
    for (Object entity : entities) {
      em.persist(entity);
    }
    em.flush();
  }
}
