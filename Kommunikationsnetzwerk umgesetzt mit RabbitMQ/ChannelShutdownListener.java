package me.mypvp.base.network;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

public class ChannelShutdownListener implements ShutdownListener {

  private static final Logger LOGGER = LogManager.getLogger();
  private static ChannelShutdownListener instance;

  public static ChannelShutdownListener get() {
    if (instance == null) {
      instance = new ChannelShutdownListener();
    }
    return instance;
  }

  @Override
  public void shutdownCompleted(ShutdownSignalException cause) {
    LOGGER.warn("RabbitMQ channel closed", cause);
  }

}
