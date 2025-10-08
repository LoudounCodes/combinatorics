package org.loudouncodes.combinatorics;

import static org.assertj.core.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IndexingAdapterTest {

  @Test
  @DisplayName("Adapts combinations to element pairs in lexicographic order")
  void mapsCombinationsToElements() {
    List<String> items = List.of("A", "B", "C", "D");

    List<List<String>> got = new ArrayList<>();
    for (List<String> pair :
        new IndexingAdapter<>(Combinations.of(items.size()).choose(2), items)) {
      got.add(pair);
    }

    assertThat(got)
        .hasSize(6)
        .satisfiesExactly(
            l -> assertThat(l).containsExactly("A", "B"),
            l -> assertThat(l).containsExactly("A", "C"),
            l -> assertThat(l).containsExactly("A", "D"),
            l -> assertThat(l).containsExactly("B", "C"),
            l -> assertThat(l).containsExactly("B", "D"),
            l -> assertThat(l).containsExactly("C", "D"));
  }

  @Test
  @DisplayName("Adapts permutations to ordered tuples (order preserved)")
  void mapsPermutationsToOrderedTuples() {
    List<String> items = List.of("X", "Y", "Z");

    List<List<String>> got = new ArrayList<>();
    for (List<String> t : new IndexingAdapter<>(Permutations.of(items.size()).take(2), items)) {
      got.add(t);
    }

    assertThat(got)
        .hasSize(6)
        .satisfiesExactly(
            l -> assertThat(l).containsExactly("X", "Y"),
            l -> assertThat(l).containsExactly("X", "Z"),
            l -> assertThat(l).containsExactly("Y", "X"),
            l -> assertThat(l).containsExactly("Y", "Z"),
            l -> assertThat(l).containsExactly("Z", "X"),
            l -> assertThat(l).containsExactly("Z", "Y"));
  }

  @Test
  @DisplayName("Returned lists are unmodifiable snapshots")
  void returnedListsAreUnmodifiable() {
    List<String> items = List.of("P", "Q", "R");

    Iterator<List<String>> it =
        new IndexingAdapter<>(Combinations.of(items.size()).choose(2), items).iterator();

    List<String> first = it.next();
    assertThat(first).containsExactly("P", "Q");
    assertThatThrownBy(() -> first.add("NEW")).isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  @DisplayName("Bounds check: invalid index in tuple throws IndexOutOfBoundsException")
  void invalidIndexThrows() {
    List<String> items = List.of("a", "b", "c", "d");
    Iterable<int[]> bad =
        () ->
            new Iterator<int[]>() {
              boolean done = false;

              @Override
              public boolean hasNext() {
                return !done;
              }

              @Override
              public int[] next() {
                if (done) throw new NoSuchElementException();
                done = true;
                return new int[] {0, 5};
              }
            };

    IndexingAdapter<String> adapter = new IndexingAdapter<>(bad, items);
    Iterator<List<String>> it = adapter.iterator();
    assertThatThrownBy(it::next).isInstanceOf(IndexOutOfBoundsException.class);
  }

  @Test
  @DisplayName("Edge case: choose(0) maps to a single empty list")
  void emptyTupleMapsToEmptyList() {
    List<String> items = List.of("one", "two", "three");

    List<List<String>> got = new ArrayList<>();
    for (List<String> t : new IndexingAdapter<>(Combinations.of(items.size()).choose(0), items)) {
      got.add(t);
    }

    assertThat(got).hasSize(1);
    assertThat(got.get(0)).isEmpty();
  }

  @Test
  @DisplayName("Iterator respects hasNext()/next() and throws on exhaustion")
  void iteratorContract() {
    List<String> items = List.of("A", "B");
    IndexingAdapter<String> adapter =
        new IndexingAdapter<>(Combinations.of(items.size()).choose(2), items);

    Iterator<List<String>> it = adapter.iterator();
    assertThat(it.hasNext()).isTrue();
    assertThat(it.next()).containsExactly("A", "B");
    assertThat(it.hasNext()).isFalse();
    assertThatThrownBy(it::next).isInstanceOf(NoSuchElementException.class);
  }
}
