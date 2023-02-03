package me.mypvp.base.event;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventManager {

  private static final Map<Class<?>, EventHandlers> HANDLERS
      = new ConcurrentHashMap<>();

  public static void registerListener(Object listener) {
    Class<?> listenerClass = listener.getClass();
    Method[] methods = listenerClass.getMethods();

    A: for (Method method : methods) {
      EventHandler annotation = method
          .getDeclaredAnnotation(EventHandler.class);

      if (annotation == null) {
        continue;
      }

      if (method.getParameterCount() != 1) {
        throw new IllegalArgumentException("The " + method.getName()
            + " method expects more than one parameter");
      }

      Class<?> parameter = method.getParameterTypes()[0];
      Class<?> assignableExpectation = Event.class;
      Class<?> superclass = parameter.getSuperclass();

      while(superclass != null) {
        for (Class<?> interfaceClass : superclass.getInterfaces()) {
          if (interfaceClass.equals(assignableExpectation)) {
            try {
              MethodHandle handle = MethodHandles.lookup().unreflect(method);
              HANDLERS.computeIfAbsent(parameter,
                  clazz -> new EventHandlers(parameter))
                      .registerHandler(handle, annotation.priority(),
                          annotation.ignoreCanceled(), listener);
              continue A;
            } catch (IllegalAccessException e) {
              throw new IllegalArgumentException("The specified listener "
                  + "is not accessible", e);
            }
          }
        }

        if (superclass.getSuperclass().equals(Object.class)) {
          superclass = null;
        } else {
          superclass = superclass.getSuperclass();
        }
      }

      throw new IllegalArgumentException("The "
          + listenerClass.getSimpleName() + " class contains "
          + method.getName() + " that expects as parameter "
          + parameter.getSimpleName() + " that does not implement "
          + assignableExpectation.getSimpleName());
    }
  }

  public static void unregisterListener(Object listener) {
    for (EventHandlers handlers : HANDLERS.values()) {
      handlers.removeHandlers(listener);
    }
  }

  public static void call(Event event) {
    EventHandlers handlers = HANDLERS.get(event.getClass());

    if (handlers == null) {
      return;
    }

    handlers.handle(event);
  }

}
