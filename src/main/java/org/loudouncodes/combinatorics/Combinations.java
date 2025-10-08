package org.loudouncodes.combinatorics;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Fluent API for generating {@code k}-combinations (unordered, no repetition) from
 * the domain {@code {0,1,...,n-1}}. Usage:
 *
 * <pre>{@code
 * for (int[] c : Combinations.of(12).choose(5)) {
 *   // c is a length-5, strictly-increasing array of indices in [0,11]
 * }
 * }</pre>
 *
 * <h2>Design</h2>
 * <ul>
 *   <li>{@link #of(int)} creates a lightweight builder that remembers {@code n}.</li>
 *   <li>{@link Builder#choose(int)} returns a sized, iterable view
 *       ({@link Combinations.KChoose}) over all {@code k}-subsets in lexicographic order.</li>
 *   <li>Each call to {@link java.util.Iterator#next() Iterator.next()} returns a defensive copy.</li>
 * </ul>
 *
 * <h2>Order of generation</h2>
 * <p>Lexicographic over strictly increasing arrays. Starts at {@code [0,1,...,k-1]} and advances
 * using a simple "odometer with carries" successor:
 * move left from the end to find a position that can be bumped, bump it, and minimally reset the
 * suffix to keep it strictly increasing.</p>
 *
 * <h2>Complexity</h2>
 * <ul>
 *   <li>Time per combination: {@code O(k)} (short left-scan and suffix reset).</li>
 *   <li>Space: {@code O(k)} iterator state.</li>
 * </ul>
 *
 * <h2>Edge cases</h2>
 * <ul>
 *   <li>{@code k == 0}: emits one empty array {@code []}.</li>
 *   <li>{@code k == n}: emits one array {@code [0,1,...,n-1]}.</li>
 *   <li>Invalid inputs (negative, or {@code k > n}) throw {@link IllegalArgumentException}.</li>
 * </ul>
 *
 * @since 0.2.0
 */
public final class Combinations {

  private Combinations() {
    // not instantiable
  }

  /**
   * Creates a builder for combinations drawn from {@code {0..n-1}}.
   *
   * @param n size of the ground set ({@code n >= 0})
   * @return a builder bound to {@code n}
   * @throws IllegalArgumentException if {@code n < 0}
   */
  public static Builder of(int n) {
    if (n < 0) throw new IllegalArgumentException("n must be >= 0");
    return new Builder(n);
  }

  /**
   * Builder capturing the domain size {@code n}. Terminal methods (like {@link #choose(int)})
   * return sized, iterable views over the requested objects.
   */
  public static final class Builder {
    private final int n;

    private Builder(int n) {
      this.n = n;
    }

    /**
     * Returns an iterable over all size-{@code k} subsets of {@code {0..n-1}} in lexicographic order.
     *
     * @param k subset size (must satisfy {@code 0 <= k <= n})
     * @return a sized iterable of combinations
     * @throws IllegalArgumentException if {@code k < 0} or {@code k > n}
     */
    public KChoose choose(int k) {
      if (k < 0 || k > n) {
        throw new IllegalArgumentException("Require 0 <= k <= n");
      }
      return new KChoose(k, n);
    }
  }

  /**
   * Iterable view of all {@code k}-combinations from {@code {0..n-1}}.
   * Provides {@link #size()} and supports enhanced {@code for} iteration.
   */
  public static final class KChoose implements Iterable<int[]> {
    private final int k;
    private final int n;

    private KChoose(int k, int n) {
      this.k = k;
      this.n = n;
    }

    /**
     * Number of combinations: {@code C(n,k) = n! / (k!(n-k)!)} computed via a safe multiplicative loop
     * into a {@code long}. This will overflow for large inputs; this library targets classroom-scale values.
     *
     * @return the count of {@code k}-subsets as a {@code long}
     */
    public long size() {
      // Compute C(n,k) multiplicatively to avoid intermediate overflow where possible.
      int kk = Math.min(k, n - k);
      long num = 1L;
      for (int i = 1; i <= kk; i++) {
        num = (num * (n - kk + i)) / i;
      }
      return num;
    }

    @Override
    public Iterator<int[]> iterator() {
      return new CombinationIterator(k, n);
    }
  }

  /**
   * Iterator implementing the lexicographic-successor algorithm over strictly increasing arrays.
   * State invariant: {@code cur[0] < cur[1] < ... < cur[k-1]} and each entry lies in {@code [0,n-1]}.
   */
  private static final class CombinationIterator implements Iterator<int[]> {
    private final int n;
    private final int k;
    private final int[] cur;
    private boolean hasNext = true;

    CombinationIterator(int k, int n) {
      this.k = k;
      this.n = n;
      this.cur = new int[k];
      for (int i = 0; i < k; i++) {
        cur[i] = i; // start at [0,1,...,k-1]
      }
    }

    @Override
    public boolean hasNext() {
      return hasNext;
    }

    @Override
    public int[] next() {
      if (!hasNext) throw new NoSuchElementException();

      int[] out = cur.clone(); // defensive copy for the caller

      // Generate next combination (lexicographic successor).
      int i = k - 1;
      while (i >= 0 && cur[i] == n - k + i) {
        i--;
      }
      if (i < 0) {
        hasNext = false; // exhausted
      } else {
        cur[i]++;
        for (int j = i + 1; j < k; j++) {
          cur[j] = cur[j - 1] + 1;
        }
      }
      return out;
    }
  }

  /**
   * Small demo for quick sanity checks.
   *
   * @param args ignored
   */
  public static void main(String[] args) {
    Combinations.KChoose k5of12 = Combinations.of(12).choose(5);
    System.out.println("C(12,5) = " + k5of12.size());
    for (int[] c : k5of12) {
      // print the first few only
      System.out.println(java.util.Arrays.toString(c));
      break;
    }
  }
}
