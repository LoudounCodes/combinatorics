package org.loudouncodes.combinatorics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Bridges from <em>indices</em> to <em>elements</em>.
 *
 * <p>Wrap any {@code Iterable<int[]>} together with a backing {@code List<E>} and iterate
 * {@code List<E>} tuples instead of raw index arrays. Each returned list is a fresh, unmodifiable
 * snapshot of the mapped elements (safe for students to keep or mutate references inside without
 * affecting the iterator's internal state).
 *
 * <h2>Example (pairs of toppings)</h2>
 * <pre>{@code
 * List<String> toppings = List.of("Pepperoni","Mushrooms","Onions","Olives");
 * for (List<String> pizza :
 *       new IndexingAdapter<>(Combinations.of(toppings.size()).choose(2), toppings)) {
 *   System.out.println(pizza);
 * }
 * // [Pepperoni, Mushrooms]
 * // [Pepperoni, Onions]
 * // [Pepperoni, Olives]
 * // [Mushrooms, Onions]
 * // [Mushrooms, Olives]
 * // [Onions, Olives]
 * }</pre>
 *
 * <p>You can wrap <em>any</em> of the generators in this package: combinations, permutations,
 * derangements, Cartesian product, etc.</p>
 *
 * @param <E> element type of the backing list
 * @since 0.1.0
 */
public final class IndexingAdapter<E> implements Iterable<List<E>> {

  private final Iterable<int[]> tuples;
  private final List<E> items;

  /**
   * Creates an adapter that maps each emitted index tuple to a list of elements from {@code items}.
   *
   * @param tuples iterable of index arrays (each index must be in {@code [0, items.size()-1]})
   * @param items backing list of elements (not copied; element references are reused)
   * @throws NullPointerException if either argument is {@code null}
   */
  public IndexingAdapter(Iterable<int[]> tuples, List<E> items) {
    this.tuples = Objects.requireNonNull(tuples, "tuples");
    this.items = Objects.requireNonNull(items, "items");
  }

  /**
   * Convenience factory identical to {@link #IndexingAdapter(Iterable, List)}.
   *
   * @param tuples iterable of index arrays
   * @param items backing list of elements
   * @param <E> element type
   * @return a new adapter
   */
  public static <E> IndexingAdapter<E> of(Iterable<int[]> tuples, List<E> items) {
    return new IndexingAdapter<>(tuples, items);
  }

  @Override
  public Iterator<List<E>> iterator() {
    return new Iterator<List<E>>() {
      private final Iterator<int[]> it = tuples.iterator();

      @Override
      public boolean hasNext() {
        return it.hasNext();
      }

      @Override
      public List<E> next() {
        if (!hasNext()) throw new NoSuchElementException();
        int[] idx = it.next(); // defensive copy not required; we read-and-map immediately
        // Map indices -> elements with bounds checks
        List<E> out = new ArrayList<>(idx.length);
        int size = items.size();
        for (int i : idx) {
          if (i < 0 || i >= size) {
            throw new IndexOutOfBoundsException(
                "Index " + i + " out of bounds for items size " + size);
          }
          out.add(items.get(i));
        }
        return Collections.unmodifiableList(out);
      }
    };
  }
}
