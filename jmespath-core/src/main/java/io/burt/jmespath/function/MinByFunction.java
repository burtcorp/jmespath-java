package io.burt.jmespath.function;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.node.JmesPathNode;

public class MinByFunction extends CompareByFunction {
  public MinByFunction() {
    super(-1);
  }
}
