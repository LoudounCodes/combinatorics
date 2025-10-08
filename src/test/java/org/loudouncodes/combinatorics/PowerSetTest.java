package org.loudouncodes.combinatorics;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class PowerSetTest {

  @Test
  @DisplayName("Invalid arguments throw IllegalArgumentException")
  void invalidArgs() {
    assertThrows(IllegalArgumentException.class, () -> PowerSet.of(-1));
  }

  @Test
  @DisplayName("Edge cases: n=0 yields one empty subset; n=1 yields two subsets")
  void edgeCases() {
    List<int[]> n0 = collect(PowerSet.of(0));
    assertEquals(1, n0.size());
    assertArrayEquals(new int[0], n0.get(0));

    List<int[]> n1 = collect(PowerSet.of(1));
    assertEquals(2, n1.size());
    assertArrayEquals(new int[0], n1.get(0));      // k=0
    assertArrayEquals(new int[] {0}, n1.get(1));   // k=1
  }

  @Test
  @DisplayName("Count equals 2^n for a few n")
  void countMatchesTwoToTheN() {
    assertEquals(BigInteger.ONE.shiftLeft(0), PowerSet.of(0).count());
    assertEquals(BigInteger.ONE.shiftLeft(1), PowerSet.of(1).count());
    assertEquals(BigInteger.ONE.shiftLeft(5), PowerSet.of(5).count());
    assertEquals(BigInteger.ONE.shiftLeft(10), PowerSet.of(10).count());
  }

  @Test
  @DisplayName("Iterator respects hasNext()/next() and throws on exhaustion")
  void iteratorContract() {
    Iterator<int[]> it = PowerSet.of(3).iterator();

    // Walk all 2^3 = 8 elements
    int seen = 0;
    while (it.hasNext()) {
      assertNotNull(it.next());
      seen++;
    }
    assertEquals(8, seen);

    // Exhausted iterator: hasNext false, next throws
    assertFalse(it.hasNext());
    assertThrows(NoSuchElementException.class, it::next);
  }

  @Test
  @DisplayName("Returned arrays are defensive copies (immutability) and fresh instances")
  void defensiveCopiesAndFreshInstances() {
    Iterator<int[]> it = PowerSet.of(3).iterator();
    int[] a = it.next(); // []
    int[] b = it.next(); // [0]
    int[] c = it.next(); // [1]

    // Fresh instances (no identity reuse)
    assertNotSame(a, b);
    assertNotSame(b, c);
    assertNotSame(a, c);

    // Attempt to mutate returned arrays does not affect subsequent outputs
    if (b.length > 0) b[0] = 999;
    int[] d = it.next(); // should be [2]
    assertArrayEquals(new int[] {2}, d);
  }

  @Test
  @DisplayName("Size-then-lexicographic order for n=3")
  void sizeThenLexOrder_n3() {
    List<int[]> got = collect(PowerSet.of(3));
    List<int[]> expected = List.of(
        new int[] {},          // k=0
        new int[] {0},         // k=1
        new int[] {1},
        new int[] {2},
        new int[] {0,1},       // k=2
        new int[] {0,2},
        new int[] {1,2},
        new int[] {0,1,2}      // k=3
    );
    assertEquals(expected.size(), got.size(), "power set size mismatch");
    for (int i = 0; i < expected.size(); i++) {
      assertArrayEquals(expected.get(i), got.get(i), "mismatch at index " + i);
    }
  }

  @Test
  @DisplayName("No duplicates; total equals 2^n (n=5)")
  void noDuplicates_n5() {
    List<int[]> all = collect(PowerSet.of(5));
    assertEquals(1 << 5, all.size());

    Set<String> seen = new HashSet<>();
    for (int[] s : all) {
      String key = toKey(s);
      assertTrue(seen.add(key), "duplicate subset encountered: " + key);
    }
  }

  // --------------------------------------------------------------------------
  // Helpers
  // --------------------------------------------------------------------------

  private static List<int[]> collect(Iterable<int[]> it) {
    List<int[]> out = new ArrayList<>();
    for (int[] a : it) out.add(a);
    return out;
  }

  private static String toKey(int[] a) {
    StringBuilder sb = new StringBuilder();
    sb.append('[');
    for (int i = 0; i < a.length; i++) {
      if (i > 0) sb.append(',');
      sb.append(a[i]);
    }
    sb.append(']');
    return sb.toString();
  }
}
