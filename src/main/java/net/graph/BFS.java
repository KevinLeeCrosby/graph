package com.resolvity.nlcr.graph;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Queues;

import java.util.Collection;
import java.util.Deque;
import java.util.Map;
import java.util.Queue;

/**
 * Breadth First Search on multimap digraph.
 *
 * @author Kevin Crosby.
 */
public class BFS<T> {
  private static final int INFINITY = Integer.MAX_VALUE;

  public BFS(final Multimap<T, T> digraph, final T source) {
    this(digraph, Lists.newArrayList(source));
  }

  public BFS(final Multimap<T, T> digraph, final Collection<T> sources) {
    bfs(digraph, sources);
  }

  private static class Node<T> {
    private final T name;
    private boolean marked = false;
    private T edgeTo = null;
    private int distTo = INFINITY;

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

    private int getDistTo() {
      return distTo;
    }

    private void setDistTo(int distTo) {
      this.distTo = distTo;
    }

    @Override
    public String toString() {
      return String.format("[%s <- %s, %d, %s]", name, edgeTo, distTo, marked);
    }
  }

  private final Map<T, Node<T>> nodeMap = Maps.newConcurrentMap();

  private Node<T> getNode(final T name) {
    return nodeMap.computeIfAbsent(name, Node::new);
  }

  private void bfs(final Multimap<T, T> digraph, final Iterable<T> parents) {
    for (final T parent : parents) {
      getNode(parent).setMarked();
      getNode(parent).setDistTo(0);
    }
    Queue<T> q = Queues.newArrayDeque(parents);
    while (!q.isEmpty()) {
      T parent = q.remove();
      for (final T child : children(digraph, parent)) {
        Node<T> node = getNode(child);
        if (!node.isMarked()) {
          node.setMarked();
          node.setEdgeTo(parent);
          node.setDistTo(getNode(parent).getDistTo() + 1);
          q.add(child);
        }
      }
    }
  }

  private Collection<T> children(final Multimap<T, T> digraph, final T parent) {
    return digraph.get(parent);
  }

  public boolean hasPathTo(final T destination) {
    return getNode(destination).isMarked();
  }

  public int distTo(final T destination) {
    return getNode(destination).getDistTo();
  }

  public Iterable<T> pathTo(final T destination) {
    Deque<T> path = Queues.newArrayDeque();
    if (!hasPathTo(destination)) {
      return path;
    }
    T x;
    for (x = destination; getNode(x).getDistTo() != 0; x = getNode(x).getEdgeTo()) {
      path.push(x);
    }
    path.push(x);

    return ImmutableList.copyOf(path);
  }
}
