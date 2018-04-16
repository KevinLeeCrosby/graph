package com.resolvity.nlcr.graph;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;

/**
 * Transitive Closure of multimap digraph.
 *
 * @author Kevin Crosby.
 */
public class TransitiveClosure<T> {
  private final Multimap<T, T> digraph;

  public TransitiveClosure(final Multimap<T, T> digraph) {
    this.digraph = HashMultimap.create(digraph);
  }

  private final Map<T, DFS<T>> dfsMap = Maps.newConcurrentMap();

  private DFS<T> getDFS(final T vertex) {
    return dfsMap.computeIfAbsent(vertex, v -> new DFS<>(digraph, v));
  }

  public boolean reachable(final T ancestor, final T descendant) {
    return getDFS(ancestor).marked(descendant);
  }

  private Multimap<T, T> closure = null;

  public Multimap<T, T> closure() {
    if (closure == null) {
      synchronized(TransitiveClosure.class) {
        if (closure == null) {
          Set<T> vertices = Sets.union(digraph.keySet(), Sets.newHashSet(digraph.values()));
          ImmutableSetMultimap.Builder<T, T> builder = ImmutableSetMultimap.builder();
          for (final T ancestor : vertices) {
            for (final T descendant : vertices) {
              if (!ancestor.equals(descendant) && reachable(ancestor, descendant)) {
                builder.put(ancestor, descendant);
              }
            }
          }
          closure = builder.build();
        }
      }
    }

    return closure;
  }
}
