package me.mypvp.base.core.probability;

import org.jetbrains.annotations.NotNull;

/**
 * The type Probability entry.
 *
 * @param <T> the type parameter
 */
public interface ProbabilityEntry<T> extends Comparable<ProbabilityEntry<T>>{

  /**
   * Creates a new {@link ProbabilityEntry}.
   *
   * @param <T>     the type parameter
   * @param element the element
   * @param weight  the weight
   *
   * @return a new {@link ProbabilityEntry} with the given parameters
   */
  public static <T> ProbabilityEntry<T> create(T element, int weight) {
    return new DefaultProbabilityEntry<>(element, weight);
  }

  /**
   * Gets the weight.
   *
   * @return the weight
   */
  public int getWeight();

  /**
   * Gets element.
   *
   * @return the element
   */
  @NotNull
  public T getElement();

}
