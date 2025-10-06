package org.loudouncodes.combinatorics;

import static org.assertj.core.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CombinationsWithRepetitionTest {

  // ---------- helpers ------------------------------------------------------

  private static long nCr(long n, long r) {
    if (r < 0 || n < 0 || r > n) throw new IllegalArgumentException();
    r = Math.min(r, n - r); // symmetry
    long num = 1, den = 1;
    for (long i = 1; i <= r; i++) {
      num *= (n - (r - i));
      den *= i;
    }
    return num / den;
  }

  private static long countWithRepetition(int n, int k) {
    // C(n + k - 1, k)
    if (k == 0) return 1;
    if (n == 0) return 0;
    return nCr(n + k - 1L, k);
  }

  private static boolean isNonDecreasing(int[] a) {
    for (int i = 1; i < a.length; i++) {
      if (a[i] < a[i - 1]) return false;
    }
    return true;
  }

  private static String key(int[] a) {
    StringBuilder sb = new StringBuilder(a.length * 2);
    for (int i = 0; i < a.length; i++) {
      if (i > 0) sb.append(',');
      sb.append(a[i]);
    }
    return sb.toString();
  }

  // ---------- tests --------------------------------------------------------

  @Test
  @DisplayName("Count equals C(n+k-1, k) and no duplicates (n=5, k=3)")
  void countMatchesFormula_NoDuplicates() {
    int n = 5, k = 3;
    long expected = countWithRepetition(n, k);

    Set<String> uniq = new HashSet<>();
    long total = 0;
    for (int[] t : new CombinationsWithRepetition(k, n)) {
      total++;
      // shape & constraints
      assertThat(t.length).isEqualTo(k);
      for (int v : t) assertThat(v).isBetween(0, n - 1);
      assertThat(isNonDecreasing(t)).isTrue();
      uniq.add(key(t));
    }

    assertThat(total).isEqualTo(expected);
    assertThat(uniq).hasSize((int) expected);
  }

  @Test
  @DisplayName("Sequence matches known small case (n=3, k=2)")
  void smallCaseSequence() {
    List<int[]> got = new ArrayList<>();
    for (int[] t : new CombinationsWithRepetition(2, 3)) {
      got.add(t.clone());
    }

    assertThat(got)
        .hasSize(6)
        .satisfiesExactly(
            a -> assertThat(a).containsExactly(0, 0),
            a -> assertThat(a).containsExactly(0, 1),
            a -> assertThat(a).containsExactly(0, 2),
            a -> assertThat(a).containsExactly(1, 1),
            a -> assertThat(a).containsExactly(1, 2),
            a -> assertThat(a).containsExactly(2, 2));
  }

  @Test
  @DisplayName("k=0 yields one empty tuple")
  void kZeroYieldsOneEmpty() {
    List<int[]> all = new ArrayList<>();
    for (int[] t : new CombinationsWithRepetition(0, 7)) {
      all.add(t);
    }
    assertThat(all).hasSize(1);
    assertThat(all.get(0)).isEmpty();
  }

  @Test
  @DisplayName("n=0 and k>0 yields no tuples; size() is 0")
  void nZeroKPositiveYieldsNone() {
    CombinationsWithRepetition gen = new CombinationsWithRepetition(3, 0);
    assertThat(gen.size()).isZero();
    Iterator<int[]> it = gen.iterator();
    assertThat(it.hasNext()).isFalse();
    assertThatThrownBy(it::next).isInstanceOf(NoSuchElementException.class);
  }

  @Test
  @DisplayName("size() agrees with iteration count across a few cases")
  void sizeMatchesIteration() {
    int[][] cases = {
      {0, 0},
      {0, 5},
      {1, 0},
      {1, 4},
      {2, 3},
      {3, 5},
      {4, 2},
    };
    for (int[] c : cases) {
      int k = c[0], n = c[1];
      CombinationsWithRepetition gen = new CombinationsWithRepetition(k, n);
      long expected = countWithRepetition(n, k);
      long seen = 0;
      for (int[] ignored : gen) seen++;
      assertThat(gen.size()).as("size() for k=%d,n=%d", k, n).isEqualTo(expected);
      assertThat(seen).as("iterated count for k=%d,n=%d", k, n).isEqualTo(expected);
    }
  }

  @Test
  @DisplayName("Iterator respects hasNext()/next() and throws on exhaustion")
  void iteratorContract() {
    CombinationsWithRepetition gen = new CombinationsWithRepetition(2, 3); // 6 tuples
    Iterator<int[]> it = gen.iterator();

    for (int i = 0; i < 6; i++) {
      assertThat(it.hasNext()).isTrue();
      int[] t = it.next();
      assertThat(t.length).isEqualTo(2);
      assertThat(isNonDecreasing(t)).isTrue();
    }
    assertThat(it.hasNext()).isFalse();
    assertThatThrownBy(it::next).isInstanceOf(NoSuchElementException.class);
  }

  @Test
  @DisplayName("Returned arrays are defensive copies")
  void defensiveCopies() {
    Iterator<int[]> it = new CombinationsWithRepetition(3, 3).iterator();
    int[] first = it.next();
    int[] snapshot = first.clone();
    first[0] = 99; // mutate caller-side copy
    int[] second = it.next();

    // second tuple should be lexicographically after the true first tuple,
    // and our local mutation should not have influenced it
    assertThat(second).isNotEqualTo(snapshot);
    assertThat(snapshot[0]).isNotEqualTo(99);
  }

  @Test
  @DisplayName("Invalid arguments throw IllegalArgumentException")
  void invalidArgumentsThrow() {
    assertThatThrownBy(() -> new CombinationsWithRepetition(-1, 3))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new CombinationsWithRepetition(2, -1))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
