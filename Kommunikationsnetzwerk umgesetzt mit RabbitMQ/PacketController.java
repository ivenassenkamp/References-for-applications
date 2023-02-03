package me.mypvp.base.network;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.jetbrains.annotations.NotNull;

import com.rabbitmq.client.BuiltinExchangeType;

import me.mypvp.base.network.packets.BaseNetworkPacket;

public class PacketController<T extends BaseNetworkPacket> {

  public static <T extends BaseNetworkPacket> PacketController<T> create(@NotNull Class<T> packetClass,
      @NotNull BaseNetworkPacketDirection packetDirection, @NotNull PacketGroup packetGroup,
      @NotNull PacketDefinition packetDefinition) throws NoSuchMethodException, SecurityException {
    Constructor<T> packetConstructor = packetClass.getDeclaredConstructor();
    packetConstructor.setAccessible(true);
    return new PacketController<>(packetClass, packetConstructor, packetDirection, packetGroup, packetDefinition);
  }

  private final Class<T> packetClass;
  private final Constructor<T> packetConstructor;
  private final BaseNetworkPacketDirection packetDirection;

  private final PacketGroup packetGroup;
  private final String packetName;
  private final BuiltinExchangeType exchangeType;

  private final PacketHandlers<T> packetHandlers;

  private PacketController(@NotNull Class<T> packetClass, @NotNull Constructor<T> packetConstructor,
      @NotNull BaseNetworkPacketDirection packetDirection, @NotNull PacketGroup packetGroup,
      @NotNull PacketDefinition packetDefinition) {
    this.packetClass = packetClass;
    this.packetConstructor = packetConstructor;
    this.packetDirection = packetDirection;
    this.packetGroup = packetGroup;
    this.packetName = packetDefinition.packetName();
    this.exchangeType = packetDefinition.exchangeType();
    this.packetHandlers = PacketHandlers.create(packetClass);
  }

  public @NotNull Class<T> getPacketClass() {
    return this.packetClass;
  }

  public @NotNull Constructor<T> getConstructor() {
    return this.packetConstructor;
  }

  public @NotNull T newPacketInstance() throws InstantiationException, IllegalAccessException,
      IllegalArgumentException, InvocationTargetException {
    return packetConstructor.newInstance();
  }

  @SuppressWarnings("unchecked")
  public void handle(@NotNull BaseNetworkPacket packet) throws Throwable {
    getHandlers().handle((T) packet);
  }

  public @NotNull BaseNetworkPacketDirection getDirection() {
    return this.packetDirection;
  }

  public @NotNull PacketGroup getGroup() {
    return this.packetGroup;
  }

  public @NotNull String getPacketName() {
    return this.packetName;
  }

  public @NotNull String getExchange() {
    return this.packetGroup.getName() + "." + this.packetName;
  }

  public PacketHandlers<T> getHandlers() {
    return this.packetHandlers;
  }

  public @NotNull BuiltinExchangeType getExchangeType() {
    return this.exchangeType;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }

    if (obj.getClass() == getClass()) {
      PacketController<?> ctrl = (PacketController<?>) obj;
      return ctrl.getGroup().getName().equals(getGroup().getName())
          && ctrl.getPacketName().equals(getPacketName());
    }

    return false;
  }

}
