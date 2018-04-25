package net.graph;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Deque;
import java.util.Map;
import java.util.Set;

/**
 * Cycle Detector for multimap digraph.
 *
 * @author Kevin Crosby.
 */
public class Cycle<T> {
  private final Deque<T> cycle;

  public Cycle(final Multimap<T, T> digraph) {
    cycle = Queues.newArrayDeque();
    Set<T> vertices = Sets.union(digraph.keySet(), Sets.newHashSet(digraph.values()));
    for (final T vertex : vertices) {
      if (!getNode(vertex).isMarked()) {
        dfs(digraph, vertex);
      }
    }
  }

  private static class Node<T> {
    private final T name;
    private boolean marked = false;
    private boolean onStack = false;
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

    private boolean isOnStack() {
      return onStack;
    }

    private void setOnStack() {
      this.onStack = true;
    }

    private void clearOnStack() {
      this.onStack = false;
    }

    private T getEdgeTo() {
      return edgeTo;
    }

    private void setEdgeTo(T edgeTo) {
      this.edgeTo = edgeTo;
    }

    @Override
    public String toString() {
      return String.format("[%s <- %s, %s, %s]", name, edgeTo, onStack, marked);
    }
  }

  private final Map<T, Node<T>> nodeMap = Maps.newConcurrentMap();

  private Node<T> getNode(final T name) {
    return nodeMap.computeIfAbsent(name, Node::new);
  }

  private void dfs(final Multimap<T, T> digraph, T parent) {
    Node<T> parentNode = getNode(parent);
    parentNode.setOnStack();
    parentNode.setMarked();
    for (final T child : children(digraph, parent)) {
      if (!cycle.isEmpty()) {
        return;
      } else {
        Node<T> childNode = getNode(child);
        if (!childNode.isMarked()) {
          childNode.setEdgeTo(parent);
          dfs(digraph, child);
        } else if (childNode.isOnStack()) {
          for (T inbred = parent; inbred != child; inbred = getNode(inbred).getEdgeTo()) {
            cycle.push(inbred);
          }
          cycle.push(child);
          cycle.push(parent);
        }
      }
    }

    parentNode.clearOnStack();
  }

  private Collection<T> children(final Multimap<T, T> digraph, final T parent) {
    return digraph.get(parent);
  }

  public boolean hasCycle() {
    return !cycle.isEmpty();
  }

  public Iterable<T> cycle() {
    return ImmutableList.copyOf(cycle);
  }
}
