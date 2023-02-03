package me.mypvp.base.network;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.mypvp.base.core.buffer.NettyByteBuffer;
import me.mypvp.base.network.packets.BaseNetworkPacket;

public class BaseNetwork {

  private static final Duration DEFAULT_RESPONSE_TIMEOUT = Duration.ofSeconds(10L);

  private final Logger logger = LogManager.getLogger();

  public static BaseNetwork newNetwork(@NotNull String virtualHost, @NotNull String clientName,
      @NotNull RabbitMqConnectionPool rabbitMqPool) {
    return new BaseNetwork(virtualHost, clientName, rabbitMqPool);
  }

  private final ScheduledExecutorService execService = Executors.newSingleThreadScheduledExecutor();
  private final BaseNetworkPacketRegistry packetRegistry;

  @Deprecated
  public BaseNetwork(@NotNull String virtualHost, @NotNull BaseNetworkPacketRegistry packetRegistry,
      @NotNull RabbitMqConnectionPool rabbitMqPool) {
    this.packetRegistry = packetRegistry;
  }

  private BaseNetwork(@NotNull String virtualHost, @NotNull String clientName,
      @NotNull RabbitMqConnectionPool rabbitMqPool) {
    this.packetRegistry = BaseNetworkPacketRegistry.create(clientName, virtualHost, rabbitMqPool);
  }

  @Deprecated
  public void registerHandler(@NotNull Object handlers) throws IllegalAccessException {
    registerHandlers(handlers);
  }

  public void registerHandlers(@NotNull Object handlers) throws IllegalAccessException {
    Method[] methods = handlers.getClass().getMethods();

    for (Method method : methods) {
      PacketHandler handler = method.getDeclaredAnnotation(PacketHandler.class);
      if (handler == null) {
        continue;
      }

      if (method.getParameterCount() != 1) {
        throw new IllegalArgumentException("The " + method.getName() + " method expects more than one parameter");
      }

      Class<?> parameter = method.getParameterTypes()[0];
      PacketDefinition packetDefinition = parameter.getAnnotation(PacketDefinition.class);
      if (packetDefinition == null) {
        throw new IllegalArgumentException("The method parameter is not a packet or has no packet definition");
      }
      PacketController<?> controller = packetRegistry.getPacket(packetDefinition.packetGroup(),
          packetDefinition.packetName());
      if (controller == null) {
        throw new IllegalArgumentException("The specified "
            + "parameter is not a packet or not registered");
      }

      MethodHandle handle = MethodHandles.lookup().unreflect(method);
      PacketHandlers<?> packetHandlers = controller.getHandlers();
      packetHandlers.registerHandler(handle, handler.priority(), handlers);
    }
  }

  public void unregisterHandlers(@NotNull Object handlers) {
    for (PacketController<?> controller : packetRegistry.getPackets()) {
      controller.getHandlers().removeHandlers(handlers);
    }
  }

  public void unregisterHandlers(@NotNull Class<?> handlersClass) {
    for (PacketController<?> controller : packetRegistry.getPackets()) {
      controller.getHandlers().removeHandlers(handlersClass);
    }
  }

  public void call(@NotNull BaseNetworkPacket packet) throws IOException {
    call(packet, "");
  }

  public void call(@NotNull BaseNetworkPacket packet, @NotNull String routingKey) throws IOException {
    PacketController<?> controller = packetRegistry.getPacket(packet.getClass());
    if (controller == null) {
      throw new IllegalArgumentException("The specified packet must be registered first.");
    }
    Channel channel = controller.getGroup().getChannel();

    if (channel == null) {
      throw new IllegalStateException("The channel of the packet group "
          + controller.getGroup().getName() + " is null");
    }

    ByteBuf buffer = Unpooled.buffer();
    NettyByteBuffer packetBuffer = new NettyByteBuffer(buffer);
    packet.write(packetBuffer);
    byte[] bytes = buffer.array();

    BasicProperties properties = new BasicProperties.Builder()
        .type(controller.getPacketName()).build();
    channel.basicPublish(controller.getExchange(), routingKey, properties, bytes);
    logger.debug("Send packet call {}.{}", controller.getGroup().getName(), controller.getPacketName());
  }

  public <T extends BaseNetworkPacket> @NotNull CompletableFuture<T> call(@NotNull BaseNetworkPacket packet,
      @NotNull Class<T> responsePacketClass) throws IOException {
    return call(packet, "", responsePacketClass, DEFAULT_RESPONSE_TIMEOUT);
  }

  public <T extends BaseNetworkPacket> @NotNull CompletableFuture<T> call(@NotNull BaseNetworkPacket packet,
      @NotNull String routingKey, @NotNull Class<T> responsePacketClass) throws IOException {
    return call(packet, routingKey, responsePacketClass, DEFAULT_RESPONSE_TIMEOUT);
  }
    public <T extends BaseNetworkPacket> @NotNull CompletableFuture<T> call(@NotNull BaseNetworkPacket packet,
        @NotNull String routingKey, @NotNull Class<T> responsePacketClass, @NotNull Duration responseTimeout)
        throws IOException {

    PacketController<?> controller = packetRegistry.getPacket(packet.getClass());
    if (controller == null) {
      throw new IllegalArgumentException("The specified packet must be registered first.");
    }
    Channel channel = controller.getGroup().getChannel();

    if (channel == null) {
      throw new IllegalStateException("The channel of the packet group "
          + controller.getGroup().getName() + " is null");
    }

    ByteBuf buffer = Unpooled.buffer();
    NettyByteBuffer packetBuffer = new NettyByteBuffer(buffer);
    packet.write(packetBuffer);
    byte[] bytes = buffer.array();

    if (packetRegistry.getPacket(responsePacketClass) == null) {
      throw new IllegalArgumentException("The response packet " + responsePacketClass.getSimpleName()
          + " has not yet been registered.");
    }

    String correlationId = UUID.randomUUID().toString();
    ResponseReceiver<T> receiver = new ResponseReceiver<>(responsePacketClass);
    controller.getGroup().getConsumer().addResponseReceiver(correlationId, receiver);
    BasicProperties properties = new BasicProperties.Builder()
        .type(controller.getPacketName())
        .correlationId(correlationId)
        .replyTo(controller.getGroup().getQueueName()).build();
    channel.basicPublish(controller.getExchange(), routingKey, properties, bytes);
    logger.debug("Send packet call {}.{} with response expectation ({})", controller.getGroup().getName(),
        controller.getPacketName(), responsePacketClass.getName());

    execService.schedule(() -> {
      if (!receiver.getFuture().isDone()) {
        logger.warn("Packet {}.{} got no response", controller.getGroup().getName(), controller.getPacketName());
        receiver.getFuture().completeExceptionally(new TimeoutException("No one has responded to the packet"));
      }
    }, responseTimeout.toMillis(), TimeUnit.MILLISECONDS);

    return receiver.getFuture();
  }

  public BaseNetworkPacketRegistry getPacketRegistry() {
    return this.packetRegistry;
  }

}
