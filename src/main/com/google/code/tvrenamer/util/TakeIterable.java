package com.google.code.tvrenamer.util;

import java.util.Iterator;

public class TakeIterable<T> implements Iterable<T> {

  private final int limit;
  private final Iterable<T> inner;

  public TakeIterable(Iterable<T> inner, int limit) {
    this.limit = limit;
    this.inner = inner;
  }

  public Iterator<T> iterator() {

    return new Iterator<T>() {

      private int count;
      private final Iterator<T> inner = TakeIterable.this.inner.iterator();

      public boolean hasNext() {
        return inner.hasNext() && count < limit;
      }

      public T next() {
        count++;
        return inner.next();
      }

      public void remove() {
        inner.remove();
      }
    };
  }


}
