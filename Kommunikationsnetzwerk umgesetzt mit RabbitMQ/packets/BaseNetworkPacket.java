package me.mypvp.base.network.packets;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import me.mypvp.base.core.buffer.BufferException;
import me.mypvp.base.core.buffer.ByteBuffer;

public interface BaseNetworkPacket {

  void write(ByteBuffer packet) throws IOException;

  void read(ByteBuffer packet) throws BufferException;

  void respond(BaseNetworkPacket respondPacket);

  boolean isResponded();

  boolean awaitsResponse();

  void setRespondFuture(CompletableFuture<BaseNetworkPacket> future);

}
