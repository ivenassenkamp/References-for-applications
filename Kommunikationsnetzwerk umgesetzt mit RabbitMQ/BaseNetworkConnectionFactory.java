package me.mypvp.base.network;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BaseNetworkConnectionFactory implements Closeable {

  private final String clientName;
  private final RabbitMqConnectionPool rabbitMqPool;

  private Map<String, BaseNetwork> networks = new HashMap<>();

  public BaseNetworkConnectionFactory(String clientName,
      RabbitMqConnectionPool rabbitMqPool) {
    this.clientName = clientName;
    this.rabbitMqPool = rabbitMqPool;
  }

  public BaseNetwork getNetwork(String virtualHost) {
    if (networks == null) {
      throw new IllegalStateException("The ConnectionFactory was "
          + "already closed");
    }

    BaseNetwork network = networks.get(virtualHost);

    if (network == null) {
      network = BaseNetwork.newNetwork(virtualHost, clientName, rabbitMqPool);
      networks.put(virtualHost, network);
    }

    return network;
  }

  @Override
  public void close() throws IOException {
    for (BaseNetwork network : networks.values()) {
      network.getPacketRegistry().unregisterAll();
    }
    networks = null;
  }

}
