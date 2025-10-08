package org.loudouncodes.combinatorics;

import static org.assertj.core.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PermutationsTest {

  @Test
  @DisplayName("Generates all k-permutations of n without duplicates (n=5, k=3)")
  void uniqueNoDuplicates_n5k3() {
    Permutations.KTake perms = Permutations.of(5).take(3);

    // Collect as strings to detect duplicates easily
    Set<String> seen = new HashSet<>();
    long count = 0;
    for (int[] p : perms) {
      assertThat(p).hasSize(3);
      String key = Arrays.toString(p);
      assertThat(seen).doesNotContain(key);
      seen.add(key);
      count++;
    }

    // 5P3 = 5*4*3 = 60
    assertThat(count).isEqualTo(60);
    assertThat(seen).hasSize(60);
    assertThat(perms.size()).isEqualTo(60);
  }

  @Test
  @DisplayName("Small order check: n=3, k=2 in lexicographic order")
  void orderCheck_n3k2() {
    Permutations.KTake perms = Permutations.of(3).take(2);
    List<int[]> got = new ArrayList<>();
    for (int[] p : perms) got.add(p);

    assertThat(got)
        .hasSize(6)
        .satisfiesExactly(
            a -> assertThat(a).containsExactly(0, 1),
            a -> assertThat(a).containsExactly(0, 2),
            a -> assertThat(a).containsExactly(1, 0),
            a -> assertThat(a).containsExactly(1, 2),
            a -> assertThat(a).containsExactly(2, 0),
            a -> assertThat(a).containsExactly(2, 1));
  }

  @Test
  @DisplayName("Edge case k=0 yields one empty tuple")
  void kEqualsZero() {
    Permutations.KTake perms = Permutations.of(7).take(0);
    List<int[]> got = new ArrayList<>();
    for (int[] p : perms) got.add(p);

    assertThat(got).hasSize(1);
    assertThat(got.get(0)).isEmpty();
    assertThat(perms.size()).isEqualTo(1);
  }

  @Test
  @DisplayName("Edge case k=n yields all n! permutations in lexicographic order (n=3)")
  void kEqualsN_fullPerms_n3() {
    int n = 3;
    Permutations.KTake perms = Permutations.of(n).take(n);
    List<int[]> got = new ArrayList<>();
    for (int[] p : perms) got.add(p);

    assertThat(got)
        .hasSize(6)
        .satisfiesExactly(
            a -> assertThat(a).containsExactly(0, 1, 2),
            a -> assertThat(a).containsExactly(0, 2, 1),
            a -> assertThat(a).containsExactly(1, 0, 2),
            a -> assertThat(a).containsExactly(1, 2, 0),
            a -> assertThat(a).containsExactly(2, 0, 1),
            a -> assertThat(a).containsExactly(2, 1, 0));
    assertThat(perms.size()).isEqualTo(6);
  }

  @Test
  @DisplayName("Iterator respects hasNext()/next() and throws on exhaustion")
  void iteratorContract() {
    Iterator<int[]> it = Permutations.of(4).take(2).iterator(); // 4P2 = 12
    int count = 0;
    while (it.hasNext()) {
      int[] p = it.next();
      assertThat(p).hasSize(2);
      count++;
    }
    assertThat(count).isEqualTo(12);
    assertThatThrownBy(it::next).isInstanceOf(NoSuchElementException.class);
  }

  @Test
  @DisplayName("Returned arrays are defensive copies (immutability)")
  void defensiveCopies() {
    Iterator<int[]> it = Permutations.of(4).take(2).iterator();
    int[] first = it.next();
    int[] snapshot = first.clone();

    // mutate caller's copy
    first[0] = 99;

    int[] second = it.next();
    assertThat(snapshot[0]).isNotEqualTo(99);
    assertThat(second).isNotEqualTo(snapshot);
  }

  @Test
  @DisplayName("Invalid arguments throw IllegalArgumentException")
  void invalidArgs() {
    // n < 0
    assertThatThrownBy(() -> Permutations.of(-1)).isInstanceOf(IllegalArgumentException.class);
    // k < 0
    assertThatThrownBy(() -> Permutations.of(5).take(-1))
        .isInstanceOf(IllegalArgumentException.class);
    // k > n
    assertThatThrownBy(() -> Permutations.of(5).take(6))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
