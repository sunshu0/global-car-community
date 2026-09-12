package com.worldgarage.backend.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.Test;

class PostgreSqlMigrationTest {

  @Test
  void initialMigrationCreatesExpectedSchema() throws Exception {
    String url =
        "jdbc:h2:mem:postgresql-migration;"
            + "MODE=PostgreSQL;DB_CLOSE_DELAY=-1";

    MigrateResult result =
        Flyway.configure()
            .dataSource(url, "sa", "")
            .locations("classpath:db/migration/postgresql")
            .load()
            .migrate();

    assertEquals(1, result.migrationsExecuted);

    try (Connection connection = DriverManager.getConnection(url, "sa", "");
        Statement statement = connection.createStatement();
        ResultSet tables =
            statement.executeQuery(
                "SELECT COUNT(*) FROM information_schema.tables "
                    + "WHERE table_schema = 'PUBLIC' "
                    + "AND LOWER(table_name) IN "
                    + "('user_accounts', 'cars', 'image_assets')")) {
      tables.next();
      assertEquals(3, tables.getInt(1));
    }
  }
}
