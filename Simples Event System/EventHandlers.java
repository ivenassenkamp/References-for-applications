package me.mypvp.base.event;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EventHandlers {

  private static final Logger LOGGER = LogManager.getLogger();
  private final Logger logger = LOGGER;

  private final Class<?> eventClass;
  private final List<Handler> handlers = new ArrayList<>();

  public EventHandlers(Class<?> eventClass) {
    this.eventClass = eventClass;
  }

  public void registerHandler(MethodHandle methodHandle,
      EventPriority eventPriority, boolean ignoreCanceled,
      Object listener) {
    handlers.add(new Handler(methodHandle, eventPriority, ignoreCanceled,
        listener));
    handlers.sort((handler1, handler2) -> {
      return Integer.compare(handler2.getEventPriority().ordinal(),
          handler1.getEventPriority().ordinal());
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

  public void handle(Event event) {
    if (event instanceof Cancelable) {
      handleCancelable((Cancelable) event);
      return;
    }

    EventExecutionException exception = null;

    for (Handler handler : handlers) {
      try {
        handler.getMethodHandle().invoke(handler.getListener(), event);
      } catch (Throwable throwable) {
        if (exception == null) {
          exception = new EventExecutionException();
        }
        exception.addSuppressed(throwable);
      }
    }

    if (exception != null) {
      throw exception;
    }
  }

  private void handleCancelable(Cancelable cancelable) {
    EventExecutionException exception = null;

    for (Handler handler : handlers) {
      try {
        if (!cancelable.isCanceled() || handler.isIgnoreCanceled()) {
          handler.getMethodHandle().invoke(handler.getListener(), cancelable);
        }
      } catch (Throwable throwable) {
        if (exception == null) {
          exception = new EventExecutionException(throwable);
        } else {
          exception.addSuppressed(throwable);
        }
      }
    }

    if (exception != null) {
      logger.error("One or more exceptions occurred while executing "
          + "the handler of the {} event", eventClass.getSimpleName(),
          exception);
    }
  }

  public Class<?> getEventClass() {
    return eventClass;
  }

  public List<Handler> getHandlers() {
    return handlers;
  }

  private static class Handler {

    private final MethodHandle methodHandle;
    private final EventPriority eventPriority;
    private final boolean ignoreCanceled;
    private final Object listener;

    public Handler(MethodHandle methodHandle, EventPriority eventPriority,
        boolean ignoreCanceled, Object listener) {
      this.methodHandle = methodHandle;
      this.eventPriority = eventPriority;
      this.ignoreCanceled = ignoreCanceled;
      this.listener = listener;
    }

    public MethodHandle getMethodHandle() {
      return methodHandle;
    }

    public EventPriority getEventPriority() {
      return eventPriority;
    }

    public boolean isIgnoreCanceled() {
      return ignoreCanceled;
    }

    public Object getListener() {
      return listener;
    }

  }

}
