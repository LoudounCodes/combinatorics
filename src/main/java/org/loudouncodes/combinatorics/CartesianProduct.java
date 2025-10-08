package org.loudouncodes.combinatorics;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Fluent API for generating tuples from a mixed-radix Cartesian product.
 *
 * <p>Given non-negative dimensions {@code d[0], d[1], ..., d[m-1]}, this iterable yields every
 * {@code m}-length tuple {@code t} such that {@code 0 <= t[i] < d[i]} for each coordinate.</p>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * // 3 attributes, each with 3 values (e.g., a SET card): 3^3 = 27 tuples
 * for (int[] t : CartesianProduct.of(3, 3, 3)) {
 *   System.out.println(Arrays.toString(t));
 * }
 *
 * // Combine with IndexingAdapter to map indices to real objects per coordinate:
 * // Suppose suits = ["♠","♥"], ranks = ["A","K","Q"], colors = ["Black","Red"]
 * // Then dims = [suits.size(), ranks.size(), colors.size()] = [2,3,2]
 * }</pre>
 *
 * <h2>Order of generation</h2>
 * <p>Lexicographic with the <em>rightmost</em> coordinate varying fastest (odometer behavior).
 * Start at {@code [0,0,...,0]}, then repeatedly increment the last position; on overflow, carry
 * left and reset trailing positions to 0.</p>
 *
 * <h2>Counting</h2>
 * <p>Total tuples = {@code Π dims[i]}.</p>
 *
 * <h2>Edge cases</h2>
 * <ul>
 *   <li>No dimensions (i.e., {@code of()}): one empty tuple {@code []}.</li>
 *   <li>If any dimension is zero and there is at least one dimension, the product is empty.</li>
 *   <li>Negative dimensions are rejected with {@link IllegalArgumentException}.</li>
 * </ul>
 *
 * <p><strong>Implementation note:</strong> Each {@link java.util.Iterator#next() Iterator.next()}
 * returns a defensive copy to protect the iterator's state.</p>
 *
 * @since 0.2.0
 */
public final class CartesianProduct {

  private CartesianProduct() {}

  /**
   * Creates an iterable over the Cartesian product of the given non-negative dimensions.
   *
   * @param dims non-null array of dimensions; each {@code dims[i] >= 0}
   * @return a sized iterable over all tuples
   * @throws NullPointerException if {@code dims} is null
   * @throws IllegalArgumentException if any dimension is negative
   */
  public static Product of(int... dims) {
    Objects.requireNonNull(dims, "dims");
    for (int d : dims) {
      if (d < 0) throw new IllegalArgumentException("All dimensions must be >= 0, got " + d);
    }
    // Defensive copy so future external changes to the passed array don't affect us
    int[] copy = Arrays.copyOf(dims, dims.length);
    return new Product(copy);
  }

  /**
   * Iterable view of the Cartesian product for fixed dimensions. Provides {@link #size()} and
   * supports enhanced-for iteration.
   */
  public static final class Product implements Iterable<int[]> {
    private final int[] dims;

    private Product(int[] dims) {
      this.dims = dims;
    }

    /**
     * Number of tuples = product of dimensions. Returns 0 if any dimension is 0 (and there is
     * at least one dimension). If there are no dimensions, returns 1 (the empty tuple).
     *
     * <p>Note: This may overflow for large inputs; intended for classroom-scale values.</p>
     */
    public long size() {
      if (dims.length == 0) return 1L;
      long prod = 1L;
      for (int d : dims) {
        if (d == 0) return 0L;
        prod *= d;
      }
      return prod;
    }

    @Override
    public Iterator<int[]> iterator() {
      return new CartesianIterator(dims);
    }
  }

  /**
   * Mixed-radix odometer iterator. State {@code cur} starts at all zeros and increments with carries.
   * Invariant: for each i, {@code 0 <= cur[i] < dims[i]}.
   */
  private static final class CartesianIterator implements Iterator<int[]> {
    private final int[] dims;
    private final int m;         // number of coordinates
    private final int[] cur;     // current tuple
    private boolean hasNext;

    CartesianIterator(int[] dims) {
      this.dims = dims;
      this.m = dims.length;

      if (m == 0) {
        // Single empty tuple
        this.cur = new int[0];
        this.hasNext = true;
      } else {
        // If any dimension is 0 -> empty product
        boolean empty = false;
        for (int d : dims) {
          if (d == 0) { empty = true; break; }
        }
        if (empty) {
          this.cur = new int[0];
          this.hasNext = false;
        } else {
          this.cur = new int[m];
          Arrays.fill(this.cur, 0);
          this.hasNext = true;
        }
      }
    }

    @Override
    public boolean hasNext() {
      return hasNext;
    }

    @Override
    public int[] next() {
      if (!hasNext) throw new NoSuchElementException();
      int[] out = cur.clone(); // defensive copy
      advance();
      return out;
    }

    /** Increment the mixed-radix number in {@code cur} with bases {@code dims}. */
    private void advance() {
      if (m == 0) { // emitted the single empty tuple
        hasNext = false;
        return;
      }
      for (int i = m - 1; i >= 0; i--) {
        cur[i]++;
        if (cur[i] < dims[i]) {
          // no carry; done
          return;
        } else {
          // carry; reset this position and continue left
          cur[i] = 0;
        }
      }
      // overflowed past the most significant digit -> exhausted
      hasNext = false;
    }
  }

  /** Simple demo. */
  public static void main(String[] args) {
    CartesianProduct.Product p = CartesianProduct.of(2, 3); // 2×3 = 6
    System.out.println("size = " + p.size());
    for (int[] t : p) {
      System.out.println(Arrays.toString(t));
    }
  }
}
