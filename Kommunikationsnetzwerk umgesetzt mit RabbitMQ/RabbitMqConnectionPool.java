package me.mypvp.base.network;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitMqConnectionPool implements Closeable {

  private final Logger logger = LogManager.getLogger();
  private final Map<String, Connection> connections = new HashMap<>();

  private final String virtualHostPrefix;

  private ConnectionFactory connectionFactory;

  public RabbitMqConnectionPool() {
    this(null);
  }

  public RabbitMqConnectionPool(boolean devMode) {
    this("dev");
  }

  public RabbitMqConnectionPool(String virtualHostPrefix) {
    if (virtualHostPrefix != null) {
      virtualHostPrefix += "-";
    }

    this.virtualHostPrefix = virtualHostPrefix;
  }

  public void createConnectionFactory(String host, String user,
      String password) {

    connectionFactory = new ConnectionFactory();
    connectionFactory.setHost(host);
    connectionFactory.setUsername(user);
    connectionFactory.setPassword(password);
    connectionFactory.setAutomaticRecoveryEnabled(true);
  }

  public Connection getOrCreateConnection(String virtualHost) {
    return getOrCreateConnection(virtualHost, 1);
  }

  private Connection getOrCreateConnection(String virtualHost, int attempts) {
    final String finalHost = (virtualHostPrefix != null
        ? virtualHostPrefix : StringUtils.EMPTY) + virtualHost;

    if (connectionFactory == null) {
      throw new IllegalStateException("The ConnectionFactory must be created "
          + "before a connection is created");
    }

    Connection con = connections.computeIfAbsent(finalHost, vh -> {
      connectionFactory.setVirtualHost(finalHost);
      try {
        Connection connection = connectionFactory.newConnection();
        connection.addShutdownListener(ChannelShutdownListener.get());
        return connection;
      } catch (IOException | TimeoutException e) {
        logger.error("An error occurred while trying to connect to the "
            + "RabbitMQ server", e);
        return null;
      }
    });

    if (con == null) {
      return null;
    } else if (con.isOpen()) {
      return con;
    } else if (attempts >= 3) {
      return null;
    }

    connections.remove(finalHost);
    return getOrCreateConnection(virtualHost, ++attempts);
  }

  @Override
  public void close() throws IOException {
    IOException exception = null;

    for (Connection connection : connections.values()) {
      try {
        connection.close();
      } catch (IOException e) {
        if (exception == null)
          exception = e;
        else
          exception.addSuppressed(e);
      }
    }

    connections.clear();
    if (exception != null)
      throw exception;
  }

}
