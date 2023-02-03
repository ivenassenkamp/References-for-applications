package me.mypvp.base.core.probability;

import org.jetbrains.annotations.NotNull;

public class DefaultProbabilityEntry<T> implements ProbabilityEntry<T> {

  private final int weight;
  private final T element;

  protected DefaultProbabilityEntry(@NotNull T element, int weight) {
    this.weight = weight;
    this.element = element;
  }

  @Override
  public int getWeight() {
    return this.weight;
  }

  @Override
  public @NotNull T getElement() {
    return element;
  }

  @Override
  public int compareTo(@NotNull ProbabilityEntry<T> o) {
    return Double.compare(this.getWeight(), o.getWeight());
  }
}
