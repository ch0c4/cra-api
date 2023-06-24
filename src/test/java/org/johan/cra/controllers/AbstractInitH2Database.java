package org.johan.cra.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public abstract class AbstractInitH2Database {

  private static final Logger log = LoggerFactory.getLogger(AbstractInitH2Database.class);

  private DataSource dataSource;

  public AbstractInitH2Database(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public void executeInitSQL(String filename) {
    InputStream sqlFileInputStream =
        Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
    Connection connection = null;

    try {
      Class.forName("org.h2.Driver");
      connection = this.dataSource.getConnection();
      this.importSQL(connection, sqlFileInputStream);
    } catch (Exception var13) {
      log.error("Unable to prepare dataSource", var13);
    } finally {
      if (connection != null) {
        try {
          connection.close();
        } catch (SQLException var12) {
          log.error("Unable to close connection", var12);
        }
      }
    }
  }

  private void importSQL(Connection connection, InputStream in) throws SQLException {
    Scanner scanner = new Scanner(in);
    scanner.useDelimiter("(;(\r)?\n)|(--\n)");
    Statement statement = null;

    try {
      statement = connection.createStatement();

      while (scanner.hasNext()) {
        String line = scanner.next();
        if (line.startsWith("/*!") && line.endsWith("*/")) {
          int i = line.indexOf(32);
          line = line.substring(i + 1, line.length() - " */".length());
        }

        if (line.trim().length() > 0) {
          statement.execute(line);
        }
      }
    } finally {
      if (statement != null) {
        statement.close();
      }

      scanner.close();
    }
  }
}
