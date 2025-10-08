package org.loudouncodes.combinatorics;

import static org.assertj.core.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CombinationsTest {

  @Test
  @DisplayName("Generates lexicographic combinations for n=4, k=2")
  void lexicographicOrder_n4k2() {
    Combinations.KChoose combos = Combinations.of(4).choose(2);
    List<int[]> got = new ArrayList<>();
    for (int[] c : combos) got.add(c);

    assertThat(combos.size()).isEqualTo(6); // C(4,2)=6
    assertThat(got)
        .hasSize(6)
        .satisfiesExactly(
            a -> assertThat(a).containsExactly(0, 1),
            a -> assertThat(a).containsExactly(0, 2),
            a -> assertThat(a).containsExactly(0, 3),
            a -> assertThat(a).containsExactly(1, 2),
            a -> assertThat(a).containsExactly(1, 3),
            a -> assertThat(a).containsExactly(2, 3));
  }

  @Test
  @DisplayName("Count matches known value: C(12,3) = 220")
  void countC12_3() {
    Combinations.KChoose combos = Combinations.of(12).choose(3);
    long count = 0;
    for (int[] ignored : combos) count++;
    assertThat(count).isEqualTo(220);
    assertThat(combos.size()).isEqualTo(220);
  }

  @Test
  @DisplayName("Edge case k=0 yields one empty combination")
  void kEqualsZero() {
    Combinations.KChoose combos = Combinations.of(5).choose(0);
    List<int[]> got = new ArrayList<>();
    for (int[] c : combos) got.add(c);

    assertThat(got).hasSize(1);
    assertThat(got.get(0)).isEmpty();
    assertThat(combos.size()).isEqualTo(1);
  }

  @Test
  @DisplayName("Edge case k=n yields exactly one combination [0,1,...,n-1]")
  void kEqualsN() {
    int n = 6;
    Combinations.KChoose combos = Combinations.of(n).choose(n);
    List<int[]> got = new ArrayList<>();
    for (int[] c : combos) got.add(c);

    assertThat(got).hasSize(1);
    assertThat(got.get(0)).containsExactly(0, 1, 2, 3, 4, 5);
    assertThat(combos.size()).isEqualTo(1);
  }

  @Test
  @DisplayName("Iterator respects hasNext()/next() contract and throws on exhaustion")
  void iteratorContract() {
    Iterator<int[]> it = Combinations.of(4).choose(2).iterator();
    int count = 0;
    while (it.hasNext()) {
      int[] c = it.next();
      assertThat(c).hasSize(2);
      count++;
    }
    assertThat(count).isEqualTo(6);
    assertThatThrownBy(it::next).isInstanceOf(NoSuchElementException.class);
  }

  @Test
  @DisplayName("Returned arrays are defensive copies (immutability)")
  void defensiveCopies() {
    Iterator<int[]> it = Combinations.of(5).choose(2).iterator();
    int[] first = it.next();
    int[] snapshot = first.clone();

    // mutate caller copy
    first[0] = 99;

    // next element should be unaffected; and snapshot should remain intact
    int[] second = it.next();
    assertThat(snapshot[0]).isNotEqualTo(99);
    assertThat(second).isNotEqualTo(snapshot);
  }

  @Test
  @DisplayName("Invalid arguments throw IllegalArgumentException")
  void invalidArgs() {
    assertThatThrownBy(() -> Combinations.of(-1))
        .isInstanceOf(IllegalArgumentException.class);

    // n=5, k<0
    assertThatThrownBy(() -> Combinations.of(5).choose(-1))
        .isInstanceOf(IllegalArgumentException.class);

    // n=5, k>n
    assertThatThrownBy(() -> Combinations.of(5).choose(6))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
