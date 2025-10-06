package org.loudouncodes.combinatorics;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Generates all {@code k}-combinations <em>with repetition</em> (a.k.a. multisets) from the integer
 * domain {@code {0, 1, ..., n-1}} and exposes them via Java's enhanced {@code for} loop by
 * implementing {@link Iterable}{@code <int[]>}.
 *
 * <h2>What is a combination with repetition?</h2>
 *
 * <p>We choose {@code k} elements from {@code n} types, allowing repeats, and where order does not
 * matter. We represent each multiset as a <strong>non-decreasing</strong> array of indices of
 * length {@code k}. For example, with {@code n = 3} and {@code k = 2}, the outputs are:
 *
 * <pre>{@code
 * [0,0], [0,1], [0,2], [1,1], [1,2], [2,2]
 * }</pre>
 *
 * <h2>Order of generation</h2>
 *
 * <p>Arrays are produced in <strong>lexicographic order</strong> subject to the non-decreasing
 * constraint. The first array is {@code [0,0,...,0]} and the last is {@code [n-1, n-1, ..., n-1]}.
 *
 * <h2>How many are there? (Stars and Bars)</h2>
 *
 * <p>The number of multisets is the binomial coefficient {@code C(n + k - 1, k)}. This is available
 * via {@link #size()}.
 *
 * <h2>Algorithm (lexicographic successor under non-decreasing constraint)</h2>
 *
 * <p>Maintain the current array {@code a[0..k-1]} (non-decreasing). To advance:
 *
 * <ol>
 *   <li>Scan from right to left for the first position {@code i} where {@code a[i] < n-1}.
 *   <li>If no such {@code i} exists, we are done.
 *   <li>Increment {@code a[i]++}, then set all suffix positions {@code a[i+1..k-1]} equal to {@code
 *       a[i]} (this keeps the sequence non-decreasing and lexicographically minimal).
 * </ol>
 *
 * <h3>Correctness intuition</h3>
 *
 * <ul>
 *   <li><strong>Exhaustiveness:</strong> Starting at {@code [0,0,...,0]} and repeatedly applying
 *       the successor rule enumerates every non-decreasing {@code k}-tuple until we reach {@code
 *       [n-1,...,n-1]}.
 *   <li><strong>No duplicates:</strong> The successor is deterministic and strictly increases in
 *       lexicographic order.
 *   <li><strong>Ordering:</strong> We always bump the rightmost possible position and minimize the
 *       suffix, yielding standard lexicographic order subject to the constraint.
 * </ul>
 *
 * <h2>Complexity</h2>
 *
 * <ul>
 *   <li><strong>Time per tuple:</strong> {@code O(k)} (short right-to-left scan and suffix reset).
 *   <li><strong>Total tuples:</strong> {@code C(n + k - 1, k)} — see {@link #size()}.
 *   <li><strong>Space:</strong> {@code O(k)} internal state. Each call to {@link
 *       java.util.Iterator#next() Iterator.next()} returns a <em>defensive copy</em> so callers can
 *       safely mutate their copy without affecting iteration.
 * </ul>
 *
 * <h2>Edge cases</h2>
 *
 * <ul>
 *   <li>{@code k == 0}: a single empty array {@code []} is emitted.
 *   <li>{@code n == 0}: emits nothing unless {@code k == 0} (there are no elements to choose).
 *   <li>Invalid inputs (negative {@code n} or {@code k}) throw {@link IllegalArgumentException}.
 * </ul>
 *
 * <h2>Example</h2>
 *
 * <pre>{@code
 * CombinationsWithRepetition m = new CombinationsWithRepetition(2, 3);
 * System.out.println("Total: " + m.size()); // prints C(3+2-1, 2) = C(4,2) = 6
 * for (int[] t : m) {
 *     System.out.printf("[%d,%d]%n", t[0], t[1]);
 * }
 * }</pre>
 *
 * <p><strong>Implementation note:</strong> Each {@link java.util.Iterator#next() next()} clones the
 * internal array to preserve iterator integrity.
 *
 * @since 0.1.0
 */
public class CombinationsWithRepetition implements Iterable<int[]> {
  private final int n;
  private final int k;

  /**
   * Constructs a generator of {@code k}-combinations with repetition from {@code {0..n-1}}.
   *
   * @param k multiset size (must satisfy {@code k >= 0})
   * @param n number of distinct element types (must satisfy {@code n >= 0})
   * @throws IllegalArgumentException if {@code k < 0} or {@code n < 0}
   */
  public CombinationsWithRepetition(int k, int n) {
    if (k < 0 || n < 0) {
      throw new IllegalArgumentException("Require n >= 0 and k >= 0");
    }
    this.k = k;
    this.n = n;
  }

  /**
   * Returns the total number of {@code k}-combinations with repetition of {@code n} items. This is
   * the binomial coefficient {@code C(n + k - 1, k)}.
   *
   * <p>Note: this uses {@code long} and will overflow for large inputs. For classroom-scale values
   * it is typically fine. For arbitrary precision, supply a {@code BigInteger} variant.
   *
   * @return {@code C(n + k - 1, k)} as a {@code long}
   */
  public long size() {
    // Handle cases where the count is 0 or 1 quickly.
    if (k == 0) return 1L;
    if (n == 0) return 0L; // no elements to choose from unless k==0

    // Compute C(n+k-1, k) iteratively: product_{i=1..k} (n + i - 1)/i
    return binomial(n + k - 1, k);
  }

  /** Helper to compute {@code C(N,K)} using integer arithmetic with symmetry. */
  private static long binomial(int N, int K) {
    if (K < 0 || K > N) return 0L;
    K = Math.min(K, N - K);
    long res = 1L;
    for (int i = 1; i <= K; i++) {
      res = (res * (N - (K - i))) / i;
    }
    return res;
  }

  /**
   * Returns a fresh iterator positioned at the first non-decreasing tuple {@code [0,0,...,0]} (or
   * the empty array if {@code k == 0}). If {@code n == 0} and {@code k > 0}, the iterator is empty.
   *
   * @return iterator over lexicographically ordered non-decreasing {@code k}-tuples
   */
  @Override
  public Iterator<int[]> iterator() {
    return new WithRepIterator(k, n);
  }

  /**
   * Iterator implementing the lexicographic-successor algorithm under the non-decreasing
   * constraint.
   *
   * <p>State invariant: {@code 0 <= a[0] <= a[1] <= ... <= a[k-1] <= n-1}.
   */
  private static final class WithRepIterator implements Iterator<int[]> {
    private final int n;
    private final int k;
    private final int[] a; // current non-decreasing k-tuple
    private boolean hasNext;

    WithRepIterator(int k, int n) {
      this.k = k;
      this.n = n;
      if (k == 0) {
        this.a = new int[0];
        this.hasNext = true; // single empty tuple
      } else if (n == 0) {
        this.a = new int[0];
        this.hasNext = false; // no tuples when n==0 and k>0
      } else {
        this.a = new int[k];
        // start at [0,0,...,0]
        for (int i = 0; i < k; i++) a[i] = 0;
        this.hasNext = true;
      }
    }

    @Override
    public boolean hasNext() {
      return hasNext;
    }

    /**
     * Returns the current tuple and advances to the next non-decreasing tuple in lexicographic
     * order.
     *
     * @return a defensive copy of the current {@code int[]} tuple
     * @throws NoSuchElementException if the iterator is exhausted
     */
    @Override
    public int[] next() {
      if (!hasNext) throw new NoSuchElementException();

      int[] out = a.clone(); // defensive copy for caller

      // Advance to next tuple: bump rightmost position that can increase (< n-1),
      // then set the suffix to that new value to stay non-decreasing and minimal.
      int i = k - 1;
      while (i >= 0 && a[i] == n - 1) {
        i--;
      }
      if (i < 0) {
        hasNext = false; // we were at [n-1, n-1, ..., n-1]
      } else {
        a[i]++;
        for (int j = i + 1; j < k; j++) {
          a[j] = a[i];
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
    CombinationsWithRepetition m = new CombinationsWithRepetition(2, 3);
    System.out.println("Total: " + m.size()); // 6
    for (int[] t : m) {
      System.out.printf("[%d,%d]%n", t[0], t[1]);
    }
  }
}
