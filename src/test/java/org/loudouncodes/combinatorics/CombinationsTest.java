package org.loudouncodes.combinatorics;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CombinationsTest {

  @Test
  @DisplayName("Generates lexicographic combinations for n=4, k=2")
  void generatesLexicographicCombinations() {
    List<int[]> seen = new ArrayList<>();
    for (int[] c : new Combinations(2, 4)) {
      seen.add(c);
    }

    // C(4,2) = 6
    assertThat(seen).hasSize(6);

    // Exact lexicographic order
    assertThat(seen.get(0)).containsExactly(0, 1);
    assertThat(seen.get(1)).containsExactly(0, 2);
    assertThat(seen.get(2)).containsExactly(0, 3);
    assertThat(seen.get(3)).containsExactly(1, 2);
    assertThat(seen.get(4)).containsExactly(1, 3);
    assertThat(seen.get(5)).containsExactly(2, 3);
  }

  @Test
  @DisplayName("Count matches known value: C(12,3) = 220")
  void countMatchesKnownValue() {
    int count = 0;
    for (int[] ignored : new Combinations(3, 12)) {
      count++;
    }
    assertThat(count).isEqualTo(220);
  }

  @Test
  @DisplayName("Edge case k=0 yields one empty combination")
  void kZeroYieldsOneEmptyCombination() {
    List<int[]> seen = new ArrayList<>();
    for (int[] c : new Combinations(0, 5)) {
      seen.add(c);
    }
    assertThat(seen).hasSize(1);
    assertThat(seen.get(0)).isEmpty();
  }

  @Test
  @DisplayName("Edge case k=n yields exactly one combination [0,1,...,n-1]")
  void kEqualsNYieldsOneFullCombination() {
    int n = 5;
    List<int[]> seen = new ArrayList<>();
    for (int[] c : new Combinations(n, n)) {
      seen.add(c);
    }
    assertThat(seen).hasSize(1);
    assertThat(seen.get(0)).containsExactly(0, 1, 2, 3, 4);
  }

  @Test
  @DisplayName("Invalid arguments throw IllegalArgumentException")
  void invalidArgumentsThrow() {
    assertThatThrownBy(() -> new Combinations(-1, 5)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new Combinations(3, -1)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new Combinations(6, 5)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("Iterator respects hasNext()/next() contract and throws on exhaustion")
  void iteratorExhaustion() {
    Combinations combos = new Combinations(2, 3); // C(3,2)=3
    Iterator<int[]> it = combos.iterator();

    assertThat(it.hasNext()).isTrue();
    it.next();
    assertThat(it.hasNext()).isTrue();
    it.next();
    assertThat(it.hasNext()).isTrue();
    it.next();
    assertThat(it.hasNext()).isFalse();
    assertThatThrownBy(it::next).isInstanceOf(NoSuchElementException.class);
  }

  @Test
  @DisplayName("Returned arrays are defensive copies (immutability)")
  void returnedArraysAreDefensiveCopies() {
    Iterator<int[]> it = new Combinations(2, 4).iterator();

    int[] first = it.next(); // expect [0,1]
    int[] snapshot = first.clone(); // keep a copy
    first[0] = 99; // mutate the returned array

    // Advance once more to force iterator to use its internal state
    int[] second = it.next(); // expect [0,2]

    // Our mutation must not have altered the original first combination value
    assertThat(snapshot).containsExactly(0, 1);
    // And the iterator should continue producing correct values
    assertThat(second).containsExactly(0, 2);
  }
}
