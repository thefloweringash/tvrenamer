package com.google.code.tvrenamer.util;

import java.util.Iterator;

public class MapIterable<T,Y> implements Iterable<Y> {
  public interface Map<T,Y> {
    public Y map(T o);
  }

  private final Iterable<T> inner;
  private final Map<T,Y> f;

  public MapIterable(Iterable<T> inner, Map<T,Y> f) {
    this.inner = inner;
    this.f = f;
  }

  public Iterator<Y> iterator() {
    return new Iterator<Y>() {
      private final Iterator<T> inner = MapIterable.this.inner.iterator();

      public boolean hasNext() {
        return inner.hasNext();
      }

      public Y next() {
        return f.map(inner.next());
      }

      public void remove() {
        inner.next();
      }
    };
  }


}
