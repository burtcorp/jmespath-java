package io.burt.jmespath.ast;

import java.util.List;
import java.util.LinkedList;

import io.burt.jmespath.Adapter;

public class SliceNode extends JmesPathNode {
  private final int start;
  private final int stop;
  private final int step;

  public SliceNode(int start, int stop, int step, JmesPathNode source) {
    super(source);
    this.start = start;
    this.stop = stop;
    this.step = step;
  }

  @Override
  public <T> T evaluateOne(Adapter<T> adapter, T projectionElement) {
    List<T> elements = adapter.toList(projectionElement);
    List<T> output = new LinkedList<>();
    int i = start < 0 ? elements.size() + start : start;
    int n = stop <= 0 ? elements.size() + stop : Math.min(elements.size(), stop);
    if (step > 0) {
      for ( ; i < n; i += step) {
        output.add(elements.get(i));
      }
    } else {
      n = Math.min(elements.size() - 1, n);
      for ( ; n > i; n += step) {
        output.add(elements.get(n));
      }
    }
    return adapter.createArray(output);
  }

  protected int start() {
    return start;
  }

  protected int stop() {
    return stop;
  }

  protected int step() {
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
