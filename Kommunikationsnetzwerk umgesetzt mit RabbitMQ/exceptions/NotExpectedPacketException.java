package me.mypvp.base.network.exceptions;

import me.mypvp.base.network.packets.BaseNetworkPacket;

public class NotExpectedPacketException extends BaseNetworkException {

  private static final long serialVersionUID = 7272952150528444968L;

  private final BaseNetworkPacket packet;

  public NotExpectedPacketException(BaseNetworkPacket packet) {
    this.packet = packet;
  }

  public NotExpectedPacketException(BaseNetworkPacket packet, String message) {
    super(message);
    this.packet = packet;
  }

  public BaseNetworkPacket getPacket() {
    return packet;
  }

}
