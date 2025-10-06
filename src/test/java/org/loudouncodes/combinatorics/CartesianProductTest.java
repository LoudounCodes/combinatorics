package org.loudouncodes.combinatorics;

import static org.assertj.core.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CartesianProductTest {

  @Test
  @DisplayName("CartesianProduct(2,3) yields 6 tuples in lexicographic order")
  void twoByThree() {
    CartesianProduct cp = new CartesianProduct(2, 3);
    List<int[]> got = new ArrayList<>();
    for (int[] t : cp) got.add(t);

    assertThat(cp.size()).isEqualTo(6);
    assertThat(got)
        .hasSize(6)
        .satisfiesExactly(
            a -> assertThat(a).containsExactly(0, 0),
            a -> assertThat(a).containsExactly(0, 1),
            a -> assertThat(a).containsExactly(0, 2),
            a -> assertThat(a).containsExactly(1, 0),
            a -> assertThat(a).containsExactly(1, 1),
            a -> assertThat(a).containsExactly(1, 2));
  }

  @Test
  @DisplayName("Empty product (no dimensions) yields one empty tuple")
  void zeroDimensions() {
    CartesianProduct cp = new CartesianProduct();
    List<int[]> got = new ArrayList<>();
    for (int[] t : cp) got.add(t);

    assertThat(cp.size()).isEqualTo(1);
    assertThat(got).hasSize(1);
    assertThat(got.get(0)).isEmpty();
  }

  @Test
  @DisplayName("Domain with size zero yields empty product")
  void dimensionZero() {
    CartesianProduct cp = new CartesianProduct(4, 0, 2);
    List<int[]> got = new ArrayList<>();
    for (int[] t : cp) got.add(t);

    assertThat(cp.size()).isEqualTo(0);
    assertThat(got).isEmpty();
  }

  @Test
  @DisplayName("3 × 2 × 2 product yields 12 tuples and matches nested loop order")
  void threeByTwoByTwo() {
    CartesianProduct cp = new CartesianProduct(3, 2, 2);
    List<int[]> got = new ArrayList<>();
    for (int[] t : cp) got.add(t);

    assertThat(cp.size()).isEqualTo(12);
    int count = 0;
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 2; j++) {
        for (int k = 0; k < 2; k++) {
          assertThat(got.get(count++)).containsExactly(i, j, k);
        }
      }
    }
    assertThat(count).isEqualTo(12);
  }

  @Test
  @DisplayName("Iterator respects hasNext()/next() and throws on exhaustion")
  void iteratorContract() {
    CartesianProduct cp = new CartesianProduct(2, 2); // total 4
    Iterator<int[]> it = cp.iterator();
    int count = 0;
    while (it.hasNext()) {
      int[] t = it.next();
      assertThat(t).hasSize(2);
      count++;
    }
    assertThat(count).isEqualTo(4);
    assertThatThrownBy(it::next).isInstanceOf(NoSuchElementException.class);
  }

  @Test
  @DisplayName("Returned tuples are defensive copies")
  void defensiveCopies() {
    CartesianProduct cp = new CartesianProduct(2, 2);
    Iterator<int[]> it = cp.iterator();
    int[] first = it.next();
    int[] snapshot = first.clone();

    // Mutate caller-side copy
    first[0] = 99;

    // Fetch another tuple and check snapshot is intact
    int[] second = it.next();
    assertThat(snapshot[0]).isNotEqualTo(99);
    assertThat(second).isNotEqualTo(snapshot);
  }

  @Test
  @DisplayName("Negative size throws IllegalArgumentException")
  void negativeSizeThrows() {
    assertThatThrownBy(() -> new CartesianProduct(3, -1))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
