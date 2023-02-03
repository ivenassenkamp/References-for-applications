package me.mypvp.base.network.packets;

import java.util.concurrent.CompletableFuture;

public abstract class AbstractBaseNetworkPacket implements BaseNetworkPacket {

  CompletableFuture<BaseNetworkPacket> respondFuture;

  @Override
  public void respond(BaseNetworkPacket respondPacket) {
    if (respondFuture == null) {
      throw new IllegalStateException("No response is expected");
    }

    if (respondFuture.isDone()) {
      throw new IllegalStateException("It has already been responded");
    }

    respondFuture.complete(respondPacket);
  }

  @Override
  public boolean isResponded() {
    return respondFuture.isDone();
  }

  @Override
  public boolean awaitsResponse() {
    return respondFuture != null;
  }

  @Override
  public void setRespondFuture(CompletableFuture<BaseNetworkPacket> future) {
    this.respondFuture = future;
  }

}
