package org.loudouncodes.combinatorics;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Generates all {@code k}-combinations (unordered, without repetition) from the integer domain
 * {@code {0, 1, ..., n-1}} in <em>lexicographic order</em> and exposes them via Java's enhanced
 * {@code for} loop by implementing {@link Iterable}{@code <int[]>}.
 *
 * <h2>What is a combination?</h2>
 *
 * <p>A {@code k}-combination of {@code n} elements is a size-{@code k} subset, where order does not
 * matter and no element is repeated. For example, with {@code n = 5} and {@code k = 3}, one
 * combination is {@code [0,2,4]} (the same as the set {0,2,4}).
 *
 * <h2>Order of generation (lexicographic)</h2>
 *
 * <p>We emit strictly increasing arrays of length {@code k}. For example:
 *
 * <pre>{@code
 * new Combinations(3, 5)  // n = 5, k = 3
 * -> [0,1,2], [0,1,3], [0,1,4], [0,2,3], [0,2,4], [0,3,4],
 *    [1,2,3], [1,2,4], [1,3,4], [2,3,4]
 * }</pre>
 *
 * <h2>Algorithm (lexicographic successor / "odometer" carry)</h2>
 *
 * <p>Maintain the current combination {@code current[0..k-1]} as a strictly increasing sequence. To
 * advance to the next combination:
 *
 * <ol>
 *   <li>Start at the rightmost index {@code i = k-1} and move left while {@code current[i] == n - k
 *       + i} (i.e., that position is at its maximum).
 *   <li>If no such {@code i} exists, we are past the last combination (set {@code hasNext =
 *       false}).
 *   <li>Otherwise, increment {@code current[i]++}, and then reset the suffix to the smallest
 *       increasing sequence: {@code current[i+1] = current[i] + 1}, {@code current[i+2] =
 *       current[i+1] + 1}, etc.
 * </ol>
 *
 * <p>This is identical in spirit to an odometer with different digit limits per position.
 *
 * <h3>Correctness intuition</h3>
 *
 * <ul>
 *   <li><strong>Exhaustiveness:</strong> Starting at {@code [0,1,...,k-1]} and repeatedly applying
 *       the successor rule enumerates every strictly increasing k-tuple until no index can be
 *       increased.
 *   <li><strong>No duplicates:</strong> The state is deterministic; each successor is unique and
 *       monotone in lexicographic order.
 *   <li><strong>Ordering:</strong> We increase the earliest position possible and reset the suffix
 *       minimally, yielding standard lexicographic order over increasing k-tuples.
 * </ul>
 *
 * <h2>Complexity</h2>
 *
 * <ul>
 *   <li><strong>Time per combination:</strong> {@code O(k)} (short left-scan and suffix reset).
 *   <li><strong>Total combinations:</strong> {@code C(n,k) = n! / (k! (n-k)!)} — available via
 *       {@link #size()}.
 *   <li><strong>Space:</strong> {@code O(k)} internal state. Each call to {@link
 *       java.util.Iterator#next() Iterator.next()} returns a <em>defensive copy</em> so callers can
 *       safely mutate their copy without affecting iteration.
 * </ul>
 *
 * <h2>Edge cases</h2>
 *
 * <ul>
 *   <li>{@code k == 0}: a single empty array {@code []} is emitted.
 *   <li>{@code k == n}: a single array {@code [0,1,...,n-1]} is emitted.
 *   <li>Invalid inputs (negative, or {@code k > n}) throw {@link IllegalArgumentException}.
 * </ul>
 *
 * <h2>Example</h2>
 *
 * <pre>{@code
 * Combinations combos = new Combinations(3, 12);
 * System.out.println("Total: " + combos.size());
 * for (int[] c : combos) {
 *     System.out.printf("[%d, %d, %d]%n", c[0], c[1], c[2]);
 * }
 * }</pre>
 *
 * <p><strong>Implementation note:</strong> Each {@link java.util.Iterator#next() next()} clones the
 * internal array to preserve iterator integrity.
 *
 * @since 0.1.0
 */
public class Combinations implements Iterable<int[]> {
  private final int n;
  private final int k;

  /**
   * Constructs a generator of {@code k}-combinations from {@code {0..n-1}}.
   *
   * @param k size of each combination (must satisfy {@code 0 <= k <= n})
   * @param n size of the ground set ({@code n >= 0})
   * @throws IllegalArgumentException if {@code k < 0}, {@code n < 0}, or {@code k > n}
   */
  public Combinations(int k, int n) {
    if (k < 0 || n < 0 || k > n) {
      throw new IllegalArgumentException("Require 0 <= k <= n");
    }
    this.k = k;
    this.n = n;
  }

  /**
   * Returns the total number of {@code k}-combinations of {@code n} items (the binomial
   * coefficient).
   *
   * @return {@code C(n,k)} as a {@code long}
   */
  public long size() {
    return binomial(n, k);
  }

  /** Helper to compute binomial coefficients with integer arithmetic. */
  private static long binomial(int n, int k) {
    if (k < 0 || k > n) return 0L;
    k = Math.min(k, n - k); // symmetry
    long result = 1L;
    for (int i = 1; i <= k; i++) {
      result = (result * (n - (k - i))) / i;
    }
    return result;
  }

  /**
   * Returns a fresh iterator positioned at the first combination {@code [0,1,...,k-1]} (or the
   * empty array if {@code k == 0}).
   *
   * @return iterator over lexicographically ordered {@code k}-combinations
   */
  @Override
  public Iterator<int[]> iterator() {
    return new CombinationIterator(k, n);
  }

  /**
   * Iterator implementing the lexicographic-successor algorithm over strictly increasing arrays.
   *
   * <p>State invariant: {@code current[0] < current[1] < ... < current[k-1]} and every {@code
   * current[i]} is in {@code [0, n-1]}.
   */
  private static class CombinationIterator implements Iterator<int[]> {
    private final int n;
    private final int k;
    private final int[] current;
    private boolean hasNext = true;

    CombinationIterator(int k, int n) {
      this.k = k;
      this.n = n;
      this.current = new int[k];
      for (int i = 0; i < k; i++) {
        current[i] = i; // start at [0,1,...,k-1]
      }
    }

    @Override
    public boolean hasNext() {
      return hasNext;
    }

    /**
     * Returns the current combination and advances to the next one in lexicographic order.
     *
     * @return a defensive copy of the current {@code int[]} combination
     * @throws NoSuchElementException if the iterator is exhausted
     */
    @Override
    public int[] next() {
      if (!hasNext) throw new NoSuchElementException();

      int[] result = current.clone(); // defensive copy for the caller

      // Generate next combination (lexicographic successor).
      int i = k - 1;
      while (i >= 0 && current[i] == n - k + i) {
        i--;
      }
      if (i < 0) {
        hasNext = false; // we were at the last combination
      } else {
        current[i]++;
        for (int j = i + 1; j < k; j++) {
          current[j] = current[j - 1] + 1;
        }
      }
      return result;
    }
  }

  /**
   * Small demo for quick sanity checks. Prints all combinations for {@code k = 3, n = 12}. This is
   * not used by the library and may be removed in production builds.
   *
   * @param args ignored
   */
  public static void main(String[] args) {
    Combinations combos = new Combinations(3, 12);
    System.out.println("Total: " + combos.size());
    for (int[] combo : combos) {
      System.out.printf("[%d, %d, %d]%n", combo[0], combo[1], combo[2]);
    }
  }
}
