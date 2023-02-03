package me.mypvp.base.network;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.rabbitmq.client.Channel;

public class PacketGroup {

  public static @NotNull PacketGroup create(@NotNull String name) {
    return new PacketGroup(name);
  }

  private final String name;

  private RabbitMqConnectionPool rabbitMqPool;
  private String virtualHost;
  private Channel channel;
  private String queueName;
  private String consumerTag;
  private GroupConsumer groupConsumer;

  private PacketGroup(@NotNull String name) {
    this.name = name;
  }

  public @NotNull String getName() {
    return this.name;
  }

  public @Nullable RabbitMqConnectionPool getRabbitMqPool() {
    return this.rabbitMqPool;
  }

  public @Nullable String getVirtualHost() {
    return this.virtualHost;
  }

  public @Nullable Channel getChannel() throws IOException {
    if ((this.channel != null) && !this.channel.isOpen()) {
      this.channel = rabbitMqPool.getOrCreateConnection(virtualHost).createChannel();
    }
    return this.channel;
  }

  public @Nullable String getQueueName() {
    return this.queueName;
  }

  public @Nullable String getConsumerTag() {
    return this.consumerTag;
  }

  public GroupConsumer getConsumer() {
    return this.groupConsumer;
  }

  public void declareQueue(@NotNull RabbitMqConnectionPool rabbitMqPool,
      @NotNull BaseNetworkPacketRegistry packetRegistry, @NotNull String virtualHost, @NotNull String clientName,
      @NotNull ExecutorService execService) throws IOException {
    this.rabbitMqPool = rabbitMqPool;
    this.virtualHost = virtualHost;
    this.channel = this.rabbitMqPool.getOrCreateConnection(virtualHost).createChannel();

    this.queueName = name + "." + clientName;
    channel.queueDeclare(queueName, false, false, true, null);

    this.groupConsumer = new GroupConsumer(packetRegistry, this, execService);
    channel.basicConsume(queueName, true, groupConsumer, cTag -> {});
  }

}
