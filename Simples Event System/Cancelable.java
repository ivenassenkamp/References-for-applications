package me.mypvp.base.event;

public interface Cancelable {

  void setCanceled(boolean canceled);
  
  boolean isCanceled();
  
}
