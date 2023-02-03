package me.mypvp.base.event;

public class DefaultCancelableEvent extends DefaultEvent implements Cancelable {

  private boolean canceled;
  
  @Override
  public void setCanceled(boolean canceled) {
    this.canceled = canceled;
  }

  @Override
  public boolean isCanceled() {
    return canceled;
  }

}
