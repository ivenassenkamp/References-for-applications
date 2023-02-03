package me.mypvp.base.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.rabbitmq.client.Channel;

import me.mypvp.base.network.packets.BaseNetworkExceptionPacket;
import me.mypvp.base.network.packets.BaseNetworkPacket;
import me.mypvp.base.network.packets.DefaultSuccessPacket;

public class BaseNetworkPacketRegistry {

  public static BaseNetworkPacketRegistry create(@NotNull String clientName, @NotNull String virtualHost,
      @NotNull RabbitMqConnectionPool rabbitMqPool) {
    return new BaseNetworkPacketRegistry(clientName, virtualHost, rabbitMqPool);
  }

  private static final Logger logger = LogManager.getLogger();

  private final String clientName;
  private final String virtualHost;
  private final RabbitMqConnectionPool rabbitMqPool;

  private final ExecutorService execService = Executors.newCachedThreadPool();

  private final Map<String, PacketGroup> groups = new HashMap<>();
  private final List<PacketController<?>> packets = new ArrayList<>();

  public BaseNetworkPacketRegistry(@NotNull String clientName, @NotNull String virtualHost,
      @NotNull RabbitMqConnectionPool rabbitMqPool) {
    this.clientName = clientName;
    this.virtualHost = virtualHost;
    this.rabbitMqPool = rabbitMqPool;

    try {
      registerPacket(BaseNetworkPacketDirection.REPLY, BaseNetworkExceptionPacket.class);
      registerPacket(BaseNetworkPacketDirection.REPLY, DefaultSuccessPacket.class);
    } catch (NoSuchMethodException | IOException e) {
      logger.error("Could not register default packets", e);
    }
  }

  public <T extends BaseNetworkPacket> void registerPacket(@NotNull BaseNetworkPacketDirection packetDirection,
      @NotNull Class<T> packetClass) throws NoSuchMethodException, IOException {
    logger.trace("Try to register packet in class {}", packetClass.getName());
    PacketDefinition definition = packetClass.getAnnotation(PacketDefinition.class);
    if (definition == null) {
      throw new IllegalArgumentException("The specified class has no PacketDefinition annotation");
    }

    PacketController<?> existingController = getPacket(definition.packetGroup(), definition.packetName());
    if (existingController != null) {
      unregisterPacket(existingController);
    }

    String groupName = definition.packetGroup();
    PacketGroup group = groups.get(groupName);
    if (group == null) {
      group = PacketGroup.create(groupName);
      group.declareQueue(rabbitMqPool, this, virtualHost, clientName, execService);
      groups.put(groupName, group);
    }

    PacketController<T> controller = PacketController.create(packetClass, packetDirection, group, definition);

    if (packetDirection == BaseNetworkPacketDirection.INCOMING || packetDirection == BaseNetworkPacketDirection.BOTH) {
      Channel channel = group.getChannel();

      if (channel == null) {
        throw new IllegalStateException("The channel of the packet group "
            + controller.getGroup().getName() + " is null");
      }

      String exchange = controller.getExchange();
      channel.exchangeDeclare(exchange, controller.getExchangeType());
      channel.queueBind(group.getQueueName(), exchange, clientName);
    }

    packets.add(controller);
    logger.debug("Registered packet {}.{}", group.getName(), controller.getPacketName());
  }

  public void unregisterAll() throws IOException {
    IOException exceptionCollection = null;
    for (PacketController<?> controller : new ArrayList<>(packets)) {
      try {
        unregisterPacket(controller);
      } catch (IOException e) {
        if (exceptionCollection == null) {
          exceptionCollection = e;
        } else {
          exceptionCollection.addSuppressed(e);
        }
      }
    }

    if (exceptionCollection != null) {
      throw exceptionCollection;
    }
  }

  public <T extends BaseNetworkPacket> void unregisterPacket(@NotNull Class<T> packetClass) throws IOException {
    PacketController<T> packetController = getPacket(packetClass);
    if (packetController == null) {
      throw new IllegalArgumentException("The passed class does not contain a registered packet");
    }
    unregisterPacket(packetController);
  }

  public void unregisterPacket(@NotNull String packetGroup, @NotNull String packetName) throws IOException {
    PacketController<?> packetController = getPacket(packetGroup, packetName);
    if (packetController == null) {
      throw new IllegalArgumentException("The passed packet group and name does not belongs to a registered packet");
    }
    unregisterPacket(packetController);
  }

  public void unregisterPacket(@NotNull PacketController<?> controller) throws IOException {
    Iterator<PacketController<?>> controllers = packets.iterator();
    while (controllers.hasNext()) {
      PacketController<?> nextController = controllers.next();
      if (nextController.equals(controller)) {
        controllers.remove();
        BaseNetworkPacketDirection direction = controller.getDirection();
        if (direction == BaseNetworkPacketDirection.INCOMING || direction == BaseNetworkPacketDirection.BOTH) {
          Channel channel = controller.getGroup().getChannel();

          if (channel == null) {
            throw new IllegalStateException("The channel of the packet group "
                + controller.getGroup().getName() + " is null");
          }

          PacketGroup group = controller.getGroup();
          channel.queueUnbind(group.getQueueName(), controller.getExchange(), clientName);
        }
        return;
      }
    }

    throw new IllegalArgumentException("The passed controller is not registered");
  }

  public @Nullable PacketController<?> getPacket(String packetGroup, String packetName) {
    for (PacketController<?> controller : packets) {
      if (controller.getGroup().getName().equals(packetGroup)
          && controller.getPacketName().equals(packetName)) {
        return controller;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public <T extends BaseNetworkPacket> @Nullable PacketController<T> getPacket(Class<T> packetClass) {
    for (PacketController<?> controller : packets) {
      if (controller.getPacketClass().equals(packetClass)) {
        return (PacketController<T>) controller;
      }
    }
    return null;
  }

  public List<PacketController<?>> getPackets() {
    return Collections.unmodifiableList(packets);
  }

}
