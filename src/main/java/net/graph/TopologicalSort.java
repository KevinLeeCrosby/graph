package net.graph;

import com.google.common.base.Joiner;
import com.google.common.collect.Multimap;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;

import java.util.Deque;
import java.util.Iterator;
import java.util.Set;

/**
 * Topological Sort of multimap Directed Acyclic Graph.
 *
 * @author Kevin Crosby.
 */
public class TopologicalSort<T> implements Iterable<T> {
  private final Set<T> marked;
  private final Deque<T> order;

  public TopologicalSort(final Multimap<T, T> dag) {
    Cycle<T> finder = new Cycle<>(dag);
    if (finder.hasCycle()) {
      throw new IllegalArgumentException(String.format("Digraph has a cycle: \n%s", Joiner.on(" -> ").join(finder.cycle())));
    }
    order = Queues.newArrayDeque();
    marked = Sets.newHashSet();
    Set<T> open = Sets.newHashSet();
    open.addAll(dag.keySet());
    open.addAll(dag.values());
    for (final T vertex : open) {
      if (!marked.contains(vertex)) {
        dfs(dag, vertex);
      }
    }
  }

  private void dfs(final Multimap<T, T> dag, final T vertex) {
    marked.add(vertex);
    for (final T progeny : dag.get(vertex)) {
      if (!marked.contains(progeny)) {
        dfs(dag, progeny);
      }
    }
    order.push(vertex);
  }

  @Override
  public Iterator<T> iterator() {
    return order.iterator();
  }
}

