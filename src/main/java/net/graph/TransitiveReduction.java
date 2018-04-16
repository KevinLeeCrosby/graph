package com.resolvity.nlcr.graph;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;

/**
 * Transitive Reduction of multimap digraph.
 *
 * @author Kevin Crosby.
 */
public class TransitiveReduction<T> {
  private final Multimap<T, T> digraph;

  public TransitiveReduction(final Multimap<T, T> digraph) {
    this.digraph = HashMultimap.create(digraph);
  }

  private final Map<T, DFS<T>> dfsMap = Maps.newConcurrentMap();

  private DFS<T> getDFS(final T vertex) {
    return dfsMap.computeIfAbsent(vertex, v -> new DFS<>(digraph, v));
  }

  public boolean reachable(final T ancestor, final T descendant) {
    return getDFS(ancestor).marked(descendant);
  }

  private Multimap<T, T> reduction = null;

  public Multimap<T, T> reduction() {  // TODO make DAG version that works in O(n) possibly based on TopologicalSort
    if (reduction == null) {
      synchronized (TransitiveReduction.class) {
        if (reduction == null) {
          Set<T> vertices = Sets.union(digraph.keySet(), Sets.newHashSet(digraph.values()));
          for (final T lineage : vertices) {
            for (final T ancestor : vertices) {
              if (!ancestor.equals(lineage) && reachable(ancestor, lineage)) {
                for (final T descendant : vertices) {
                  if (!lineage.equals(descendant) && reachable(lineage, descendant)) {
                    digraph.remove(ancestor, descendant);
                  }
                }
              }
            }
          }
          reduction = ImmutableSetMultimap.copyOf(digraph);
        }
      }
    }

    return reduction;
  }
}
