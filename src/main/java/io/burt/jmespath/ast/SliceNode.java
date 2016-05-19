package io.burt.jmespath.ast;

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
