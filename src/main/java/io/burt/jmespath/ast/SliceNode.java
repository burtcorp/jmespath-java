package io.burt.jmespath.ast;

public class SliceNode extends JmesPathNode {
  private final int start;
  private final int stop;
  private final int step;

  public SliceNode(int start, int stop, int step) {
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
  public String toString() {
    return String.format("SliceNode(%d, %d, %d)", start, stop, step);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SliceNode)) {
      return false;
    }
    SliceNode other = (SliceNode) o;
    return this.start() == other.start() && this.stop() == other.stop() && this.step() == other.step();
  }

  @Override
  public int hashCode() {
    int h = 1;
    h = h * 31 + start;
    h = h * 31 + stop;
    h = h * 31 + step;
    return h;
  }
}
