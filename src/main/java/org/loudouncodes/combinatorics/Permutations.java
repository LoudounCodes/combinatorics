package org.loudouncodes.combinatorics;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Fluent API for generating {@code k}-permutations (ordered selections without repetition) from the
 * domain {@code {0,1,...,n-1}}.
 *
 * <p>Usage:
 *
 * <pre>{@code
 * for (int[] p : Permutations.of(5).take(3)) {
 *   // p is a length-3 array of distinct indices from [0..4]
 * }
 * }</pre>
 *
 * <h2>Design</h2>
 *
 * <ul>
 *   <li>{@link #of(int)} creates a builder bound to {@code n}.
 *   <li>{@link Builder#take(int)} returns an iterable view over all ordered {@code k}-tuples.
 *   <li>Lexicographic order over the {@code k}-length arrays, with no duplicates.
 *   <li>Each call to {@link Iterator#next()} returns a defensive copy.
 * </ul>
 *
 * <h2>Edge cases</h2>
 *
 * <ul>
 *   <li>{@code k == 0}: one empty tuple {@code []}.
 *   <li>{@code k == n}: all full permutations of {@code {0..n-1}}.
 *   <li>Invalid inputs throw {@link IllegalArgumentException}.
 * </ul>
 */
public final class Permutations {

  private Permutations() {}

  /**
   * Creates a builder for permutations drawn from {@code {0..n-1}}.
   *
   * @param n domain size, must be {@code >= 0}
   * @return builder bound to {@code n}
   * @throws IllegalArgumentException if {@code n < 0}
   */
  public static Builder of(int n) {
    if (n < 0) throw new IllegalArgumentException("n must be >= 0");
    return new Builder(n);
  }

  /** Builder capturing the domain size {@code n}. */
  public static final class Builder {
    private final int n;

    private Builder(int n) {
      this.n = n;
    }

    /**
     * Returns all ordered tuples of length {@code k} without repetition.
     *
     * @param k tuple length (0 ≤ k ≤ n)
     * @return iterable view over k-permutations
     * @throws IllegalArgumentException if {@code k < 0} or {@code k > n}
     */
    public KTake take(int k) {
      if (k < 0 || k > n) throw new IllegalArgumentException("Require 0 ≤ k ≤ n");
      return new KTake(k, n);
    }
  }

  /** Iterable view of all {@code k}-permutations from {@code {0..n-1}}. */
  public static final class KTake implements Iterable<int[]> {
    private final int k, n;

    private KTake(int k, int n) {
      this.k = k;
      this.n = n;
    }

    /**
     * Count of ordered {@code k}-permutations.
     *
     * @return {@code P(n,k) = n! / (n - k)!}
     */
    public long size() {
      long result = 1L;
      for (int i = 0; i < k; i++) {
        result *= (n - i);
      }
      return result;
    }

    /**
     * Exact count of ordered {@code k}-permutations.
     *
     * @return {@code P(n,k)} as a {@link BigInteger}
     */
    public BigInteger sizeExact() {
      BigInteger r = BigInteger.ONE;
      for (int i = 0; i < k; i++) {
        r = r.multiply(BigInteger.valueOf(n - i));
      }
      return r;
    }

    @Override
    public Iterator<int[]> iterator() {
      return new KPermIterator(k, n);
    }
  }

  /**
   * Iterator that enumerates k-length permutations in lexicographic order with no duplicates.
   * Algorithm:
   *
   * <ol>
   *   <li>Start at {@code [0,1,...,k-1]} (or [] if k==0).
   *   <li>To advance, scan i from k-1 down to 0:
   *       <ul>
   *         <li>Mark values used in prefix {@code p[0..i-1]}.
   *         <li>Find smallest {@code cand > p[i]} not used in prefix. If found, set {@code
   *             p[i]=cand}.
   *         <li>Rebuild suffix {@code p[i+1..k-1]} with the smallest available values in ascending
   *             order.
   *       </ul>
   *   <li>If no position can increase, we are exhausted.
   * </ol>
   */
  private static final class KPermIterator implements Iterator<int[]> {
    private final int n, k;
    private final int[] cur;
    private boolean hasNext;

    KPermIterator(int k, int n) {
      this.k = k;
      this.n = n;
      this.cur = new int[k];
      if (k == 0) {
        // single empty tuple
        this.hasNext = true;
      } else {
        for (int i = 0; i < k; i++) cur[i] = i; // minimal lex tuple
        this.hasNext = (n >= k);
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
      hasNext = nextKPermutation(cur, n, k);
      return out;
    }

    // Returns true if successor exists; false if exhausted.
    private static boolean nextKPermutation(int[] p, int n, int k) {
      if (k == 0) return false; // already emitted the single empty tuple

      boolean[] used = new boolean[n];
      for (int i = k - 1; i >= 0; i--) {
        Arrays.fill(used, false);
        for (int t = 0; t < i; t++) used[p[t]] = true;

        for (int cand = p[i] + 1; cand < n; cand++) {
          if (!used[cand]) {
            p[i] = cand;

            // rebuild suffix with smallest available values
            Arrays.fill(used, false);
            for (int t = 0; t <= i; t++) used[p[t]] = true;

            int write = i + 1;
            for (int v = 0; v < n && write < k; v++) {
              if (!used[v]) {
                p[write++] = v;
                used[v] = true;
              }
            }
            return true;
          }
        }
      }
      return false; // exhausted
    }
  }

  /**
   * Demo entry point.
   *
   * @param args command-line arguments (unused)
   */
  public static void main(String[] args) {
    Permutations.KTake p = Permutations.of(4).take(2);
    System.out.println("P(4,2) = " + p.size());
    for (int[] tup : p) {
      System.out.println(java.util.Arrays.toString(tup));
    }
  }
}
