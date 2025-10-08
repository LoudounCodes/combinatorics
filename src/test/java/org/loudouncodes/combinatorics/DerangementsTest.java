package org.loudouncodes.combinatorics;

import static org.assertj.core.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DerangementsTest {

  @Test
  @DisplayName("Small order check: n=3 in lexicographic order")
  void order_n3() {
    Derangements.All all = Derangements.of(3).all();
    List<int[]> got = new ArrayList<>();
    for (int[] d : all) got.add(d);

    // !3 = 2
    assertThat(all.size()).isEqualTo(2);
    assertThat(got)
        .hasSize(2)
        .satisfiesExactly(
            a -> assertThat(a).containsExactly(1, 2, 0),
            a -> assertThat(a).containsExactly(2, 0, 1));
  }

  @Test
  @DisplayName("Every output is a derangement: d[i] != i")
  void property_noFixedPoints() {
    int n = 6;
    for (int[] d : Derangements.of(n).all()) {
      assertThat(d).hasSize(n);
      for (int i = 0; i < n; i++) {
        assertThat(d[i]).isNotEqualTo(i);
      }
    }
  }

  @Test
  @DisplayName("Counts match subfactorial values for n=0..6")
  void counts_matchSubfactorial() {
    long[] expected = {1, 0, 1, 2, 9, 44, 265}; // !0..!6
    for (int n = 0; n <= 6; n++) {
      Derangements.All all = Derangements.of(n).all();
      long iterCount = 0;
      for (int[] ignored : all) iterCount++;
      assertThat(iterCount).as("!%s via iteration", n).isEqualTo(expected[n]);
      assertThat(all.size()).as("!%s via size()", n).isEqualTo(expected[n]);
    }
  }

  @Test
  @DisplayName("Iterator respects hasNext()/next() and throws on exhaustion (n=4)")
  void iteratorContract_n4() {
    Iterator<int[]> it = Derangements.of(4).all().iterator(); // !4 = 9
    int count = 0;
    while (it.hasNext()) {
      int[] d = it.next();
      assertThat(d).hasSize(4);
      count++;
    }
    assertThat(count).isEqualTo(9);
    assertThatThrownBy(it::next).isInstanceOf(NoSuchElementException.class);
  }

  @Test
  @DisplayName("Returned arrays are defensive copies (immutability)")
  void defensiveCopies() {
    Iterator<int[]> it = Derangements.of(4).all().iterator();
    int[] first = it.next();
    int[] snapshot = first.clone();

    // mutate caller's copy
    first[0] = 99;

    int[] second = it.next();
    assertThat(snapshot[0]).isNotEqualTo(99);
    assertThat(second).isNotEqualTo(snapshot);
  }

  @Test
  @DisplayName("Edge case n=0 yields one empty derangement; n=1 yields none")
  void edgeCases_n0_n1() {
    // n = 0
    Derangements.All zero = Derangements.of(0).all();
    List<int[]> z = new ArrayList<>();
    for (int[] d : zero) z.add(d);
    assertThat(z).hasSize(1);
    assertThat(z.get(0)).isEmpty();
    assertThat(zero.size()).isEqualTo(1);

    // n = 1
    Derangements.All one = Derangements.of(1).all();
    List<int[]> o = new ArrayList<>();
    for (int[] d : one) o.add(d);
    assertThat(o).isEmpty();
    assertThat(one.size()).isEqualTo(0);
  }

  @Test
  @DisplayName("Invalid arguments throw IllegalArgumentException")
  void invalidArgs() {
    assertThatThrownBy(() -> Derangements.of(-1)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("No duplicates for n=5 (exactly 44 unique derangements)")
  void uniqueness_n5() {
    Derangements.All all = Derangements.of(5).all(); // !5 = 44
    Set<String> seen = new HashSet<>();
    int count = 0;
    for (int[] d : all) {
      String key = Arrays.toString(d);
      assertThat(seen).doesNotContain(key);
      seen.add(key);
      count++;
    }
    assertThat(count).isEqualTo(44);
    assertThat(seen).hasSize(44);
  }
}
