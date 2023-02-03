package me.mypvp.base.network.packets;

import java.io.IOException;

import me.mypvp.base.core.buffer.BufferException;
import me.mypvp.base.core.buffer.ByteBuffer;
import me.mypvp.base.network.PacketDefinition;

@PacketDefinition(
    packetName = "success"
)
public class DefaultSuccessPacket extends AbstractBaseNetworkPacket {

  public DefaultSuccessPacket() {
  }

  @Override
  public void write(ByteBuffer packet) throws IOException {
  }

  @Override
  public void read(ByteBuffer packet) throws BufferException {
  }

}
