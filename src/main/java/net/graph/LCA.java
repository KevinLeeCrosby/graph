package com.resolvity.nlcr.graph;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Least Common Ancestor on multimap digraph.
 *
 * @author Kevin Crosby.
 */
public class LCA<T> {
  private static final int INFINITY = Integer.MAX_VALUE;

  private final Multimap<T, T> digraph;
  private final Set<T> vertices;

  public LCA(final Multimap<T, T> digraph) {
    this.digraph = HashMultimap.create(digraph);
    this.vertices = Sets.union(this.digraph.keySet(), Sets.newHashSet(this.digraph.values()));
  }

  private final Map<T, BFS<T>> bfsMap = Maps.newConcurrentMap();

  private BFS<T> getBFS(final T vertex) {
    return bfsMap.computeIfAbsent(vertex, v -> new BFS<>(digraph, v));
  }

  public int length(final T v, final T w) {
    BFS<T> bfsV = getBFS(v);
    BFS<T> bfsW = getBFS(w);

    int minimumLength = INFINITY;
    for (final T vertex : vertices) {
      if (bfsV.hasPathTo(vertex) && bfsW.hasPathTo(vertex)) {
        int length = bfsV.distTo(vertex) + bfsW.distTo(vertex);
        if (minimumLength > length) {
          minimumLength = length;
        }
      }
    }
    if (minimumLength == INFINITY) {
      return -1;
    }
    return minimumLength;
  }

  public T ancestor(final T v, final T w) {
    BFS<T> bfsV = getBFS(v);
    BFS<T> bfsW = getBFS(w);

    int minimumLength = INFINITY;
    T ancestor = null;
    for (final T vertex : vertices) {
      if (bfsV.hasPathTo(vertex) && bfsW.hasPathTo(vertex)) {
        int length = bfsV.distTo(vertex) + bfsW.distTo(vertex);
        if (minimumLength > length) {
          minimumLength = length;
          ancestor = vertex;
        }
      }
    }
    return ancestor;
  }

  public T ancestor(final List<T> vertices) {
    int n = vertices.size();
    switch (n) {
      case 0:
        return null;
      case 1:
        return vertices.get(0);
      case 2:
        return ancestor(vertices.get(0), vertices.get(1));
      default:
        return ancestor(ancestor(vertices.subList(0, n / 2)), ancestor(vertices.subList(n / 2, n)));
    }
  }

  public int distTo(final T descendant, final T ancestor) {
    return getBFS(descendant).distTo(ancestor);
  }

  public boolean hasPathTo(final T descendant, final T ancestor) {
    return getBFS(descendant).hasPathTo(ancestor);
  }

  public Iterable<T> pathTo(final T descendant, final T ancestor) {
    return getBFS(descendant).pathTo(ancestor);
  }
}
