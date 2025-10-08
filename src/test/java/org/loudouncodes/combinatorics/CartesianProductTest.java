package org.loudouncodes.combinatorics;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CartesianProductTest {

  @Test
  @DisplayName("Rightmost coordinate varies fastest (dims = [2,3])")
  void order_rightmostFastest() {
    CartesianProduct.Product prod = CartesianProduct.of(2, 3);
    List<int[]> got = new ArrayList<>();
    for (int[] t : prod) got.add(t);

    assertThat(prod.size()).isEqualTo(6); // 2 * 3
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
  @DisplayName("Count equals product of dimensions (dims = [3,4,2])")
  void count_matchesProduct() {
    CartesianProduct.Product prod = CartesianProduct.of(3, 4, 2);
    long count = 0;
    for (int[] ignored : prod) count++;
    assertThat(count).isEqualTo(3L * 4L * 2L);
    assertThat(prod.size()).isEqualTo(24L);
  }

  @Test
  @DisplayName("No dimensions => one empty tuple []")
  void noDimensions_yieldsEmptyTuple() {
    CartesianProduct.Product prod = CartesianProduct.of(); // zero-length dims
    List<int[]> got = new ArrayList<>();
    for (int[] t : prod) got.add(t);

    assertThat(got).hasSize(1);
    assertThat(got.get(0)).isEmpty();
    assertThat(prod.size()).isEqualTo(1L);
  }

  @Test
  @DisplayName("Any zero dimension => empty product (dims = [3,0,2])")
  void zeroDimension_empty() {
    CartesianProduct.Product prod = CartesianProduct.of(3, 0, 2);
    List<int[]> got = new ArrayList<>();
    for (int[] t : prod) got.add(t);

    assertThat(got).isEmpty();
    assertThat(prod.size()).isEqualTo(0L);
  }

  @Test
  @DisplayName("Returned arrays are defensive copies")
  void defensiveCopies() {
    Iterator<int[]> it = CartesianProduct.of(2, 2).iterator();
    int[] first = it.next();
    int[] snapshot = first.clone();

    // mutate caller's copy
    first[0] = 99;

    int[] second = it.next();
    assertThat(snapshot[0]).isNotEqualTo(99); // our copy is unaffected
    assertThat(second).isNotEqualTo(snapshot); // next tuple is a fresh copy
  }

  @Test
  @DisplayName("Iterator respects hasNext()/next() and throws on exhaustion")
  void iteratorContract() {
    Iterator<int[]> it = CartesianProduct.of(1, 2).iterator(); // tuples: [0,0], [0,1]
    assertThat(it.hasNext()).isTrue();
    assertThat(it.next()).containsExactly(0, 0);
    assertThat(it.hasNext()).isTrue();
    assertThat(it.next()).containsExactly(0, 1);
    assertThat(it.hasNext()).isFalse();
    assertThatThrownBy(it::next).isInstanceOf(NoSuchElementException.class);
  }

  @Test
  @DisplayName("Negative dimension throws IllegalArgumentException")
  void negativeDimensionThrows() {
    assertThatThrownBy(() -> CartesianProduct.of(2, -1, 3))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
