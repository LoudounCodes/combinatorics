package org.loudouncodes.combinatorics;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Generates ordered {@code k}-permutations (k-tuples without repetition) drawn from the integer
 * domain {@code {0, 1, ..., n-1}} and exposes them via Java's enhanced {@code for} loop by
 * implementing {@link Iterable}{@code <int[]>}.
 *
 * <h2>What is a k-permutation?</h2>
 *
 * <p>A {@code k}-permutation of {@code n} distinct items is an ordered selection of {@code k}
 * different elements, no repeats allowed. For example, with {@code n = 4} and {@code k = 2}, valid
 * tuples include {@code [0,1]}, {@code [1,0]}, {@code [2,3]}, etc. The total count is {@code nPk =
 * n! / (n-k)!}.
 *
 * <h2>Order of generation (lexicographic over the k-length array)</h2>
 *
 * <p>Tuples are produced in lexicographic order with respect to their array entries. Example:
 *
 * <pre>{@code
 * new Permutations(2, 3)  // n = 3, k = 2
 * -> [0,1], [0,2], [1,0], [1,2], [2,0], [2,1]
 * }</pre>
 *
 * <h2>Algorithm (lexicographic successor on k-length tuples)</h2>
 *
 * <p>Maintain the current tuple {@code p[0..k-1]}. To advance to the next tuple:
 *
 * <ol>
 *   <li>Scan positions from right to left ({@code i = k-1} down to {@code 0}).
 *   <li>For the first position {@code i} that can be increased, choose the smallest value {@code
 *       cand > p[i]} that is not already used in the prefix {@code p[0..i-1]}.
 *   <li>Set {@code p[i] = cand}, then rebuild the suffix {@code p[i+1..k-1]} with the smallest
 *       unused values in ascending order.
 *   <li>If no position can be increased, we are past the final tuple.
 * </ol>
 *
 * <p>This is analogous to an odometer that carries left, except that each position must be a value
 * not used earlier in the tuple; the suffix is always reset to its minimal lexicographic
 * configuration consistent with the prefix.
 *
 * <h3>Correctness intuition</h3>
 *
 * <ul>
 *   <li><strong>Exhaustiveness:</strong> Starting at {@code [0,1,...,k-1]} and repeatedly applying
 *       the successor rule reaches every legal k-tuple exactly once.
 *   <li><strong>No duplicates:</strong> The algorithm never reuses a value within a tuple and never
 *       revisits an already-emitted tuple; the successor is deterministic and strictly increases
 *       lexicographically until exhaustion.
 *   <li><strong>Ordering:</strong> We increase the rightmost position that can be increased and
 *       then minimize the suffix, which yields standard lexicographic order over the k-length
 *       arrays.
 * </ul>
 *
 * <h2>Complexity</h2>
 *
 * <ul>
 *   <li><strong>Time per tuple:</strong> {@code O(n)} worst case (we may scan a boolean "used"
 *       array and rebuild the suffix).
 *   <li><strong>Total tuples:</strong> {@code nPk = n! / (n-k)!} — available via {@link #size()}.
 *   <li><strong>Space:</strong> {@code O(k)} for the current tuple, plus {@code O(n)} for a
 *       transient "used" bitmap inside the successor step.
 * </ul>
 *
 * <h2>Edge cases</h2>
 *
 * <ul>
 *   <li>{@code k == 0}: a single empty array {@code []} is emitted.
 *   <li>{@code k == n}: emits all {@code n!} permutations in lexicographic order.
 *   <li>Invalid inputs (negative, or {@code k > n}) throw {@link IllegalArgumentException}.
 * </ul>
 *
 * <h2>Example</h2>
 *
 * <pre>{@code
 * Permutations perms = new Permutations(3, 5);
 * System.out.println("Total: " + perms.size()); // prints nPk
 * for (int[] p : perms) {
 *     System.out.printf("[%d, %d, %d]%n", p[0], p[1], p[2]);
 * }
 * }</pre>
 *
 * <p><strong>Implementation note:</strong> Each {@link java.util.Iterator#next() Iterator.next()}
 * returns a defensive copy of the internal array. This protects the iterator's state from external
 * mutation.
 *
 * @since 0.1.0
 */
public class Permutations implements Iterable<int[]> {
  private final int n;
  private final int k;

  /**
   * Constructs a generator of ordered {@code k}-permutations from {@code {0..n-1}}.
   *
   * @param k length of each tuple (must satisfy {@code 0 <= k <= n})
   * @param n size of the ground set ({@code n >= 0})
   * @throws IllegalArgumentException if {@code k < 0}, {@code n < 0}, or {@code k > n}
   */
  public Permutations(int k, int n) {
    if (k < 0 || n < 0 || k > n) {
      throw new IllegalArgumentException("Require 0 <= k <= n");
    }
    this.k = k;
    this.n = n;
  }

  /**
   * Returns the total number of ordered {@code k}-permutations (falling factorial {@code nPk}).
   *
   * <p>Note: this uses {@code long} and will overflow for large inputs. For classroom-scale inputs
   * (e.g., {@code n <= 20}) it is typically fine. For arbitrary precision, provide a {@code
   * BigInteger} variant.
   *
   * @return {@code nPk} as a {@code long}
   */
  public long size() {
    return fallingFactorial(n, k);
  }

  /** Helper to compute the falling factorial nPk = n*(n-1)*...*(n-k+1). */
  private static long fallingFactorial(int n, int k) {
    long r = 1L;
    for (int i = 0; i < k; i++) {
      r *= (n - i);
    }
    return r;
  }

  /**
   * Returns a fresh iterator positioned at the first tuple {@code [0,1,...,k-1]} (or the empty
   * array if {@code k == 0}).
   *
   * @return iterator over lexicographically ordered {@code k}-permutations
   */
  @Override
  public Iterator<int[]> iterator() {
    return new KPermIterator(k, n);
  }

  /**
   * Iterator implementing the lexicographic-successor algorithm on k-length tuples.
   *
   * <p>State invariant: all entries in {@code cur} are distinct, in the range {@code [0, n-1]}.
   */
  private static final class KPermIterator implements Iterator<int[]> {
    private final int n;
    private final int k;
    private final int[] cur; // current k-tuple
    private boolean hasNext;

    KPermIterator(int k, int n) {
      this.k = k;
      this.n = n;
      if (k == 0) {
        this.cur = new int[0];
        this.hasNext = true; // one empty tuple
      } else {
        this.cur = new int[k];
        for (int i = 0; i < k; i++) cur[i] = i; // minimal lex k-permutation
        this.hasNext = (n >= k);
      }
    }

    @Override
    public boolean hasNext() {
      return hasNext;
    }

    /**
     * Returns the current tuple and advances to the next tuple in lexicographic order.
     *
     * @return a defensive copy of the current {@code int[]} tuple
     * @throws NoSuchElementException if the iterator is exhausted
     */
    @Override
    public int[] next() {
      if (!hasNext) throw new NoSuchElementException();
      int[] out = cur.clone(); // defensive copy for the caller
      hasNext = nextKPermutation(cur, n, k);
      return out;
    }

    /**
     * In-place lexicographic successor for a k-permutation {@code p} over domain {@code {0..n-1}}.
     *
     * <p>Procedure:
     *
     * <ol>
     *   <li>For {@code i = k-1 .. 0}, mark values used in the prefix {@code p[0..i-1]}.
     *   <li>Find the smallest {@code cand > p[i]} not used in the prefix; if found, set {@code p[i]
     *       = cand}.
     *   <li>Rebuild the suffix {@code p[i+1..k-1]} with the smallest unused values in ascending
     *       order.
     *   <li>If no {@code i} can be increased, return {@code false} (exhausted).
     * </ol>
     *
     * @return {@code true} if a successor exists; {@code false} if we were at the last tuple
     */
    private static boolean nextKPermutation(int[] p, int n, int k) {
      if (k == 0) return false; // already emitted the single empty tuple

      boolean[] used = new boolean[n];

      // Try to bump from the rightmost position that can increase.
      for (int i = k - 1; i >= 0; i--) {
        // Mark used values in the prefix [0..i-1].
        Arrays.fill(used, false);
        for (int t = 0; t < i; t++) used[p[t]] = true;

        // Find next candidate > p[i] not used by the prefix.
        for (int cand = p[i] + 1; cand < n; cand++) {
          if (!used[cand]) {
            p[i] = cand;

            // Rebuild suffix with the smallest available values.
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
        // Otherwise, move left and try to bump an earlier position.
      }
      return false; // exhausted
    }
  }

  /**
   * Small demo for quick sanity checks. Prints all k-permutations for {@code k = 3, n = 5}. This is
   * not used by the library and may be removed in production builds.
   *
   * @param args ignored
   */
  public static void main(String[] args) {
    Permutations perms = new Permutations(3, 5);
    System.out.println("Total: " + perms.size());
    for (int[] p : perms) {
      System.out.printf("[%d, %d, %d]%n", p[0], p[1], p[2]);
    }
  }
}
