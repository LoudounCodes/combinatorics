package org.loudouncodes.combinatorics;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class CombinationsTest {

  // --------------------------------------------------------------------------
  // WITHOUT REPETITION — LEXICOGRAPHIC (default)
  // --------------------------------------------------------------------------
  @Nested
  @DisplayName("Combinations without repetition (lexicographic)")
  class NoRepetitionLex {

    @Test
    @DisplayName("Invalid arguments throw IllegalArgumentException")
    void invalidArgs() {
      assertThrows(IllegalArgumentException.class, () -> Combinations.of(-1));
      assertThrows(
          IllegalArgumentException.class,
          () -> {
            // negative k
            Combinations.of(5).choose(-1).iterator().next();
          });
      assertThrows(
          IllegalArgumentException.class,
          () -> {
            // k > n invalid when no repetition
            Combinations.of(4).choose(5).iterator();
          });
    }

    @Test
    @DisplayName("Count matches known value: C(12,3) = 220")
    void countMatchesKnownValue() {
      int count = 0;
      for (int[] a : Combinations.of(12).choose(3)) count++;
      assertEquals(220, count);
    }

    @Test
    @DisplayName("Iterator respects hasNext()/next() contract and throws on exhaustion")
    void iteratorContract() {
      Iterator<int[]> it = Combinations.of(4).choose(2).iterator();
      int seen = 0;
      while (it.hasNext()) {
        assertNotNull(it.next());
        seen++;
      }
      assertEquals(nCk(4, 2), seen);
      assertFalse(it.hasNext());
      assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    @DisplayName("Edge case k=0 yields one empty combination")
    void edgeCaseKZero() {
      List<int[]> out = collect(Combinations.of(5).choose(0));
      assertEquals(1, out.size());
      assertArrayEquals(new int[0], out.get(0));
    }

    @Test
    @DisplayName("Edge case k=n yields exactly one combination [0,1,...,n-1]")
    void edgeCaseKEqualsN() {
      int n = 6;
      List<int[]> out = collect(Combinations.of(n).choose(n));
      assertEquals(1, out.size());
      int[] expected = new int[n];
      for (int i = 0; i < n; i++) expected[i] = i;
      assertArrayEquals(expected, out.get(0));
    }

    @Test
    @DisplayName("Returned arrays are defensive copies (immutability)")
    void defensiveCopies() {
      Iterator<int[]> it = Combinations.of(4).choose(2).iterator();
      int[] first = it.next(); // [0,1]
      int[] second = it.next(); // [0,2]
      assertNotSame(first, second);

      // mutate the previously returned array; should not affect future outputs
      second[0] = 999;
      int[] third = it.next(); // [0,3]
      assertArrayEquals(new int[] {0, 3}, third);
    }

    @Test
    @DisplayName("Generates lexicographic combinations for n=4, k=2")
    void lexOrderN4K2() {
      List<int[]> out = collect(Combinations.of(4).choose(2));
      List<int[]> expected =
          List.of(
              new int[] {0, 1},
              new int[] {0, 2},
              new int[] {0, 3},
              new int[] {1, 2},
              new int[] {1, 3},
              new int[] {2, 3});
      assertEquals(expected.size(), out.size());
      for (int i = 0; i < expected.size(); i++) {
        assertArrayEquals(expected.get(i), out.get(i), "mismatch at index " + i);
      }
    }
  }

  // --------------------------------------------------------------------------
  // WITHOUT REPETITION — GRAY ORDER (revolving-door style)
  // --------------------------------------------------------------------------
  @Nested
  @DisplayName("Combinations without repetition in Gray order")
  class NoRepetitionGray {

    @Test
    @DisplayName(
        "Gray order adjacency: successive k-subsets differ by swapping exactly one element")
    void grayAdjacencyProperty() {
      int n = 6, k = 3;
      Iterator<int[]> it = Combinations.of(n).choose(k).inGrayOrder().iterator();
      assertTrue(it.hasNext());
      int[] prev = it.next();
      int seen = 1;
      while (it.hasNext()) {
        int[] cur = it.next();
        // In a k-combination Gray code, symmetric difference between successive sets is 2
        assertEquals(2, symmetricDifferenceSize(prev, cur), "not Gray-adjacent");
        prev = cur;
        seen++;
      }
      assertEquals(nCk(n, k), seen);
    }

    @Test
    @DisplayName(
        "Gray and lex enumerations produce the same set of combinations (no dupes, no misses)")
    void sameCoverageAsLex() {
      int n = 7, k = 3;

      Set<String> lex = new HashSet<>();
      for (int[] a : Combinations.of(n).choose(k)) lex.add(key(a));

      Set<String> gray = new HashSet<>();
      for (int[] a : Combinations.of(n).choose(k).inGrayOrder()) gray.add(key(a));

      assertEquals(lex.size(), gray.size(), "counts differ");
      assertEquals(lex, gray, "elements differ");
    }

    @Test
    @DisplayName("Iterator respects hasNext()/next() and throws on exhaustion (Gray)")
    void iteratorContractGray() {
      Iterator<int[]> it = Combinations.of(5).choose(2).inGrayOrder().iterator();
      int seen = 0;
      while (it.hasNext()) {
        assertNotNull(it.next());
        seen++;
      }
      assertEquals(nCk(5, 2), seen);
      assertFalse(it.hasNext());
      assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    @DisplayName("Returned arrays are defensive copies (Gray)")
    void defensiveCopiesGray() {
      Iterator<int[]> it = Combinations.of(5).choose(3).inGrayOrder().iterator();
      int[] a = it.next();
      int[] b = it.next();
      assertNotSame(a, b);

      b[0] = 999;
      int[] c = it.next();
      // Only checks immutability: c must be a fresh, correct tuple of size 3
      assertEquals(3, c.length);
      for (int i = 1; i < c.length; i++) {
        assertTrue(c[i - 1] < c[i], "tuple must remain strictly increasing");
      }
    }

    @Test
    @DisplayName("Edge cases: k=0 yields one empty; k=n yields [0..n-1] (Gray)")
    void edgeCasesGray() {
      List<int[]> k0 = collect(Combinations.of(8).choose(0).inGrayOrder());
      assertEquals(1, k0.size());
      assertArrayEquals(new int[0], k0.get(0));

      int n = 5;
      List<int[]> kn = collect(Combinations.of(n).choose(n).inGrayOrder());
      assertEquals(1, kn.size());
      int[] expected = new int[n];
      for (int i = 0; i < n; i++) expected[i] = i;
      assertArrayEquals(expected, kn.get(0));
    }

    @Test
    @DisplayName("Invalid arguments still throw when requesting Gray order")
    void invalidArgsGray() {
      assertThrows(IllegalArgumentException.class, () -> Combinations.of(-3));
      assertThrows(
          IllegalArgumentException.class,
          () -> {
            Combinations.of(5).choose(-1).inGrayOrder().iterator().next();
          });
      assertThrows(
          IllegalArgumentException.class,
          () -> {
            Combinations.of(3).choose(4).inGrayOrder().iterator();
          });
    }

    @Test
    @DisplayName("Independent iterators in Gray order advance independently")
    void iteratorIndependenceGray() {
      Iterator<int[]> it1 = Combinations.of(6).choose(3).inGrayOrder().iterator();
      Iterator<int[]> it2 = Combinations.of(6).choose(3).inGrayOrder().iterator();

      // Advance it1 by a few steps
      it1.next();
      it1.next();
      it1.next();

      // it2 must still start from the beginning
      int[] start2 = it2.next();
      // basic sanity: strictly increasing tuple shape
      for (int i = 1; i < start2.length; i++) assertTrue(start2[i - 1] < start2[i]);

      int total1 = 3;
      while (it1.hasNext()) {
        it1.next();
        total1++;
      }

      int total2 = 1; // already consumed one
      while (it2.hasNext()) {
        it2.next();
        total2++;
      }

      assertEquals(nCk(6, 3), total1);
      assertEquals(nCk(6, 3), total2);
    }

    @Test
    @DisplayName("Edge case k=n (Gray): emits [0..n-1] once and exhausts")
    void grayEdgeCaseKEqualsN() {
      int n = 5;
      Iterator<int[]> it = Combinations.of(n).choose(n).inGrayOrder().iterator();
      assertTrue(it.hasNext(), "iterator should have one element");
      assertArrayEquals(new int[] {0, 1, 2, 3, 4}, it.next(), "single tuple must be [0..n-1]");
      assertFalse(it.hasNext(), "iterator should be exhausted");
      assertThrows(NoSuchElementException.class, it::next, "must throw when exhausted");
    }
  }

  // --------------------------------------------------------------------------
  // WITH REPETITION (multiset combinations) — still lexicographic
  // --------------------------------------------------------------------------
  @Nested
  @DisplayName("Combinations with repetition")
  class WithRepetition {

    @Test
    @DisplayName("Invalid arguments throw for negative k; n<0 rejected at of(n)")
    void invalidArgs() {
      assertThrows(IllegalArgumentException.class, () -> Combinations.of(-1));
      assertThrows(
          IllegalArgumentException.class,
          () -> {
            Combinations.of(3).withRepetition().choose(-2).iterator().next();
          });
    }

    @Test
    @DisplayName("Edge case: k=0 yields one empty combination (any n)")
    void edgeCaseKZero() {
      List<int[]> out = collect(Combinations.of(7).withRepetition().choose(0));
      assertEquals(1, out.size());
      assertArrayEquals(new int[0], out.get(0));
    }

    @Test
    @DisplayName("Edge case: n=0, k=0 emits one empty; n=0, k>0 emits none")
    void edgeCaseNZero() {
      List<int[]> out0 = collect(Combinations.of(0).withRepetition().choose(0));
      assertEquals(1, out0.size());
      assertArrayEquals(new int[0], out0.get(0));

      List<int[]> out1 = collect(Combinations.of(0).withRepetition().choose(3));
      assertEquals(0, out1.size());
    }

    @Test
    @DisplayName("Count matches multichoose formula: C(n+k-1, k) for n=5, k=3")
    void countMatchesMultichoose() {
      int n = 5, k = 3;
      int expected = (int) nCk(n + k - 1, k); // C(7,3)=35
      int count = 0;
      for (int[] a : Combinations.of(n).withRepetition().choose(k)) count++;
      assertEquals(expected, count);
    }

    @Test
    @DisplayName("Lexicographic order for n=3, k=2 (nondecreasing arrays)")
    void lexOrderN3K2() {
      List<int[]> out = collect(Combinations.of(3).withRepetition().choose(2));
      List<int[]> expected =
          List.of(
              new int[] {0, 0},
              new int[] {0, 1},
              new int[] {0, 2},
              new int[] {1, 1},
              new int[] {1, 2},
              new int[] {2, 2});
      assertEquals(expected.size(), out.size());
      for (int i = 0; i < expected.size(); i++) {
        assertArrayEquals(expected.get(i), out.get(i), "mismatch at index " + i);
      }
      // Invariant: nondecreasing within each tuple
      for (int[] a : out) {
        for (int i = 1; i < a.length; i++) {
          assertTrue(a[i - 1] <= a[i], "tuple not nondecreasing");
        }
      }
    }

    @Test
    @DisplayName("Iterator respects hasNext()/next() and throws on exhaustion")
    void iteratorContract() {
      Iterator<int[]> it = Combinations.of(2).withRepetition().choose(3).iterator();
      // n=2, k=3 -> C(4,3)=4 combos: [0,0,0],[0,0,1],[0,1,1],[1,1,1]
      int seen = 0;
      while (it.hasNext()) {
        assertNotNull(it.next());
        seen++;
      }
      assertEquals(4, seen);
      assertFalse(it.hasNext());
      assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    @DisplayName("Returned arrays are defensive copies (immutability) and fresh")
    void defensiveCopies() {
      Iterator<int[]> it = Combinations.of(3).withRepetition().choose(2).iterator();
      int[] a = it.next(); // [0,0]
      int[] b = it.next(); // [0,1]
      assertNotSame(a, b);

      b[0] = 999;
      int[] c = it.next(); // [0,2]
      assertArrayEquals(new int[] {0, 2}, c);
    }
  }

  // --------------------------------------------------------------------------
  // CROSS-CUTTING sanity
  // --------------------------------------------------------------------------
  @Test
  @DisplayName(
      "Independent iterators from the same spec (lexicographic) advance independently and match")
  void iteratorIndependenceLex() {
    Combinations.Spec spec = Combinations.of(6); // no repetition
    Iterator<int[]> it1 = spec.choose(2).iterator();
    Iterator<int[]> it2 = spec.choose(2).iterator();

    // Advance it1 by three
    it1.next(); // [0,1]
    it1.next(); // [0,2]
    it1.next(); // [0,3]

    // it2 should still start from the beginning
    assertArrayEquals(new int[] {0, 1}, it2.next());
    assertArrayEquals(new int[] {0, 2}, it2.next());
    assertArrayEquals(new int[] {0, 3}, it2.next());

    int total1 = 3;
    while (it1.hasNext()) {
      it1.next();
      total1++;
    }

    int total2 = 3;
    while (it2.hasNext()) {
      it2.next();
      total2++;
    }

    assertEquals(total1, total2);
    assertEquals(nCk(6, 2), total1);
  }

  @Test
  @DisplayName("No duplicates across enumeration (sample case, lexicographic)")
  void noDuplicatesSample() {
    Set<String> seen = new HashSet<>();
    for (int[] a : Combinations.of(7).choose(3)) {
      String key = key(a);
      assertTrue(seen.add(key), "duplicate: " + key);
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

  // small nCk for test expectations (fits in long for the small inputs we use)
  private static long nCk(int n, int k) {
    if (k < 0 || k > n) return 0;
    k = Math.min(k, n - k);
    long num = 1, den = 1;
    for (int i = 1; i <= k; i++) {
      num *= (n - (k - i));
      den *= i;
      long g = gcd(num, den);
      num /= g;
      den /= g;
    }
    return num / den;
  }

  private static long gcd(long a, long b) {
    while (b != 0) {
      long t = a % b;
      a = b;
      b = t;
    }
    return Math.abs(a);
  }

  private static int symmetricDifferenceSize(int[] a, int[] b) {
    // a and b are strictly increasing k-length arrays of indices
    int i = 0, j = 0, diff = 0;
    while (i < a.length && j < b.length) {
      if (a[i] == b[j]) {
        i++;
        j++;
      } else if (a[i] < b[j]) {
        diff++;
        i++;
      } else {
        diff++;
        j++;
      }
    }
    diff += (a.length - i) + (b.length - j);
    return diff;
  }

  private static String key(int[] a) {
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
