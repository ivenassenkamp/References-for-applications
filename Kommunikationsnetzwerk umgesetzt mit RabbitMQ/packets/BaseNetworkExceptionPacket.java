package me.mypvp.base.network.packets;

import me.mypvp.base.core.buffer.BufferException;
import me.mypvp.base.core.buffer.ByteBuffer;
import me.mypvp.base.network.PacketDefinition;
import me.mypvp.base.network.exceptions.ResponseException.ResponseExceptionType;

@PacketDefinition(
    packetName = "base-network_exception"
)
public class BaseNetworkExceptionPacket extends AbstractBaseNetworkPacket {

  private ResponseExceptionType exceptionType;

  protected BaseNetworkExceptionPacket() {
  }

  public BaseNetworkExceptionPacket(ResponseExceptionType exceptionType) {
    this.exceptionType = exceptionType;
  }

  @Override
  public void write(ByteBuffer buffer) {
    buffer.writeVarInt(exceptionType.ordinal());
  }

  @Override
  public void read(ByteBuffer buffer) throws BufferException {
    this.exceptionType = ResponseExceptionType.values()[buffer.readVarInt()];
  }

  public ResponseExceptionType getException() {
    return exceptionType;
  }

}
