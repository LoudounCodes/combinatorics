package org.loudouncodes.combinatorics;

import static org.assertj.core.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DerangementsTest {

  // ---------- helpers ------------------------------------------------------

  /** subfactorial via recurrence: !0=1, !1=0, !n=(n-1)(!(n-1)+!(n-2)) */
  private static long subfactorial(int n) {
    if (n < 0) throw new IllegalArgumentException();
    if (n == 0) return 1L;
    if (n == 1) return 0L;
    long dNm2 = 1L; // !0
    long dNm1 = 0L; // !1
    long dN = 0L;
    for (int i = 2; i <= n; i++) {
      dN = (i - 1L) * (dNm1 + dNm2);
      dNm2 = dNm1;
      dNm1 = dN;
    }
    return dN;
  }

  private static String key(int[] a) {
    StringBuilder sb = new StringBuilder(a.length * 2);
    for (int i = 0; i < a.length; i++) {
      if (i > 0) sb.append(',');
      sb.append(a[i]);
    }
    return sb.toString();
  }

  private static boolean isDerangement(int[] p) {
    for (int i = 0; i < p.length; i++) {
      if (p[i] == i) return false;
    }
    // also check it's a permutation of 0..n-1
    boolean[] seen = new boolean[p.length];
    for (int v : p) {
      if (v < 0 || v >= p.length) return false;
      if (seen[v]) return false;
      seen[v] = true;
    }
    return true;
  }

  // ---------- tests --------------------------------------------------------

  @Test
  @DisplayName("Count equals subfactorial !n and no duplicates (n=7)")
  void countMatchesSubfactorial_NoDuplicates() {
    int n = 7;
    long expected = subfactorial(n);

    Set<String> uniq = new HashSet<>();
    long total = 0;
    for (int[] p : new Derangements(n)) {
      total++;
      assertThat(p.length).isEqualTo(n);
      assertThat(isDerangement(p)).isTrue();
      uniq.add(key(p));
    }

    assertThat(total).isEqualTo(expected);
    assertThat(uniq).hasSize((int) expected);
  }

  @Test
  @DisplayName("size() agrees with iteration count for n=0..8")
  void sizeMatchesIterationForSmallN() {
    for (int n = 0; n <= 8; n++) {
      Derangements d = new Derangements(n);
      long expected = subfactorial(n);
      long seen = 0;
      for (int[] ignored : d) seen++;
      assertThat(d.size()).as("size() for n=%d", n).isEqualTo(expected);
      assertThat(seen).as("iterated count for n=%d", n).isEqualTo(expected);
    }
  }

  @Test
  @DisplayName("Exact lexicographic sequence for n=3")
  void lexicographicSequence_n3() {
    List<int[]> got = new ArrayList<>();
    for (int[] p : new Derangements(3)) got.add(p.clone());

    assertThat(got)
        .hasSize(2)
        .satisfiesExactly(
            a -> assertThat(a).containsExactly(1, 2, 0), // first in lexicographic order
            a -> assertThat(a).containsExactly(2, 0, 1) // second
            );
  }

  @Test
  @DisplayName("n=0 yields one empty permutation; n=1 yields none")
  void edgeCases_n0_n1() {
    // n = 0
    List<int[]> n0 = new ArrayList<>();
    for (int[] p : new Derangements(0)) n0.add(p);
    assertThat(n0).hasSize(1);
    assertThat(n0.get(0)).isEmpty();

    // n = 1
    Iterator<int[]> it = new Derangements(1).iterator();
    assertThat(it.hasNext()).isFalse();
    assertThatThrownBy(it::next).isInstanceOf(NoSuchElementException.class);
  }

  @Test
  @DisplayName("Iterator respects hasNext()/next() and throws on exhaustion (n=4)")
  void iteratorContract_n4() {
    // For n=4, !4 = 9
    Iterator<int[]> it = new Derangements(4).iterator();
    int count = 0;
    while (it.hasNext()) {
      int[] p = it.next();
      assertThat(isDerangement(p)).isTrue();
      count++;
    }
    assertThat(count).isEqualTo(9);
    assertThatThrownBy(it::next).isInstanceOf(NoSuchElementException.class);
  }

  @Test
  @DisplayName("Returned arrays are defensive copies")
  void defensiveCopies() {
    Iterator<int[]> it = new Derangements(4).iterator();
    int[] first = it.next();
    int[] snapshot = first.clone();
    first[0] = 99; // mutate caller-side copy
    int[] second = it.next();

    // Our mutation shouldn't have affected the iterator state.
    assertThat(snapshot[0]).isNotEqualTo(99);
    assertThat(second).isNotEqualTo(snapshot);
    assertThat(isDerangement(second)).isTrue();
  }

  @Test
  @DisplayName("Invalid arguments throw IllegalArgumentException")
  void invalidArgsThrow() {
    assertThatThrownBy(() -> new Derangements(-1)).isInstanceOf(IllegalArgumentException.class);
  }
}
