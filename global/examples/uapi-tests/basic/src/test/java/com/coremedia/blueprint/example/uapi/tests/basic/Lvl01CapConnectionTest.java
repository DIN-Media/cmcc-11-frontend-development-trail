package com.coremedia.blueprint.example.uapi.tests.basic;

import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;

/**
 * <p>
 * This test just shows how to retrieve a connection backed by the XML
 * repository. The connection comes configured and established via
 * importing {@link XmlRepoConfiguration}.
 * </p>
 * <p><strong>Remarks:</strong></p>
 * <pre>{@code
 * @SpringJUnitConfig(XmlRepoConfiguration.class)
 * }</pre>
 * <p>
 * {@link SpringJUnitConfig} combines several annotations into one and is
 * very suitable to get started with testing fast. {@link XmlRepoConfiguration}
 * is all you need, if you are satisfied with its defaults. Otherwise, you
 * will see later a common pattern of using a {@code LocalConfig}.
 * </p>
 * <pre>{@code
 * @DirtiesContext(classMode = AFTER_CLASS)
 * }</pre>
 * <p>
 * We should ensure to close the connection at least after our test is done.
 * Using {@code AFTER_CLASS} keeps the connection open between tests and will
 * speed up your tests a little.
 * </p>
 */
@SpringJUnitConfig(XmlRepoConfiguration.class)
@DirtiesContext(classMode = AFTER_CLASS)
class Lvl01CapConnectionTest {
  @NonNull
  private final CapConnection connection;

  Lvl01CapConnectionTest(@Autowired @NonNull CapConnection connection) {
    this.connection = connection;
  }

  @Test
  void connectionShouldBeAvailable() {
    assertThat(connection.getSession().isOpen())
            .as("A connection should have been established.")
            .isTrue();
  }
}
