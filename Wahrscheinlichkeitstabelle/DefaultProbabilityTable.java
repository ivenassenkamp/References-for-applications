package me.mypvp.base.core.probability;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Spliterator;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

public class DefaultProbabilityTable<T> implements ProbabilityTable<T> {

  private final List<ProbabilityEntry<T>> entries;
  private int totalWeight;

  protected DefaultProbabilityTable(@NotNull List<ProbabilityEntry<T>> entries) {
    this.entries = new ArrayList<>(entries);
    for (ProbabilityEntry<T> entry : entries) {
      totalWeight += entry.getWeight();
    }
  }

  @Override
  public @NotNull List<ProbabilityEntry<T>> entries() {
    return Collections.unmodifiableList(entries);
  }

  @Override
  public void add(@NotNull ProbabilityEntry<T> entry) {
    entries.add(entry);
    totalWeight += entry.getWeight();
    entries.sort(Comparator.comparing(ProbabilityEntry::getWeight));
  }

  @Override
  public void add(@NotNull T element, int weight) {
    add(ProbabilityEntry.create(element, weight));
  }

  @Override
  public void remove(ProbabilityEntry<T> entry) {
    if (entries.remove(entry)) {
      totalWeight -= entry.getWeight();
    }
  }

  @Override
  public void removeAllElements(@NotNull T element) {
    Iterator<ProbabilityEntry<T>> iterator = entries.iterator();
    while (iterator.hasNext()) {
      ProbabilityEntry<T> entry = iterator.next();
      if (entry.getElement().equals(element)) {
        iterator.remove();
        totalWeight -= entry.getWeight();
      }
    }
  }

  @Override
  public @NotNull Collection<T> elements() {
    List<T> list = new ArrayList<>();
    for (ProbabilityEntry<T> entry : entries) {
      list.add(entry.getElement());
    }
    return list;
  }

  @Override
  public @NotNull Iterator<ProbabilityEntry<T>> iterator() {
    return entries().iterator();
  }

  @Override
  public void forEach(Consumer<? super ProbabilityEntry<T>> action) {
    entries().forEach(action);
  }

  @Override
  public Spliterator<ProbabilityEntry<T>> spliterator() {
    return entries().spliterator();
  }

  @Override
  public int getTotalWeight() {
    return this.totalWeight;
  }

  @Override
  public @NotNull T getRandomElement() {
    return getRandomElement(ThreadLocalRandom.current());
  }

  @Override
  public @NotNull T getRandomElement(Random random) {
    if (entries.isEmpty()) {
      throw new IllegalStateException("The table is empty");
    }

    int i = 0;
    for (double r = random.nextDouble() * totalWeight; i < (entries.size() - 1); ++i) {
      ProbabilityEntry<T> entry = entries.get(i);
      r -= entry.getWeight();
      if (r <= 0.0) return entry.getElement();
    }
    return entries.get(i).getElement();
  }

}
