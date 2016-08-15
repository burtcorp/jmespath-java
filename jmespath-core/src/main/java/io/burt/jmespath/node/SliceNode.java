package io.burt.jmespath.node;

import java.util.ArrayList;
import java.util.List;

import io.burt.jmespath.Adapter;

public class SliceNode<T> extends Node<T> {
  private final int start;
  private final int stop;
  private final int step;
  private final int limit;
  private final int rounding;

  public SliceNode(Adapter<T> runtime, Integer start, Integer stop, Integer step, Node<T> source) {
    super(runtime, source);
    this.step = (step == null) ? 1 : step;
    this.rounding = (this.step < 0) ? (this.step + 1) : (this.step - 1);
    this.limit = (this.step < 0) ? -1 : 0;
    this.start = (start == null) ? this.limit : start;
    this.stop = (stop == null) ? ((this.step < 0) ? Integer.MIN_VALUE : Integer.MAX_VALUE) : stop;
  }

  @Override
  public T searchOne(T projectionElement) {
    List<T> elements = runtime.toList(projectionElement);
    int begin = (start < 0) ? Math.max(elements.size() + start, 0) : Math.min(start, elements.size() + limit);
    int end = (stop < 0) ? Math.max(elements.size() + stop, limit) : Math.min(stop, elements.size());
    int steps = Math.max(0, (end - begin + rounding) / step);
    List<T> output = new ArrayList<>(steps);
    for (int i = 0, offset = begin; i < steps; i++, offset += step) {
      output.add(elements.get(offset));
    }
    return runtime.createArray(output);
  }

  @Override
  protected String internalToString() {
    return String.format("%d, %d, %d", start, stop, step);
  }

  @Override
  protected boolean internalEquals(Object o) {
    SliceNode other = (SliceNode) o;
    return start == other.start && stop == other.stop && step == other.step;
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
