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
  // WITHOUT REPETITION
  // --------------------------------------------------------------------------
  @Nested
  @DisplayName("Combinations without repetition")
  class NoRepetition {

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

      if (second.length > 0) second[0] = 999;
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
  // WITH REPETITION (multiset combinations)
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
        for (int i = 0; i + 1 < a.length; i++) {
          assertTrue(a[i] <= a[i + 1], "tuple not nondecreasing");
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

      if (b.length > 0) b[0] = 999;
      int[] c = it.next(); // [0,2]
      assertArrayEquals(new int[] {0, 2}, c);
    }
  }

  // --------------------------------------------------------------------------
  // CROSS-CUTTING sanity
  // --------------------------------------------------------------------------
  @Test
  @DisplayName("Independent iterators from the same spec advance independently and match")
  void iteratorIndependence() {
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
  @DisplayName("No duplicates across enumeration (sample case)")
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
