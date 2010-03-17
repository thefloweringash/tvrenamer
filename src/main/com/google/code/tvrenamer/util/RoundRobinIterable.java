package com.google.code.tvrenamer.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RoundRobinIterable<T> implements Iterable<T> {

  private final List<Iterable<T>> iterables;

  public RoundRobinIterable(List<Iterable<T>> ts) {
    iterables = ts;
  }

  public Iterator<T> iterator() {
    ArrayList<Iterator<T>> iterators = new ArrayList<Iterator<T>>();
    for (Iterable<T> iterable : iterables) {
      iterators.add(iterable.iterator());
    }
    return new RoundRobinIterator(iterators);
  }

  class RoundRobinIterator implements Iterator<T> {
    private final List<Iterator<T>> iterators;

    public RoundRobinIterator(List<Iterator<T>> ts) {
      iterators = ts;
    }

    private int current;

    private void stepCurrent() {
      current = (current + 1) % iterables.size();
    }

    public boolean hasNext() {
      return iterators.get(current).hasNext();
    }

    public T next() {
      T ret = iterators.get(current).next();
      stepCurrent();
      return ret;
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }

  }

}
