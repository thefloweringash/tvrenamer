package com.google.code.tvrenamer;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import com.google.code.tvrenamer.util.RoundRobinIterable;


public class RoundRobinIterableTest {
  @Test
  public void test() {
    Iterable<String> s1 = Arrays.asList("abc", "def", "ghi");
    Iterable<String> s2 = Arrays.asList("1", "2");

    // stringly typed, oops
    StringBuilder sb = new StringBuilder();
    for (String s : new RoundRobinIterable<String>(Arrays.asList(s1, s2))) {
      sb.append(s);
    }
    assertEquals("abc1def2ghi", sb.toString());
  }
}
