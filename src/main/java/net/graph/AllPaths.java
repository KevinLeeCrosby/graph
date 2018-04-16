package com.resolvity.nlcr.graph;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Deque;
import java.util.Set;

/**
 * All Paths from Source to Sink on multimap DAG.
 *
 * @author Kevin Crosby.
 */
public class AllPaths<T> {
  private final ImmutableList.Builder<Iterable<T>> paths;
  private final Set<T> destinations;

  public AllPaths(final Multimap<T, T> dag, final Collection<T> sources, final Collection<T> destinations) {
    Cycle<T> finder = new Cycle<>(dag);
    if (finder.hasCycle()) {
      throw new IllegalArgumentException(String.format("Digraph has a cycle: \n%s", Joiner.on(" -> ").join(finder.cycle())));
    }
    this.destinations = ImmutableSet.copyOf(destinations);
    this.paths = ImmutableList.builder();
    for (final T source : sources) {
      dfs(dag, source);
    }
  }

  public AllPaths(final Multimap<T, T> dag, final T source, final T destination) {
    this(dag, Lists.newArrayList(source), Lists.newArrayList(destination));
  }

  public AllPaths(final Multimap<T, T> dag, final T source) {
    this(dag, Lists.newArrayList(source), getSinks(dag));
  }

  public AllPaths(final Multimap<T, T> dag) {
    this(dag, getSources(dag), getSinks(dag));
  }

  public static <T> Collection<T> getSources(final Multimap<T, T> dag) {
    return getSinks(Multimaps.invertFrom(dag, LinkedHashMultimap.create()));
  }

  public static <T> Collection<T> getSinks(final Multimap<T, T> dag) {
    Set<T> sinks = Sets.newLinkedHashSet();
    for (final T parent : new TopologicalSort<>(dag)) {
      if (!dag.containsKey(parent)) { // i.e. if parent is barren
        sinks.add(parent);
      }
    }
    return sinks;
  }

  public static <T> long countPaths(final Multimap<T, T> dag, final Collection<T> sources, final Collection<T> destinations) {
    Multiset<T> counter = HashMultiset.create();

    counter.addAll(sources);
    for (final T parent : new TopologicalSort<>(dag)) {
      for (final T child : dag.get(parent)) {
        counter.add(child, counter.count(parent));
      }
    }
    return destinations.stream()
        .mapToLong(counter::count)
        .sum();
  }

  public static <T> long countPaths(final Multimap<T, T> dag, final T source, final T destination) {
    return countPaths(dag, Lists.newArrayList(source), Lists.newArrayList(destination));
  }

  public static <T> long countPaths(final Multimap<T, T> dag) {
    return countPaths(dag, getSources(dag), getSinks(dag));
  }

  private void dfs(final Multimap<T, T> dag, final T source) {
    dfs(dag, source, Queues.newArrayDeque());
  }

  private void dfs(final Multimap<T, T> dag, final T parent, final Deque<T> path) {
    path.add(parent);
    if (destinations.contains(parent)) { // i.e. if parent is a destination
      paths.add(ImmutableList.copyOf(path));
    } else {
      Collection<T> children = dag.get(parent);
      for (final T child : children) {
        dfs(dag, child, path);
      }
    }
    path.removeLast();
  }

  public Iterable<Iterable<T>> paths() {
    return paths.build();
  }
}
