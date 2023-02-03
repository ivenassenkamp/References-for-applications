package me.mypvp.base.network;

import java.util.concurrent.CompletableFuture;

import me.mypvp.base.network.exceptions.NotExpectedPacketException;
import me.mypvp.base.network.packets.BaseNetworkPacket;

public class ResponseReceiver<T extends BaseNetworkPacket> {

  private final Class<T> responsePacketClass;
  private final CompletableFuture<T> future;

  public ResponseReceiver(Class<T> responsePacketClass) {
    this.responsePacketClass = responsePacketClass;
    this.future = new CompletableFuture<>();
  }

  public Class<T> getResponsePacketClass() {
    return this.responsePacketClass;
  }

  public CompletableFuture<T> getFuture() {
    return this.future;
  }

  @SuppressWarnings("unchecked")
  public void receive(BaseNetworkPacket packet) {
    if (future.isDone()) {
      return;
    }

    if (responsePacketClass.isInstance(packet)) {
      future.complete((T) packet);
    } else {
      future.completeExceptionally(new NotExpectedPacketException(packet));
    }
  }

}
