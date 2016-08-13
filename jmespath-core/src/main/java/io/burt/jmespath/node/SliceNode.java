package io.burt.jmespath.node;

import java.util.List;
import java.util.LinkedList;
import java.util.Collections;

import io.burt.jmespath.Adapter;

public class SliceNode<T> extends Node<T> {
  private final Integer start;
  private final Integer stop;
  private final Integer step;

  public SliceNode(Adapter<T> runtime, Integer start, Integer stop, Integer step, Node<T> source) {
    super(runtime, source);
    this.start = start;
    this.stop = stop;
    this.step = step;
  }

  @Override
  public T searchOne(T projectionElement) {
    List<T> elements = runtime.toList(projectionElement);
    List<T> output = new LinkedList<>();
    int i;
    int n;
    if (start == null) {
      i = step > 0 ? 0 : elements.size();
    } else if (start < 0) {
      i = elements.size() + start;
    } else {
      i = start;
    }
    if (stop == null) {
      n = step > 0 ? elements.size() : -1;
    } else if (stop < 0) {
      n = elements.size() + stop;
    } else {
      n = Math.min(elements.size(), stop);
    }
    if (step > 0) {
      for ( ; i < n; i += step) {
        output.add(elements.get(i));
      }
    } else {
      i = Math.min(i, elements.size() - 1);
      n = Math.max(n, -1);
      for ( ; i > n; i += step) {
        output.add(elements.get(i));
      }
    }
    return runtime.createArray(output);
  }

  protected Integer start() {
    return start;
  }

  protected Integer stop() {
    return stop;
  }

  protected Integer step() {
    return step;
  }

  @Override
  protected String internalToString() {
    return String.format("%d, %d, %d", start, stop, step);
  }

  @Override
  protected boolean internalEquals(Object o) {
    SliceNode other = (SliceNode) o;
    return start() == other.start() && stop() == other.stop() && step() == other.step();
  }

  @Override
  protected int internalHashCode() {
    int h = 1;
    h = h * 31 + start;
    h = h * 31 + stop;
    h = h * 31 + step;
    return h;
  }
}
