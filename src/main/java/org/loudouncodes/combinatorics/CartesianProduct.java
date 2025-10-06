package org.loudouncodes.combinatorics;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Generates the Cartesian product of multiple domains, each defined by its size, and exposes the
 * tuples via Java's enhanced {@code for} loop by implementing {@link Iterable}{@code <int[]>}.
 *
 * <h2>What is the Cartesian product?</h2>
 *
 * <p>Given {@code k} sets (domains) {@code A_0, A_1, ..., A_{k-1}}, their Cartesian product is the
 * set of ordered tuples {@code (a0, a1, ..., a_{k-1})} where {@code ai ∈ A_i}. For example:
 *
 * <pre>{@code
 * {0,1} × {a,b} = {(0,a), (0,b), (1,a), (1,b)}
 * }</pre>
 *
 * <h2>Usage in code</h2>
 *
 * <pre>{@code
 * // Two domains: {0,1} and {0,1,2}
 * CartesianProduct cp = new CartesianProduct(2, 3);
 * for (int[] tuple : cp) {
 *     System.out.println(Arrays.toString(tuple));
 * }
 * // prints:
 * // [0, 0]
 * // [0, 1]
 * // [0, 2]
 * // [1, 0]
 * // [1, 1]
 * // [1, 2]
 * }</pre>
 *
 * <h2>Design</h2>
 *
 * <ul>
 *   <li>The constructor takes an array of domain sizes. For example, {@code new
 *       CartesianProduct(2,3,4)} means the first coordinate ranges over {@code 0..1}, the second
 *       over {@code 0..2}, the third over {@code 0..3}.
 *   <li>Each emitted {@code int[]} is a defensive copy so callers may mutate without affecting the
 *       iterator.
 *   <li>Tuples are generated in lexicographic order with the leftmost index changing slowest, like
 *       nested loops.
 * </ul>
 *
 * <h2>Complexity</h2>
 *
 * <ul>
 *   <li><strong>Time:</strong> proportional to the product of domain sizes (the total number of
 *       tuples).
 *   <li><strong>Space:</strong> {@code O(k)} for the current tuple.
 * </ul>
 *
 * <h2>Edge cases</h2>
 *
 * <ul>
 *   <li>If any size is zero, the Cartesian product is empty.
 *   <li>If no sizes are provided (zero dimensions), the Cartesian product has exactly one element:
 *       the empty tuple {@code []}.
 * </ul>
 *
 * @since 0.1.0
 */
public final class CartesianProduct implements Iterable<int[]> {

  private final int[] sizes;

  /**
   * Constructs a Cartesian product generator.
   *
   * @param sizes array of domain sizes (each ≥ 0)
   * @throws IllegalArgumentException if any size is negative
   */
  public CartesianProduct(int... sizes) {
    Objects.requireNonNull(sizes, "sizes");
    for (int s : sizes) {
      if (s < 0) {
        throw new IllegalArgumentException("Sizes must be nonnegative");
      }
    }
    this.sizes = sizes.clone();
  }

  /**
   * Returns the total number of tuples in the Cartesian product.
   *
   * <p>This is the product of all domain sizes, returned as a {@code long}. For larger inputs it
   * may overflow; in that case consider using {@code java.math.BigInteger} in a separate
   * implementation.
   *
   * @return total number of tuples
   */
  public long size() {
    long result = 1L;
    for (int s : sizes) {
      result *= s;
    }
    return result;
  }

  @Override
  public Iterator<int[]> iterator() {
    return new CartesianIterator(sizes);
  }

  /** Iterator that enumerates tuples in lexicographic order. */
  private static final class CartesianIterator implements Iterator<int[]> {
    private final int[] sizes;
    private final int[] current;
    private boolean hasNext;

    CartesianIterator(int[] sizes) {
      this.sizes = sizes.clone();
      this.current = new int[sizes.length];
      this.hasNext = true;

      // If any size is zero, mark empty immediately
      for (int s : sizes) {
        if (s == 0) {
          hasNext = false;
          break;
        }
      }
    }

    @Override
    public boolean hasNext() {
      return hasNext;
    }

    @Override
    public int[] next() {
      if (!hasNext) {
        throw new NoSuchElementException();
      }

      // Defensive copy of current tuple
      int[] out = current.clone();

      // Advance like an odometer: rightmost index increments fastest
      for (int pos = sizes.length - 1; pos >= 0; pos--) {
        current[pos]++;
        if (current[pos] < sizes[pos]) {
          // still in range, stop incrementing
          return out;
        }
        // carry over
        current[pos] = 0;
      }

      // If we carried past the leftmost position, we’re done
      hasNext = false;
      return out;
    }
  }

  /**
   * Small demo.
   *
   * @param args ignored
   */
  public static void main(String[] args) {
    CartesianProduct cp = new CartesianProduct(2, 3);
    System.out.println("Total: " + cp.size());
    for (int[] tuple : cp) {
      System.out.println(java.util.Arrays.toString(tuple));
    }
  }
}
