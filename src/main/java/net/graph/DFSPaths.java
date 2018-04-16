package com.resolvity.nlcr.graph;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Queues;

import java.util.Deque;
import java.util.Map;

/**
 * Depth First Search Paths on multimap digraph.
 *
 * @author Kevin Crosby.
 */
public class DFSPaths<T> {
  private final T source;

  public DFSPaths(final Multimap<T, T> digraph, final T source) {
    this.source = source;
    dfs(digraph, source);
  }

  private static class Node<T> {
    private final T name;
    private boolean marked = false;
    private T edgeTo = null;

    private Node(final T name) {
      this.name = name;
    }

    private boolean isMarked() {
      return marked;
    }

    private void setMarked() {
      this.marked = true;
    }

    private T getEdgeTo() {
      return edgeTo;
    }

    private void setEdgeTo(T edgeTo) {
      this.edgeTo = edgeTo;
    }

    @Override
    public String toString() {
      return String.format("[%s <- %s, %s]", name, edgeTo, marked);
    }
  }

  private final Map<T, Node<T>> nodeMap = Maps.newConcurrentMap();

  private Node<T> getNode(final T name) {
    return nodeMap.computeIfAbsent(name, Node::new);
  }

  private void dfs(final Multimap<T, T> digraph, final T parent) {
    getNode(parent).setMarked();
    for (final T child : digraph.get(parent)) {
      if (!hasPathTo(child)) {
        getNode(child).setEdgeTo(parent);
        dfs(digraph, child);
      }
    }
  }

  public boolean hasPathTo(final T destination) {
    return getNode(destination).isMarked();
  }

  public Iterable<T> pathTo(final T destination) {
    Deque<T> path = Queues.newArrayDeque();
    if (!hasPathTo(destination)) {
      return path;
    }
    for (T x = destination; x != source; x = getNode(x).getEdgeTo()) {
      path.push(x);
    }
    path.push(source);

    return ImmutableList.copyOf(path);
  }
}
