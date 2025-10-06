package org.loudouncodes.combinatorics;

import static org.assertj.core.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IndexingAdapterTest {

  @Test
  @DisplayName("Maps combinations of indices to element lists (names example)")
  void mapsCombinationsToLists() {
    List<String> names = List.of("Ada", "Grace", "Edsger", "Barbara");
    // n = 4, k = 2 → C(4,2) = 6 pairs
    IndexingAdapter<String> adapted =
        new IndexingAdapter<>(new Combinations(2, names.size()), names);

    List<List<String>> got = new ArrayList<>();
    for (List<String> pair : adapted) {
      got.add(pair);
    }

    assertThat(got)
        .hasSize(6)
        .satisfiesExactly(
            a -> assertThat(a).containsExactly("Ada", "Grace"),
            a -> assertThat(a).containsExactly("Ada", "Edsger"),
            a -> assertThat(a).containsExactly("Ada", "Barbara"),
            a -> assertThat(a).containsExactly("Grace", "Edsger"),
            a -> assertThat(a).containsExactly("Grace", "Barbara"),
            a -> assertThat(a).containsExactly("Edsger", "Barbara"));
  }

  @Test
  @DisplayName("Maps permutations of indices to element lists (integers example)")
  void mapsPermutationsToLists() {
    List<Integer> data = List.of(10, 20, 30);
    // n = 3, k = 2 → 3P2 = 6
    IndexingAdapter<Integer> adapted =
        new IndexingAdapter<>(new Permutations(2, data.size()), data);

    List<List<Integer>> got = new ArrayList<>();
    for (List<Integer> t : adapted) {
      got.add(t);
    }

    assertThat(got)
        .hasSize(6)
        .satisfiesExactly(
            a -> assertThat(a).containsExactly(10, 20),
            a -> assertThat(a).containsExactly(10, 30),
            a -> assertThat(a).containsExactly(20, 10),
            a -> assertThat(a).containsExactly(20, 30),
            a -> assertThat(a).containsExactly(30, 10),
            a -> assertThat(a).containsExactly(30, 20));
  }

  @Test
  @DisplayName("Returned lists are unmodifiable (defensive output)")
  void returnedListsAreUnmodifiable() {
    List<String> names = List.of("A", "B", "C");
    IndexingAdapter<String> adapted =
        new IndexingAdapter<>(new Combinations(2, names.size()), names);

    Iterator<List<String>> it = adapted.iterator();
    List<String> first = it.next();

    assertThatThrownBy(() -> first.add("X")).isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> first.remove(0)).isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  @DisplayName("Out-of-bounds indices throw IndexOutOfBoundsException when iterated")
  void outOfBoundsIndicesThrow() {
    // A tiny iterable that yields one invalid index tuple [0, 3] for a source of size 3 (valid
    // 0..2).
    Iterable<int[]> badTuples =
        () ->
            new Iterator<>() {
              private boolean used = false;

              @Override
              public boolean hasNext() {
                return !used;
              }

              @Override
              public int[] next() {
                if (used) throw new NoSuchElementException();
                used = true;
                return new int[] {0, 3}; // 3 is out of bounds for source size 3
              }
            };

    List<String> source = List.of("A", "B", "C");
    IndexingAdapter<String> adapted = new IndexingAdapter<>(badTuples, source);

    Iterator<List<String>> it = adapted.iterator();
    assertThat(it.hasNext()).isTrue();
    assertThatThrownBy(it::next).isInstanceOf(IndexOutOfBoundsException.class);
  }

  @Test
  @DisplayName("Iterator respects hasNext()/next() and throws on exhaustion")
  void iteratorContract() {
    List<String> names = List.of("Ada", "Grace", "Edsger");
    IndexingAdapter<String> adapted =
        new IndexingAdapter<>(new Combinations(1, names.size()), names); // 3 tuples

    Iterator<List<String>> it = adapted.iterator();
    int count = 0;
    while (it.hasNext()) {
      List<String> t = it.next();
      assertThat(t).hasSize(1);
      assertThat(names).contains(t.get(0));
      count++;
    }
    assertThat(count).isEqualTo(3);
    assertThatThrownBy(it::next).isInstanceOf(NoSuchElementException.class);
  }

  @Test
  @DisplayName("Factory method IndexingAdapter.of(...) returns a working adapter")
  void factoryMethodWorks() {
    List<String> data = List.of("X", "Y");
    IndexingAdapter<String> adapted = IndexingAdapter.of(new Combinations(2, 2), data);

    List<List<String>> got = new ArrayList<>();
    adapted.forEach(got::add);

    assertThat(got).hasSize(1).first().isEqualTo(List.of("X", "Y"));
  }
}
