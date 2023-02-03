package me.mypvp.base.network;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.mypvp.base.core.buffer.NettyByteBuffer;
import me.mypvp.base.network.exceptions.ResponseException.ResponseExceptionType;
import me.mypvp.base.network.packets.BaseNetworkExceptionPacket;
import me.mypvp.base.network.packets.BaseNetworkPacket;

public class GroupConsumer implements DeliverCallback {

  private static final Logger logger = LogManager.getLogger();

  public static @NotNull GroupConsumer create(@NotNull BaseNetworkPacketRegistry packetRegistry,
      @NotNull PacketGroup packetGroup, @NotNull ExecutorService execService) {
    return new GroupConsumer(packetRegistry, packetGroup, execService);
  }

  private final BaseNetworkPacketRegistry packetRegistry;
  private final PacketGroup packetGroup;
  private final ExecutorService execService;

  private final Map<String, ResponseReceiver<?>> receivers = new HashMap<>();

  public GroupConsumer(@NotNull BaseNetworkPacketRegistry packetRegistry, @NotNull PacketGroup packetGroup,
      @NotNull ExecutorService execService) {
    this.packetRegistry = packetRegistry;
    this.packetGroup = packetGroup;
    this.execService = execService;
  }

  @Override
  public void handle(String consumerTag, Delivery message) throws IOException {
    try {
      handlePacket(consumerTag, message);
    } catch (Exception e) {
      BasicProperties properties = message.getProperties();
      logger.error("An error occurred while processing the incoming message "
          + "(Group: {}, Type: {}, ReplyTo: {}, CorrelationId: {})", packetGroup.getName(), properties.getType(),
          properties.getReplyTo(), properties.getCorrelationId(), e);
    }
  }

  private void handlePacket(String consumerTag, Delivery message) throws IOException {
    BasicProperties basicProperties = message.getProperties();
    String packetName = basicProperties.getType();
    PacketController<?> packetController = packetRegistry.getPacket(packetGroup.getName(), packetName);
    logger.trace("Receiving incoming packet {}.{}", packetGroup.getName(), packetName);

    if (packetController == null) {
      logger.error("Could not process packet because the packet {}.{} is not yet registered.",
          packetGroup.getName(), packetName);
      return;
    }

    BaseNetworkPacket packet;
    try {
      packet = packetController.newPacketInstance();
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      logger.error("An error occurred while trying to create a new instance "
          + "of the {} packet", packetController.getPacketName(), e);
      return;
    }

    logger.debug("Handling incoming packet {}.{}", packetGroup.getName(), packetName);
    ByteBuf buffer = Unpooled.wrappedBuffer(message.getBody());
    NettyByteBuffer packetBuffer = new NettyByteBuffer(buffer);
    packet.read(packetBuffer);

    if (basicProperties.getReplyTo() != null) {
      logger.debug("Packet is expecting a response");
      CompletableFuture<BaseNetworkPacket> respondFuture = new CompletableFuture<>();
      respondFuture.exceptionally(e -> {
        return new BaseNetworkExceptionPacket(ResponseExceptionType.RESPONSE_HANDLE_ERROR);
      })
      .thenAccept(responsePacket -> {
        handleResponse(message, responsePacket);
      });
      packet.setRespondFuture(respondFuture);
    } else if (basicProperties.getCorrelationId() != null) {
      logger.debug("Packet is a response");
      ResponseReceiver<?> response = receivers.remove(basicProperties.getCorrelationId());

      if (response != null) {
        response.receive(packet);
      } else {
        logger.warn("Received response {}.{} was not expected", packetController.getGroup().getName(),
            packetController.getPacketName());
      }
      return;
    }

    execService.execute(() -> {
      try {
        packetController.handle(packet);
      } catch (Throwable e) {
        logger.error("An error occurred while trying to process the packet", e);
      }
    });
  }

  private void handleResponse(Delivery message, BaseNetworkPacket responsePacket) {
    ByteBuf responseBuffer = Unpooled.buffer();
    NettyByteBuffer responsePacketBuffer = new NettyByteBuffer(responseBuffer);
    logger.trace("Preparing outgoing response");

    PacketController<?> packetController = packetRegistry.getPacket(responsePacket.getClass());
    if (packetController == null) {
      logger.error("The packet {} is not registered yet", responsePacket.getClass());
      return;
    }

    try {
      responsePacket.write(responsePacketBuffer);
      byte[] bytes = responseBuffer.array();

      Channel channel = packetGroup.getChannel();

      if (channel == null) {
        logger.error("The channel of the packet group {} is null",
            packetController.getGroup().getName());
        return;
      }

      BasicProperties properties = message.getProperties();
      String replyTo = properties.getReplyTo();
      BasicProperties replyProperties = new BasicProperties().builder()
          .type(packetController.getPacketName())
          .correlationId(properties.getCorrelationId()).build();
      packetGroup.getChannel().basicPublish("", replyTo, replyProperties, bytes);
      logger.debug("Published outgoing response for {}", replyTo);
    } catch (IOException e) {
      logger.error("An error occurred while trying to send the response", e);
    }
  }

  public void addResponseReceiver(String correlationId, ResponseReceiver<?> receiver) {
    receivers.put(correlationId, receiver);
  }

}
