package me.mypvp.base.core.probability;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Spliterator;

import org.jetbrains.annotations.NotNull;

/**
 * A {@link ProbabilityTable} extended with element chance calculation.
 * It contains {@link ProbabilityEntry} with element data
 *
 * @param <T> the type of all elements
 */
public interface ProbabilityTable<T> extends Iterable<ProbabilityEntry<T>> {

  /**
   * Creates a new {@link ProbabilityTable} with no entries.
   *
   * @param <T> the type of alle elements
   *
   * @return a new empty {@link ProbabilityTable}
   */
  public static <T> ProbabilityTable<T> create() {
    return new DefaultProbabilityTable<>(new ArrayList<>());
  }

  /**
   * Creates a new {@link ProbabilityTable}.
   *
   * @param <T> the type of alle elements
   * @param entries with which the table should be created
   *
   * @return a new {@link ProbabilityTable}
   */
  public static <T> ProbabilityTable<T> create(@NotNull List<ProbabilityEntry<T>> entries) {
    return new DefaultProbabilityTable<>(entries);
  }

  /**
   * Creates a new {@link ProbabilityTable}.
   *
   * @param <T> the type of alle elements
   * @param entries with which the table should be created
   *
   * @return a new {@link ProbabilityTable}
   */
  @SuppressWarnings("unchecked")
  public static <T> ProbabilityTable<T> create(@NotNull ProbabilityEntry<T>... entries) {
    return new DefaultProbabilityTable<>(Arrays.asList(entries));
  }

  /**
   * Creates a new {@link ProbabilityTable}
   *
   * @param <T> the type of all elements
   * @param table from which the elements should be copied
   *
   * @return a copy of the specified table
   */
  public static <T> ProbabilityTable<T> copy(@NotNull ProbabilityTable<T> table) {
    return new DefaultProbabilityTable<>(table.entries());
  }

  /**
   * Returns a unmodifiable collection of entries
   *
   * @return the collection of entries
   */
  public @NotNull List<ProbabilityEntry<T>> entries();

  /**
   * Add a entry to list
   *
   * @param entry - the entry
   */
  public void add(@NotNull ProbabilityEntry<T> entry);

  /**
   * Add a entry to list
   *
   * @param element - the element of entry
   * @param weight  - the weight of the entry
   */
  public void add(@NotNull T element, int weight);

  /**
   * Remove a entry
   *
   * @param entry - the entry
   */
  public void remove(ProbabilityEntry<T> entry);

  /**
   * Remove all elements of entries
   *
   * @param element - the element
   */
  public void removeAllElements(@NotNull T element);

  /**
   * Returns only list of elements
   *
   * @return the list
   */
  public @NotNull Collection<T> elements();

  /**
   * @return a non-modifiable iterator of the entries
   */
  @NotNull
  @Override
  public Iterator<ProbabilityEntry<T>> iterator();

  /**
   * @return a non-modifiable spliterator of the entries
   */
  @Override
  public Spliterator<ProbabilityEntry<T>> spliterator();

  /**
   * @return the total weight of the table
   */
  public int getTotalWeight();

  /**
   * Gets random element of entries.
   * The element is randomly from this probability from the entry data.
   *
   * @return the random element of entry
   *
   * @throws IllegalStateException if the table is empty
   */
  public @NotNull T getRandomElement();

  /**
   * Gets random element of entries.
   * The element is randomly from this probability from the entry data.
   *
   * @param random - custom random
   *
   * @return the random element of entry
   *
   * @throws IllegalStateException if the table is empty
   */
  public @NotNull T getRandomElement(Random random);
}
