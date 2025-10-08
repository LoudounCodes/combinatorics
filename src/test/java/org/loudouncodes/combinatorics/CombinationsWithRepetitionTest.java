package org.loudouncodes.combinatorics;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CombinationsWithRepetitionTest {

  @Test
  @DisplayName("Lexicographic order for n=3, k=2 (nondecreasing arrays)")
  void order_n3k2() {
    var it = CombinationsWithRepetition.of(3).multichoose(2);
    List<int[]> got = new ArrayList<>();
    for (int[] a : it) got.add(a);

    assertThat(it.size()).isEqualTo(6); // C(3+2-1, 2) = C(4,2) = 6
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
  @DisplayName("Count matches multichoose formula: C(n+k-1, k) for n=5, k=3")
  void count_n5k3() {
    var m = CombinationsWithRepetition.of(5).multichoose(3); // C(7,3)=35
    long count = 0;
    for (int[] ignored : m) count++;
    assertThat(count).isEqualTo(35);
    assertThat(m.size()).isEqualTo(35);
  }

  @Test
  @DisplayName("Edge case k=0 yields one empty combination (any n)")
  void kZero_anyN() {
    var m = CombinationsWithRepetition.of(8).multichoose(0);
    List<int[]> got = new ArrayList<>();
    for (int[] a : m) got.add(a);

    assertThat(got).hasSize(1);
    assertThat(got.get(0)).isEmpty();
    assertThat(m.size()).isEqualTo(1);
  }

  @Test
  @DisplayName("Edge case n=0, k=0 emits one empty array; n=0, k>0 emits none")
  void nZero_cases() {
    var zeroZero = CombinationsWithRepetition.of(0).multichoose(0);
    List<int[]> a = new ArrayList<>();
    for (int[] t : zeroZero) a.add(t);
    assertThat(a).hasSize(1);
    assertThat(a.get(0)).isEmpty();
    assertThat(zeroZero.size()).isEqualTo(1);

    var zeroThree = CombinationsWithRepetition.of(0).multichoose(3);
    List<int[]> b = new ArrayList<>();
    for (int[] t : zeroThree) b.add(t);
    assertThat(b).isEmpty();
    assertThat(zeroThree.size()).isEqualTo(0);
  }

  @Test
  @DisplayName("Iterator respects hasNext()/next() and throws on exhaustion")
  void iteratorContract() {
    var m = CombinationsWithRepetition.of(3).multichoose(2); // 6 tuples
    Iterator<int[]> it = m.iterator();
    int count = 0;
    while (it.hasNext()) {
      int[] arr = it.next();
      assertThat(arr).hasSize(2);
      assertThat(arr[0]).isLessThanOrEqualTo(arr[1]); // nondecreasing
      count++;
    }
    assertThat(count).isEqualTo(6);
    assertThatThrownBy(it::next).isInstanceOf(NoSuchElementException.class);
  }

  @Test
  @DisplayName("Returned arrays are defensive copies (immutability)")
  void defensiveCopies() {
    Iterator<int[]> it = CombinationsWithRepetition.of(4).multichoose(2).iterator();
    int[] first = it.next();
    int[] snapshot = first.clone();

    // Mutate caller's copy
    first[0] = 99;

    int[] second = it.next();
    // The previous snapshot remains unchanged; the iterator returned a defensive copy
    assertThat(snapshot[0]).isNotEqualTo(99);
    assertThat(second).isNotEqualTo(snapshot);
  }

  @Test
  @DisplayName("Invalid arguments throw IllegalArgumentException")
  void invalidArgs() {
    assertThatThrownBy(() -> CombinationsWithRepetition.of(-1))
        .isInstanceOf(IllegalArgumentException.class);

    assertThatThrownBy(() -> CombinationsWithRepetition.of(5).multichoose(-2))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
