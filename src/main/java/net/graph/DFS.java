package net.graph;

import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Depth First Search on multimap digraph.
 *
 * @author Kevin Crosby.
 */
public class DFS<T> {
  private final Set<T> marked;
  private int count;

  public DFS(final Multimap<T, T> digraph, final T source) {
    count = 0;
    marked = Sets.newHashSet();
    dfs(digraph, source);
  }

  private void dfs(final Multimap<T, T> digraph, final T parent) {
    count++;
    marked.add(parent);
    for (final T child : digraph.get(parent)) {
      if (!marked(child)) {
        dfs(digraph, child);
      }
    }
  }

  public boolean marked(final T destination) {
    return marked.contains(destination);
  }

  // number of vertices reachable from a source vertex
  public int count() {
    return count;
  }
}
