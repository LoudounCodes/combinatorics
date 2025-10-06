package org.loudouncodes.combinatorics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Adapts an {@link Iterable}{@code <int[]>} of index tuples to an {@link Iterable}{@code <List<E>>}
 * of element tuples, using a provided {@link List}{@code <E>} as the backing source.
 *
 * <h2>What problem does this solve?</h2>
 *
 * <p>The combinatorics generators in this package (e.g., {@link Combinations}, {@link
 * Permutations}) produce tuples of <em>indices</em> into a conceptual domain {@code {0..n-1}}. In
 * many real uses, you have an actual list of elements (names, cards, objects) and you want the
 * tuples of elements directly. {@code IndexingAdapter} bridges that gap:
 *
 * <pre>{@code
 * List<String> names = List.of("Ada", "Grace", "Edsger", "Barbara");
 * Iterable<int[]> pairsOfIndices = new Combinations(2, names.size());
 *
 * for (List<String> pair : new IndexingAdapter<>(pairsOfIndices, names)) {
 *     System.out.println(pair); // e.g., [Ada, Grace], [Ada, Edsger], ...
 * }
 * }</pre>
 *
 * <h2>Design</h2>
 *
 * <ul>
 *   <li>Lazily maps each {@code int[]} tuple to a new unmodifiable {@code List<E>} of the same
 *       length.
 *   <li>Per-tuple bounds checking: each index must satisfy {@code 0 <= i < source.size()}.
 *   <li>Does not copy the source list; only builds a new result list per emitted tuple.
 * </ul>
 *
 * <h2>Complexity</h2>
 *
 * <ul>
 *   <li><strong>Time per tuple:</strong> {@code O(k)} to build the {@code List<E>} of length {@code
 *       k}.
 *   <li><strong>Space per tuple:</strong> {@code O(k)} for the result list (returned to the
 *       caller).
 * </ul>
 *
 * <h2>Example with permutations</h2>
 *
 * <pre>{@code
 * List<Integer> nums = List.of(10, 20, 30);
 * for (List<Integer> p : new IndexingAdapter<>(new Permutations(2, nums.size()), nums)) {
 *     System.out.println(p); // e.g., [10, 20], [10, 30], [20, 10], ...
 * }
 * }</pre>
 *
 * @param <E> element type of the backing list
 * @since 0.1.0
 */
public final class IndexingAdapter<E> implements Iterable<List<E>> {

  private final Iterable<int[]> tuples;
  private final List<E> source;

  /**
   * Creates an adapter that maps index tuples from {@code tuples} to element tuples from {@code
   * source}.
   *
   * @param tuples an iterable of index arrays (e.g., {@code new Combinations(k, n)})
   * @param source the backing list of elements (size must be {@code n})
   * @throws NullPointerException if {@code tuples} or {@code source} is {@code null}
   */
  public IndexingAdapter(Iterable<int[]> tuples, List<E> source) {
    this.tuples = Objects.requireNonNull(tuples, "tuples");
    this.source = Objects.requireNonNull(source, "source");
  }

  /**
   * Returns an iterator that lazily maps each {@code int[]} to an unmodifiable {@code List<E>}.
   *
   * <p>Each call to {@link Iterator#next()}:
   *
   * <ol>
   *   <li>Reads the next index array from {@code tuples}.
   *   <li>Checks each index for bounds {@code 0 <= i < source.size()}.
   *   <li>Builds a new {@code ArrayList<E>} of the same length and wraps it with {@link
   *       java.util.Collections#unmodifiableList(List)}.
   * </ol>
   *
   * @return iterator over {@code List<E>} views of the original index tuples
   */
  @Override
  public Iterator<List<E>> iterator() {
    final Iterator<int[]> it = tuples.iterator();
    return new Iterator<>() {
      @Override
      public boolean hasNext() {
        return it.hasNext();
      }

      @Override
      public List<E> next() {
        int[] idx = it.next(); // may throw NoSuchElementException as per Iterator contract
        // Map indices -> elements, with bounds checking.
        List<E> out = new ArrayList<>(idx.length);
        int n = source.size();
        for (int i : idx) {
          if (i < 0 || i >= n) {
            throw new IndexOutOfBoundsException(
                "Index " + i + " out of bounds for source size " + n);
          }
          out.add(source.get(i));
        }
        return Collections.unmodifiableList(out);
      }
    };
  }

  /**
   * Convenience factory to improve type inference in call sites.
   *
   * @param tuples iterable of index arrays
   * @param source backing list of elements
   * @param <E> element type
   * @return a new {@code IndexingAdapter<E>}
   */
  public static <E> IndexingAdapter<E> of(Iterable<int[]> tuples, List<E> source) {
    return new IndexingAdapter<>(tuples, source);
  }
}
