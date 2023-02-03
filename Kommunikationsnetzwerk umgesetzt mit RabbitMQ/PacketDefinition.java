package me.mypvp.base.network;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.rabbitmq.client.BuiltinExchangeType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PacketDefinition {

  String packetGroup() default "default";

  String packetName();

  @Deprecated
  boolean exchange() default true;

  @Deprecated
  boolean purge() default true;

  @Deprecated
  boolean autoDelete() default false;

  @Deprecated
  boolean exclusive() default false;

  @Deprecated
  boolean durable() default false;

  BuiltinExchangeType exchangeType() default BuiltinExchangeType.FANOUT;

}
