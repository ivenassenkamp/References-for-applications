package me.mypvp.base.network;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.mypvp.base.network.packets.BaseNetworkPacket;

public class PacketHandlers<T extends BaseNetworkPacket> {

  private static final Logger LOGGER = LogManager.getLogger();

  public static <T extends BaseNetworkPacket> PacketHandlers<T> create(Class<T> packetClass) {
    return new PacketHandlers<>(packetClass);
  }

  private final List<Handler> handlers = new ArrayList<>();
  private final Class<T> packetClass;

  public PacketHandlers(Class<T> packetClass) {
    this.packetClass = packetClass;
  }

  public void registerHandler(MethodHandle methodHandle,
      HandlerPriority handlerPriority, Object listener) {
    handlers.add(new Handler(methodHandle, handlerPriority, listener));
    handlers.sort((handler1, handler2) -> {
      return Integer.compare(handler2.getHandlerPriority().ordinal(),
          handler1.getHandlerPriority().ordinal());
    });
  }

  public void removeHandlers(Object listener) {
    Iterator<Handler> handlersItr = handlers.iterator();
    while (handlersItr.hasNext()) {
      Handler handler = handlersItr.next();
      if (handler.getListener() == listener) {
        handlersItr.remove();
      }
    }
  }

  public void removeHandlers(Class<?> listenerClass) {
    Iterator<Handler> handlersItr = handlers.iterator();
    while (handlersItr.hasNext()) {
      Handler handler = handlersItr.next();
      if (handler.getListener().getClass() == listenerClass) {
        handlersItr.remove();
      }
    }
  }

  public void handle(T packet) {
    for (Handler handler : handlers) {
      try {
        handler.getMethodHandle().invoke(handler.getListener(), packet);
      } catch (Throwable e) {
        LOGGER.error("Unhandled exception at {} handler in {}",
            packetClass.getSimpleName(), handler.getClass(), e);
      }
    }
  }

  public List<Handler> getHandlers() {
    return handlers;
  }

  private static class Handler {

    private final MethodHandle methodHandle;
    private final HandlerPriority eventPriority;
    private final Object listener;

    public Handler(MethodHandle methodHandle, HandlerPriority eventPriority,
        Object listener) {
      this.methodHandle = methodHandle;
      this.eventPriority = eventPriority;
      this.listener = listener;
    }

    public MethodHandle getMethodHandle() {
      return methodHandle;
    }

    public HandlerPriority getHandlerPriority() {
      return eventPriority;
    }

    public Object getListener() {
      return listener;
    }

  }

}
