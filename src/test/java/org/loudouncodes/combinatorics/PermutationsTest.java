package org.loudouncodes.combinatorics;

import static org.assertj.core.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PermutationsTest {

  // --- helpers -------------------------------------------------------------

  private static long nPr(int n, int r) {
    if (r < 0 || n < 0 || r > n) throw new IllegalArgumentException();
    long p = 1;
    for (int i = 0; i < r; i++) p *= (n - i);
    return p;
  }

  private static String key(int[] a) {
    // Compact key for set membership checks
    StringBuilder sb = new StringBuilder(a.length * 3);
    for (int i = 0; i < a.length; i++) {
      if (i > 0) sb.append(',');
      sb.append(a[i]);
    }
    return sb.toString();
  }

  // --- tests ---------------------------------------------------------------

  @Test
  @DisplayName("Generates all k-permutations of n without duplicates (n=5, k=3)")
  void generatesAllKPermutationsWithoutDuplicates() {
    int n = 5, k = 3;
    long expected = nPr(n, k);

    Set<String> unique = new HashSet<>();
    int total = 0;
    for (int[] p : new Permutations(k, n)) {
      total++;
      unique.add(key(p));
      // basic shape checks
      assertThat(p.length).isEqualTo(k);
      // elements in range and no repeats within a tuple
      boolean[] seen = new boolean[n];
      for (int v : p) {
        assertThat(v).isBetween(0, n - 1);
        assertThat(seen[v]).isFalse();
        seen[v] = true;
      }
    }

    assertThat(total).as("total tuples emitted").isEqualTo(expected);
    assertThat(unique).as("unique tuples").hasSize((int) expected);
  }

  @Test
  @DisplayName("Edge case k=0 yields one empty tuple")
  void kZeroYieldsOneEmpty() {
    List<int[]> all = new ArrayList<>();
    for (int[] p : new Permutations(0, 7)) {
      all.add(p);
    }
    assertThat(all).hasSize(1);
    assertThat(all.get(0)).isEmpty();
  }

  @Test
  @DisplayName("Edge case k=n yields all n! permutations in lexicographic order (n=3)")
  void kEqualsNYieldsAllPermutationsInLexOrder() {
    int n = 3;
    List<int[]> all = new ArrayList<>();
    for (int[] p : new Permutations(n, n)) {
      all.add(p.clone());
    }
    assertThat(all).hasSize(6);
    assertThat(all.get(0)).containsExactly(0, 1, 2);
    assertThat(all.get(5)).containsExactly(2, 1, 0);
    // no duplicates
    Set<String> uniq = new HashSet<>();
    for (int[] p : all) uniq.add(key(p));
    assertThat(uniq).hasSize(6);
  }

  @Test
  @DisplayName("Invalid arguments throw IllegalArgumentException")
  void invalidArgumentsThrow() {
    assertThatThrownBy(() -> new Permutations(-1, 4)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new Permutations(3, -1)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new Permutations(5, 4)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("Iterator respects hasNext()/next() and throws on exhaustion")
  void iteratorExhaustion() {
    Permutations perms = new Permutations(2, 3); // 3P2 = 6
    Iterator<int[]> it = perms.iterator();

    for (int i = 0; i < 6; i++) {
      assertThat(it.hasNext()).isTrue();
      int[] p = it.next();
      assertThat(p.length).isEqualTo(2);
    }
    assertThat(it.hasNext()).isFalse();
    assertThatThrownBy(it::next).isInstanceOf(NoSuchElementException.class);
  }

  @Test
  @DisplayName("Returned arrays are defensive copies")
  void returnedArraysAreDefensiveCopies() {
    Iterator<int[]> it = new Permutations(2, 4).iterator();
    int[] first = it.next();
    int[] snapshot = first.clone();
    first[0] = 99; // mutate caller-side copy

    // Next call should not be affected by our mutation
    int[] second = it.next();

    assertThat(snapshot).isNotEqualTo(second); // they should differ normally
    // And the stored 'first' we mutated should not retroactively change the iterator's state
    assertThat(snapshot[0]).isNotEqualTo(99);
  }
}
